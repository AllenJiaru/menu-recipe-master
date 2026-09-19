package com.example.shiyu.data.db.dao

import androidx.room.*
import com.example.shiyu.data.db.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM supplier ORDER BY create_time DESC")
    fun getAll(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM supplier ORDER BY create_time DESC")
    suspend fun getAllOnce(): List<SupplierEntity>

    @Query("SELECT * FROM supplier WHERE id = :id")
    suspend fun getById(id: Long): SupplierEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SupplierEntity): Long

    @Update
    suspend fun update(item: SupplierEntity)

    @Query("DELETE FROM supplier WHERE id = :id")
    suspend fun delete(id: Long)
}
