package com.example.shiyu.data.db.dao

import androidx.room.*
import com.example.shiyu.data.db.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM order_record ORDER BY order_time DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM order_record ORDER BY order_time DESC")
    suspend fun getAllOrdersOnce(): List<OrderEntity>

    @Query("SELECT * FROM order_record WHERE status = :status ORDER BY order_time DESC")
    fun getOrdersByStatus(status: Int): Flow<List<OrderEntity>>

    @Query("SELECT * FROM order_record WHERE status = :status ORDER BY order_time DESC")
    suspend fun getOrdersByStatusOnce(status: Int): List<OrderEntity>

    @Query("SELECT * FROM order_record WHERE id = :id")
    suspend fun getOrderById(id: Long): OrderEntity?

    @Query("SELECT * FROM order_record WHERE sync_id = :syncId")
    suspend fun getBySyncId(syncId: Long): OrderEntity?

    @Query("SELECT COUNT(*) FROM order_record WHERE status = 0")
    fun getPendingOrderCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM order_record WHERE status = 0")
    suspend fun getPendingOrderCountOnce(): Int

    @Query("SELECT COUNT(*) FROM order_record")
    fun getOrderCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM order_record")
    suspend fun getOrderCountOnce(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("DELETE FROM order_record WHERE id = :id")
    suspend fun deleteOrder(id: Long)
}
