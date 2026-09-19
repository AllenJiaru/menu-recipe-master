<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon blue">
          <el-icon :size="22"><Grid /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>分类管理</h2>
          <p>管理菜谱分类信息</p>
        </div>
      </div>
      <el-button type="primary" @click="showDialog()" v-permission="'content:category:create'">
        <el-icon><Plus /></el-icon> 新建分类
      </el-button>
    </div>

    <div class="page-toolbar">
      <div class="page-search">
        <el-icon class="search-icon"><Search /></el-icon>
        <input v-model="searchKeyword" placeholder="搜索分类名称..." @keyup.enter="handleSearch" />
      </div>
      <div class="page-toolbar-right">
        <span class="page-count">共 <b>{{ total }}</b> 条</span>
      </div>
    </div>

    <div class="page-table-card" v-loading="loading">
      <el-table :data="categories" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70">
          <template #default="{ row }">
            <span class="id-badge">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" min-width="140">
          <template #default="{ row }">
            <span class="name-cell">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="图标" width="80">
          <template #default="{ row }">
            <div class="icon-cell">
              <el-icon v-if="iconMap[row.icon]" :size="18" :color="iconColors[row.icon] || '#6366f1'">
                <component :is="iconMap[row.icon]" />
              </el-icon>
              <span v-else class="icon-placeholder">{{ row.icon || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <span class="badge" :class="row.status === 1 ? 'badge-success' : 'badge-info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            <span class="time-cell">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" size="small" class="action-btn" @click="showDialog(row)" v-permission="'content:category:edit'">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-popconfirm title="确定删除?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button text type="danger" size="small" class="action-btn danger" v-permission="'content:category:delete'">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </template>
            </el-popconfirm>
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

    <el-dialog v-model="dialogVisible" :title="editItem ? '编辑分类' : '新建分类'" width="450px" destroy-on-close>
      <el-form :model="form" label-width="80px" size="large">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/category'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'

const categories = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editItem = ref<any>(null)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchKeyword = ref('')
const form = reactive({ name: '', icon: '', sortOrder: 0, status: 1 })

const iconMap: Record<string, string> = {
  meat: 'Finished',
  vegetable: 'Magnet',
  soup: 'Coffee',
  dessert: 'IceCream',
  steamed: 'Bowl',
  stewed: 'House',
  cold: 'Snowflake',
  'stir-fry': 'Sunny',
  braised: 'AlarmClock',
  other: 'More'
}

const iconColors: Record<string, string> = {
  meat: '#ef4444',
  vegetable: '#10b981',
  soup: '#f59e0b',
  dessert: '#ec4899',
  steamed: '#06b6d4',
  stewed: '#8b5cf6',
  cold: '#3b82f6',
  'stir-fry': '#f97316',
  braised: '#6366f1',
  other: '#6b7280'
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getCategories({ page: page.value, size: pageSize.value })
    categories.value = res.data.records
    total.value = res.data.total
  } catch { ElMessage.error('加载分类失败') } finally { loading.value = false }
}

const handleSearch = () => { page.value = 1; loadData() }
const handleSizeChange = () => { page.value = 1; loadData() }

const handleDelete = async (id: number) => {
  try { await deleteCategory(id); ElMessage.success('删除成功'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '删除失败') }
}

const showDialog = (item?: any) => {
  editItem.value = item || null
  if (item) { Object.assign(form, { name: item.name, icon: item.icon, sortOrder: item.sortOrder, status: item.status }) }
  else { Object.assign(form, { name: '', icon: '', sortOrder: 0, status: 1 }) }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.name.trim()) { ElMessage.warning('请输入分类名称'); return }
  saving.value = true
  try {
    if (editItem.value) { await updateCategory(editItem.value.id, form) }
    else { await createCategory(form) }
    ElMessage.success(editItem.value ? '更新成功' : '创建成功')
    dialogVisible.value = false; loadData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
  finally { saving.value = false }
}

onMounted(loadData)
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

.icon-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-placeholder {
  color: var(--text-tertiary);
  font-size: 13px;
}

.time-cell {
  color: var(--text-tertiary);
  font-size: 13px;
  font-family: 'SF Mono', 'Menlo', monospace;
}

.action-btn {
  font-size: 13px !important;
  color: var(--text-secondary) !important;
  height: 30px !important;
  padding: 0 8px !important;
  border-radius: var(--radius-xs) !important;
  &:hover {
    color: var(--apple-blue) !important;
    background: rgba(0, 122, 255, 0.06) !important;
  }
  &.danger:hover {
    color: var(--apple-red) !important;
    background: rgba(255, 59, 48, 0.06) !important;
  }
  .el-icon {
    margin-right: 3px;
  }
}
</style>
