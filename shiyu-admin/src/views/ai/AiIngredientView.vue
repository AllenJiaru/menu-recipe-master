<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon green"><el-icon :size="22"><Food /></el-icon></div>
        <div class="page-header-text">
          <h2>食材变菜谱</h2>
          <p>告诉 AI 你有什么食材，帮你推荐菜谱</p>
        </div>
      </div>
    </div>

    <div class="ingredient-layout">
      <div class="input-card">
        <h3>🥘 我有的食材</h3>
        <div class="tag-input">
          <el-tag v-for="(ing, i) in form.ingredients" :key="i" closable @close="form.ingredients.splice(i, 1)" round>{{ ing }}</el-tag>
          <el-input v-model="newIngredient" placeholder="输入食材后回车" @keyup.enter="addIngredient" size="default" style="width: 140px" />
        </div>
        <div class="quick-tags">
          <span v-for="q in quickIngredients" :key="q" class="chip" @click="addQuick(q)">{{ q }}</span>
        </div>
        <el-divider />
        <el-form label-position="top">
          <el-row :gutter="16">
            <el-col :span="12"><el-form-item label="口味偏好"><el-select v-model="form.taste" placeholder="可选" clearable style="width:100%"><el-option v-for="t in tastes" :key="t" :label="t" :value="t" /></el-select></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="菜系"><el-select v-model="form.cuisine" placeholder="可选" clearable style="width:100%"><el-option v-for="c in cuisines" :key="c" :label="c" :value="c" /></el-select></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="人数"><el-input-number v-model="form.servings" :min="1" :max="20" style="width:100%" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="最长烹饪时间(分钟)"><el-input-number v-model="form.maxTime" :min="10" :max="180" :step="10" style="width:100%" /></el-form-item></el-col>
          </el-row>
        </el-form>
        <el-button type="primary" @click="generate" :loading="loading" :disabled="!form.ingredients.length" style="width:100%" size="large">
          <el-icon><MagicStick /></el-icon> AI 推荐菜谱
        </el-button>
      </div>

      <div class="result-card" v-loading="loading">
        <h3>🍽️ AI 为你推荐</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">👨‍🍳</div>
          <p>添加食材后点击生成</p>
        </div>
        <div v-if="rawResult" class="result-content markdown-body" v-html="renderMarkdown(rawResult)"></div>
        <el-button v-if="rawResult" size="small" text @click="copyResult" style="margin-top: 8px;">
          <el-icon><DocumentCopy /></el-icon> 复制结果
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { aiIngredientToRecipe } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const loading = ref(false)
const newIngredient = ref('')
const rawResult = ref('')
const quickIngredients = ['鸡蛋', '番茄', '土豆', '豆腐', '青椒', '猪肉', '鸡肉', '白菜', '米饭', '面条', '虾仁', '蘑菇']
const tastes = ['清淡', '麻辣', '酸甜', '咸鲜', '微辣', '香辣']
const cuisines = ['川菜', '粤菜', '湘菜', '鲁菜', '苏菜', '浙菜', '闽菜', '徽菜', '西餐', '日料', '韩餐']
const form = reactive({ ingredients: [] as string[], taste: '', cuisine: '', servings: 2, maxTime: 60 })

const addIngredient = () => { if (newIngredient.value.trim()) { form.ingredients.push(newIngredient.value.trim()); newIngredient.value = '' } }
const addQuick = (q: string) => { if (!form.ingredients.includes(q)) form.ingredients.push(q) }

const generate = async () => {
  loading.value = true
  try {
    const res: any = await aiIngredientToRecipe(form)
    rawResult.value = res.data?.content || ''
  } catch (e: any) { ElMessage.error(e?.message || '生成失败，请检查网络和AI配置') } finally { loading.value = false }
}

const copyResult = async () => {
  try {
    await navigator.clipboard.writeText(rawResult.value)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped lang="scss">
.ingredient-layout { display: grid; grid-template-columns: 380px 1fr; gap: 24px; }
.input-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.input-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; color: var(--apple-text-primary); }
.tag-input { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; min-height: 36px; }
.quick-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 8px; }
.chip { padding: 4px 12px; border-radius: 16px; font-size: 12px; cursor: pointer; background: var(--apple-bg-secondary, #f5f5f7); border: 1px solid var(--apple-border, #e8e8e8); transition: all 0.2s; &:hover { background: var(--apple-green, #34c759); color: #fff; border-color: var(--apple-green); } }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.result-content { font-size: 14px; line-height: 1.8; color: var(--apple-text-primary); }
.page-header-icon.green { background: rgba(52, 199, 89, 0.1); color: #34c759; }
@media (max-width: 900px) { .ingredient-layout { grid-template-columns: 1fr; } }
</style>
