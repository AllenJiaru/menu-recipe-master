<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <div class="page-header-left">
        <el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon> 返回列表</el-button>
        <div class="page-header-icon blue"><el-icon :size="22"><Document /></el-icon></div>
        <div class="page-header-text"><h2>订单详情 #{{ order?.id }}</h2><p>查看订单信息与执行操作</p></div>
      </div>
    </div>

    <template v-if="order">
      <div class="info-grid">
        <div class="page-card">
          <div class="page-card-header"><span>订单信息</span></div>
          <div class="info-grid">
            <div class="info-item"><span class="info-label">订单号</span><span class="info-value">#{{ order.id }}</span></div>
            <div class="info-item"><span class="info-label">菜名</span><span class="info-value name">{{ order.recipeName }}</span></div>
            <div class="info-item"><span class="info-label">状态</span><span :class="['badge', statusBadgeClass[order.status]]">{{ orderStatusMap[order.status] }}</span></div>
            <div class="info-item"><span class="info-label">备注</span><span class="info-value">{{ order.remark || '无' }}</span></div>
            <div class="info-item"><span class="info-label">驳回原因</span><span class="info-value">{{ order.rejectReason || '无' }}</span></div>
            <div class="info-item"><span class="info-label">下单时间</span><span class="info-value time-cell">{{ formatDate(order.orderTime) }}</span></div>
            <div class="info-item"><span class="info-label">接单时间</span><span class="info-value time-cell">{{ order.acceptTime ? formatDate(order.acceptTime) : '-' }}</span></div>
            <div class="info-item"><span class="info-label">完成时间</span><span class="info-value time-cell">{{ order.completeTime ? formatDate(order.completeTime) : '-' }}</span></div>
          </div>
        </div>

        <div class="page-card">
          <div class="page-card-header"><span>订单操作</span></div>
          <template v-if="order.status < 3">
            <div class="action-list">
              <div v-if="order.status === 0" class="action-item" @click="handleAction('accept')">
                <div class="page-header-icon green"><el-icon :size="20"><Check /></el-icon></div>
                <div><h4>接受订单</h4><p>确认接单并开始准备</p></div>
              </div>
              <div v-if="order.status === 1" class="action-item" @click="handleAction('cooking')">
                <div class="page-header-icon blue"><el-icon :size="20"><Edit /></el-icon></div>
                <div><h4>开始制作</h4><p>标记为制作中状态</p></div>
              </div>
              <div v-if="order.status === 2" class="action-item" @click="handleAction('complete')">
                <div class="page-header-icon orange"><el-icon :size="20"><SuccessFilled /></el-icon></div>
                <div><h4>标记完成</h4><p>确认菜品已完成</p></div>
              </div>
              <div class="action-item danger" @click="handleAction('cancel')">
                <div class="page-header-icon red"><el-icon :size="20"><CircleCloseFilled /></el-icon></div>
                <div><h4>取消订单</h4><p>取消此订单</p></div>
              </div>
            </div>
          </template>
          <template v-else>
            <el-result :icon="order.status === 3 ? 'success' : 'warning'" :title="orderStatusMap[order.status]" :sub-title="order.status === 3 ? '订单已完成' : '订单已取消'" />
          </template>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderById, acceptOrder, cookingOrder, completeOrder, cancelOrder } from '@/api/order'
import { orderStatusMap, formatDate } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute(); const router = useRouter()
const order = ref<any>(null); const loading = ref(true)

const statusBadgeClass: Record<number, string> = {
  0: 'badge-orange',
  1: 'badge-blue',
  2: 'badge-blue',
  3: 'badge-green',
  4: 'badge-red',
}

const loadData = async () => {
  try {
    const res: any = await getOrderById(Number(route.params.id))
    order.value = res.data
  } catch { ElMessage.error('加载订单失败') } finally { loading.value = false }
}

const handleAction = async (action: string) => {
  try {
    if (action === 'cancel') {
      const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', { inputType: 'textarea' })
      await cancelOrder(order.value.id, value)
    } else {
      const fn = action === 'accept' ? acceptOrder : action === 'cooking' ? cookingOrder : completeOrder
      await fn(order.value.id)
    }
    ElMessage.success('操作成功'); loadData()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.info-value.name {
  color: var(--apple-blue);
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;

  &:hover {
    border-color: var(--apple-blue);
    background: rgba(0, 122, 255, 0.04);
  }

  &.danger:hover {
    border-color: var(--apple-red);
    background: rgba(255, 59, 48, 0.04);
  }

  h4 {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 2px;
  }

  p {
    font-size: 12px;
    color: var(--text-secondary);
  }
}
</style>
