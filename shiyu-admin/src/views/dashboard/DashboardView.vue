<template>
  <div class="dashboard">
    <div class="page-header">
      <div>
        <h2>仪表盘</h2>
        <p>数据概览与系统状态</p>
      </div>
    </div>

    <div class="welcome-banner tilt-3d" data-tilt="4">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>{{ greeting }}，<span class="text-gradient-flow">{{ userInfo?.nickname || 'Admin' }}</span></h1>
          <p>欢迎回到食遇管理后台</p>
        </div>
      </div>
    </div>

    <div class="stats-row stagger-in" v-loading="loading">
      <div class="stat-card tilt-3d" data-tilt="8">
        <div class="stat-icon blue"><el-icon :size="20"><Notebook /></el-icon></div>
        <div class="stat-info"><span class="stat-value">{{ animated.totalRecipes }}</span><span class="stat-label">菜谱总数</span></div>
      </div>
      <div class="stat-card tilt-3d" data-tilt="8">
        <div class="stat-icon green"><el-icon :size="20"><ShoppingCart /></el-icon></div>
        <div class="stat-info"><span class="stat-value">{{ animated.totalOrders }}</span><span class="stat-label">订单总数</span></div>
      </div>
      <div class="stat-card tilt-3d" data-tilt="8">
        <div class="stat-icon orange"><el-icon :size="20"><Document /></el-icon></div>
        <div class="stat-info"><span class="stat-value">{{ animated.todayOrders }}</span><span class="stat-label">今日订单</span></div>
      </div>
      <div class="stat-card tilt-3d" data-tilt="8">
        <div class="stat-icon red"><el-icon :size="20"><Bell /></el-icon></div>
        <div class="stat-info"><span class="stat-value">{{ animated.pendingOrders }}</span><span class="stat-label">待处理</span></div>
      </div>
    </div>

    <div class="charts-row stagger-in" v-loading="loading">
      <div class="chart-card chart-wide spectrum-card">
        <div class="card-title"><span>订单 & 菜谱趋势</span><span class="time-badge">近30天</span></div>
        <div v-if="stats.trendLabels?.length" ref="trendChartRef" class="chart-area" />
        <el-empty v-else description="暂无趋势数据" />
      </div>
      <div class="chart-card chart-narrow spectrum-card">
        <div class="card-title"><span>热门菜谱排行</span></div>
        <div v-if="stats.popularRecipes?.length" ref="barChartRef" class="chart-area" />
        <el-empty v-else description="暂无排行数据" />
      </div>
    </div>

    <div class="bottom-row stagger-in" v-loading="loading">
      <div class="chart-card bottom-card spectrum-card">
        <div class="card-title"><span>最近订单</span><el-button text type="primary" size="small" @click="$router.push('/orders')">查看全部</el-button></div>
        <div class="order-list">
          <div v-for="order in stats.recentOrders" :key="order.id" class="order-item">
            <div class="order-avatar">{{ order.recipeName?.charAt(0) || '?' }}</div>
            <div class="order-info">
              <div class="order-name">{{ order.recipeName || '未知菜谱' }}</div>
              <div class="order-time">{{ formatDateShort(order.orderTime) }}</div>
            </div>
            <el-tag :type="orderStatusType[order.status]" size="small">{{ orderStatusMap[order.status] }}</el-tag>
          </div>
          <el-empty v-if="!stats.recentOrders?.length" description="暂无订单" :image-size="60" />
        </div>
      </div>
      <div class="chart-card bottom-card spectrum-card">
        <div class="card-title"><span>热门菜谱 TOP5</span></div>
        <div class="recipe-list">
          <div v-for="(recipe, idx) in stats.popularRecipes?.slice(0, 5)" :key="recipe.id" class="recipe-item">
            <div class="recipe-rank" :class="['rank-' + (idx + 1)]">{{ idx + 1 }}</div>
            <div class="recipe-avatar">
              <el-image v-if="recipe.coverImage" :src="imgUrl(recipe.coverImage)" fit="cover" class="recipe-img" />
              <el-icon v-else :size="16" color="#c0c4cc"><Picture /></el-icon>
            </div>
            <div class="recipe-info">
              <div class="recipe-name">{{ recipe.name }}</div>
              <div class="recipe-count">被点 {{ recipe.orderCount || 0 }} 次</div>
            </div>
            <div class="recipe-bar"><div class="bar-fill" :style="{ width: getBarWidth(recipe.orderCount) + '%' }"></div></div>
          </div>
          <el-empty v-if="!stats.popularRecipes?.length" description="暂无菜谱数据" :image-size="60" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'
