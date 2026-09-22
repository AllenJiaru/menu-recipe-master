<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon emerald"><el-icon :size="22"><Grid /></el-icon></div>
        <div class="page-header-text">
          <h2>AI 整桌菜分析</h2>
          <p>分析整桌菜品的营养搭配和上菜顺序</p>
        </div>
      </div>
    </div>
    <div class="menu-layout">
      <div class="input-card">
        <h3>🍽️ 菜品列表</h3>
        <el-input v-model="input" type="textarea" :rows="6" placeholder="列出你要分析的菜品，例如：&#10;凉菜：凉拌黄瓜、皮蛋豆腐&#10;热菜：红烧肉、清蒸鱼、宫保鸡丁&#10;汤：番茄蛋汤&#10;主食：米饭" />
        <el-button type="primary" @click="submit" :loading="loading" :disabled="!input.trim()" style="width:100%;margin-top:16px" size="large">
          <el-icon><MagicStick /></el-icon> 分析菜品
        </el-button>
      </div>
      <div class="result-card" v-loading="loading">
        <h3>📋 分析报告</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">🍽️</div>
          <p>列出菜品，AI 帮你分析搭配</p>
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
import { aiMenuAnalysis } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'
const loading = ref(false)
const input = ref('')
const rawResult = ref('')
const submit = async () => {
  loading.value = true
  try { const res: any = await aiMenuAnalysis({ message: input.value }); rawResult.value = res.data?.content || '' }
  catch (e: any) { ElMessage.error(e?.message || '操作失败') } finally { loading.value = false }
}
const copyResult = async () => {
  try { await navigator.clipboard.writeText(rawResult.value); ElMessage.success('已复制') } catch { ElMessage.error('复制失败') }
}
</script>
<style scoped lang="scss">
.menu-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.input-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.input-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.result-content { font-size: 14px; line-height: 1.8; }
.page-header-icon.emerald { background: rgba(16, 185, 129, 0.1); color: #10b981; }
@media (max-width: 900px) { .menu-layout { grid-template-columns: 1fr; } }
</style>
