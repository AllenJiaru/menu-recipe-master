<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon blue"><el-icon><Dish /></el-icon></div>
        <div class="page-header-text">
          <h2>{{ recipe?.name || '菜谱详情' }}</h2>
          <p>查看菜谱的详细信息</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
        <el-button :type="isFavorited ? 'danger' : 'default'" @click="toggleFav">
          <el-icon><StarFilled v-if="isFavorited" /><Star v-else /></el-icon>
          {{ isFavorited ? '已收藏' : '收藏' }}
        </el-button>
        <el-button @click="$router.push(`/recipes/${route.params.id}/print`)">
          <el-icon><Printer /></el-icon> 打印
        </el-button>
        <el-button @click="$router.push(`/recipes/${route.params.id}/versions`)">
          <el-icon><Clock /></el-icon> 版本历史
        </el-button>
        <el-button @click="saveVer" :loading="savingVersion" v-permission="'content:recipe:edit'">
          <el-icon><DocumentChecked /></el-icon> 保存版本
        </el-button>
        <el-button type="primary" @click="$router.push(`/recipes/${route.params.id}/edit`)" v-permission="'content:recipe:edit'">
          <el-icon><Edit /></el-icon> 编辑
        </el-button>
      </div>
    </div>

    <div class="detail-grid" v-if="recipe">
      <div class="page-card">
        <div class="cover-section">
          <el-image v-if="recipe.coverImage" :src="imgUrl(recipe.coverImage)" fit="cover" class="cover-img" />
          <div v-else class="cover-placeholder"><el-icon :size="48"><Picture /></el-icon><span>暂无封面</span></div>
        </div>
        <div class="info-section">
          <div class="info-row"><span class="info-label">分类</span><span class="badge badge-blue">{{ recipeTypeMap[recipe.type] }}</span></div>
          <div class="info-row"><span class="info-label">烹饪时间</span><span class="info-value">{{ recipe.cookingTime }}分钟</span></div>
          <div class="info-row"><span class="info-label">难度</span><el-rate :model-value="recipe.difficulty" disabled /></div>
          <div class="info-row"><span class="info-label">被点次数</span><span class="info-value count">{{ recipe.orderCount || 0 }}</span></div>
          <div class="info-row"><span class="info-label">收藏</span><span :class="isFavorited ? 'badge badge-red' : 'badge badge-gray'">{{ isFavorited ? '已收藏' : '未收藏' }}</span></div>
          <div class="info-row"><span class="info-label">创建时间</span><span class="time-cell">{{ recipe.createTime }}</span></div>
        </div>
        <div class="nutrition-section" v-if="hasNutrition">
          <div class="page-card-header"><h3>营养信息</h3></div>
          <div class="nutrition-grid">
            <div class="nutrition-item"><span class="n-value">{{ recipe.calories || '-' }}</span><span class="n-label">千卡</span></div>
            <div class="nutrition-item"><span class="n-value">{{ recipe.protein || '-' }}g</span><span class="n-label">蛋白质</span></div>
            <div class="nutrition-item"><span class="n-value">{{ recipe.fat || '-' }}g</span><span class="n-label">脂肪</span></div>
            <div class="nutrition-item"><span class="n-value">{{ recipe.carbs || '-' }}g</span><span class="n-label">碳水</span></div>
            <div class="nutrition-item"><span class="n-value">{{ recipe.fiber || '-' }}g</span><span class="n-label">膳食纤维</span></div>
          </div>
        </div>
      </div>

      <div class="right-col">
        <div class="page-card">
          <div class="page-card-header"><h3>食材清单</h3></div>
          <el-table :data="recipe.materials || []" size="small" stripe>
            <el-table-column prop="name" label="食材" />
            <el-table-column prop="amount" label="用量" width="120" />
            <el-table-column prop="unit" label="单位" width="100" />
          </el-table>
          <el-empty v-if="!recipe.materials?.length" description="暂无食材" :image-size="60" />
        </div>

        <div class="page-card">
          <div class="page-card-header"><h3>烹饪步骤</h3></div>
          <div v-for="step in [...(recipe.steps || [])].sort((a: any, b: any) => a.stepNumber - b.stepNumber)" :key="step.id" class="step-item">
            <span class="badge badge-orange">步骤 {{ step.stepNumber }}</span>
            <p class="step-desc">{{ step.description }}</p>
          </div>
          <el-empty v-if="!recipe.steps?.length" description="暂无步骤" :image-size="60" />
        </div>

        <RecipeCommentSection :recipe-id="Number(route.params.id)" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getRecipeById } from '@/api/recipe'
import { toggleFavorite, checkFavorite } from '@/api/favorite'
import { saveVersion } from '@/api/recipeVersion'
import { recipeTypeMap, imgUrl } from '@/utils/format'
import { ElMessage } from 'element-plus'
import RecipeCommentSection from './RecipeCommentSection.vue'

const route = useRoute()
const recipe = ref<any>(null)
const loading = ref(true)
const isFavorited = ref(false)
const savingVersion = ref(false)

const hasNutrition = computed(() => {
  if (!recipe.value) return false
  return recipe.value.calories || recipe.value.protein || recipe.value.fat || recipe.value.carbs || recipe.value.fiber
})

const toggleFav = async () => {
  try {
    await toggleFavorite(recipe.value.id, recipe.value.name, recipe.value.coverImage)
    isFavorited.value = !isFavorited.value
    ElMessage.success(isFavorited.value ? '已收藏' : '已取消收藏')
  } catch { ElMessage.error('操作失败') }
}

const saveVer = async () => {
  savingVersion.value = true
  try {
    await saveVersion(recipe.value.id, '手动保存版本')
    ElMessage.success('版本已保存')
  } catch { ElMessage.error('保存失败') } finally { savingVersion.value = false }
}

onMounted(async () => {
  try {
    const res: any = await getRecipeById(Number(route.params.id))
    recipe.value = res.data
    const favRes: any = await checkFavorite(recipe.value.id)
    isFavorited.value = favRes.data?.favorited || false
  } catch { ElMessage.error('加载菜谱失败') } finally { loading.value = false }
})
</script>

<style scoped lang="scss">
.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-grid {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 24px;
}

.cover-section { margin-bottom: 20px; }

.cover-img {
  width: 100%;
  height: 260px;
  border-radius: var(--radius-md);
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 260px;
  border-radius: var(--radius-md);
  background: var(--bg-page);
  border: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--text-tertiary);
}

.info-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 13px;
  color: var(--text-tertiary);
}

.info-value {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.info-value.count {
  color: var(--apple-blue);
  font-weight: 600;
  font-size: 18px;
}

.right-col {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.nutrition-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

.nutrition-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

.nutrition-item {
  text-align: center;
  padding: 12px 8px;
  background: var(--bg-page);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
}

.n-value {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: var(--apple-blue);
}

.n-label {
  display: block;
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 4px;
}

.step-item { margin-bottom: 16px; }

.step-desc {
  margin-top: 8px;
  color: var(--text-secondary);
  line-height: 1.8;
  font-size: 14px;
}

@media (max-width: 900px) { .detail-grid { grid-template-columns: 1fr; } }
</style>
