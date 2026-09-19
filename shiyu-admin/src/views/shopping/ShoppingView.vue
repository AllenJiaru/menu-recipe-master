<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon green"><el-icon :size="22"><ShoppingCart /></el-icon></div>
        <div class="page-header-text"><h2>购物清单</h2><p>管理食材采购清单，支持手动添加或从菜谱导入</p></div>
      </div>
      <div>
        <el-button @click="handleAddFromRecipe"><el-icon><DocumentAdd /></el-icon> 从菜谱添加</el-button>
        <el-button type="primary" @click="dialogVisible = true"><el-icon><Plus /></el-icon> 添加食材</el-button>
      </div>
    </div>

    <div class="page-card stats-bar">
      <div class="stat-item">
        <span class="stat-label">总项数</span>
        <span class="stat-value">{{ stats.totalItems || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">已完成</span>
        <span class="stat-value green">{{ stats.checkedItems || 0 }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">未完成</span>
        <span class="stat-value orange">{{ (stats.totalItems || 0) - (stats.checkedItems || 0) }}</span>
      </div>
      <div class="progress-section">
        <el-progress :percentage="progressPercent" :stroke-width="10" :color="'#34C759'" />
      </div>
      <el-button v-if="stats.checkedItems > 0" type="danger" plain @click="handleClearChecked">
        <el-icon><Delete /></el-icon> 清除已完成
      </el-button>
    </div>

    <div class="page-card list-card" v-loading="loading">
      <template v-for="(items, category) in groupedItems" :key="category">
        <div class="category-header">
          <el-tag effect="dark" size="small">{{ category }}</el-tag>
          <span class="category-count">{{ items.length }} 项</span>
        </div>
        <div v-for="item in items" :key="item.id" class="shopping-item" :class="{ checked: item.checked }">
          <el-checkbox v-model="item.checked" @change="handleToggle(item.id)" />
          <div class="item-info">
            <span class="item-name name-cell">{{ item.name }}</span>
            <span class="item-amount">{{ item.amount }} {{ item.unit }}</span>
          </div>
          <el-popconfirm title="确定删除?" @confirm="handleDeleteItem(item.id)">
            <template #reference>
              <el-button class="action-btn danger" :icon="Delete" circle />
            </template>
          </el-popconfirm>
        </div>
      </template>
      <el-empty v-if="!Object.keys(groupedItems).length && !loading" description="购物清单为空，添加一些食材吧" />
    </div>

    <el-dialog v-model="dialogVisible" title="添加食材" width="420px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="60px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" placeholder="食材名称" /></el-form-item>
        <el-form-item label="数量" prop="amount"><el-input-number v-model="form.amount" :min="0.1" :step="0.5" style="width:100%" /></el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-select v-model="form.unit" style="width:100%">
            <el-option v-for="u in units" :key="u" :label="u" :value="u" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" allow-create filterable style="width:100%" placeholder="选择或输入分类">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAddItem">添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recipeDialogVisible" title="从菜谱添加食材" width="500px" destroy-on-close>
      <div class="page-search" style="margin-bottom: 16px">
        <el-icon class="search-icon"><Search /></el-icon>
        <input v-model="recipeSearchKey" placeholder="搜索菜谱..." @input="searchRecipes" />
      </div>
      <div class="recipe-search-list" v-loading="recipeSearchLoading">
        <div v-for="r in recipeResults" :key="r.id" class="recipe-search-item" @click="handleAddFromRecipeConfirm(r.id)">
          <span class="name-cell">{{ r.name }}</span>
          <el-button type="primary" size="small" link>添加食材</el-button>
        </div>
        <el-empty v-if="!recipeResults.length && !recipeSearchLoading" description="输入关键词搜索" :image-size="60" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getShoppingList, addItem, addFromRecipe, toggleItem, deleteItem, clearChecked, getShoppingStats } from '@/api/shopping'
import { getRecipes } from '@/api/recipe'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const loading = ref(false)
const items = ref<any[]>([])
const stats = ref<any>({})
const units = ['个', '克', '千克', '毫升', '升', '包', '袋', '瓶', '盒', '斤', '两']
const categories = ['蔬菜', '肉类', '海鲜', '调料', '主食', '水果', '其他']

const groupedItems = computed(() => {
  const groups: Record<string, any[]> = {}
  items.value.forEach(item => {
    const cat = item.category || '其他'
    if (!groups[cat]) groups[cat] = []
    groups[cat].push(item)
  })
  return groups
})

const progressPercent = computed(() => {
  const total = stats.value.totalItems || 0
  if (!total) return 0
  return Math.round(((stats.value.checkedItems || 0) / total) * 100)
})

const loadData = async () => {
  loading.value = true
  try {
    const [listRes, statsRes]: any[] = await Promise.all([getShoppingList('default'), getShoppingStats()])
    items.value = listRes.data || []
    stats.value = statsRes.data || {}
  } catch { ElMessage.error('加载购物清单失败') } finally { loading.value = false }
}

const handleToggle = async (id: number) => {
  try { await toggleItem(id); loadData() }
  catch { ElMessage.error('操作失败') }
}

const handleDeleteItem = async (id: number) => {
  try { await deleteItem(id); ElMessage.success('已删除'); loadData() }
  catch { ElMessage.error('删除失败') }
}

const handleClearChecked = async () => {
  try {
    await ElMessageBox.confirm('确定清除所有已完成的项目?', '提示', { type: 'warning' })
    await clearChecked()
    ElMessage.success('已清除')
    loadData()
  } catch {}
}

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const submitting = ref(false)
const form = reactive({ name: '', amount: 1, unit: '个', category: '其他' })
const rules: FormRules = {
  name: [{ required: true, message: '请输入食材名称', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  unit: [{ required: true, message: '请选择单位', trigger: 'change' }],
}

const handleAddItem = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    await addItem({ ...form, listName: 'default', checked: false })
    ElMessage.success('已添加')
    dialogVisible.value = false
    Object.assign(form, { name: '', amount: 1, unit: '个', category: '其他' })
    loadData()
  } catch (e: any) { ElMessage.error(e.message || '添加失败') } finally { submitting.value = false }
}

const recipeDialogVisible = ref(false)
const recipeSearchKey = ref('')
const recipeSearchLoading = ref(false)
const recipeResults = ref<any[]>([])

const handleAddFromRecipe = () => {
  recipeSearchKey.value = ''
  recipeResults.value = []
  recipeDialogVisible.value = true
}

const searchRecipes = async () => {
  if (!recipeSearchKey.value) { recipeResults.value = []; return }
  recipeSearchLoading.value = true
  try {
    const res: any = await getRecipes({ keyword: recipeSearchKey.value, page: 1, size: 10 })
    recipeResults.value = res.data.records || []
  } catch {} finally { recipeSearchLoading.value = false }
}

const handleAddFromRecipeConfirm = async (recipeId: number) => {
  try {
    await addFromRecipe(recipeId)
    ElMessage.success('已从菜谱添加食材')
    recipeDialogVisible.value = false
    loadData()
  } catch (e: any) { ElMessage.error(e.message || '添加失败') }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.stats-bar {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 16px 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label { font-size: 12px; color: var(--text-secondary); }
.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  &.green { color: var(--apple-green); }
  &.orange { color: var(--apple-orange); }
}

.progress-section { flex: 1; }

.list-card { padding: 20px 24px; }

.category-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

.category-count { font-size: 12px; color: var(--text-secondary); }

.shopping-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius-xs);
  transition: background 0.2s ease;
  &:hover { background: var(--bg-page); }
  &.checked {
    .item-name { text-decoration: line-through; color: var(--text-secondary); }
  }
}

.item-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-amount { font-size: 13px; color: var(--text-secondary); }

.recipe-search-list {
  max-height: 300px;
  overflow-y: auto;
}

.recipe-search-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: var(--radius-xs);
  cursor: pointer;
  transition: background 0.2s ease;
  &:hover { background: var(--bg-page); }
}
</style>