import { getDashboard } from '@/api/dashboard'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { orderStatusMap, orderStatusType, formatDateShort, imgUrl } from '@/utils/format'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const themeStore = useThemeStore()
const userInfo = computed(() => authStore.userInfo)
const loading = ref(false)
const trendChartRef = ref<HTMLElement>()
const barChartRef = ref<HTMLElement>()

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 17) return '下午好'
  if (h < 19) return '傍晚好'
  return '晚上好'
})

const stats = reactive<any>({
  totalRecipes: 0, totalOrders: 0, todayOrders: 0, pendingOrders: 0, completedOrders: 0,
  trendLabels: [], trendOrderData: [], trendRecipeData: [],
  recentOrders: [], popularRecipes: []
})

const animated = reactive({ totalRecipes: 0, totalOrders: 0, todayOrders: 0, pendingOrders: 0 })
const animFrames: Record<string, number> = {}

function animateValue(key: keyof typeof animated, target: number) {
  const from = animated[key]
  const to = Number(target) || 0
  if (from === to) { animated[key] = to; return }
  const dur = 900
  const start = performance.now()
  const step = (now: number) => {
    const t = Math.min((now - start) / dur, 1)
    const eased = 1 - Math.pow(1 - t, 3)
    animated[key] = Math.round(from + (to - from) * eased)
    if (t < 1) animFrames[key] = requestAnimationFrame(step)
  }
  if (animFrames[key]) cancelAnimationFrame(animFrames[key])
  animFrames[key] = requestAnimationFrame(step)
}

function syncAnimatedStats() {
  animateValue('totalRecipes', stats.totalRecipes)
  animateValue('totalOrders', stats.totalOrders)
  animateValue('todayOrders', stats.todayOrders)
  animateValue('pendingOrders', stats.pendingOrders)
}

/* 3D tilt */
let tiltCleanups: Array<() => void> = []
function initTilt() {
  tiltCleanups.forEach(fn => fn())
  tiltCleanups = []
  document.querySelectorAll<HTMLElement>('.tilt-3d[data-tilt]').forEach(el => {
    const max = Number(el.dataset.tilt) || 6
    const onMove = (e: MouseEvent) => {
      const r = el.getBoundingClientRect()
      const px = (e.clientX - r.left) / r.width - 0.5
      const py = (e.clientY - r.top) / r.height - 0.5
      el.style.transform = `perspective(800px) rotateY(${px * max}deg) rotateX(${-py * max}deg) translateZ(4px)`
      el.style.boxShadow = `${-px * 16}px ${8 + py * 8}px 32px rgba(0,122,255,0.16)`
    }
    const onLeave = () => {
      el.style.transform = ''
      el.style.boxShadow = ''
    }
    el.addEventListener('mousemove', onMove)
    el.addEventListener('mouseleave', onLeave)
    tiltCleanups.push(() => { el.removeEventListener('mousemove', onMove); el.removeEventListener('mouseleave', onLeave) })
  })
}

const trendChart = ref<echarts.ECharts>()
const barChart = ref<echarts.ECharts>()

const isDarkMode = () => document.documentElement.classList.contains('dark') || document.documentElement.getAttribute('data-theme') === 'dark'

function chartPalette() {
  return isDarkMode()
    ? { axis: '#6B7A73', split: 'rgba(255,255,255,0.06)', label: '#9BA8A2', tipBg: 'rgba(12,16,14,0.95)', tipBorder: 'rgba(0,229,153,0.2)', tipText: '#F2F6F4', primary: '#00E599', primaryA: 'rgba(0,229,153,0.28)', primaryB: 'rgba(0,229,153,0.02)', second: '#22D3EE', secondA: 'rgba(34,211,238,0.25)', secondB: 'rgba(34,211,238,0.02)', bar: '#00E599', barLabel: '#9BA8A2', yAxisLabel: '#F2F6F4' }
    : { axis: '#E5E5EA', split: '#F5F5F7', label: '#86868B', tipBg: 'rgba(255,255,255,0.96)', tipBorder: '#E5E5EA', tipText: '#1D1D1F', primary: '#007AFF', primaryA: 'rgba(0,122,255,0.15)', primaryB: 'rgba(0,122,255,0.01)', second: '#34C759', secondA: 'rgba(52,199,89,0.15)', secondB: 'rgba(52,199,89,0.01)', bar: '#007AFF', barLabel: '#6E6E73', yAxisLabel: '#1D1D1F' }
}

