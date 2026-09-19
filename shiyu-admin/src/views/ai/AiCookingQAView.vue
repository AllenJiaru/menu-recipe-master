<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon indigo"><el-icon :size="22"><ChatLineRound /></el-icon></div>
        <div class="page-header-text">
          <h2>烹饪问答</h2>
          <p>关于烹饪技巧、食材处理、厨房常识，有问必答</p>
        </div>
      </div>
    </div>

    <div class="qa-layout">
      <div class="input-card">
        <h3>❓ 你的问题</h3>
        <el-input v-model="question" type="textarea" :rows="4" placeholder="例如：&#10;红烧肉要炖多久？&#10;怎么蒸鸡蛋羹更嫩？&#10;炒肉怎么才不老？" />
        <div class="quick-questions">
          <span v-for="q in quickQs" :key="q" class="chip" @click="question = q">{{ q }}</span>
        </div>
        <el-button type="primary" @click="ask" :loading="loading" :disabled="!question.trim()" style="width:100%" size="large">
          <el-icon><ChatLineRound /></el-icon> 提问
        </el-button>
      </div>

      <div class="result-card" v-loading="loading">
        <h3>💬 回答</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">👨‍🍳</div>
          <p>有任何烹饪问题尽管问</p>
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
import { aiCookingQA } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const loading = ref(false)
const question = ref('')
const rawResult = ref('')
const quickQs = ['红烧肉要炖多久？', '蒸鸡蛋羹怎么才嫩？', '炒肉怎么不老？', '鱼怎么去腥？', '米饭怎么煮更香？']

const ask = async () => {
  loading.value = true
  try {
    const res: any = await aiCookingQA({ message: question.value })
    rawResult.value = res.data?.content || ''
  } catch (e: any) { ElMessage.error(e?.message || '提问失败，请检查网络和AI配置') } finally { loading.value = false }
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
.qa-layout { display: grid; grid-template-columns: 380px 1fr; gap: 24px; }
.input-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.input-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; }
.quick-questions { display: flex; flex-wrap: wrap; gap: 6px; margin: 12px 0; }
.chip { padding: 4px 12px; border-radius: 16px; font-size: 12px; cursor: pointer; background: var(--apple-bg-secondary, #f5f5f7); border: 1px solid var(--apple-border, #e8e8e8); transition: all 0.2s; &:hover { background: var(--apple-blue, #007aff); color: #fff; border-color: var(--apple-blue); } }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.result-content { font-size: 14px; line-height: 1.8; }
.page-header-icon.indigo { background: rgba(88, 86, 214, 0.1); color: #5856d6; }
@media (max-width: 900px) { .qa-layout { grid-template-columns: 1fr; } }
</style>
