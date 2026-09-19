package com.example.shiyu.data.repository

import com.example.shiyu.data.db.dao.CoupleConfigDao
import com.example.shiyu.data.db.entity.CoupleConfigEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoupleConfigRepository @Inject constructor(
    private val coupleConfigDao: CoupleConfigDao
) {
    fun getConfig(): Flow<CoupleConfigEntity?> = coupleConfigDao.getConfig()

    suspend fun getConfigOnce(): CoupleConfigEntity? = coupleConfigDao.getConfigOnce()

    suspend fun getCount(): Int = coupleConfigDao.getCount()

    suspend fun insert(config: CoupleConfigEntity): Long = coupleConfigDao.insert(config)

    suspend fun update(config: CoupleConfigEntity) {
        coupleConfigDao.update(config)
    }

    suspend fun deleteAll() {
        coupleConfigDao.deleteAll()
    }
}
