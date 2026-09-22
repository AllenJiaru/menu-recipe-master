<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon lime"><el-icon :size="22"><Sunny /></el-icon></div>
        <div class="page-header-text">
          <h2>AI 趋势预测</h2>
          <p>预测美食趋势和热门菜品</p>
        </div>
      </div>
    </div>
    <div class="trend-layout">
      <div class="input-card">
        <h3>🔮 预测方向</h3>
        <el-input v-model="input" type="textarea" :rows="6" placeholder="描述你想了解的趋势，例如：&#10;下个月什么菜会火&#10;健康饮食趋势&#10;年轻人喜欢什么口味&#10;适合秋季的菜品趋势" />
        <el-button type="primary" @click="submit" :loading="loading" :disabled="!input.trim()" style="width:100%;margin-top:16px" size="large">
          <el-icon><MagicStick /></el-icon> 预测趋势
        </el-button>
      </div>
      <div class="result-card" v-loading="loading">
        <h3>📊 趋势报告</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">🔮</div>
          <p>描述你想了解的趋势方向</p>
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
import { aiTrendPredict } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'
const loading = ref(false)
const input = ref('')
const rawResult = ref('')
const submit = async () => {
  loading.value = true
  try { const res: any = await aiTrendPredict({ message: input.value }); rawResult.value = res.data?.content || '' }
  catch (e: any) { ElMessage.error(e?.message || '操作失败') } finally { loading.value = false }
}
const copyResult = async () => {
  try { await navigator.clipboard.writeText(rawResult.value); ElMessage.success('已复制') } catch { ElMessage.error('复制失败') }
}
</script>
<style scoped lang="scss">
.trend-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.input-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.input-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.result-content { font-size: 14px; line-height: 1.8; }
.page-header-icon.lime { background: rgba(132, 204, 22, 0.1); color: #84cc16; }
@media (max-width: 900px) { .trend-layout { grid-template-columns: 1fr; } }
</style>
