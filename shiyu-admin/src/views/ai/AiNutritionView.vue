<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon red"><el-icon :size="22"><DataAnalysis /></el-icon></div>
        <div class="page-header-text">
          <h2>营养分析</h2>
          <p>AI 分析菜谱的营养成分和健康建议</p>
        </div>
      </div>
    </div>

    <div class="nutrition-layout">
      <div class="input-card">
        <h3>📊 菜谱信息</h3>
        <el-form label-position="top">
          <el-form-item label="菜名"><el-input v-model="form.recipeName" placeholder="输入菜名" /></el-form-item>
          <el-form-item label="食材（每行一个）">
            <el-input v-model="ingredientsText" type="textarea" :rows="4" placeholder="鸡蛋&#10;番茄&#10;盐" />
          </el-form-item>
          <el-form-item label="份量"><el-input v-model="form.servings" placeholder="如：2人份" /></el-form-item>
        </el-form>
        <el-button type="primary" @click="analyze" :loading="loading" :disabled="!form.recipeName" style="width:100%" size="large">
          <el-icon><DataAnalysis /></el-icon> 分析营养
        </el-button>
      </div>

      <div class="result-card" v-loading="loading">
        <h3>🥗 营养报告</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">🥦</div>
          <p>输入菜谱信息后点击分析</p>
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
import { aiNutrition } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const loading = ref(false)
const rawResult = ref('')
const ingredientsText = ref('')
const form = reactive({ recipeName: '', servings: '2人份' })

const analyze = async () => {
  loading.value = true
  try {
    const ingredients = ingredientsText.value.split('\n').map(s => s.trim()).filter(Boolean)
    const res: any = await aiNutrition({ recipeName: form.recipeName, ingredients, servings: form.servings })
    rawResult.value = res.data?.content || ''
  } catch (e: any) { ElMessage.error(e?.message || '分析失败，请检查网络和AI配置') } finally { loading.value = false }
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
.nutrition-layout { display: grid; grid-template-columns: 380px 1fr; gap: 24px; }
.input-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.input-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.result-content { font-size: 14px; line-height: 1.8; color: var(--apple-text-primary); }
.page-header-icon.red { background: rgba(255, 59, 48, 0.1); color: #ff3b30; }
@media (max-width: 900px) { .nutrition-layout { grid-template-columns: 1fr; } }
</style>
