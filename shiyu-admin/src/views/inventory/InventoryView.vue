<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon purple">
          <el-icon :size="22"><Box /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>库存管理</h2>
          <p>管理食材库存信息与补货</p>
        </div>
      </div>
      <el-button type="primary" @click="showDialog()" v-permission="'business:inventory:create'"><el-icon><Plus /></el-icon> 新建库存</el-button>
    </div>

    <div class="page-toolbar">
      <div class="page-search">
        <el-icon class="search-icon"><Search /></el-icon>
        <input v-model="filters.keyword" placeholder="搜索食材..." @keyup.enter="loadData" />
      </div>
      <el-select v-model="filters.categoryId" placeholder="全部分类" clearable style="width: 140px" @change="loadData">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <div class="page-toolbar-right">
        <span class="page-count">共 <b>{{ total }}</b> 条</span>
      </div>
    </div>

    <div class="page-table-card" v-loading="loading">
      <el-table :data="items" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60">
          <template #default="{ row }"><span class="id-badge">{{ row.id }}</span></template>
        </el-table-column>
        <el-table-column prop="name" label="食材名称" min-width="130">
          <template #default="{ row }"><span class="name-cell">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column label="分类" width="100">
          <template #default="{ row }"><span class="badge badge-blue">{{ row.categoryName }}</span></template>
        </el-table-column>
        <el-table-column prop="quantity" label="库存量" width="100">
          <template #default="{ row }">
            <span class="quantity-cell" :class="{ 'low-stock': row.quantity <= row.threshold }">{{ row.quantity }}</span>
            <span v-if="row.quantity <= row.threshold" class="badge badge-red" style="margin-left: 6px;">低</span>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column prop="threshold" label="预警值" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <span :class="row.status === 1 ? 'badge badge-green' : 'badge badge-gray'">{{ row.status === 1 ? '正常' : '停用' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最后补货" width="170">
          <template #default="{ row }"><span class="time-cell">{{ row.lastRestockTime ? formatDate(row.lastRestockTime) : '-' }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button class="action-btn" size="small" @click="showRestock(row)" v-permission="'business:inventory:restock'">补货</el-button>
            <el-button class="action-btn" size="small" @click="showDialog(row)" v-permission="'business:inventory:edit'">编辑</el-button>
            <el-popconfirm title="确定删除?" @confirm="handleDelete(row.id)">
              <template #reference><el-button class="action-btn danger" size="small" v-permission="'business:inventory:delete'">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @current-change="loadData" @size-change="handleSizeChange" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editItem ? '编辑库存' : '新建库存'" width="450px" destroy-on-close>
      <el-form :model="form" label-width="80px" size="large">
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="请输入食材名称" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存量"><el-input-number v-model="form.quantity" :min="0" :max="99999" /></el-form-item>
        <el-form-item label="单位"><el-input v-model="form.unit" placeholder="请输入单位" /></el-form-item>
        <el-form-item label="预警值"><el-input-number v-model="form.threshold" :min="0" :max="99999" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="restockVisible" title="补货" width="400px" destroy-on-close>
      <el-form :model="restockForm" label-width="80px" size="large">
        <el-form-item label="食材"><span>{{ restockItem?.name }}</span></el-form-item>
        <el-form-item label="当前库存"><span>{{ restockItem?.quantity }} {{ restockItem?.unit }}</span></el-form-item>
        <el-form-item label="补货量"><el-input-number v-model="restockForm.quantity" :min="1" :max="99999" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="restockVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleRestock">确认补货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getInventory, createInventory, updateInventory, deleteInventory, restock } from '@/api/inventory'
import { getCategories } from '@/api/category'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'

const items = ref<any[]>([])
const categories = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const restockVisible = ref(false)
const editItem = ref<any>(null)
const restockItem = ref<any>(null)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const filters = reactive({ keyword: '', categoryId: null as number | null })
const form = reactive({ name: '', categoryId: null as number | null, quantity: 0, unit: '', threshold: 10, status: 1 })
const restockForm = reactive({ quantity: 1 })

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getInventory({ page: page.value, size: pageSize.value, ...filters })
    items.value = res.data.records
    total.value = res.data.total
  } catch { ElMessage.error('加载库存失败') } finally { loading.value = false }
}
const handleSizeChange = () => { page.value = 1; loadData() }
const handleDelete = async (id: number) => {
  try { await deleteInventory(id); ElMessage.success('删除成功'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '删除失败') }
}

const showDialog = (item?: any) => {
  editItem.value = item || null
  if (item) { Object.assign(form, { name: item.name, categoryId: item.categoryId, quantity: item.quantity, unit: item.unit, threshold: item.threshold, status: item.status }) }
  else { Object.assign(form, { name: '', categoryId: null, quantity: 0, unit: '', threshold: 10, status: 1 }) }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.name.trim()) { ElMessage.warning('请输入食材名称'); return }
  if (!form.unit.trim()) { ElMessage.warning('请输入单位'); return }
  saving.value = true
  try {
    if (editItem.value) { await updateInventory(editItem.value.id, form) }
    else { await createInventory(form) }
    ElMessage.success(editItem.value ? '更新成功' : '创建成功')
    dialogVisible.value = false; loadData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
  finally { saving.value = false }
}

const showRestock = (item: any) => {
  restockItem.value = item
  restockForm.quantity = 1
  restockVisible.value = true
}

const handleRestock = async () => {
  if (!restockItem.value) return
  saving.value = true
  try {
    await restock(restockItem.value.id, { quantity: restockForm.quantity })
    ElMessage.success('补货成功')
    restockVisible.value = false; loadData()
  } catch (e: any) { ElMessage.error(e.message || '补货失败') }
  finally { saving.value = false }
}

onMounted(async () => {
  const res: any = await getCategories({}).catch(() => ({ data: [] }))
  categories.value = res.data || []
  loadData()
})
</script>

<style scoped lang="scss">
.quantity-cell {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
  font-family: 'SF Mono', 'Menlo', monospace;
  &.low-stock { color: var(--apple-red); }
}
</style>
