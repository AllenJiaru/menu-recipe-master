<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon rose"><el-icon :size="22"><User /></el-icon></div>
        <div class="page-header-text">
          <h2>AI 用户画像</h2>
          <p>分析用户口味偏好，生成个性化画像</p>
        </div>
      </div>
    </div>
    <div class="profile-layout">
      <div class="input-card">
        <h3>👤 用户信息</h3>
        <el-input v-model="input" type="textarea" :rows="6" placeholder="描述用户信息，例如：&#10;常点川菜，喜欢辣&#10;不碰海鲜&#10;注重健康&#10;经常一个人吃饭" />
        <el-button type="primary" @click="submit" :loading="loading" :disabled="!input.trim()" style="width:100%;margin-top:16px" size="large">
          <el-icon><MagicStick /></el-icon> 生成画像
        </el-button>
      </div>
      <div class="result-card" v-loading="loading">
        <h3>🎯 口味画像</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">👤</div>
          <p>输入用户行为信息，AI 生成口味画像</p>
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
import { aiUserProfile } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'
const loading = ref(false)
const input = ref('')
const rawResult = ref('')
const submit = async () => {
  loading.value = true
  try { const res: any = await aiUserProfile({ message: input.value }); rawResult.value = res.data?.content || '' }
  catch (e: any) { ElMessage.error(e?.message || '操作失败') } finally { loading.value = false }
}
const copyResult = async () => {
  try { await navigator.clipboard.writeText(rawResult.value); ElMessage.success('已复制') } catch { ElMessage.error('复制失败') }
}
</script>
<style scoped lang="scss">
.profile-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.input-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.input-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.result-content { font-size: 14px; line-height: 1.8; }
.page-header-icon.rose { background: rgba(244, 63, 94, 0.1); color: #f43f5e; }
@media (max-width: 900px) { .profile-layout { grid-template-columns: 1fr; } }
</style>
