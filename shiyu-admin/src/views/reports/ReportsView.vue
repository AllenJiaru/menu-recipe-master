<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon indigo">
          <el-icon :size="22"><TrendCharts /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>报表分析</h2>
          <p>综合数据报表与趋势分析</p>
        </div>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-value">{{ stats.totalRecipes }}</div>
        <div class="stat-label">菜谱总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.totalOrders }}</div>
        <div class="stat-label">订单总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.totalUsers }}</div>
        <div class="stat-label">用户总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.avgRating }}</div>
        <div class="stat-label">平均评分</div>
      </div>
    </div>

    <div class="charts-grid">
      <div class="page-card chart-card">
        <div class="page-card-header"><h3>订单月度趋势</h3></div>
        <div ref="barChartRef" class="chart-container"></div>
      </div>
      <div class="page-card chart-card">
        <div class="page-card-header"><h3>菜谱分类分布</h3></div>
        <div ref="pieChartRef" class="chart-container"></div>
      </div>
      <div class="page-card chart-card full-width">
        <div class="page-card-header"><h3>用户增长趋势</h3></div>
        <div ref="lineChartRef" class="chart-container"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getDashboard } from '@/api/dashboard'
import { getOrderStats } from '@/api/statistics'

const stats = ref({
  totalRecipes: 0,
  totalOrders: 0,
  totalUsers: 0,
  avgRating: 0
})

const barChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
const lineChartRef = ref<HTMLElement>()

let barChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null
let lineChart: echarts.ECharts | null = null

const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
const ordersData = ref<number[]>([])
const categories = ref<string[]>([])
const categoryData = ref<number[]>([])
const userMonths = ref<string[]>([])
const userGrowthData = ref<number[]>([])

async function loadDashboardData() {
  try {
    const res: any = await getDashboard()
    const data = res.data || {}
    stats.value = {
      totalRecipes: data.totalRecipes || 0,
      totalOrders: data.totalOrders || 0,
      totalUsers: data.totalUsers || 0,
      avgRating: data.avgRating || 0
    }
    if (data.monthlyOrders) ordersData.value = data.monthlyOrders
    if (data.categoryDistribution) {
      categories.value = Object.keys(data.categoryDistribution)
      categoryData.value = Object.values(data.categoryDistribution)
    }
    if (data.userGrowth) {
      userMonths.value = Object.keys(data.userGrowth)
      userGrowthData.value = Object.values(data.userGrowth)
    }
  } catch {
    // 使用默认空数据
  }
}

async function loadOrderStats() {
  try {
    const res: any = await getOrderStats({})
    if (res.data?.monthlyOrders) ordersData.value = res.data.monthlyOrders
  } catch {
    // 使用默认空数据
  }
}

function initBarChart() {
  if (!barChartRef.value) return
  barChart = echarts.init(barChartRef.value)
  barChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.96)', borderColor: '#E5E5EA', borderWidth: 1, textStyle: { color: '#1D1D1F' } },
    grid: { left: 60, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: months, axisLine: { lineStyle: { color: '#E5E5EA' } }, axisLabel: { color: '#86868B', fontSize: 12 }, axisTick: { show: false } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#F2F2F7', type: 'dashed' } }, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#86868B' } },
    series: [{ data: ordersData.value.length ? ordersData.value : [0,0,0,0,0,0,0,0,0,0,0,0], type: 'bar', barWidth: 20, itemStyle: { borderRadius: [6, 6, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#7B78F2' }, { offset: 1, color: '#5856D6' }]) } }]
  })
}

function initPieChart() {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', backgroundColor: 'rgba(255,255,255,0.96)', borderColor: '#E5E5EA', borderWidth: 1 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '50%'],
      data: categories.value.length
        ? categories.value.map((name, i) => ({ name, value: categoryData.value[i] }))
        : [{ name: '暂无数据', value: 1 }],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 3, color: '#5856D6' },
      label: { fontSize: 12, color: '#6E6E73' }
    }]
  })
}

function initLineChart() {
  if (!lineChartRef.value) return
  lineChart = echarts.init(lineChartRef.value)
  lineChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.96)', borderColor: '#E5E5EA', borderWidth: 1, textStyle: { color: '#1D1D1F' } },
    grid: { left: 60, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: userMonths.value.length ? userMonths.value : ['暂无数据'], axisLine: { lineStyle: { color: '#E5E5EA' } }, axisLabel: { color: '#86868B', fontSize: 12 }, axisTick: { show: false } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#F2F2F7', type: 'dashed' } }, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#86868B' } },
    series: [{ data: userGrowthData.value.length ? userGrowthData.value : [0], type: 'line', smooth: true, lineStyle: { color: '#34C759', width: 2.5 }, itemStyle: { color: '#34C759' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(52,199,89,0.15)' }, { offset: 1, color: 'rgba(52,199,89,0.01)' }]) } }]
  })
}

function handleResize() {
  barChart?.resize()
  pieChart?.resize()
  lineChart?.resize()
}

onMounted(async () => {
  await loadDashboardData()
  await loadOrderStats()
  initBarChart()
  initPieChart()
  initLineChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped lang="scss">
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  text-align: center;
  padding: 24px 20px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-xs);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--apple-blue);
  font-family: 'SF Pro Display', -apple-system, BlinkMacSystemFont, sans-serif;
}

.stat-label {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-top: 6px;
  font-weight: 500;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-container {
  height: 360px;
  width: 100%;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
