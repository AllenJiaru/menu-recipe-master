<template>
  <div class="image-upload">
    <el-upload
      :http-request="handleUpload"
      :show-file-list="false"
      :before-upload="beforeUpload"
      accept="image/*"
      class="upload-area"
    >
      <img v-if="modelValue" :src="getModelUrl()" class="preview" />
      <div v-else class="upload-placeholder">
        <el-icon size="32" color="#c0c4cc"><Plus /></el-icon>
        <span>点击上传图片</span>
      </div>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps<{ modelValue?: string }>()
const emit = defineEmits(['update:modelValue'])

function getModelUrl() {
  if (!props.modelValue) return ''
  if (props.modelValue.startsWith('http')) return props.modelValue
  const path = props.modelValue.replace(/^\/uploads\//, '')
  return `/api/files/preview?path=${encodeURIComponent(path)}`
}

const beforeUpload = (file: File) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片文件'); return false }
  if (file.size / 1024 / 1024 > 10) { ElMessage.error('图片大小不能超过10MB'); return false }
  return true
}

const handleUpload = async (options: any) => {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res: any = await request.post('/files/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    if (res.code === 200) {
      emit('update:modelValue', res.data)
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e: any) {
    ElMessage.error('上传失败: ' + (e.message || '未知错误'))
  }
}
</script>

<style scoped lang="scss">
.image-upload {
  :deep(.el-upload) {
    border: 2px dashed #e2e8f0;
    border-radius: 12px;
    cursor: pointer;
    overflow: hidden;
    width: 140px;
    height: 140px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    background: #fafbfc;

    &:hover {
      border-color: var(--primary-light);
      background: rgba(99, 102, 241, 0.02);
    }
  }
}

.preview {
  width: 140px;
  height: 140px;
  object-fit: cover;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #94a3b8;
  font-size: 12px;
}
</style>
