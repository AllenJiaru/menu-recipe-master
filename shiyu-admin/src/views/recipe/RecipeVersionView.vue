<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon indigo"><el-icon><Clock /></el-icon></div>
        <div class="page-header-text">
          <h2>版本历史</h2>
          <p v-if="recipeName">菜谱：{{ recipeName }}</p>
        </div>
      </div>
      <el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
    </div>

    <div class="version-layout">
      <div class="page-card version-list-card" v-loading="loading">
        <div v-for="v in versions" :key="v.id" class="version-item" :class="{ active: selectedVersion?.id === v.id }" @click="selectVersion(v)">
          <div class="version-dot"></div>
          <div class="version-info">
            <div class="version-header">
              <span class="name-cell">v{{ v.versionNumber }}</span>
              <span class="time-cell">{{ v.createTime }}</span>
            </div>
            <p class="version-note">{{ v.changeNote || '无备注' }}</p>
          </div>
        </div>
        <el-empty v-if="!versions.length && !loading" description="暂无版本记录" />
      </div>

      <div class="page-card detail-card" v-if="selectedVersion">
        <div class="page-card-header">
          <h3>版本 v{{ selectedVersion.versionNumber }} 详情</h3>
          <el-button type="primary" size="small" @click="handleRestore(selectedVersion.id)" v-permission="'content:recipe:edit'">
            <el-icon><RefreshRight /></el-icon> 恢复此版本
          </el-button>
        </div>

        <div v-if="versionDetail" class="version-content">
          <div class="content-section">
            <h4>基本信息</h4>
            <div class="info-grid">
              <div class="info-item"><span class="label">名称</span><span class="value">{{ versionDetail.name }}</span></div>
              <div class="info-item"><span class="label">分类</span><span class="value">{{ versionDetail.type }}</span></div>
              <div class="info-item"><span class="label">烹饪时间</span><span class="value">{{ versionDetail.cookingTime }}分钟</span></div>
              <div class="info-item"><span class="label">难度</span><span class="value"><el-rate :model-value="versionDetail.difficulty" disabled size="small" /></span></div>
            </div>
          </div>

          <div class="content-section">
            <h4>食材清单</h4>
            <el-table :data="versionDetail.materials || []" size="small" stripe>
              <el-table-column prop="name" label="食材" />
              <el-table-column prop="amount" label="用量" width="100" />
              <el-table-column prop="unit" label="单位" width="80" />
            </el-table>
          </div>

          <div class="content-section">
            <h4>烹饪步骤</h4>
            <div v-for="step in [...(versionDetail.steps || [])].sort((a: any, b: any) => a.stepNumber - b.stepNumber)" :key="step.id" class="step-item">
              <span class="badge badge-orange">步骤 {{ step.stepNumber }}</span>
              <p class="step-desc">{{ step.description }}</p>
            </div>
          </div>
        </div>
        <el-skeleton v-else :rows="6" animated />
      </div>

      <div class="page-card detail-card empty-state" v-if="!selectedVersion && versions.length">
        <el-icon :size="48" color="#d1d5db"><Document /></el-icon>
        <p>选择左侧版本查看详情</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getVersionHistory, getVersionDetail, restoreVersion } from '@/api/recipeVersion'
import { getRecipeById } from '@/api/recipe'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const recipeId = Number(route.params.id)
const recipeName = ref('')
const loading = ref(false)
const versions = ref<any[]>([])
const selectedVersion = ref<any>(null)
const versionDetail = ref<any>(null)

const loadData = async () => {
  loading.value = true
  try {
    const [recipeRes, versionRes]: any[] = await Promise.all([
      getRecipeById(recipeId).catch(() => ({ data: {} })),
      getVersionHistory(recipeId),
    ])
    recipeName.value = recipeRes.data?.name || ''
    versions.value = versionRes.data || []
  } catch { ElMessage.error('加载版本历史失败') } finally { loading.value = false }
}

const selectVersion = async (v: any) => {
  selectedVersion.value = v
  versionDetail.value = null
  try {
    const res: any = await getVersionDetail(v.id)
    versionDetail.value = res.data
  } catch { ElMessage.error('加载版本详情失败') }
}

const handleRestore = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定恢复到此版本?当前内容将被覆盖。', '确认恢复', { type: 'warning' })
    await restoreVersion(id)
    ElMessage.success('版本已恢复')
    router.back()
  } catch {}
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.version-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 24px;
}

.version-list-card {
  max-height: 600px;
  overflow-y: auto;
  padding: 16px;
}

.version-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: var(--transition);
  &:hover { background: var(--bg-page); }
  &.active { background: rgba(var(--primary-rgb), 0.08); .version-dot { background: var(--primary); } }
}

.version-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #d1d5db;
  margin-top: 6px;
  flex-shrink: 0;
}

.version-info { flex: 1; }

.version-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.version-note { font-size: 13px; color: var(--text-secondary); margin: 0; }

.detail-card { min-height: 400px; }

.content-section {
  margin-bottom: 24px;
  h4 {
    font-size: 14px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 12px;
    padding-left: 10px;
    border-left: 3px solid var(--primary);
  }
}

.step-item { margin-bottom: 12px; }
.step-desc { margin-top: 6px; font-size: 14px; color: var(--text-secondary); line-height: 1.7; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  p { font-size: 14px; color: var(--text-muted); }
}
</style>
