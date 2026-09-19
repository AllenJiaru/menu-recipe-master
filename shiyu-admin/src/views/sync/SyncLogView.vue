<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon teal">
          <el-icon :size="22"><Refresh /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>同步日志</h2>
          <p>查看设备数据同步记录</p>
        </div>
      </div>
      <el-button @click="loadData" :loading="loading"><el-icon><Refresh /></el-icon> 刷新</el-button>
    </div>

    <div class="page-table-card" v-loading="loading">
      <el-table :data="logs" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center">
          <template #default="{ row }"><span class="id-badge">{{ row.id }}</span></template>
        </el-table-column>
        <el-table-column prop="deviceId" label="设备ID" min-width="150" show-overflow-tooltip>
          <template #default="{ row }"><span class="time-cell">{{ row.deviceId }}</span></template>
        </el-table-column>
        <el-table-column prop="syncType" label="同步类型" width="110" align="center">
          <template #default="{ row }">
            <span :class="row.syncType === 'full' ? 'badge badge-blue' : 'badge badge-gray'">{{ row.syncType === 'full' ? '全量同步' : '增量同步' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="syncDirection" label="方向" width="90" align="center">
          <template #default="{ row }"><span class="badge badge-orange">{{ row.syncDirection || '-' }}</span></template>
        </el-table-column>
        <el-table-column prop="tableName" label="数据表" width="120" show-overflow-tooltip />
        <el-table-column prop="recordCount" label="记录数" width="80" align="center">
          <template #default="{ row }"><span class="count-cell">{{ row.recordCount ?? '-' }}</span></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.status === 'success' ? 'badge badge-green' : row.status === 'failed' ? 'badge badge-red' : 'badge badge-orange'">
              {{ row.status === 'success' ? '成功' : row.status === 'failed' ? '失败' : '部分成功' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="同步时间" width="170" align="center">
          <template #default="{ row }"><span class="time-cell">{{ row.createTime || '-' }}</span></template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && logs.length === 0" description="暂无同步日志" />

      <div class="page-pagination" v-if="total > 0">
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50,100]" layout="total, sizes, prev, pager, next" @current-change="loadData" @size-change="handleSizeChange" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getSyncLogs } from '@/api/sync'
import { ElMessage } from 'element-plus'

const logs = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getSyncLogs({ page: page.value, size: pageSize.value })
    logs.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { ElMessage.error('加载同步日志失败') } finally { loading.value = false }
}
const handleSizeChange = () => { page.value = 1; loadData() }

onMounted(loadData)

let refreshTimer: ReturnType<typeof setInterval> | null = null
onMounted(() => { refreshTimer = setInterval(loadData, 30000) })
onUnmounted(() => { if (refreshTimer) clearInterval(refreshTimer) })
</script>

<style scoped lang="scss">
.count-cell {
  font-weight: 600;
  color: var(--apple-blue);
  font-size: 14px;
  font-family: 'SF Mono', 'Menlo', monospace;
}
</style>
