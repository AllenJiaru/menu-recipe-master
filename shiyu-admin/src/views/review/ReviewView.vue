<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon red">
          <el-icon :size="22"><Bell /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>菜谱审核</h2>
          <p>审核用户提交的菜谱，支持批量通过 / 拒绝</p>
        </div>
      </div>
      <span v-if="pendingCount > 0" class="badge badge-orange" style="font-size: 13px; height: 28px; padding: 0 12px; gap: 6px;">
        <el-icon><Bell /></el-icon> {{ pendingCount }} 条待审核
      </span>
    </div>

    <div class="page-toolbar">
      <div class="page-chips">
        <span class="chip" :class="{ active: filters.status === '' }" @click="filters.status = ''; loadData()">全部</span>
        <span class="chip" :class="{ active: filters.status === 'pending' }" @click="filters.status = 'pending'; loadData()">
          <span class="chip-dot" style="background: var(--apple-orange);"></span>待审核
        </span>
        <span class="chip" :class="{ active: filters.status === 'approved' }" @click="filters.status = 'approved'; loadData()">
          <span class="chip-dot" style="background: var(--apple-green);"></span>已通过
        </span>
        <span class="chip" :class="{ active: filters.status === 'rejected' }" @click="filters.status = 'rejected'; loadData()">
          <span class="chip-dot" style="background: var(--apple-red);"></span>已拒绝
        </span>
      </div>
      <div class="page-toolbar-right">
        <span class="page-count">共 <b>{{ total }}</b> 条</span>
      </div>
    </div>

    <div class="page-table-card" v-loading="loading">
      <transition name="batch-slide">
        <div v-if="selectedRows.length" class="batch-bar">
          <div class="batch-info">
            <el-icon><Select /></el-icon>
            已选择 <b>{{ selectedRows.length }}</b> 条
            <span v-if="selectedPendingCount" class="batch-hint">（其中 {{ selectedPendingCount }} 条待审核）</span>
          </div>
          <div class="batch-actions">
            <el-button size="small" class="batch-btn ok" :disabled="!selectedPendingCount" @click="openBatchDialog('approved')">
              <el-icon><CircleCheck /></el-icon> 批量通过
            </el-button>
            <el-button size="small" class="batch-btn no" :disabled="!selectedPendingCount" @click="openBatchDialog('rejected')">
              <el-icon><CircleClose /></el-icon> 批量拒绝
            </el-button>
            <el-button size="small" type="danger" plain @click="handleBatchDelete">
              <el-icon><Delete /></el-icon> 批量删除
            </el-button>
            <el-button size="small" text @click="clearSelection">取消选择</el-button>
          </div>
        </div>
      </transition>

      <el-table ref="tableRef" :data="reviews" stripe style="width: 100%" row-key="id" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="46" reserve-selection :selectable="() => true" />
        <el-table-column prop="id" label="ID" width="70">
          <template #default="{ row }"><span class="id-badge">{{ row.id }}</span></template>
        </el-table-column>
        <el-table-column label="菜谱名称" min-width="180">
          <template #default="{ row }">
            <span class="name-cell">{{ row.recipeName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reviewer" label="提交/审核人" width="130">
          <template #default="{ row }"><span class="time-cell">{{ row.reviewer || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="status-badge" :class="row.status">
              <i class="dot" />{{ statusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="审核意见" min-width="180" show-overflow-tooltip>
          <template #default="{ row }"><span class="time-cell">{{ row.comment || '-' }}</span></template>
        </el-table-column>
        <el-table-column label="审核时间" width="170">
          <template #default="{ row }"><span class="time-cell">{{ row.reviewTime ? formatDate(row.reviewTime) : '-' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'pending'">
              <el-button class="action-btn" size="small" @click="showReview(row, 'approved')" v-permission="'business:review:approve'">通过</el-button>
              <el-button class="action-btn danger" size="small" @click="showReview(row, 'rejected')" v-permission="'business:review:reject'">拒绝</el-button>
            </template>
            <span v-else class="time-cell">已审核</span>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-state">
            <el-icon :size="48"><CircleCheck /></el-icon>
            <p>暂无审核记录</p>
          </div>
        </template>
      </el-table>

      <div class="page-pagination">
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" background @current-change="loadData" @size-change="handleSizeChange" />
      </div>
    </div>

    <el-dialog v-model="reviewDialogVisible" :title="reviewAction === 'approved' ? '通过审核' : '拒绝审核'" width="460px" destroy-on-close>
      <el-form :model="reviewForm" label-width="80px" size="large">
        <el-form-item label="菜谱"><span>{{ reviewItem?.recipeName }}</span></el-form-item>
        <el-form-item label="审核意见"><el-input v-model="reviewForm.comment" type="textarea" :rows="3" placeholder="请输入审核意见" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button :type="reviewAction === 'approved' ? 'success' : 'danger'" :loading="saving" @click="handleReview">{{ reviewAction === 'approved' ? '确认通过' : '确认拒绝' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchDialogVisible" :title="batchAction === 'approved' ? '批量通过审核' : '批量拒绝审核'" width="460px" destroy-on-close>
      <el-form :model="batchForm" label-width="80px" size="large">
        <el-form-item label="数量">
          <span class="batch-count">将对 <b>{{ selectedPendingCount }}</b> 条待审核记录执行{{ batchAction === 'approved' ? '通过' : '拒绝' }}操作</span>
        </el-form-item>
        <el-form-item label="审核意见"><el-input v-model="batchForm.comment" type="textarea" :rows="3" :placeholder="batchAction === 'approved' ? '可填写通过备注' : '可填写拒绝原因'" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button :type="batchAction === 'approved' ? 'success' : 'danger'" :loading="saving" @click="handleBatchAudit">{{ batchAction === 'approved' ? '确认全部通过' : '确认全部拒绝' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { getReviews, updateReview, batchAuditReviews, batchDeleteReviews } from '@/api/review'
import { formatDate } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'

const reviews = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const tableRef = ref()
const selectedRows = ref<any[]>([])
const reviewDialogVisible = ref(false)
const reviewItem = ref<any>(null)
const reviewAction = ref<'approved' | 'rejected'>('approved')
const batchDialogVisible = ref(false)
const batchAction = ref<'approved' | 'rejected'>('approved')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const filters = reactive({ status: '' })
const reviewForm = reactive({ comment: '' })
const batchForm = reactive({ comment: '' })

const pendingCount = computed(() => reviews.value.filter(r => r.status === 'pending').length)
const selectedPendingCount = computed(() => selectedRows.value.filter(r => r.status === 'pending').length)

const statusText = (status: string) => status === 'pending' ? '待审核' : status === 'approved' ? '已通过' : '已拒绝'

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getReviews({ page: page.value, size: pageSize.value, ...filters })
    reviews.value = res.data.records
    total.value = res.data.total
  } catch { ElMessage.error('加载审核列表失败') } finally { loading.value = false }
}

const handleSizeChange = () => { page.value = 1; loadData() }

const onSelectionChange = (rows: any[]) => { selectedRows.value = rows }
const clearSelection = () => { tableRef.value?.clearSelection(); selectedRows.value = [] }

const showReview = (item: any, action: 'approved' | 'rejected') => {
  reviewItem.value = item
  reviewAction.value = action
  reviewForm.comment = ''
  reviewDialogVisible.value = true
}

const handleReview = async () => {
  if (!reviewItem.value) return
  saving.value = true
  try {
    await updateReview(reviewItem.value.id, { status: reviewAction.value, comment: reviewForm.comment })
    ElMessage.success(reviewAction.value === 'approved' ? '审核通过' : '已拒绝')
    reviewDialogVisible.value = false; loadData()
  } catch (e: any) { ElMessage.error(e.message || '审核失败') }
  finally { saving.value = false }
}

const openBatchDialog = (action: 'approved' | 'rejected') => {
  if (!selectedPendingCount.value) { ElMessage.warning('所选记录中没有待审核项'); return }
  batchAction.value = action
  batchForm.comment = ''
  batchDialogVisible.value = true
}

const handleBatchAudit = async () => {
  const ids = selectedRows.value.filter(r => r.status === 'pending').map(r => r.id)
  if (!ids.length) return
  saving.value = true
  try {
    await batchAuditReviews(ids, batchAction.value === 'approved' ? 1 : 2, batchForm.comment)
    ElMessage.success(`已批量${batchAction.value === 'approved' ? '通过' : '拒绝'} ${ids.length} 条审核`)
    batchDialogVisible.value = false
    clearSelection()
    loadData()
  } catch (e: any) { ElMessage.error(e.message || '批量审核失败') }
  finally { saving.value = false }
}

const handleBatchDelete = async () => {
  const ids = selectedRows.value.map(r => r.id)
  if (!ids.length) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 条审核记录吗？`, '批量删除确认', {
      type: 'warning', confirmButtonText: '全部删除', cancelButtonText: '取消'
    })
    await batchDeleteReviews(ids)
    ElMessage.success(`已删除 ${ids.length} 条记录`)
    clearSelection()
    loadData()
  } catch { /* cancelled */ }
}

onMounted(loadData)

let refreshTimer: ReturnType<typeof setInterval> | null = null
onMounted(() => { refreshTimer = setInterval(loadData, 30000) })
onUnmounted(() => { if (refreshTimer) clearInterval(refreshTimer) })
</script>

<style scoped lang="scss">
.batch-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 20px;
  background: rgba(0, 122, 255, 0.04);
  border-bottom: 1px solid var(--border-light);

  .batch-info {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: var(--text-secondary);
    .el-icon { color: var(--apple-blue); }
    b { color: var(--apple-blue); font-size: 16px; font-weight: 700; }
    .batch-hint { font-size: 12px; color: var(--text-tertiary); }
  }
  .batch-actions {
    display: flex;
    align-items: center;
    gap: 8px;
    .batch-btn {
      border-radius: var(--radius-sm);
      font-weight: 600;
      &.ok:not(:disabled):hover { color: var(--apple-green); border-color: var(--apple-green); background: rgba(52, 199, 89, 0.06); }
      &.no:not(:disabled):hover { color: var(--apple-red); border-color: var(--apple-red); background: rgba(255, 59, 48, 0.06); }
    }
  }
}

.batch-slide-enter-active,
.batch-slide-leave-active { transition: all 0.28s ease; }
.batch-slide-enter-from,
.batch-slide-leave-to { opacity: 0; transform: translateY(-8px); }

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 600;
  border-radius: var(--radius-full);
  .dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
  &.approved { color: var(--apple-green); background: rgba(52, 199, 89, 0.08); }
  &.pending { color: var(--apple-orange); background: rgba(255, 149, 0, 0.08); }
  &.rejected { color: var(--apple-red); background: rgba(255, 59, 48, 0.08); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 0;
  color: var(--text-tertiary);
  p { font-size: 14px; }
}

.batch-count {
  font-size: 14px;
  color: var(--text-secondary);
  b { color: var(--apple-blue); font-size: 16px; font-weight: 700; }
}
</style>
