<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon blue"><el-icon :size="22"><Clock /></el-icon></div>
        <div class="page-header-text">
          <h2>AI 使用历史</h2>
          <p>查看所有 AI 功能的使用记录和结果</p>
        </div>
      </div>
      <div class="page-header-right">
        <el-select v-model="filterFeature" placeholder="全部功能" clearable style="width: 160px" @change="loadHistory">
          <el-option v-for="(label, key) in featureLabels" :key="key" :label="label" :value="key" />
        </el-select>
      </div>
    </div>
    <div class="stats-bar" v-if="stats && Object.keys(stats).length > 1">
      <div class="stat-item" v-for="(count, key) in statEntries" :key="key">
        <span class="stat-count">{{ count }}</span>
        <span class="stat-name">{{ featureLabels[key] || key }}</span>
      </div>
    </div>
    <div class="history-list" v-loading="loading">
      <el-empty v-if="!logs.length && !loading" description="暂无 AI 使用记录" />
      <div v-for="log in logs" :key="log.id" class="history-item">
        <div class="item-header">
          <div class="item-meta">
            <el-tag size="small">{{ featureLabels[log.feature] || log.feature }}</el-tag>
            <span class="item-time">{{ formatTime(log.createTime) }}</span>
            <span class="item-time" v-if="log.executionTimeMs">{{ log.executionTimeMs }}ms</span>
          </div>
          <el-button type="danger" text size="small" @click="deleteLog(log.id!)"><el-icon><Delete /></el-icon></el-button>
        </div>
        <div class="item-title">{{ log.title }}</div>
        <div class="item-input" v-if="log.inputText">
          <strong>输入：</strong>{{ log.inputText.substring(0, 200) }}{{ log.inputText.length > 200 ? '...' : '' }}
        </div>
        <div class="item-result markdown-body" v-if="log.resultText" v-html="renderMarkdown(log.resultText.substring(0, 500))"></div>
        <div class="item-actions" v-if="log.resultText">
          <el-button size="small" text @click="copyResult(log.resultText!)"><el-icon><DocumentCopy /></el-icon> 复制结果</el-button>
          <el-button size="small" text @click="reuseLog(log)"><el-icon><RefreshRight /></el-icon> 重新使用</el-button>
        </div>
      </div>
    </div>
    <div class="pagination" v-if="total > pageSize">
      <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="total" layout="prev, pager, next" @current-change="loadHistory" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { aiGetHistory, aiGetStats, aiDeleteHistory } from '@/api/ai'
import { ElMessage, ElMessageBox } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const logs = ref<any[]>([])
const stats = ref<any>(null)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 20
const filterFeature = ref('')
const statEntries = computed(() => {
  if (!stats.value) return []
  return Object.entries(stats.value).filter(([k]) => k !== 'total')
})

const featureLabels: Record<string, string> = {
  semantic_search: '语义搜索', smart_order: '智能点餐', recipe_assist: '菜谱创作',
  inventory_advisor: '库存顾问', inventory_predict: '库存预测', scene_menu: '场景菜单',
  data_insight: '数据洞察', copywriting: '文案助手', smart_schedule: '智能排班',
  user_profile: '用户画像', trend_predict: '趋势预测', menu_analysis: '整桌菜分析',
  order_analysis: '订单分析', chat: 'AI 对话', recommend: '智能推荐',
  nutrition: '营养分析', meal_plan: '周菜谱', leftover: '剩菜妙招',
  score: 'AI 评分', cooking_qa: '烹饪问答', health_report: '健康报告',
  translate: '菜谱翻译', recognize: '菜品识别', shopping_list: '购物清单',
  ingredient_to_recipe: '食材变菜谱'
}

const featureRoutes: Record<string, string> = {
  semantic_search: '/ai/semantic-search', smart_order: '/ai/smart-order',
  recipe_assist: '/ai/recipe-assist', inventory_advisor: '/ai/inventory-advisor',
  scene_menu: '/ai/scene-menu', copywriting: '/ai/copywriting',
  data_insight: '/ai/data-insight', smart_schedule: '/ai/smart-schedule',
  user_profile: '/ai/user-profile', trend_predict: '/ai/trend-predict',
  menu_analysis: '/ai/menu-analysis', order_analysis: '/ai/order-analysis',
  inventory_predict: '/ai/inventory-predict', recommend: '/ai/recommend',
  nutrition: '/ai/nutrition', meal_plan: '/ai/meal-plan',
  leftover: '/ai/leftover', score: '/ai/score', cooking_qa: '/ai/cooking-qa',
  health_report: '/ai/health', translate: '/ai/translate', chat: '/ai'
}

const loadHistory = async () => {
  loading.value = true
  try {
    const res: any = await aiGetHistory({ page: currentPage.value - 1, size: pageSize, feature: filterFeature.value || undefined })
    logs.value = res.data?.logs || []
    total.value = res.data?.total || 0
  } catch (e: any) { ElMessage.error(e?.message || '加载失败') } finally { loading.value = false }
}

const loadStats = async () => {
  try { const res: any = await aiGetStats(); stats.value = res.data || {} } catch {}
}

const deleteLog = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除这条记录？', '删除确认', { type: 'warning' })
    await aiDeleteHistory(id)
    ElMessage.success('已删除')
    loadHistory()
  } catch {}
}

const copyResult = async (text: string) => {
  try { await navigator.clipboard.writeText(text); ElMessage.success('已复制') } catch { ElMessage.error('复制失败') }
}

const reuseLog = (log: any) => {
  const route = featureRoutes[log.feature]
  if (route) router.push(route)
}

const formatTime = (t: any) => {
  if (!t) return ''
  const d = new Date(t)
  return d.toLocaleDateString('zh-CN') + ' ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

onMounted(() => { loadHistory(); loadStats() })
</script>

<style scoped lang="scss">
.stats-bar { display: flex; gap: 16px; margin-bottom: 24px; flex-wrap: wrap; }
.stat-item { background: var(--apple-card-bg, #fff); border: 1px solid var(--apple-border, #e8e8e8); border-radius: 12px; padding: 12px 20px; text-align: center; min-width: 80px; }
.stat-count { display: block; font-size: 20px; font-weight: 700; color: var(--el-color-primary); }
.stat-name { font-size: 12px; color: var(--apple-text-secondary, #999); }
.history-list { display: flex; flex-direction: column; gap: 12px; }
.history-item { background: var(--apple-card-bg, #fff); border: 1px solid var(--apple-border, #e8e8e8); border-radius: 12px; padding: 20px; }
.item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.item-meta { display: flex; align-items: center; gap: 8px; }
.item-time { font-size: 12px; color: var(--apple-text-secondary, #999); }
.item-title { font-size: 15px; font-weight: 600; margin-bottom: 8px; }
.item-input { font-size: 13px; color: var(--apple-text-secondary, #666); margin-bottom: 8px; padding: 8px 12px; background: var(--apple-bg, #f5f5f5); border-radius: 8px; }
.item-result { font-size: 13px; max-height: 200px; overflow-y: auto; }
.item-actions { margin-top: 8px; display: flex; gap: 8px; }
.pagination { margin-top: 24px; display: flex; justify-content: center; }
</style>
