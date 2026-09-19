<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon green"><el-icon :size="22"><Notebook /></el-icon></div>
        <div class="page-header-text">
          <h2>菜谱管理</h2>
          <p>管理所有菜谱信息，支持搜索、筛选和分类</p>
        </div>
      </div>
      <el-button type="primary" @click="$router.push('/recipes/create')" v-permission="'content:recipe:create'">
        <el-icon><Plus /></el-icon> 新建菜谱
      </el-button>
    </div>

    <div class="page-toolbar">
      <div class="page-search">
        <el-input v-model="filters.keyword" placeholder="搜索菜名..." clearable @keyup.enter="loadData" @clear="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
      <el-select v-model="filters.difficulty" placeholder="全部难度" clearable @change="loadData" style="width: 140px">
        <el-option v-for="d in 5" :key="d" :label="difficultyMap[d]" :value="d" />
      </el-select>
      <el-select v-model="filters.status" placeholder="全部状态" clearable @change="loadData" style="width: 140px">
        <el-option label="上架" :value="1" />
        <el-option label="待审核" :value="2" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button @click="resetFilters">
        <el-icon><RefreshLeft /></el-icon> 重置
      </el-button>
      <div class="page-toolbar-right">
        <span class="page-count">共 <b>{{ total }}</b> 道菜谱</span>
      </div>
    </div>

    <div class="page-chips">
      <span class="chip" :class="{ active: filters.categoryId === null }" @click="selectCategory(null)">全部</span>
      <span v-for="c in categories" :key="c.id" class="chip" :class="{ active: filters.categoryId === c.id }" @click="selectCategory(c.id)">{{ c.name }}</span>
    </div>

    <div class="page-table-card" v-loading="loading">
      <transition name="batch-slide">
        <div v-if="selectedRows.length" class="batch-bar">
          <div class="batch-info">
            <el-icon><Select /></el-icon>
            已选择 <b>{{ selectedRows.length }}</b> 道菜谱
          </div>
          <div class="batch-actions">
            <el-button size="small" class="batch-btn" @click="handleBatchStatus(1)">
              <el-icon><Top /></el-icon> 批量上架
            </el-button>
            <el-button size="small" class="batch-btn" @click="handleBatchStatus(0)">
              <el-icon><Bottom /></el-icon> 批量下架
            </el-button>
            <el-button size="small" class="batch-btn warn" @click="handleBatchReview">
              <el-icon><Upload /></el-icon> 批量提交审核
            </el-button>
            <el-button size="small" type="danger" plain @click="handleBatchDelete">
              <el-icon><Delete /></el-icon> 批量删除
            </el-button>
            <el-button size="small" text @click="clearSelection">取消选择</el-button>
          </div>
        </div>
      </transition>

      <el-table ref="tableRef" :data="recipes" stripe style="width: 100%" row-key="id" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="46" reserve-selection />
        <el-table-column label="菜谱" min-width="260">
          <template #default="{ row }">
            <div class="dish-cell">
              <div class="dish-cover">
                <el-image v-if="row.coverImage" :src="imgUrl(row.coverImage)" fit="cover" class="cover-img" lazy />
                <div v-else class="cover-placeholder"><el-icon :size="20"><Picture /></el-icon></div>
              </div>
              <div class="dish-info">
                <div class="dish-name" @click.stop="$router.push(`/recipes/${row.id}`)">{{ row.name }}</div>
                <div class="dish-meta">
                  <el-tag size="small" effect="plain" round>{{ recipeTypeMap[row.type] }}</el-tag>
                  <span v-if="row.description" class="dish-desc">{{ row.description }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="难度" width="130">
          <template #default="{ row }">
            <div class="difficulty">
              <span class="dots">
                <i v-for="i in 5" :key="i" class="dot" :class="{ on: i <= (row.difficulty || 0) }" />
              </span>
              <span class="diff-text">{{ difficultyMap[row.difficulty] || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="烹饪时间" width="120">
          <template #default="{ row }">
            <span class="time-cell"><el-icon><Clock /></el-icon>{{ row.cookingTime || 0 }} 分钟</span>
          </template>
        </el-table-column>
        <el-table-column label="被点次数" width="120" sortable sort-by="orderCount">
          <template #default="{ row }">
            <span class="meta-item hot"><el-icon><Histogram /></el-icon>{{ row.orderCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="status-badge" :class="statusClass(row.status)">
              <i class="dot" />{{ statusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <div class="actions">
              <el-tooltip content="查看详情" placement="top" :show-after="400">
                <el-button circle class="icon-btn" @click="$router.push(`/recipes/${row.id}`)"><el-icon><View /></el-icon></el-button>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top" :show-after="400">
                <el-button circle class="icon-btn" v-permission="'content:recipe:edit'" @click="$router.push(`/recipes/${row.id}/edit`)"><el-icon><Edit /></el-icon></el-button>
              </el-tooltip>
              <el-tooltip v-if="row.status !== 2" content="提交审核" placement="top" :show-after="400">
                <el-button circle class="icon-btn warn" @click="handleSubmitReview(row)"><el-icon><Upload /></el-icon></el-button>
              </el-tooltip>
              <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, row)">
                <el-button circle class="icon-btn"><el-icon><MoreFilled /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="toggle">
                      <el-icon><Switch /></el-icon>{{ row.status === 1 ? '下架' : '上架' }}
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided class="danger-item">
                      <el-icon><Delete /></el-icon>删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="empty-state">
            <el-icon :size="48"><Notebook /></el-icon>
            <p>暂无菜谱数据</p>
            <el-button type="primary" plain @click="resetFilters">清除筛选条件</el-button>
          </div>
        </template>
      </el-table>

      <div class="page-pagination">
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" background @current-change="loadData" @size-change="handleSizeChange" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getRecipes, deleteRecipe, updateRecipeStatus, getCategories, batchDeleteRecipes, batchUpdateRecipeStatus } from '@/api/recipe'
import { createReview, batchCreateReviews } from '@/api/review'
import { recipeTypeMap, difficultyMap, imgUrl } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'

const recipes = ref<any[]>([])
const categories = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const tableRef = ref()
const selectedRows = ref<any[]>([])
const filters = reactive({
  keyword: '',
  categoryId: null as number | null,
  difficulty: null as number | null,
  status: null as number | null
})

const onSelectionChange = (rows: any[]) => { selectedRows.value = rows }
const clearSelection = () => { tableRef.value?.clearSelection(); selectedRows.value = [] }

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getRecipes({ page: page.value, size: pageSize.value, ...filters })
    recipes.value = res.data.records
    total.value = res.data.total
  } catch { ElMessage.error('加载菜谱失败') } finally { loading.value = false }
}

const handleSizeChange = () => { page.value = 1; loadData() }

const selectCategory = (id: number | null) => {
  filters.categoryId = id
  page.value = 1
  loadData()
}

const resetFilters = () => {
  filters.keyword = ''
  filters.categoryId = null
  filters.difficulty = null
  filters.status = null
  page.value = 1
  loadData()
}

const statusText = (status: number) => status === 1 ? '上架' : status === 2 ? '待审核' : '下架'
const statusClass = (status: number) => status === 1 ? 'success' : status === 2 ? 'warning' : 'info'

const handleDelete = async (id: number) => {
  try { await deleteRecipe(id); ElMessage.success('删除成功'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '删除失败') }
}

const handleStatus = async (row: any) => {
  try { await updateRecipeStatus(row.id, row.status === 1 ? 0 : 1); ElMessage.success('操作成功'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

const handleSubmitReview = async (row: any) => {
  try {
    await createReview({ recipeId: row.id, status: 'pending', comment: '' })
    ElMessage.success('已提交审核，等待管理员审批')
    loadData()
  } catch (e: any) { ElMessage.error(e.message || '提交审核失败') }
}

const handleCommand = async (cmd: string, row: any) => {
  if (cmd === 'toggle') {
    await handleStatus(row)
  } else if (cmd === 'delete') {
    try {
      await ElMessageBox.confirm(`确定删除菜谱「${row.name}」吗？`, '删除确认', {
        type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
      })
      await handleDelete(row.id)
    } catch { /* cancelled */ }
  }
}

const handleBatchStatus = async (status: number) => {
  const ids = selectedRows.value.map(r => r.id)
  if (!ids.length) return
  try {
    await batchUpdateRecipeStatus(ids, status)
    ElMessage.success(`已批量${status === 1 ? '上架' : '下架'} ${ids.length} 道菜谱`)
    clearSelection()
    loadData()
  } catch (e: any) { ElMessage.error(e.message || '批量操作失败') }
}

const handleBatchReview = async () => {
  const ids = selectedRows.value.filter(r => r.status !== 2).map(r => r.id)
  if (!ids.length) { ElMessage.warning('所选菜谱均已处于待审核状态'); return }
  try {
    await batchCreateReviews(ids)
    ElMessage.success(`已批量提交 ${ids.length} 道菜谱审核`)
    clearSelection()
    loadData()
  } catch (e: any) { ElMessage.error(e.message || '批量提交失败') }
}

const handleBatchDelete = async () => {
  const ids = selectedRows.value.map(r => r.id)
  if (!ids.length) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 道菜谱吗？此操作不可恢复。`, '批量删除确认', {
      type: 'warning', confirmButtonText: '全部删除', cancelButtonText: '取消'
    })
    await batchDeleteRecipes(ids)
    ElMessage.success(`已删除 ${ids.length} 道菜谱`)
    clearSelection()
    loadData()
  } catch { /* cancelled */ }
}

onMounted(async () => {
  const res: any = await getCategories().catch(() => ({ data: [] }))
  categories.value = res.data || []
  loadData()
})
</script>

<style scoped lang="scss">
.batch-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 20px;
  background: var(--bg-page);
  border-bottom: 1px solid var(--border-color);
  .batch-info {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: var(--text-secondary);
    .el-icon { color: var(--apple-blue); }
    b { color: var(--apple-blue); font-size: 16px; font-weight: 600; }
  }
  .batch-actions {
    display: flex;
    align-items: center;
    gap: 8px;
    .batch-btn {
      border-radius: var(--radius-sm);
      font-weight: 500;
      &:hover { color: var(--apple-blue); border-color: var(--apple-blue); background: rgba(0, 122, 255, 0.08); }
      &.warn:hover { color: var(--apple-orange); border-color: var(--apple-orange); background: rgba(255, 149, 0, 0.08); }
    }
  }
}

.batch-slide-enter-active,
.batch-slide-leave-active { transition: all 0.2s ease; }
.batch-slide-enter-from,
.batch-slide-leave-to { opacity: 0; transform: translateY(-8px); }

.dish-cell {
  display: flex;
  align-items: center;
  gap: 14px;
}

.dish-cover {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--bg-page);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border-color);
  .cover-img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.2s ease; }
  &:hover .cover-img { transform: scale(1.05); }
}

.cover-placeholder { color: var(--text-tertiary); }

.dish-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dish-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.2;
  cursor: pointer;
  transition: color 0.15s;

  &:hover {
    color: var(--apple-blue);
  }
}

.dish-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  .dish-desc {
    font-size: 12px;
    color: var(--text-tertiary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 220px;
  }
}

.difficulty {
  display: flex;
  align-items: center;
  gap: 8px;
  .dots { display: inline-flex; gap: 3px; }
  .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--border-color);
    &.on { background: var(--apple-blue); }
  }
  .diff-text { font-size: 12px; color: var(--text-secondary); font-weight: 500; }
}

.time-cell {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: var(--text-tertiary);
  font-size: 13px;
  font-family: 'SF Mono', 'Menlo', monospace;
  .el-icon { color: var(--text-tertiary); }
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  .el-icon { color: var(--text-tertiary); }
  &.hot { color: var(--apple-blue); font-weight: 600; .el-icon { color: var(--apple-blue); } }
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;
  border-radius: 20px;
  .dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
  &.success { color: var(--apple-green); background: rgba(52, 199, 89, 0.1); }
  &.warning { color: var(--apple-orange); background: rgba(255, 149, 0, 0.1); }
  &.info { color: var(--text-tertiary); background: var(--bg-page); }
}

.actions {
  display: flex;
  align-items: center;
  gap: 6px;
  .icon-btn {
    width: 30px;
    height: 30px;
    border: 1px solid var(--border-color);
    background: var(--bg-card);
    color: var(--text-secondary);
    transition: all 0.2s ease;
    &:hover { color: var(--apple-blue); border-color: var(--apple-blue); background: rgba(0, 122, 255, 0.08); }
    &.warn:hover { color: var(--apple-orange); border-color: var(--apple-orange); background: rgba(255, 149, 0, 0.08); }
  }
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
</style>
