package com.example.shiyu.data.db.dao

import androidx.room.*
import com.example.shiyu.data.db.entity.CoupleConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoupleConfigDao {
    @Query("SELECT * FROM couple_config LIMIT 1")
    fun getConfig(): Flow<CoupleConfigEntity?>

    @Query("SELECT * FROM couple_config LIMIT 1")
    suspend fun getConfigOnce(): CoupleConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: CoupleConfigEntity): Long

    @Update
    suspend fun update(config: CoupleConfigEntity)

    @Query("SELECT COUNT(*) FROM couple_config")
    suspend fun getCount(): Int

    @Query("DELETE FROM couple_config")
    suspend fun deleteAll()
}
