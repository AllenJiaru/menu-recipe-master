<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon blue"><el-icon :size="22"><List /></el-icon></div>
        <div class="page-header-text"><h2>订单管理</h2><p>查看和管理所有点餐订单</p></div>
      </div>
    </div>

    <div class="page-toolbar">
      <div class="page-search">
        <el-icon class="search-icon"><Search /></el-icon>
        <input v-model="searchKey" placeholder="搜索菜名..." @keyup.enter="handleSearch" />
      </div>
      <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 140px" @change="loadData">
        <el-option v-for="(label, key) in orderStatusMap" :key="key" :label="label" :value="Number(key)" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" @change="handleDateChange" />
      <div class="page-toolbar-right"><span class="page-count">共 <b>{{ total }}</b> 条</span></div>
    </div>

    <div class="page-table-card" v-loading="loading">
      <el-table :data="orders" style="width: 100%">
        <el-table-column prop="id" label="订单号" width="90">
          <template #default="{ row }"><span class="id-badge">#{{ row.id }}</span></template>
        </el-table-column>
        <el-table-column prop="recipeName" label="菜名" min-width="140">
          <template #default="{ row }"><span class="name-cell">{{ row.recipeName || '未知菜谱' }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span :class="['badge', statusBadgeClass[row.status]]">{{ orderStatusMap[row.status] }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip>
          <template #default="{ row }"><span class="time-cell">{{ row.remark || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="下单时间" width="170">
          <template #default="{ row }"><span class="time-cell">{{ formatDate(row.orderTime) }}</span></template>
        </el-table-column>
        <el-table-column label="接单时间" width="170">
          <template #default="{ row }"><span class="time-cell">{{ row.acceptTime ? formatDate(row.acceptTime) : '-' }}</span></template>
        </el-table-column>
        <el-table-column label="完成时间" width="170">
          <template #default="{ row }"><span class="time-cell">{{ row.completeTime ? formatDate(row.completeTime) : '-' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button class="action-btn" @click="$router.push(`/orders/${row.id}`)"><el-icon><View /></el-icon>详情</el-button>
            <el-button v-if="row.status === 0" class="action-btn" v-permission="'business:order:edit'" @click="handleAccept(row)"><el-icon><Check /></el-icon>接受</el-button>
            <el-button v-if="row.status === 1" class="action-btn" v-permission="'business:order:edit'" @click="handleCooking(row)"><el-icon><Edit /></el-icon>制作中</el-button>
            <el-button v-if="row.status === 2" class="action-btn" v-permission="'business:order:edit'" @click="handleComplete(row)"><el-icon><SuccessFilled /></el-icon>完成</el-button>
            <el-button v-if="row.status < 3" class="action-btn danger" v-permission="'business:order:delete'" @click="handleCancel(row)"><el-icon><CircleCloseFilled /></el-icon>取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="page-pagination">
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @current-change="loadData" @size-change="handleSizeChange" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { getOrders, acceptOrder, cookingOrder, completeOrder, cancelOrder } from '@/api/order'
import { orderStatusMap, formatDate } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchKey = ref('')
const dateRange = ref<[string, string] | null>(null)
const filters = reactive({ status: null as number | null, startDate: '', endDate: '' })

const statusBadgeClass: Record<number, string> = {
  0: 'badge-orange',
  1: 'badge-blue',
  2: 'badge-blue',
  3: 'badge-green',
  4: 'badge-red',
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getOrders({ page: page.value, size: pageSize.value, ...filters, keyword: searchKey.value })
    orders.value = res.data.records
    total.value = res.data.total
  } catch { ElMessage.error('加载订单失败') } finally { loading.value = false }
}

const handleSearch = () => { page.value = 1; loadData() }
const handleSizeChange = () => { page.value = 1; loadData() }
const handleDateChange = (val: [string, string] | null) => { filters.startDate = val?.[0] || ''; filters.endDate = val?.[1] || ''; loadData() }

const handleAccept = async (row: any) => {
  try { await acceptOrder(row.id); ElMessage.success('已接受'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '操作失败') }
}
const handleCooking = async (row: any) => {
  try { await cookingOrder(row.id); ElMessage.success('开始制作'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '操作失败') }
}
const handleComplete = async (row: any) => {
  try { await completeOrder(row.id); ElMessage.success('已完成'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '操作失败') }
}
const handleCancel = async (row: any) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', { inputType: 'textarea' })
    await cancelOrder(row.id, value); ElMessage.success('已取消'); loadData()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') ElMessage.error(e.message || '取消失败')
  }
}

onMounted(loadData)

let refreshTimer: ReturnType<typeof setInterval> | null = null
onMounted(() => {
  refreshTimer = setInterval(loadData, 10000)
})
onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped lang="scss">
.remark-cell {
  color: var(--text-secondary);
  font-size: 13px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