function getBarWidth(count: number) {
  const counts = stats.popularRecipes?.map((r: any) => r.orderCount) || []
  const max = counts.length ? Math.max(...counts) : 1
  return max > 0 ? (count / max) * 100 : 0
}

function initTrendChart() {
  if (!trendChartRef.value || !stats.trendLabels?.length) return
  const p = chartPalette()
  if (!trendChart.value) trendChart.value = echarts.init(trendChartRef.value)
  trendChart.value.setOption({
    tooltip: { trigger: 'axis', backgroundColor: p.tipBg, borderColor: p.tipBorder, borderWidth: 1, textStyle: { color: p.tipText, fontSize: 13 } },
    legend: { data: ['订单趋势', '新增菜谱'], top: 0, right: 0, textStyle: { color: p.label, fontSize: 12 }, itemWidth: 16, itemHeight: 3 },
    grid: { left: '2%', right: '3%', bottom: '2%', top: 40, containLabel: true },
    xAxis: { type: 'category', data: stats.trendLabels, boundaryGap: false, axisLine: { lineStyle: { color: p.axis } }, axisLabel: { color: p.label, fontSize: 12, hideOverlap: true }, axisTick: { show: false } },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: p.split, type: 'dashed' } }, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: p.label, fontSize: 12 } },
    series: [
      { name: '订单趋势', type: 'line', smooth: true, data: stats.trendOrderData, lineStyle: { width: 2, color: p.primary }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: p.primaryA }, { offset: 1, color: p.primaryB }]) }, itemStyle: { color: p.primary }, symbol: 'circle', symbolSize: 6 },
      { name: '新增菜谱', type: 'line', smooth: true, data: stats.trendRecipeData, lineStyle: { width: 2, color: p.second }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: p.secondA }, { offset: 1, color: p.secondB }]) }, itemStyle: { color: p.second }, symbol: 'circle', symbolSize: 6 }
    ]
  }, true)
}

function initBarChart() {
  if (!barChartRef.value || !stats.popularRecipes?.length) return
  const p = chartPalette()
  const recipes = stats.popularRecipes.slice(0, 6)
  if (!barChart.value) barChart.value = echarts.init(barChartRef.value)
  barChart.value.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: p.tipBg, borderColor: p.tipBorder, borderWidth: 1, textStyle: { color: p.tipText } },
    grid: { left: '2%', right: '8%', bottom: '2%', top: 8, containLabel: true },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: p.split, type: 'dashed' } }, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: p.label, fontSize: 11 } },
    yAxis: { type: 'category', data: recipes.map((r: any) => r.name || '?').reverse(), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: p.yAxisLabel, fontSize: 12, width: 60, overflow: 'truncate', ellipsis: '..' } },
    series: [{ type: 'bar', data: recipes.map((r: any) => r.orderCount || 0).reverse(), barWidth: 14, itemStyle: { borderRadius: [0, 4, 4, 0], color: p.bar }, label: { show: true, position: 'right', color: p.barLabel, fontSize: 12, formatter: '{c}次' } }]
  }, true)
}

function refreshCharts() {
  trendChart.value?.dispose(); trendChart.value = undefined
  barChart.value?.dispose(); barChart.value = undefined
  nextTick(() => { initTrendChart(); initBarChart() })
}

function handleResize() { trendChart.value?.resize(); barChart.value?.resize() }

onMounted(async () => {
  loading.value = true
  try { const res: any = await getDashboard(); Object.assign(stats, res.data) } catch { ElMessage.error('加载仪表盘数据失败') }
  loading.value = false
  syncAnimatedStats()
  await nextTick(); initTrendChart(); initBarChart(); initTilt()
  window.addEventListener('resize', handleResize)
})

const loadData = async () => {
  try { const res: any = await getDashboard(); Object.assign(stats, res.data); syncAnimatedStats() } catch { /* ignore */ }
}

