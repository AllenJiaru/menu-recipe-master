<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon teal">
          <el-icon :size="22"><Calendar /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>每周餐食计划</h2>
          <p>安排每周的早、中、晚餐及加餐，合理规划饮食</p>
        </div>
      </div>
      <div class="page-header-right">
        <el-button @click="goPrevWeek"><el-icon><ArrowLeft /></el-icon> 上一周</el-button>
        <span class="week-label">{{ weekLabel }}</span>
        <el-button @click="goNextWeek">下一周 <el-icon><ArrowRight /></el-icon></el-button>
        <el-button type="primary" @click="goToday">今天</el-button>
      </div>
    </div>

    <div class="plan-layout">
      <div class="page-card plan-grid-wrap" v-loading="loading">
        <div class="plan-grid">
          <div class="grid-header corner"></div>
          <div v-for="day in weekDays" :key="day.date" class="grid-header" :class="{ today: day.isToday }">
            <span class="day-name">{{ day.name }}</span>
            <span class="day-date">{{ day.dateStr }}</span>
          </div>

          <template v-for="slot in mealSlots" :key="slot.key">
            <div class="grid-row-label">
              <span class="slot-icon">{{ slot.icon }}</span>
              <span>{{ slot.label }}</span>
            </div>
            <div v-for="day in weekDays" :key="`${day.date}-${slot.key}`" class="grid-cell" :class="{ today: day.isToday }" @click="openRecipePicker(day.date, slot.key)">
              <template v-if="getPlan(day.date, slot.key)">
                <el-image v-if="getPlan(day.date, slot.key).recipeCover" :src="imgUrl(getPlan(day.date, slot.key).recipeCover)" fit="cover" class="cell-cover" />
                <span class="cell-recipe-name">{{ getPlan(day.date, slot.key).recipeName }}</span>
                <el-button class="cell-remove" type="danger" :icon="Delete" circle size="small" @click.stop="handleRemovePlan(getPlan(day.date, slot.key).id)" />
              </template>
              <div v-else class="cell-empty">
                <el-icon :size="18"><Plus /></el-icon>
              </div>
            </div>
          </template>
        </div>
      </div>

      <div class="stats-sidebar">
        <div class="page-card stats-card">
          <div class="page-card-header">
            <h3>本周统计</h3>
          </div>
          <div class="stat-item">
            <span class="stat-label">已安排餐次</span>
            <span class="stat-value">{{ planStats.totalSlots || 0 }} / 28</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">使用菜谱数</span>
            <span class="stat-value">{{ planStats.uniqueRecipes || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">总热量(预估)</span>
            <span class="stat-value">{{ planStats.totalCalories || 0 }} kcal</span>
          </div>
          <el-divider />
          <div class="page-card-header">
            <h3>快捷操作</h3>
          </div>
          <el-button type="primary" style="width:100%" @click="openRecipePicker()">从菜谱库添加</el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="pickerVisible" title="选择菜谱" width="600px" destroy-on-close>
      <el-input v-model="searchKey" placeholder="搜索菜谱..." clearable style="margin-bottom:16px" @input="searchRecipes">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <div class="recipe-picker-grid" v-loading="searchLoading">
        <div v-for="r in recipeResults" :key="r.id" class="picker-item" @click="handlePickRecipe(r)">
          <el-image v-if="r.coverImage" :src="imgUrl(r.coverImage)" fit="cover" class="picker-cover" />
          <div v-else class="picker-cover placeholder"><el-icon><Picture /></el-icon></div>
          <span class="picker-name">{{ r.name }}</span>
        </div>
        <el-empty v-if="!recipeResults.length && !searchLoading" description="输入关键词搜索菜谱" :image-size="60" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getWeeklyPlan, savePlan, deletePlan } from '@/api/mealPlan'
import { getRecipes } from '@/api/recipe'
import { imgUrl } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const plans = ref<any[]>([])
const currentDate = ref(new Date())

const mealSlots = [
  { key: 'breakfast', label: '早餐', icon: '🌅' },
  { key: 'lunch', label: '午餐', icon: '☀️' },
  { key: 'dinner', label: '晚餐', icon: '🌙' },
  { key: 'snack', label: '加餐', icon: '🍪' },
]

const getMonday = (d: Date) => {
  const date = new Date(d)
  const day = date.getDay()
  const diff = date.getDate() - day + (day === 0 ? -6 : 1)
  date.setDate(diff)
  return date
}

const formatDate = (d: Date) => d.toISOString().split('T')[0]

const weekDays = computed(() => {
  const monday = getMonday(currentDate.value)
  const days = []
  const todayStr = formatDate(new Date())
  for (let i = 0; i < 7; i++) {
    const d = new Date(monday)
    d.setDate(monday.getDate() + i)
    const dateStr = formatDate(d)
    days.push({
      date: dateStr,
      name: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'][i],
      dateStr: `${d.getMonth() + 1}/${d.getDate()}`,
      isToday: dateStr === todayStr,
    })
  }
  return days
})

const weekLabel = computed(() => {
  const days = weekDays.value
  return `${days[0].dateStr} - ${days[6].dateStr}`
})

const planStats = computed(() => {
  const total = plans.value.length
  const unique = new Set(plans.value.map(p => p.recipeId)).size
  return { totalSlots: total, uniqueRecipes: unique, totalCalories: total * 450 }
})

const getPlan = (date: string, slot: string) => plans.value.find(p => p.planDate === date && p.mealType === slot)

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getWeeklyPlan(formatDate(getMonday(currentDate.value)))
    plans.value = res.data || []
  } catch { ElMessage.error('加载餐食计划失败') } finally { loading.value = false }
}

