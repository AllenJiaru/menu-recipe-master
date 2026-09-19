package com.example.shiyu.data.repository

import com.example.shiyu.data.db.dao.GalleryImageDao
import com.example.shiyu.data.db.entity.GalleryImageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GalleryRepository @Inject constructor(
    private val galleryImageDao: GalleryImageDao
) {
    fun getAllImages(): Flow<List<GalleryImageEntity>> = galleryImageDao.getAllImages()

    suspend fun getAllImagesOnce(): List<GalleryImageEntity> = galleryImageDao.getAllImagesOnce()

    suspend fun getImageById(id: Long): GalleryImageEntity? = galleryImageDao.getImageById(id)

    suspend fun findByBackendId(backendId: Long): GalleryImageEntity? = galleryImageDao.findByBackendId(backendId)

    suspend fun insertImage(image: GalleryImageEntity): Long = galleryImageDao.insertImage(image)

    suspend fun updateImage(image: GalleryImageEntity) {
        galleryImageDao.updateImage(image)
    }

    suspend fun deleteImage(id: Long) {
        galleryImageDao.deleteImage(id)
    }

    suspend fun deleteAll() {
        galleryImageDao.deleteAll()
    }
}
