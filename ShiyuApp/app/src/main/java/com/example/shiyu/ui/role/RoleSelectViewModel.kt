package com.example.shiyu.ui.role

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.data.db.entity.CoupleConfigEntity
import com.example.shiyu.data.repository.CoupleConfigRepository
import com.example.shiyu.util.RoleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoleSelectViewModel @Inject constructor(
    private val roleManager: RoleManager,
    private val coupleConfigRepository: CoupleConfigRepository
) : ViewModel() {

    suspend fun saveConfig(
        spaceName: String,
        chefName: String,
        dinerName: String,
        role: String
    ): Boolean {
        return try {
            val config = CoupleConfigEntity(
                space_name = spaceName,
                chef_name = chefName,
                diner_name = dinerName,
                create_time = System.currentTimeMillis()
            )

            val count = coupleConfigRepository.getCount()
            if (count > 0) {
                val existing = coupleConfigRepository.getConfigOnce()
                if (existing != null) {
                    coupleConfigRepository.update(config.copy(id = existing.id))
                }
            } else {
                coupleConfigRepository.insert(config)
            }

            roleManager.setCurrentRole(role)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