const goPrevWeek = () => { const d = new Date(currentDate.value); d.setDate(d.getDate() - 7); currentDate.value = d; loadData() }
const goNextWeek = () => { const d = new Date(currentDate.value); d.setDate(d.getDate() + 7); currentDate.value = d; loadData() }
const goToday = () => { currentDate.value = new Date(); loadData() }

const handleRemovePlan = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定移除该餐食安排?', '提示', { type: 'warning' })
    await deletePlan(id)
    ElMessage.success('已移除')
    loadData()
  } catch {}
}

const pickerVisible = ref(false)
const searchKey = ref('')
const searchLoading = ref(false)
const recipeResults = ref<any[]>([])
let pickerTarget = { date: '', slot: '' }

const openRecipePicker = (date?: string, slot?: string) => {
  pickerTarget = { date: date || '', slot: slot || '' }
  searchKey.value = ''
  recipeResults.value = []
  pickerVisible.value = true
}

const searchRecipes = async () => {
  if (!searchKey.value) { recipeResults.value = []; return }
  searchLoading.value = true
  try {
    const res: any = await getRecipes({ keyword: searchKey.value, page: 1, size: 20 })
    recipeResults.value = res.data.records || []
  } catch {} finally { searchLoading.value = false }
}

const handlePickRecipe = async (recipe: any) => {
  if (!pickerTarget.date || !pickerTarget.slot) { pickerVisible.value = false; return }
  try {
    await savePlan({ planDate: pickerTarget.date, mealType: pickerTarget.slot, recipeId: recipe.id, recipeName: recipe.name, recipeCover: recipe.coverImage })
    ElMessage.success('已添加')
    pickerVisible.value = false
    loadData()
  } catch (e: any) { ElMessage.error(e.message || '添加失败') }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.page-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.week-label {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary);
  min-width: 120px;
  text-align: center;
}

.plan-layout {
  display: grid;
  grid-template-columns: 1fr 260px;
  gap: 20px;
}

.plan-grid-wrap { padding: 16px; overflow-x: auto; }

.plan-grid {
  display: grid;
  grid-template-columns: 80px repeat(7, 1fr);
  gap: 2px;
  min-width: 700px;
}

.grid-header {
  padding: 10px 8px;
  text-align: center;
  border-radius: var(--radius-sm);
  background: var(--bg-page);
  &.today { background: var(--apple-blue); color: #fff; .day-date { color: rgba(255,255,255,0.8); } }
}

.day-name { display: block; font-weight: 600; font-size: 13px; }
.day-date { font-size: 11px; color: var(--text-secondary); }

.grid-row-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  background: var(--bg-page);
  border-radius: var(--radius-sm);
}

.slot-icon { font-size: 18px; }

.grid-cell {
  min-height: 80px;
  padding: 6px;
  border-radius: var(--radius-sm);
  border: 1px dashed var(--border-color);
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  &:hover { border-color: var(--apple-blue); background: rgba(0, 122, 255, 0.02); }
  &.today { border-color: rgba(0, 122, 255, 0.3); }
}

.cell-cover {
  width: 100%;
  height: 44px;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.cell-recipe-name {
  font-size: 11px;
  text-align: center;
  color: var(--text-primary);
  font-weight: 500;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cell-remove {
  position: absolute;
  top: 2px;
  right: 2px;
  opacity: 0;
  transition: opacity 0.2s ease;
  :deep(.el-icon) { font-size: 10px; }
}
.grid-cell:hover .cell-remove { opacity: 1; }

.cell-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: var(--border-color);
}

.stats-sidebar { display: flex; flex-direction: column; gap: 16px; }

.stats-card {
  padding: 24px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-light);
  &:last-child { border-bottom: none; }
}

.stat-label { font-size: 13px; color: var(--text-secondary); }
.stat-value { font-size: 15px; font-weight: 600; color: var(--apple-blue); }

.recipe-picker-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.picker-item {
  cursor: pointer;
  border-radius: var(--radius-sm);
  overflow: hidden;
  border: 2px solid transparent;
  transition: all 0.2s ease;
  &:hover { border-color: var(--apple-blue); transform: translateY(-2px); }
}

.picker-cover {
  width: 100%;
  height: 100px;
  object-fit: cover;
  &.placeholder {
    background: var(--bg-page);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--border-color);
  }
}

.picker-name {
  display: block;
  padding: 6px 8px;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  color: var(--text-primary);
}

@media (max-width: 900px) {
  .plan-layout { grid-template-columns: 1fr; }
  .stats-sidebar { order: -1; }
}
</style>