let refreshTimer: ReturnType<typeof setInterval> | null = null
onMounted(() => { refreshTimer = setInterval(loadData, 30000) })
onUnmounted(() => { if (refreshTimer) clearInterval(refreshTimer) })

watch(() => themeStore.isDark, () => { refreshCharts() })

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart.value?.dispose(); barChart.value?.dispose()
  tiltCleanups.forEach(fn => fn()); tiltCleanups = []
  Object.values(animFrames).forEach(id => cancelAnimationFrame(id))
})
</script>

<style scoped lang="scss">
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  h2 { font-size: 24px; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; letter-spacing: -0.3px; }
  p { font-size: 14px; color: var(--text-secondary); }
}

.welcome-banner {
  background: linear-gradient(135deg, #007AFF 0%, #5856D6 35%, #AF52DE 70%, #007AFF 100%);
  background-size: 200% 200%;
  animation: bannerGradientDrift 12s ease infinite;
  border-radius: var(--radius-md);
  padding: 32px 36px;
  color: #fff;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 122, 255, 0.25), 0 2px 8px rgba(88, 86, 214, 0.2);

  &::after {
    content: '';
    position: absolute;
    right: -20px;
    top: -20px;
    width: 160px;
    height: 160px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    animation: bannerFloat 8s ease-in-out infinite;
  }
  &::before {
    content: '';
    position: absolute;
    right: 60px;
    bottom: -30px;
    width: 100px;
    height: 100px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.07);
    animation: bannerFloat 10s ease-in-out infinite reverse;
  }

  .welcome-text h1 {
    text-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  }
}

@keyframes bannerGradientDrift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

@keyframes bannerFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-12px, 10px) scale(1.08); }
}

.welcome-text {
  position: relative;
  z-index: 1;
  h1 { font-size: 22px; font-weight: 600; margin-bottom: 6px; }
  p { font-size: 14px; opacity: 0.7; }
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px) saturate(160%);
  -webkit-backdrop-filter: blur(20px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: var(--radius-md);
  padding: 22px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
  cursor: default;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0 0 auto 0;
    height: 3px;
    background: linear-gradient(90deg, transparent, rgba(0, 122, 255, 0.5), rgba(88, 86, 214, 0.5), transparent);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover {
    border-color: rgba(0, 122, 255, 0.35);
    box-shadow: 0 12px 32px rgba(0, 122, 255, 0.14), 0 2px 8px rgba(0, 0, 0, 0.04);
    transform: translateY(-4px);

    &::before { opacity: 1; }

    .stat-icon {
      transform: scale(1.12) rotate(-4deg);
      box-shadow: 0 6px 18px rgba(0, 0, 0, 0.12);
    }

    .stat-value {
      background: linear-gradient(135deg, #007AFF, #5856D6);
      -webkit-background-clip: text;
      background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }
}

html.dark .stat-card {
  background: rgba(28, 28, 32, 0.55);
  border-color: rgba(255, 255, 255, 0.08);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);

  &.blue { background: linear-gradient(135deg, rgba(0, 122, 255, 0.14), rgba(0, 122, 255, 0.05)); color: var(--apple-blue); }
  &.green { background: linear-gradient(135deg, rgba(52, 199, 89, 0.14), rgba(52, 199, 89, 0.05)); color: var(--apple-green); }
  &.orange { background: linear-gradient(135deg, rgba(255, 149, 0, 0.14), rgba(255, 149, 0, 0.05)); color: var(--apple-orange); }
  &.red { background: linear-gradient(135deg, rgba(255, 59, 48, 0.14), rgba(255, 59, 48, 0.05)); color: var(--apple-red); }
}

.stat-info { display: flex; flex-direction: column; }
.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.5px;
  transition: all 0.3s ease;
}
.stat-label { font-size: 12px; color: var(--text-tertiary); margin-top: 4px; }

.charts-row { display: grid; grid-template-columns: 1.6fr 1fr; gap: 16px; }
.bottom-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

