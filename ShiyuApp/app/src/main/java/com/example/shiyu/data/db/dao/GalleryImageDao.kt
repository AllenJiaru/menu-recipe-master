package com.example.shiyu.data.db.dao

import androidx.room.*
import com.example.shiyu.data.db.entity.GalleryImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GalleryImageDao {
    @Query("SELECT * FROM gallery_img ORDER BY create_time DESC")
    fun getAllImages(): Flow<List<GalleryImageEntity>>

    @Query("SELECT * FROM gallery_img ORDER BY create_time DESC")
    suspend fun getAllImagesOnce(): List<GalleryImageEntity>

    @Query("SELECT * FROM gallery_img WHERE id = :id")
    suspend fun getImageById(id: Long): GalleryImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: GalleryImageEntity): Long

    @Update
    suspend fun updateImage(image: GalleryImageEntity)

    @Query("DELETE FROM gallery_img WHERE id = :id")
    suspend fun deleteImage(id: Long)

    @Query("DELETE FROM gallery_img")
    suspend fun deleteAll()

    @Query("SELECT * FROM gallery_img WHERE sync_id IS NULL ORDER BY create_time DESC")
    suspend fun getUnsyncedImages(): List<GalleryImageEntity>

    @Query("UPDATE gallery_img SET sync_id = :syncId, sync_time = :syncTime WHERE id = :id")
    suspend fun updateSyncInfo(id: Long, syncId: String, syncTime: Long)

    @Query("SELECT * FROM gallery_img WHERE sync_id = :syncId LIMIT 1")
    suspend fun findBySyncId(syncId: String): GalleryImageEntity?

    @Query("SELECT * FROM gallery_img WHERE backend_id = :backendId LIMIT 1")
    suspend fun findByBackendId(backendId: Long): GalleryImageEntity?
}
