<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon blue"><el-icon :size="22"><Camera /></el-icon></div>
        <div class="page-header-text">
          <h2>菜品识别</h2>
          <p>上传菜品照片，AI 帮你识别是什么菜</p>
        </div>
      </div>
    </div>

    <div class="recognize-layout">
      <div class="upload-card">
        <h3>📷 上传菜品照片</h3>
        <el-upload drag :auto-upload="false" :show-file-list="false" accept="image/*" @change="onFileChange">
          <div v-if="!imageUrl" class="upload-placeholder">
            <el-icon :size="48"><UploadFilled /></el-icon>
            <p>拖拽图片到这里，或 <em>点击上传</em></p>
          </div>
          <img v-else :src="imageUrl" class="preview-img" />
        </el-upload>
        <el-button type="primary" @click="recognize" :loading="loading" :disabled="!imageUrl" style="width:100%;margin-top:16px" size="large">
          <el-icon><Camera /></el-icon> 识别菜品
        </el-button>
      </div>

      <div class="result-card" v-loading="loading">
        <h3>🔍 识别结果</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">📸</div>
          <p>上传照片后点击识别</p>
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
import { aiRecognize } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const loading = ref(false)
const imageUrl = ref('')
const rawResult = ref('')
let base64Data = ''

const onFileChange = (file: any) => {
  const raw = file.raw
  if (imageUrl.value) URL.revokeObjectURL(imageUrl.value)
  imageUrl.value = URL.createObjectURL(raw)
  const reader = new FileReader()
  reader.onload = (e) => { base64Data = (e.target?.result as string) || '' }
  reader.readAsDataURL(raw)
}

const recognize = async () => {
  if (!base64Data) { ElMessage.warning('请先上传图片'); return }
  loading.value = true
  try {
    const res: any = await aiRecognize({ image: base64Data })
    rawResult.value = res.data?.content || ''
  } catch (e: any) { ElMessage.error(e?.message || '识别失败，请检查网络和AI配置') } finally { loading.value = false }
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
.recognize-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.upload-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.upload-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; }
.upload-placeholder { padding: 40px 0; color: var(--apple-text-secondary); p { margin-top: 12px; em { color: var(--apple-blue); font-style: normal; } } }
.preview-img { max-width: 100%; max-height: 360px; border-radius: 8px; object-fit: contain; }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.result-content { font-size: 14px; line-height: 1.8; }
.page-header-icon.blue { background: rgba(0, 122, 255, 0.1); color: #007aff; }
@media (max-width: 900px) { .recognize-layout { grid-template-columns: 1fr; } }
</style>