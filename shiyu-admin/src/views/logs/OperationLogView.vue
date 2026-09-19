<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon orange">
          <el-icon :size="22"><Document /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>操作日志</h2>
          <p>查看系统操作记录</p>
        </div>
      </div>
    </div>

    <div class="page-toolbar">
      <div class="page-search">
        <el-icon class="search-icon"><Search /></el-icon>
        <input v-model="filters.keyword" placeholder="搜索用户、操作..." @keyup.enter="loadData" />
      </div>
      <div class="page-toolbar-right">
        <span class="page-count">共 <b>{{ total }}</b> 条</span>
      </div>
    </div>

    <div class="page-table-card" v-loading="loading">
      <el-table :data="logs" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70">
          <template #default="{ row }">
            <span class="id-badge">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户" width="110">
          <template #default="{ row }">
            <span class="name-cell">{{ row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作" width="110">
          <template #default="{ row }">
            <span class="badge badge-primary">{{ row.action }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="目标" width="110" />
        <el-table-column prop="targetId" label="目标ID" width="80">
          <template #default="{ row }">
            <span class="id-cell">#{{ row.targetId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="详情" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="remark-cell">{{ row.detail || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="140">
          <template #default="{ row }">
            <span class="ip-cell">{{ row.ip }}</span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            <span class="time-cell">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadData"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { getLogs } from '@/api/operationLog'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'

const logs = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const filters = reactive({ keyword: '' })

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getLogs({ page: page.value, size: pageSize.value, ...filters })
    logs.value = res.data.records
    total.value = res.data.total
  } catch { ElMessage.error('加载日志失败') } finally { loading.value = false }
}
const handleSizeChange = () => { page.value = 1; loadData() }

onMounted(loadData)

let refreshTimer: ReturnType<typeof setInterval> | null = null
onMounted(() => { refreshTimer = setInterval(loadData, 30000) })
onUnmounted(() => { if (refreshTimer) clearInterval(refreshTimer) })
</script>

<style scoped lang="scss">
.id-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 26px;
  height: 22px;
  padding: 0 6px;
  border-radius: var(--radius-xs);
  background: var(--bg-page);
  color: var(--text-tertiary);
  font-size: 12px;
  font-weight: 600;
  font-family: 'SF Mono', 'Menlo', monospace;
}

.name-cell {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.id-cell {
  font-family: 'SF Mono', 'Menlo', monospace;
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 13px;
}

.remark-cell {
  color: var(--text-secondary);
  font-size: 13px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ip-cell {
  font-family: 'SF Mono', 'Menlo', monospace;
  color: var(--text-tertiary);
  font-size: 12px;
}

.time-cell {
  color: var(--text-tertiary);
  font-size: 13px;
  font-family: 'SF Mono', 'Menlo', monospace;
}
</style>
