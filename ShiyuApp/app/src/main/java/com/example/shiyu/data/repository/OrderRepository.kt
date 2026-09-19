package com.example.shiyu.data.repository

import com.example.shiyu.data.db.dao.OrderDao
import com.example.shiyu.data.db.entity.OrderEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val orderDao: OrderDao
) {
    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllOrders()

    suspend fun getAllOrdersOnce(): List<OrderEntity> = orderDao.getAllOrdersOnce()

    fun getOrdersByStatus(status: Int): Flow<List<OrderEntity>> = orderDao.getOrdersByStatus(status)

    suspend fun getOrdersByStatusOnce(status: Int): List<OrderEntity> = orderDao.getOrdersByStatusOnce(status)

    suspend fun getOrderById(id: Long): OrderEntity? = orderDao.getOrderById(id)

    fun getPendingOrderCount(): Flow<Int> = orderDao.getPendingOrderCount()

    suspend fun getPendingOrderCountOnce(): Int = orderDao.getPendingOrderCountOnce()

    fun getOrderCount(): Flow<Int> = orderDao.getOrderCount()

    suspend fun getOrderCountOnce(): Int = orderDao.getOrderCountOnce()

    suspend fun insertOrder(order: OrderEntity): Long = orderDao.insertOrder(order)

    suspend fun updateOrder(order: OrderEntity) {
        orderDao.updateOrder(order)
    }

    suspend fun deleteOrder(id: Long) {
        orderDao.deleteOrder(id)
    }
}
