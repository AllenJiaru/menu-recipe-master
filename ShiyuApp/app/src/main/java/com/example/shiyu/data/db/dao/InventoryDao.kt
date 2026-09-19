package com.example.shiyu.data.db.dao

import androidx.room.*
import com.example.shiyu.data.db.entity.InventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory ORDER BY name ASC")
    fun getAll(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory ORDER BY name ASC")
    suspend fun getAllOnce(): List<InventoryEntity>

    @Query("SELECT * FROM inventory WHERE id = :id")
    suspend fun getById(id: Long): InventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InventoryEntity): Long

    @Update
    suspend fun update(item: InventoryEntity)

    @Query("DELETE FROM inventory WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM inventory")
    suspend fun getCount(): Int

    @Query("SELECT * FROM inventory WHERE quantity <= threshold AND threshold > 0")
    suspend fun getLowStock(): List<InventoryEntity>
}
