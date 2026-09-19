<template>
  <div class="page-container print-page">
    <div class="page-header no-print">
      <div class="page-header-left">
        <div class="page-header-icon purple"><el-icon><Printer /></el-icon></div>
        <div class="page-header-text">
          <h2>菜谱打印</h2>
          <p>打印友好格式的菜谱详情</p>
        </div>
      </div>
      <el-button type="primary" @click="handlePrint">
        <el-icon><Printer /></el-icon> 打印
      </el-button>
    </div>

    <div class="page-card recipe-print-card" v-loading="loading">
      <template v-if="recipe">
        <div class="print-header">
          <div class="print-cover">
            <el-image v-if="recipe.coverImage" :src="imgUrl(recipe.coverImage)" fit="cover" class="cover-img" />
            <div v-else class="cover-placeholder"><el-icon :size="48"><Picture /></el-icon></div>
          </div>
          <div class="print-title-section">
            <h1 class="print-title">{{ recipe.name }}</h1>
            <div class="print-meta">
              <span class="badge badge-blue">分类：{{ recipeTypeMap[recipe.type] }}</span>
              <span>难度：<el-rate :model-value="recipe.difficulty" disabled size="small" /></span>
              <span class="badge badge-green">烹饪时间：{{ recipe.cookingTime }} 分钟</span>
            </div>
            <p v-if="recipe.description" class="print-desc">{{ recipe.description }}</p>
          </div>
        </div>

        <div v-if="hasNutrition" class="print-section">
          <div class="page-card-header"><h3>营养信息</h3></div>
          <table class="nutrition-table">
            <thead>
              <tr>
                <th v-for="key in nutritionKeys" :key="key">{{ nutritionLabels[key] }}</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td v-for="key in nutritionKeys" :key="key">{{ recipe[key] || '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="print-section">
          <div class="page-card-header"><h3>食材清单</h3></div>
          <table class="materials-table">
            <thead>
              <tr><th>食材</th><th>用量</th><th>单位</th></tr>
            </thead>
            <tbody>
              <tr v-for="(m, idx) in recipe.materials" :key="idx">
                <td>{{ m.name }}</td>
                <td>{{ m.amount }}</td>
                <td>{{ m.unit }}</td>
              </tr>
            </tbody>
          </table>
          <el-empty v-if="!recipe.materials?.length" description="暂无食材" :image-size="40" />
        </div>

        <div class="print-section">
          <div class="page-card-header"><h3>烹饪步骤</h3></div>
          <div v-for="step in sortedSteps" :key="step.id" class="print-step">
            <div class="step-number">{{ step.stepNumber }}</div>
            <div class="step-content">
              <p class="step-desc">{{ step.description }}</p>
              <el-image v-if="step.image" :src="imgUrl(step.image)" fit="cover" class="step-image" />
            </div>
          </div>
          <el-empty v-if="!recipe.steps?.length" description="暂无步骤" :image-size="40" />
        </div>

        <div class="print-footer">
          <p>打印时间：{{ new Date().toLocaleString() }}</p>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getRecipeById } from '@/api/recipe'
import { recipeTypeMap, imgUrl } from '@/utils/format'
import { ElMessage } from 'element-plus'

const route = useRoute()
const recipe = ref<any>(null)
const loading = ref(true)

const nutritionLabels: Record<string, string> = { calories: '热量', protein: '蛋白质', fat: '脂肪', carbs: '碳水', fiber: '膳食纤维' }
const nutritionKeys = computed(() => Object.keys(nutritionLabels))
const hasNutrition = computed(() => recipe.value && (recipe.value.calories || recipe.value.protein || recipe.value.fat || recipe.value.carbs || recipe.value.fiber))
const sortedSteps = computed(() => [...(recipe.value?.steps || [])].sort((a: any, b: any) => a.stepNumber - b.stepNumber))

const handlePrint = () => window.print()

onMounted(async () => {
  try {
    const res: any = await getRecipeById(Number(route.params.id))
    recipe.value = res.data
  } catch { ElMessage.error('加载菜谱失败') } finally { loading.value = false }
})
</script>

<style scoped lang="scss">
.print-header {
  display: flex;
  gap: 28px;
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 2px solid var(--border-color);
}

.print-cover { flex-shrink: 0; }

.cover-img {
  width: 220px;
  height: 180px;
  border-radius: var(--radius-md);
  object-fit: cover;
  box-shadow: var(--shadow-md);
}

.cover-placeholder {
  width: 220px;
  height: 180px;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #cbd5e1;
}

.print-title-section { flex: 1; }

.print-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.print-meta {
  display: flex;
  gap: 12px;
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.print-desc {
  font-size: 14px;
  color: var(--text-muted);
  line-height: 1.8;
}

.print-section {
  margin-bottom: 28px;
}

.nutrition-table,
.materials-table {
  width: 100%;
  border-collapse: collapse;
  th, td {
    padding: 10px 16px;
    text-align: left;
    border-bottom: 1px solid var(--border-color);
    font-size: 14px;
  }
  th {
    font-weight: 700;
    color: var(--text-primary);
    background: var(--bg-page);
  }
  td { color: var(--text-secondary); }
}

.print-step {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  border-radius: var(--radius-md);
  background: var(--bg-page);
}

.step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--apple-blue);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}

.step-content { flex: 1; }

.step-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
}

.step-image {
  margin-top: 12px;
  width: 200px;
  height: 140px;
  border-radius: var(--radius-md);
  object-fit: cover;
}

.print-footer {
  margin-top: 32px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
  p { font-size: 12px; color: var(--text-muted); text-align: center; }
}

@media print {
  .no-print { display: none !important; }
  .page-container { gap: 0; }
  .page-card { border: none; box-shadow: none; padding: 0; border-radius: 0; }
  .print-page { background: #fff; }
  .print-header { border-bottom-color: #000; }
  .print-title { color: #000; }
  .nutrition-table th, .materials-table th { background: #f0f0f0; color: #000; }
  .nutrition-table td, .materials-table td { color: #333; }
  .step-number { background: #333; }
}
</style>
