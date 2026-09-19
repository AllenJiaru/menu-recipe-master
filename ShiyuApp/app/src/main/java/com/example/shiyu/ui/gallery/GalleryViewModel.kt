package com.example.shiyu.ui.gallery

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.api.ApiClient
import com.example.shiyu.data.db.entity.GalleryImageEntity
import com.example.shiyu.data.repository.GalleryRepository
import com.example.shiyu.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val application: Application
) : ViewModel() {

    private val _images = mutableStateListOf<GalleryImageEntity>()
    val images: List<GalleryImageEntity> = _images

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _selectedImages = mutableStateOf(setOf<Long>())
    val selectedImages: State<Set<Long>> = _selectedImages

    private val _isSelectionMode = mutableStateOf(false)
    val isSelectionMode: State<Boolean> = _isSelectionMode

    init {
        loadImages()
    }

    fun loadImages() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = galleryRepository.getAllImagesOnce()
                _images.clear()
                _images.addAll(list)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addImage(imagePath: String, description: String = "") {
        viewModelScope.launch {
            val syncId = UUID.randomUUID().toString()
            val image = GalleryImageEntity(
                image_path = imagePath,
                description = description,
                create_time = System.currentTimeMillis(),
                sync_id = syncId
            )
            val localId = galleryRepository.insertImage(image)

            uploadToBackend(localId, imagePath, description, syncId)
        }
    }

    private suspend fun uploadToBackend(
        localId: Long,
        imagePath: String,
        description: String,
        syncId: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                val originalFile = File(imagePath)
                if (!originalFile.exists()) return@withContext

                val fileToUpload = if (FileUtils.needsCompression(imagePath)) {
                    FileUtils.compressImageForUpload(application, imagePath) ?: originalFile
                } else {
                    originalFile
                }

                val requestFile = fileToUpload.asRequestBody("image/*".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", fileToUpload.name, requestFile)
                val descPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
                val coupleIdPart = "".toRequestBody("text/plain".toMediaTypeOrNull())

                val response = ApiClient.backendApi.uploadGalleryImage(filePart, descPart, null, coupleIdPart)
                if (response.code == 200 && response.data != null) {
                    val dto = response.data
                    galleryRepository.updateImage(
                        GalleryImageEntity(
                            id = localId,
                            image_path = imagePath,
                            description = description,
                            create_time = System.currentTimeMillis(),
                            sync_id = syncId,
                            sync_time = System.currentTimeMillis(),
                            image_url = dto.imageUrl,
                            backend_id = dto.id
                        )
                    )
                }

                if (fileToUpload != originalFile) {
                    fileToUpload.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteImage(image: GalleryImageEntity) {
        viewModelScope.launch {
            try {
                image.backend_id?.let { backendId ->
                    ApiClient.backendApi.deleteGalleryImage(backendId)
                }
            } catch (_: Exception) {}
            galleryRepository.deleteImage(image.id)
            loadImages()
        }
    }

    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
        if (!_isSelectionMode.value) {
            _selectedImages.value = emptySet()
        }
    }

    fun toggleImageSelection(imageId: Long) {
        _selectedImages.value = if (imageId in _selectedImages.value) {
            _selectedImages.value - imageId
        } else {
            _selectedImages.value + imageId
        }
    }

    fun selectAll() {
        _selectedImages.value = if (_selectedImages.value.size == _images.size) {
            emptySet()
        } else {
            _images.map { it.id }.toSet()
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val backendIds = mutableListOf<Long>()
            _selectedImages.value.forEach { id ->
                val img = galleryRepository.getImageById(id)
                img?.backend_id?.let { backendIds.add(it) }
                galleryRepository.deleteImage(id)
            }
            if (backendIds.isNotEmpty()) {
                try { ApiClient.backendApi.batchDeleteGalleryImages(backendIds) } catch (_: Exception) {}
            }
            _selectedImages.value = emptySet()
            _isSelectionMode.value = false
            loadImages()
        }
    }

    fun updateImageDescription(image: GalleryImageEntity, description: String) {
        viewModelScope.launch {
            galleryRepository.updateImage(image.copy(description = description))
            loadImages()
        }
    }
}
