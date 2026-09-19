package com.example.shiyu.ui.diner

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.data.db.entity.OrderEntity
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.data.repository.OrderRepository
import com.example.shiyu.util.Constants
import com.example.shiyu.util.RoleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val backendRepository: BackendRepository,
    private val roleManager: RoleManager
) : ViewModel() {

    private val _order = mutableStateOf<OrderEntity?>(null)
    val order: State<OrderEntity?> = _order

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isChef = mutableStateOf(false)
    val isChef: State<Boolean> = _isChef

    private val _message = mutableStateOf("")
    val message: State<String> = _message

    init {
        viewModelScope.launch {
            roleManager.currentRole.collect { role ->
                _isChef.value = role == Constants.ROLE_CHEF
            }
        }
    }

    fun loadOrder(orderId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _order.value = orderRepository.getOrderById(orderId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** 从后端拉取最新订单状态（实时同步） */
    fun refreshFromBackend() {
        viewModelScope.launch {
            if (!backendRepository.isLoggedIn) return@launch
            val current = _order.value ?: return@launch
            if (current.status == Constants.ORDER_STATUS_COMPLETED ||
                current.status == Constants.ORDER_STATUS_CANCELLED
            ) return@launch
            try {
                backendRepository.pullOrders()
                val updated = orderRepository.getOrderById(current.id)
                if (updated != null && updated.status != current.status) {
                    _order.value = updated
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateOrderStatus(status: Int, reason: String = "") {
        viewModelScope.launch {
            _order.value?.let { order ->
                val now = System.currentTimeMillis()
                val updatedOrder = when (status) {
                    Constants.ORDER_STATUS_ACCEPTED -> order.copy(
                        status = status,
                        accept_time = now
                    )
                    Constants.ORDER_STATUS_COOKING -> order.copy(status = status)
                    Constants.ORDER_STATUS_COMPLETED -> order.copy(
                        status = status,
                        complete_time = now
                    )
                    Constants.ORDER_STATUS_CANCELLED -> order.copy(
                        status = status,
                        reject_reason = reason
                    )
                    else -> order.copy(status = status)
                }
                orderRepository.updateOrder(updatedOrder)
                if (backendRepository.isLoggedIn && updatedOrder.sync_id != null) {
                    backendRepository.pushOrderStatus(updatedOrder)
                }
                _order.value = updatedOrder
                _message.value = "操作成功"
            }
        }
    }
}
