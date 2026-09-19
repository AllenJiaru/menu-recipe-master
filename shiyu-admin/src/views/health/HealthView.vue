<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon green">
          <el-icon :size="22"><Monitor /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>系统监控</h2>
          <p>系统健康状态与维护管理</p>
        </div>
      </div>
      <el-button type="primary" @click="loadAll"><el-icon><Refresh /></el-icon> 刷新状态</el-button>
    </div>

    <div class="stat-cards">
      <div class="page-card stat-card" v-for="(item, key) in healthStatus" :key="key">
        <div class="stat-icon" :class="item.status">
          <el-icon :size="24"><component :is="item.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-label">{{ item.label }}</span>
          <span class="stat-value" :class="item.status">{{ item.statusText }}</span>
        </div>
      </div>
    </div>

    <div class="action-cards">
      <div class="page-card action-card">
        <div class="page-card-header">
          <h3>备份管理</h3>
          <el-button type="primary" size="small" @click="handleBackup"><el-icon><FolderAdd /></el-icon> 创建备份</el-button>
        </div>
        <div class="backup-list" v-loading="backupLoading">
          <div v-for="item in backups" :key="item.id" class="backup-item">
            <div class="backup-info">
              <span class="backup-name">{{ item.name }}</span>
              <span class="backup-time">{{ formatDate(item.createTime) }}</span>
            </div>
            <el-button class="action-btn" text type="primary" size="small" @click="handleRestore(item)">恢复</el-button>
          </div>
          <el-empty v-if="!backupLoading && backups.length === 0" description="暂无备份" :image-size="60" />
        </div>
      </div>

      <div class="page-card action-card">
        <div class="page-card-header">
          <h3>缓存管理</h3>
          <el-button type="warning" size="small" @click="handleClearCache" :loading="clearingCache"><el-icon><Delete /></el-icon> 清除缓存</el-button>
        </div>
        <div class="cache-info">
          <p>清除系统缓存以释放内存空间</p>
          <p class="cache-tip">注意：清除缓存后系统可能短暂变慢</p>
        </div>
      </div>

      <div class="page-card action-card">
        <div class="page-card-header">
          <h3>数据库优化</h3>
          <el-button type="success" size="small" @click="handleOptimize" :loading="optimizing"><el-icon><Setting /></el-icon> 优化数据库</el-button>
        </div>
        <div class="cache-info">
          <p>优化数据库表结构与索引</p>
          <p class="cache-tip">注意：优化过程中数据库可能暂时不可用</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { getSystemHealth, getDatabaseHealth, getDiskHealth, getCacheHealth, clearCache, optimizeDatabase, getBackupList, createBackup, restoreBackup } from '@/api/health'
import { formatDate } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'

const healthStatus = reactive({
  database: { label: '数据库', status: 'loading', statusText: '检测中...', icon: 'Coin' },
  disk: { label: '磁盘', status: 'loading', statusText: '检测中...', icon: 'Coin' },
  cache: { label: '缓存', status: 'loading', statusText: '检测中...', icon: 'Coin' },
  system: { label: '系统', status: 'loading', statusText: '检测中...', icon: 'Coin' }
})

const backups = ref<any[]>([])
const backupLoading = ref(false)
const clearingCache = ref(false)
const optimizing = ref(false)

const loadAll = async () => {
  const checks = [
    { key: 'database', fn: getDatabaseHealth },
    { key: 'disk', fn: getDiskHealth },
    { key: 'cache', fn: getCacheHealth },
    { key: 'system', fn: getSystemHealth }
  ]
  for (const check of checks) {
    try {
      const res: any = await check.fn()
      const ok = res.data?.status === 'ok' || res.data?.status === 'UP'
      healthStatus[check.key as keyof typeof healthStatus].status = ok ? 'success' : 'danger'
      healthStatus[check.key as keyof typeof healthStatus].statusText = ok ? '正常' : '异常'
    } catch {
      healthStatus[check.key as keyof typeof healthStatus].status = 'danger'
      healthStatus[check.key as keyof typeof healthStatus].statusText = '异常'
    }
  }
}

const loadBackups = async () => {
  backupLoading.value = true
  try {
    const res: any = await getBackupList()
    backups.value = res.data?.records || res.data || []
  } catch { /* ignore */ } finally { backupLoading.value = false }
}

const handleBackup = async () => {
  try {
    await ElMessageBox.confirm('确定创建系统备份？', '创建备份', { type: 'info' })
    await createBackup()
    ElMessage.success('备份创建成功'); loadBackups()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') ElMessage.error(e.message || '备份失败')
  }
}

const handleRestore = async (item: any) => {
  try {
    await ElMessageBox.confirm(`确定恢复到备份 ${item.name}？此操作不可逆！`, '恢复备份', { type: 'warning' })
    await restoreBackup(item.name)
    ElMessage.success('恢复成功')
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') ElMessage.error(e.message || '恢复失败')
  }
}

const handleClearCache = async () => {
  clearingCache.value = true
  try {
    await clearCache()
    ElMessage.success('缓存清除成功')
  } catch (e: any) { ElMessage.error(e.message || '清除失败') }
  finally { clearingCache.value = false }
}

const handleOptimize = async () => {
  optimizing.value = true
  try {
    await optimizeDatabase()
    ElMessage.success('数据库优化完成')
  } catch (e: any) { ElMessage.error(e.message || '优化失败') }
  finally { optimizing.value = false }
}

onMounted(() => { loadAll(); loadBackups() })

let refreshTimer: ReturnType<typeof setInterval> | null = null
onMounted(() => { refreshTimer = setInterval(loadAll, 30000) })
onUnmounted(() => { if (refreshTimer) clearInterval(refreshTimer) })
</script>

<style scoped lang="scss">
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  &.loading { background: var(--bg-page); color: var(--text-secondary); }
  &.success { background: rgba(52, 199, 89, 0.12); color: var(--apple-green); }
  &.danger { background: rgba(255, 59, 48, 0.12); color: var(--apple-red); }
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  &.loading { color: var(--text-secondary); }
  &.success { color: var(--apple-green); }
  &.danger { color: var(--apple-red); }
}

.action-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.action-card {
  padding: 24px;
}

.backup-list {
  max-height: 200px;
  overflow-y: auto;
}

.backup-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-light);
  &:last-child { border-bottom: none; }
}

.backup-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.backup-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.backup-time {
  font-size: 11px;
  color: var(--text-tertiary);
}

.cache-info {
  p {
    font-size: 13px;
    color: var(--text-secondary);
    margin-bottom: 4px;
  }
  .cache-tip {
    font-size: 12px;
    color: var(--text-tertiary);
    font-style: italic;
  }
}

.action-btn {
  font-size: 13px !important; color: var(--text-secondary) !important;
  height: 30px !important; padding: 0 8px !important;
  border-radius: var(--radius-xs) !important;
  &:hover { color: var(--apple-blue) !important; background: rgba(0, 122, 255, 0.06) !important; }
}

@media (max-width: 900px) {
  .stat-cards { grid-template-columns: repeat(2, 1fr); }
  .action-cards { grid-template-columns: 1fr; }
}
</style>