<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon cyan"><el-icon :size="22"><TrendCharts /></el-icon></div>
        <div class="page-header-text">
          <h2>AI 库存预测</h2>
          <p>预测未来食材需求，智能补货建议</p>
        </div>
      </div>
    </div>
    <div class="predict-layout">
      <div class="input-card">
        <h3>📊 库存信息</h3>
        <el-input v-model="input" type="textarea" :rows="6" placeholder="描述你的库存情况，例如：&#10;鸡蛋每天用约10个&#10;牛奶每周消耗3箱&#10;大米每月50kg&#10;肉类每周采购2次" />
        <el-button type="primary" @click="submit" :loading="loading" :disabled="!input.trim()" style="width:100%;margin-top:16px" size="large">
          <el-icon><MagicStick /></el-icon> 预测需求
        </el-button>
      </div>
      <div class="result-card" v-loading="loading">
        <h3>📈 预测结果</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">📊</div>
          <p>输入库存数据，AI 帮你预测未来需求</p>
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
import { aiInventoryPredict } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const loading = ref(false)
const input = ref('')
const rawResult = ref('')

const submit = async () => {
  loading.value = true
  try {
    const res: any = await aiInventoryPredict({ message: input.value })
    rawResult.value = res.data?.content || ''
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败，请检查网络和AI配置')
  } finally { loading.value = false }
}

const copyResult = async () => {
  try { await navigator.clipboard.writeText(rawResult.value); ElMessage.success('已复制到剪贴板') } catch { ElMessage.error('复制失败') }
}
</script>

<style scoped lang="scss">
.predict-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.input-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.input-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.result-content { font-size: 14px; line-height: 1.8; }
.page-header-icon.cyan { background: rgba(6, 182, 212, 0.1); color: #06b6d4; }
@media (max-width: 900px) { .predict-layout { grid-template-columns: 1fr; } }
</style>