.chart-card {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(20px) saturate(160%);
  -webkit-backdrop-filter: blur(20px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: var(--radius-md);
  padding: 24px;
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    inset: 0 0 auto 0;
    height: 2px;
    border-radius: var(--radius-md) var(--radius-md) 0 0;
    background: linear-gradient(90deg, #007AFF, #5856D6, #AF52DE, #FF2D55, #007AFF);
    background-size: 300% 100%;
    animation: borderGlow 6s linear infinite;
    opacity: 0;
    transition: opacity 0.4s ease;
  }

  &:hover {
    border-color: rgba(0, 122, 255, 0.3);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.06), 0 2px 8px rgba(0, 0, 0, 0.03);

    &::before { opacity: 1; }
  }
}

html.dark .chart-card {
  background: rgba(28, 28, 32, 0.55);
  border-color: rgba(255, 255, 255, 0.08);
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.time-badge {
  font-size: 12px;
  color: #fff;
  background: linear-gradient(135deg, #007AFF, #5856D6);
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.3);
}

.chart-area { min-height: 280px; }
.bottom-card { min-height: 380px; }

.order-list, .recipe-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  transition: background 0.15s;
  &:hover { background: var(--bg-hover); }
}

.order-avatar {
  width: 38px;
  height: 38px;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, rgba(255, 149, 0, 0.1), rgba(255, 59, 48, 0.08));
  color: var(--apple-orange);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.order-info { flex: 1; min-width: 0; }
.order-name { font-size: 13px; font-weight: 500; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.order-time { font-size: 11px; color: var(--text-tertiary); margin-top: 2px; }

.recipe-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 9px 12px;
  border-radius: var(--radius-sm);
  transition: background 0.15s;
  &:hover { background: var(--bg-hover); }
}

.recipe-rank {
  width: 24px;
  height: 24px;
  border-radius: var(--radius-xs);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
  background: var(--bg-page);
  color: var(--text-tertiary);

  &.rank-1 {
    background: linear-gradient(135deg, #FF9500, #FFB340);
    color: #fff;
    box-shadow: 0 0 0 0 rgba(255, 149, 0, 0.4);
    animation: rankPulse 2s ease-in-out infinite;
  }
  &.rank-2 { background: linear-gradient(135deg, #8E8E93, #AEAEB2); color: #fff; box-shadow: 0 2px 6px rgba(142, 142, 147, 0.3); }
  &.rank-3 { background: linear-gradient(135deg, #CD7F32, #D4A76A); color: #fff; box-shadow: 0 2px 6px rgba(205, 127, 50, 0.3); }
}

@keyframes rankPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 149, 0, 0.45); }
  50% { box-shadow: 0 0 0 6px rgba(255, 149, 0, 0); }
}

.recipe-avatar {
  width: 38px;
  height: 38px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--bg-page);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.recipe-img { width: 100%; height: 100%; object-fit: cover; }
.recipe-info { flex: 1; min-width: 0; }
.recipe-name { font-size: 13px; font-weight: 500; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.recipe-count { font-size: 11px; color: var(--text-tertiary); margin-top: 2px; }

.recipe-bar {
  width: 60px;
  height: 5px;
  background: var(--bg-page);
  border-radius: 3px;
  overflow: hidden;
  flex-shrink: 0;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--apple-blue), var(--apple-cyan));
  border-radius: 3px;
  transition: width 0.8s cubic-bezier(0.25, 0.1, 0.25, 1);
  position: relative;
  box-shadow: 0 0 8px rgba(0, 122, 255, 0.45);

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.6), transparent);
    transform: translateX(-100%);
    animation: barShimmer 2.5s ease-in-out infinite;
  }
}

@keyframes barShimmer {
  0% { transform: translateX(-100%); }
  60%, 100% { transform: translateX(100%); }
}

/* ═══ Tier-2：错落入场 ═══ */
.stagger-in > * {
  animation: dashCardIn 0.55s cubic-bezier(0.22, 1, 0.36, 1) backwards;
}
.stagger-in > *:nth-child(1) { animation-delay: 0.05s; }
.stagger-in > *:nth-child(2) { animation-delay: 0.12s; }
.stagger-in > *:nth-child(3) { animation-delay: 0.19s; }
.stagger-in > *:nth-child(4) { animation-delay: 0.26s; }
@keyframes dashCardIn {
  from { opacity: 0; transform: translateY(28px) scale(0.96); filter: blur(4px); }
  to { opacity: 1; transform: translateY(0) scale(1); filter: blur(0); }
}

