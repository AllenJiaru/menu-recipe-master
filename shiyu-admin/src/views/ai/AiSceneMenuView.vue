<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon pink"><el-icon :size="22"><Calendar /></el-icon></div>
        <div class="page-header-text">
          <h2>AI 场景菜单</h2>
          <p>根据场景生成完整菜单方案</p>
        </div>
      </div>
    </div>
    <div class="scene-layout">
      <div class="input-card">
        <h3>🎉 场景描述</h3>
        <el-input v-model="input" type="textarea" :rows="6" placeholder="描述你的用餐场景，例如：&#10;4人生日宴，预算300元&#10;减脂期晚餐，热量控制500kcal&#10;家庭聚餐有老人小孩&#10;浪漫烛光晚餐" />
        <el-button type="primary" @click="submit" :loading="loading" :disabled="!input.trim()" style="width:100%;margin-top:16px" size="large">
          <el-icon><MagicStick /></el-icon> 生成菜单
        </el-button>
      </div>
      <div class="result-card" v-loading="loading">
        <h3>🍽️ 完整菜单</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">🎉</div>
          <p>描述用餐场景，AI 帮你策划完整菜单</p>
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
import { aiSceneMenu } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const loading = ref(false)
const input = ref('')
const rawResult = ref('')

const submit = async () => {
  loading.value = true
  try {
    const res: any = await aiSceneMenu({ message: input.value })
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
.scene-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.input-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.input-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.result-content { font-size: 14px; line-height: 1.8; }
.page-header-icon.pink { background: rgba(236, 72, 153, 0.1); color: #ec4899; }
@media (max-width: 900px) { .scene-layout { grid-template-columns: 1fr; } }
</style>
