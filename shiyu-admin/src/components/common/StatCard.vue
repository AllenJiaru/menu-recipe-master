<template>
  <div class="stat-card" :class="variant">
    <div class="stat-bg">
      <div class="stat-bg-circle"></div>
      <div class="stat-bg-circle circle-2"></div>
    </div>
    <div class="stat-content">
      <div class="stat-info">
        <div class="stat-label">{{ title }}</div>
        <div class="stat-value">
          <span class="stat-number">{{ displayValue }}</span>
          <span v-if="suffix" class="stat-suffix">{{ suffix }}</span>
        </div>
        <div v-if="trend !== undefined" class="stat-trend" :class="trend >= 0 ? 'up' : 'down'">
          <el-icon :size="12"><Top v-if="trend >= 0" /><Bottom v-else /></el-icon>
          <span>{{ Math.abs(trend) }}%</span>
          <span class="trend-label">较昨日</span>
        </div>
      </div>
      <div class="stat-icon-wrap">
        <div class="stat-icon">
          <el-icon :size="26"><component :is="icon" /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'

const props = defineProps<{
  title: string
  value: number | string
  icon: string
  variant?: 'blue' | 'green' | 'orange' | 'red' | 'purple'
  suffix?: string
  trend?: number
}>()

const displayValue = ref(0)

onMounted(() => {
  animateValue(0, Number(props.value) || 0, 800)
})

watch(() => props.value, (newVal) => {
  animateValue(Number(displayValue.value), Number(newVal) || 0, 600)
})

function animateValue(start: number, end: number, duration: number) {
  const startTime = performance.now()
  const step = (timestamp: number) => {
    const progress = Math.min((timestamp - startTime) / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3) // easeOutCubic
    displayValue.value = Math.round(start + (end - start) * eased)
    if (progress < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}
</script>

<style scoped lang="scss">
.stat-card {
  position: relative;
  border-radius: var(--radius-lg);
  padding: 24px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 130px;

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-xl);
  }

  &:hover .stat-bg-circle {
    transform: scale(1.1);
  }

  &:hover .stat-icon {
    transform: scale(1.1) rotate(-5deg);
  }
}

.stat-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.stat-bg-circle {
  position: absolute;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  right: -20px;
  top: -20px;
  opacity: 0.15;
  transition: transform 0.5s ease;
}

.circle-2 {
  width: 80px;
  height: 80px;
  right: 30px;
  top: 40px;
  opacity: 0.1;
}

/* Variants */
.blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  .stat-bg-circle { background: #fff; }
  .stat-icon { background: rgba(255,255,255,0.2); color: #fff; }
}

.green {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: #fff;
  .stat-bg-circle { background: #fff; }
  .stat-icon { background: rgba(255,255,255,0.2); color: #fff; }
}

.orange {
  background: linear-gradient(135deg, #f2994a 0%, #f2c94c 100%);
  color: #fff;
  .stat-bg-circle { background: #fff; }
  .stat-icon { background: rgba(255,255,255,0.2); color: #fff; }
}

.red {
  background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%);
  color: #fff;
  .stat-bg-circle { background: #fff; }
  .stat-icon { background: rgba(255,255,255,0.2); color: #fff; }
}

.purple {
  background: linear-gradient(135deg, #a855f7 0%, #6366f1 100%);
  color: #fff;
  .stat-bg-circle { background: #fff; }
  .stat-icon { background: rgba(255,255,255,0.2); color: #fff; }
}

.stat-content {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.stat-label {
  font-size: 13px;
  font-weight: 500;
  opacity: 0.85;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}

.stat-number {
  font-size: 34px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.5px;
}

.stat-suffix {
  font-size: 16px;
  font-weight: 500;
  opacity: 0.7;
  margin-left: 2px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
  font-size: 12px;
  font-weight: 500;

  &.up { color: #d1fae5; }
  &.down { color: #fecaca; }
}

.trend-label {
  opacity: 0.7;
}

.stat-icon-wrap {
  flex-shrink: 0;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.35s ease;
}
</style>
