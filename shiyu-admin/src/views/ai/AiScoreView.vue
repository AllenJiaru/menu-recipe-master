<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon amber"><el-icon :size="22"><Star /></el-icon></div>
        <div class="page-header-text">
          <h2>AI 菜谱评分</h2>
          <p>AI 从口味、营养、难度、性价比等维度综合评分</p>
        </div>
      </div>
    </div>

    <div class="score-layout">
      <div class="input-card">
        <h3>⭐ 菜谱信息</h3>
        <el-form label-position="top">
          <el-form-item label="菜名"><el-input v-model="recipeName" placeholder="输入菜名" /></el-form-item>
          <el-form-item label="食材"><el-input v-model="ingredients" placeholder="用逗号分隔，如：牛肉, 土豆, 酱油" /></el-form-item>
          <el-form-item label="做法描述"><el-input v-model="description" type="textarea" :rows="4" placeholder="简单描述做法..." /></el-form-item>
        </el-form>
        <el-button type="primary" @click="score" :loading="loading" :disabled="!recipeName" style="width:100%" size="large">
          <el-icon><Star /></el-icon> AI 评分
        </el-button>
      </div>

      <div class="result-card" v-loading="loading">
        <h3>📊 评分报告</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">🏆</div>
          <p>输入菜谱信息后获取 AI 评分</p>
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
import { ref } from 'vue'
import { aiScore } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const loading = ref(false)
const recipeName = ref('')
const ingredients = ref('')
const description = ref('')
const rawResult = ref('')

const score = async () => {
  loading.value = true
  try {
    const context = `菜名：${recipeName.value}\n食材：${ingredients.value}\n做法：${description.value}`
    const res: any = await aiScore({ message: context })
    rawResult.value = res.data?.content || ''
  } catch (e: any) { ElMessage.error(e?.message || '评分失败，请检查网络和AI配置') } finally { loading.value = false }
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
.score-layout { display: grid; grid-template-columns: 380px 1fr; gap: 24px; }
.input-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.input-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.result-content { font-size: 14px; line-height: 1.8; }
.page-header-icon.amber { background: rgba(255, 159, 10, 0.1); color: #ff9f0a; }
@media (max-width: 900px) { .score-layout { grid-template-columns: 1fr; } }
</style>
