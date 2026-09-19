<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon yellow"><el-icon :size="22"><Bowl /></el-icon></div>
        <div class="page-header-text">
          <h2>剩菜妙招</h2>
          <p>告诉 AI 你剩了什么，帮你变出新菜</p>
        </div>
      </div>
    </div>

    <div class="leftover-layout">
      <div class="input-card">
        <h3>🍳 剩余食材/菜品</h3>
        <el-input v-model="leftovers" type="textarea" :rows="6" placeholder="例如：&#10;昨天剩的红烧肉&#10;半个白菜&#10;几个鸡蛋" />
        <el-button type="primary" @click="suggest" :loading="loading" :disabled="!leftovers.trim()" style="width:100%;margin-top:16px" size="large">
          <el-icon><MagicStick /></el-icon> 获取建议
        </el-button>
      </div>

      <div class="result-card" v-loading="loading">
        <h3>💡 创意做法</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">♻️</div>
          <p>输入剩余食材，AI 帮你想新做法</p>
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
import { aiLeftover } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const loading = ref(false)
const leftovers = ref('')
const rawResult = ref('')

const suggest = async () => {
  loading.value = true
  try {
    const res: any = await aiLeftover({ message: leftovers.value })
    rawResult.value = res.data?.content || ''
  } catch (e: any) { ElMessage.error(e?.message || '获取建议失败，请检查网络和AI配置') } finally { loading.value = false }
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
.leftover-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.input-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.input-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.result-content { font-size: 14px; line-height: 1.8; }
.page-header-icon.yellow { background: rgba(255, 204, 0, 0.1); color: #ffcc00; }
@media (max-width: 900px) { .leftover-layout { grid-template-columns: 1fr; } }
</style>