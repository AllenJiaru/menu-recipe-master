<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon blue">
          <el-icon :size="22"><DataAnalysis /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>数据统计</h2>
          <p>分析订单趋势与菜品偏好</p>
        </div>
      </div>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" @change="loadData" />
    </div>

    <div class="stats-grid">
      <div class="page-card chart-card" v-loading="loading">
        <div class="page-card-header">
          <h3>订单趋势</h3>
        </div>
        <div ref="chartRef" class="chart-area" />
      </div>

      <div class="page-card side-card">
        <div class="page-card-header">
          <h3>菜品分类</h3>
        </div>
        <div ref="pieRef" class="chart-area" />
      </div>
    </div>

    <div class="page-card" v-loading="loading">
      <div class="page-card-header">
        <h3>热门菜谱 TOP5</h3>
      </div>
      <el-table :data="topRecipes" stripe style="width: 100%">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="name" label="菜名">
          <template #default="{ row }"><span class="name-cell">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="orderCount" label="被点次数" width="120">
          <template #default="{ row }">
            <span class="count-cell">{{ row.orderCount }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import { getOrderStats } from '@/api/statistics'
import { recipeTypeMap } from '@/utils/format'
import { ElMessage } from 'element-plus'

const chartRef = ref<HTMLElement>()
const pieRef = ref<HTMLElement>()
const dateRange = ref<[string, string] | null>(null)
const topRecipes = ref<any[]>([])
const loading = ref(false)

let chart: ECharts | null = null
let pie: ECharts | null = null
let resizeHandler: (() => void) | null = null

const disposeCharts = () => { if (chart) { chart.dispose(); chart = null }; if (pie) { pie.dispose(); pie = null } }

const loadData = async () => {
  loading.value = true
  const params: any = {}
  if (dateRange.value) { params.startDate = dateRange.value[0]; params.endDate = dateRange.value[1] }
  try {
    const res: any = await getOrderStats(params); const data = res.data; topRecipes.value = data.topRecipes || []
    await nextTick()
    if (chartRef.value) {
      if (chart) chart.dispose(); chart = echarts.init(chartRef.value)
      chart.setOption({
        tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.96)', borderColor: '#E5E5EA', borderWidth: 1, textStyle: { color: '#1D1D1F' } },
        grid: { left: '3%', right: '4%', bottom: '3%', top: 40, containLabel: true },
        xAxis: { type: 'category', data: data.labels || [], axisLine: { lineStyle: { color: '#E5E5EA' } }, axisLabel: { color: '#86868B', fontSize: 12 }, axisTick: { show: false } },
        yAxis: { type: 'value', splitLine: { lineStyle: { color: '#F2F2F7', type: 'dashed' } }, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#86868B' } },
        series: [{ name: '订单数', type: 'bar', data: data.orderData || [], barWidth: 20, itemStyle: { borderRadius: [6, 6, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#5AC8FA' }, { offset: 1, color: '#007AFF' }]) } }]
      })
    }
    if (pieRef.value && data.categoryStats) {
      if (pie) pie.dispose(); pie = echarts.init(pieRef.value)
      const pieData = Object.entries(data.categoryStats).map(([k, v]) => ({ name: recipeTypeMap[Number(k)] || k, value: v }))
      pie.setOption({
        tooltip: { trigger: 'item', backgroundColor: 'rgba(255,255,255,0.96)', borderColor: '#E5E5EA', borderWidth: 1 },
        series: [{ type: 'pie', radius: ['40%', '70%'], center: ['50%', '50%'], data: pieData, itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 3 }, label: { fontSize: 12, color: '#6E6E73' } }]
      })
    }
  } catch { ElMessage.error('加载统计数据失败') } finally { loading.value = false }
}

onMounted(() => { loadData(); resizeHandler = () => { chart?.resize(); pie?.resize() }; window.addEventListener('resize', resizeHandler) })
onBeforeUnmount(() => { if (resizeHandler) { window.removeEventListener('resize', resizeHandler); resizeHandler = null }; disposeCharts() })
</script>

<style scoped lang="scss">
.stats-grid {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: 20px;
}

.chart-card { min-height: 420px; }
.side-card { min-height: 320px; }

.chart-area { height: 320px; }

.name-cell {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
}

.count-cell {
  font-weight: 600;
  color: var(--apple-blue);
  font-size: 14px;
  font-family: 'SF Mono', 'Menlo', monospace;
}

@media (max-width: 900px) { .stats-grid { grid-template-columns: 1fr; } }
</style>