/* 统计数字渐变描金 */
.stat-value {
  font-variant-numeric: tabular-nums;
  background: linear-gradient(135deg, var(--text-primary) 60%, rgba(0, 122, 255, 0.85));
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* ═══════════════════════════════════════════════
   NEON GLASS DARK · 仪表盘暗色覆盖
   ═══════════════════════════════════════════════ */

/* 欢迎横幅 → 深绿霓虹渐变 */
html.dark .welcome-banner, html[data-theme="dark"] .welcome-banner {
  background: linear-gradient(135deg, #04140E 0%, #062318 35%, #0A3325 65%, #04140E 100%) !important;
  background-size: 200% 200% !important;
  border: 1px solid rgba(0,229,153,0.22) !important;
  box-shadow: 0 12px 40px rgba(0,0,0,0.55), inset 0 1px 0 rgba(255,255,255,0.05), 0 0 48px rgba(0,229,153,0.08) !important;
  animation: bannerGradientDrift 12s ease infinite !important;
}
html.dark .welcome-banner::before, html[data-theme="dark"] .welcome-banner::before,
html.dark .welcome-banner::after, html[data-theme="dark"] .welcome-banner::after {
  background: rgba(0,229,153,0.1) !important;
}
html.dark .welcome-text h1, html[data-theme="dark"] .welcome-text h1 { color: #F2F6F4 !important; }
html.dark .welcome-text p, html[data-theme="dark"] .welcome-text p { color: #9BA8A2 !important; }
html.dark .text-gradient-flow, html[data-theme="dark"] .text-gradient-flow {
  background: linear-gradient(90deg, #33FFB3, #22D3EE, #A78BFA, #33FFB3) !important;
  background-size: 300% 100% !important;
}

/* 统计卡 → 深色玻璃 */
html.dark .stat-card, html[data-theme="dark"] .stat-card {
  background: rgba(255,255,255,0.04) !important;
  border: 1px solid rgba(255,255,255,0.08) !important;
  backdrop-filter: blur(20px) saturate(130%) !important;
  -webkit-backdrop-filter: blur(20px) saturate(130%) !important;
  box-shadow: 0 8px 28px rgba(0,0,0,0.4) !important;
}
html.dark .stat-card::before, html[data-theme="dark"] .stat-card::before {
  background: linear-gradient(90deg, transparent, rgba(0,229,153,0.6), rgba(34,211,238,0.5), transparent) !important;
}
html.dark .stat-card:hover, html[data-theme="dark"] .stat-card:hover {
  border-color: rgba(0,229,153,0.4) !important;
  box-shadow: 0 14px 40px rgba(0,0,0,0.55), 0 0 32px rgba(0,229,153,0.12) !important;
}
html.dark .stat-card:hover .stat-value, html[data-theme="dark"] .stat-card:hover .stat-value {
  background: linear-gradient(135deg, #33FFB3, #22D3EE) !important;
}
html.dark .stat-value, html[data-theme="dark"] .stat-value {
  background: linear-gradient(135deg, #F2F6F4 55%, rgba(0,229,153,0.95)) !important;
}
/* 图标底色暗色提亮 */
html.dark .stat-icon.blue, html[data-theme="dark"] .stat-icon.blue { background: rgba(0,229,153,0.16) !important; color: #33FFB3 !important; }
html.dark .stat-icon.green, html[data-theme="dark"] .stat-icon.green { background: rgba(0,229,153,0.16) !important; color: #33FFB3 !important; }
html.dark .stat-icon.orange, html[data-theme="dark"] .stat-icon.orange { background: rgba(255,176,32,0.14) !important; color: #FFB020 !important; }
html.dark .stat-icon.red, html[data-theme="dark"] .stat-icon.red { background: rgba(255,92,92,0.14) !important; color: #FF5C5C !important; }

/* 图表卡 → 深色玻璃 */
html.dark .chart-card, html[data-theme="dark"] .chart-card {
  background: rgba(255,255,255,0.04) !important;
  border: 1px solid rgba(255,255,255,0.08) !important;
  backdrop-filter: blur(20px) saturate(130%) !important;
  -webkit-backdrop-filter: blur(20px) saturate(130%) !important;
  box-shadow: 0 8px 28px rgba(0,0,0,0.4) !important;
}
html.dark .chart-card:hover, html[data-theme="dark"] .chart-card:hover {
  border-color: rgba(0,229,153,0.28) !important;
  box-shadow: 0 12px 36px rgba(0,0,0,0.5), 0 0 28px rgba(0,229,153,0.08) !important;
}
html.dark .chart-card::before, html[data-theme="dark"] .chart-card::before {
  background: linear-gradient(90deg, transparent, rgba(0,229,153,0.5), rgba(34,211,238,0.4), transparent) !important;
  opacity: 0.7;
}
html.dark .card-title, html[data-theme="dark"] .card-title { color: #F2F6F4 !important; }

/* time-badge */
html.dark .time-badge, html[data-theme="dark"] .time-badge {
  background: rgba(0,229,153,0.14) !important;
  color: #33FFB3 !important;
  border: 1px solid rgba(0,229,153,0.25) !important;
}

/* 订单/菜谱列表 */
html.dark .order-avatar, html[data-theme="dark"] .order-avatar {
  background: rgba(0,229,153,0.14) !important;
  color: #33FFB3 !important;
}
html.dark .recipe-rank, html[data-theme="dark"] .recipe-rank {
  background: linear-gradient(135deg, #00E599, #0BAF8A) !important;
  color: #041008 !important;
  box-shadow: 0 2px 10px rgba(0,229,153,0.4) !important;
}
html.dark .recipe-rank.rank-2, html[data-theme="dark"] .recipe-rank.rank-2 {
  background: linear-gradient(135deg, #8BE9FF, #22D3EE) !important;
  box-shadow: 0 2px 10px rgba(34,211,238,0.35) !important;
}
html.dark .recipe-rank.rank-3, html[data-theme="dark"] .recipe-rank.rank-3 {
  background: linear-gradient(135deg, #D8B4FE, #A78BFA) !important;
  box-shadow: 0 2px 10px rgba(167,139,250,0.35) !important;
}
html.dark .bar-fill, html[data-theme="dark"] .bar-fill {
  background: linear-gradient(90deg, #00E599, #22D3EE) !important;
  box-shadow: 0 0 8px rgba(0,229,153,0.5);
}
html.dark .recipe-avatar, html[data-theme="dark"] .recipe-avatar {
  background: rgba(255,255,255,0.06) !important;
  border: 1px solid rgba(255,255,255,0.08);
}
html.dark .order-item, html[data-theme="dark"] .order-item:hover { border-color: rgba(255,255,255,0.06) !important; }
html.dark .recipe-item, html[data-theme="dark"] .recipe-item:hover { border-color: rgba(255,255,255,0.06) !important; }

/* 状态标签暗色（Element tag 已有全局覆盖，这里补 dashboard 常用） */
html.dark .el-tag--success, html[data-theme="dark"] .el-tag--success { --el-tag-bg-color: rgba(0,229,153,0.12); --el-tag-text-color: #33FFB3; --el-tag-border-color: rgba(0,229,153,0.3); }
html.dark .el-tag--warning, html[data-theme="dark"] .el-tag--warning { --el-tag-bg-color: rgba(255,176,32,0.12); --el-tag-text-color: #FFD28A; --el-tag-border-color: rgba(255,176,32,0.3); }
html.dark .el-tag--danger, html[data-theme="dark"] .el-tag--danger { --el-tag-bg-color: rgba(255,92,92,0.12); --el-tag-text-color: #FFA8A8; --el-tag-border-color: rgba(255,92,92,0.3); }
html.dark .el-tag--info, html[data-theme="dark"] .el-tag--info { --el-tag-bg-color: rgba(255,255,255,0.08); --el-tag-text-color: #9BA8A2; --el-tag-border-color: rgba(255,255,255,0.14); }
html.dark .el-tag--primary, html[data-theme="dark"] .el-tag--primary { --el-tag-bg-color: rgba(34,211,238,0.12); --el-tag-text-color: #8BE9FF; --el-tag-border-color: rgba(34,211,238,0.3); }

/* page-header 暗色 */
html.dark .page-header h2, html[data-theme="dark"] .page-header h2 { color: #F2F6F4 !important; }
html.dark .page-header p, html[data-theme="dark"] .page-header p { color: #9BA8A2 !important; }

/* 图表空状态 */
html.dark .el-empty__description, html[data-theme="dark"] .el-empty__description { color: #6B7A73 !important; }

@media (max-width: 1200px) {
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .charts-row, .bottom-row { grid-template-columns: 1fr; }
}
</style>
