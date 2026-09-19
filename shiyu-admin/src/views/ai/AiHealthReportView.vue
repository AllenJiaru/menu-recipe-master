<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon mint"><el-icon :size="22"><FirstAidKit /></el-icon></div>
        <div class="page-header-text">
          <h2>饮食健康报告</h2>
          <p>AI 分析你的饮食习惯，生成健康报告</p>
        </div>
      </div>
      <el-button type="primary" @click="generateReport" :loading="loading">
        <el-icon><Refresh /></el-icon> 生成报告
      </el-button>
    </div>

    <div v-loading="loading" class="report-container">
      <div v-if="!rawResult && !loading" class="empty-state">
        <div style="font-size: 64px">📋</div>
        <h3>点击「生成报告」获取你的饮食健康分析</h3>
        <p>AI 将从营养均衡、热量摄入、饮食多样性等维度给出评估和建议</p>
      </div>
      <div v-if="rawResult" class="report-content">
        <div class="report-header-card">
          <h3>🏥 饮食健康分析报告</h3>
          <p>生成时间：{{ generatedTime }}</p>
        </div>
        <div class="report-body markdown-body" v-html="renderMarkdown(rawResult)"></div>
        <el-button v-if="rawResult" size="small" text @click="copyResult" style="margin-top: 8px;">
          <el-icon><DocumentCopy /></el-icon> 复制结果
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { aiHealthReport } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const loading = ref(false)
const rawResult = ref('')
const generatedTime = ref('')

const generateReport = async () => {
  loading.value = true
  generatedTime.value = ''
  try {
    const res: any = await aiHealthReport()
    rawResult.value = res.data?.content || ''
    generatedTime.value = new Date().toLocaleString('zh-CN')
  } catch (e: any) { ElMessage.error(e?.message || '生成报告失败，请检查网络和AI配置') } finally { loading.value = false }
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
.report-container { min-height: 400px; }
.empty-state { text-align: center; padding: 80px 0; color: var(--apple-text-secondary); h3 { color: var(--apple-text-primary); margin: 16px 0 8px; } }
.report-content { background: var(--apple-card-bg, #fff); border-radius: 16px; border: 1px solid var(--apple-border, #e8e8e8); overflow: hidden; }
.report-header-card { background: linear-gradient(135deg, #34c759 0%, #30d158 100%); padding: 24px 32px; color: #fff; h3 { margin: 0 0 4px; font-size: 20px; } p { margin: 0; opacity: 0.8; font-size: 13px; } }
.report-body { padding: 32px; font-size: 14px; line-height: 2; color: var(--apple-text-primary); }
.page-header-icon.mint { background: rgba(52, 199, 89, 0.1); color: #30d158; }
</style>
