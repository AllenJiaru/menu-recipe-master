package com.example.shiyu.data.db.dao

import androidx.room.*
import com.example.shiyu.data.db.entity.OperationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationLogDao {
    @Query("SELECT * FROM operation_log ORDER BY create_time DESC")
    fun getAll(): Flow<List<OperationLogEntity>>

    @Query("SELECT * FROM operation_log ORDER BY create_time DESC")
    suspend fun getAllOnce(): List<OperationLogEntity>

    @Insert
    suspend fun insert(log: OperationLogEntity): Long

    @Query("DELETE FROM operation_log")
    suspend fun deleteAll()
}
