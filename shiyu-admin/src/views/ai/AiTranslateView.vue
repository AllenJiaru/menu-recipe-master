<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon cyan"><el-icon :size="22"><EditPen /></el-icon></div>
        <div class="page-header-text">
          <h2>菜谱翻译</h2>
          <p>将菜谱翻译成多种语言，方便分享给外国朋友</p>
        </div>
      </div>
    </div>

    <div class="translate-layout">
      <div class="input-card">
        <h3>🌐 原文菜谱</h3>
        <el-input v-model="sourceText" type="textarea" :rows="12" placeholder="粘贴你的菜谱内容..." />
        <el-form-item label="目标语言" style="margin-top:16px;margin-bottom:0">
          <el-select v-model="targetLang" style="width:100%">
            <el-option label="English" value="en" />
            <el-option label="日本語" value="ja" />
            <el-option label="한국어" value="ko" />
            <el-option label="Français" value="fr" />
            <el-option label="Deutsch" value="de" />
            <el-option label="Español" value="es" />
          </el-select>
        </el-form-item>
        <el-button type="primary" @click="translate" :loading="loading" :disabled="!sourceText.trim()" style="width:100%;margin-top:16px" size="large">
          <el-icon><EditPen /></el-icon> 翻译
        </el-button>
      </div>

      <div class="result-card" v-loading="loading">
        <h3>📄 翻译结果</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">🌍</div>
          <p>粘贴菜谱内容后点击翻译</p>
        </div>
        <div v-if="rawResult" class="result-content">
          <div class="translated-text markdown-body" v-html="renderMarkdown(rawResult)"></div>
          <el-button type="primary" plain @click="copyResult" style="margin-top:16px">
            <el-icon><DocumentCopy /></el-icon> 复制翻译
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { aiTranslate } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const loading = ref(false)
const sourceText = ref('')
const targetLang = ref('en')
const rawResult = ref('')

const translate = async () => {
  loading.value = true
  try {
    const res: any = await aiTranslate({ text: sourceText.value, targetLang: targetLang.value })
    rawResult.value = res.data?.content || ''
  } catch (e: any) { ElMessage.error(e?.message || '翻译失败，请检查网络和AI配置') } finally { loading.value = false }
}

const copyResult = () => {
  navigator.clipboard.writeText(rawResult.value)
  ElMessage.success('已复制到剪贴板')
}
</script>

<style scoped lang="scss">
.translate-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.input-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.input-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.translated-text { font-size: 14px; line-height: 1.8; white-space: pre-wrap; background: var(--apple-bg-secondary, #f5f5f7); padding: 16px; border-radius: 8px; }
.page-header-icon.cyan { background: rgba(100, 211, 255, 0.1); color: #64d3ff; }
@media (max-width: 900px) { .translate-layout { grid-template-columns: 1fr; } }
</style>
