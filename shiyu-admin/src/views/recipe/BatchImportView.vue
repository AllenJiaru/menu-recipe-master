<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon orange">
          <el-icon :size="22"><Upload /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>批量导入菜谱</h2>
          <p>通过 JSON 或 CSV 文件批量导入菜谱数据</p>
        </div>
      </div>
    </div>

    <div class="page-card">
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :before-upload="beforeUpload"
        accept=".json,.csv"
        class="upload-area"
      >
        <div class="upload-content">
          <el-icon class="upload-icon"><upload-filled /></el-icon>
          <p>拖拽文件到此处，或<em>点击上传</em></p>
          <p class="upload-hint">支持 JSON 和 CSV 格式文件</p>
        </div>
      </el-upload>
    </div>

    <div v-if="previewData.length > 0" class="page-card">
      <div class="preview-header">
        <h3>预览数据（{{ previewData.length }} 条菜谱）</h3>
        <el-button type="primary" :loading="importing" @click="handleImport">
          {{ importing ? '导入中...' : '导入全部' }}
        </el-button>
      </div>

      <el-table :data="previewData" style="width: 100%" max-height="400">
        <el-table-column prop="name" label="菜谱名称" width="200">
          <template #default="{ row }"><span class="name-cell">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120">
          <template #default="{ row }">
            <span class="badge badge-orange">{{ row.category }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ingredients" label="食材" min-width="200">
          <template #default="{ row }">
            {{ Array.isArray(row.ingredients) ? row.ingredients.join(', ') : row.ingredients }}
          </template>
        </el-table-column>
        <el-table-column prop="cookTime" label="烹饪时间" width="100">
          <template #default="{ row }"><span class="time-cell">{{ row.cookTime }}</span></template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100" />
      </el-table>
    </div>

    <div v-if="importProgress > 0 && importing" class="page-card">
      <div class="page-card-header"><h3>导入进度</h3></div>
      <el-progress :percentage="importProgress" :status="importProgress === 100 ? 'success' : ''" />
    </div>

    <div v-if="importResult" class="page-card result-card">
      <div class="page-card-header"><h3>导入结果</h3></div>
      <div class="result-stats">
        <div class="result-item success">
          <span class="result-count">{{ importResult.success }}</span>
          <span class="result-label">成功</span>
        </div>
        <div class="result-item error">
          <span class="result-count">{{ importResult.error }}</span>
          <span class="result-label">失败</span>
        </div>
        <div class="result-item total">
          <span class="result-count">{{ importResult.total }}</span>
          <span class="result-label">总计</span>
        </div>
      </div>
      <el-button v-if="importResult.error > 0" type="warning" @click="downloadErrors">
        下载错误报告
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

interface RecipeData {
  name: string
  category: string
  ingredients: string[]
  cookTime: string
  difficulty: string
  instructions?: string
}

interface ImportResult {
  success: number
  error: number
  total: number
  errors: string[]
}

const uploadRef = ref()
const previewData = ref<RecipeData[]>([])
const importing = ref(false)
const importProgress = ref(0)
const importResult = ref<ImportResult | null>(null)
const errorReport = ref<string[]>([])

function beforeUpload(file: File) {
  const isValid = file.type === 'application/json' || file.name.endsWith('.csv')
  if (!isValid) {
    ElMessage.error('Only JSON and CSV files are allowed')
    return false
  }
  return true
}

async function handleFileChange(file: any) {
  if (!file) return
  const rawFile = file.raw || file
  const reader = new FileReader()

  reader.onload = (e) => {
    try {
      const content = e.target?.result as string
      if (rawFile.name.endsWith('.json')) {
        previewData.value = JSON.parse(content)
      } else if (rawFile.name.endsWith('.csv')) {
        previewData.value = parseCSV(content)
      }
      importResult.value = null
      ElMessage.success(`已加载 ${previewData.value.length} 条菜谱`)
    } catch (err) {
      ElMessage.error('文件解析失败')
    }
  }

  reader.readAsText(rawFile)
}

function parseCSV(content: string): RecipeData[] {
  const lines = content.trim().split('\n')
  const headers = lines[0].split(',').map(h => h.trim())
  return lines.slice(1).map(line => {
    const values = line.split(',').map(v => v.trim())
    const row: Record<string, string> = {}
    headers.forEach((h, i) => { row[h] = values[i] || '' })
    return {
      name: row.name || '',
      category: row.category || '',
      ingredients: (row.ingredients || '').split(';'),
      cookTime: row.cookTime || '',
      difficulty: row.difficulty || ''
    }
  })
}

async function handleImport() {
  importing.value = true
  importProgress.value = 0
  errorReport.value = []

  const total = previewData.value.length
  let successCount = 0
  let errorCount = 0

  for (let i = 0; i < total; i++) {
    const item = previewData.value[i]
    const errors: string[] = []

    if (!item.name || !item.name.trim()) {
      errors.push(`第${i + 1}行：菜谱名称不能为空`)
    }
    if (!item.category || !item.category.trim()) {
      errors.push(`第${i + 1}行：分类不能为空`)
    }

    if (errors.length > 0) {
      errorCount++
      errorReport.value.push(...errors)
    } else {
      successCount++
    }

    await new Promise(resolve => setTimeout(resolve, 50))
    importProgress.value = Math.round(((i + 1) / total) * 100)
  }

  importResult.value = {
    success: successCount,
    error: errorCount,
    total,
    errors: errorReport.value
  }

  importing.value = false
  ElMessage.success(`导入完成：${successCount} 条成功，${errorCount} 条失败`)
}

function downloadErrors() {
  const blob = new Blob([importResult.value?.errors.join('\n') || ''], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'import-errors.txt'
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<style scoped lang="scss">
.upload-area { width: 100%; }
.upload-content { padding: 40px; text-align: center; }
.upload-icon { font-size: 48px; color: var(--text-quaternary); }
.upload-hint { font-size: 12px; color: var(--text-tertiary); }

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.name-cell {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
}

.time-cell {
  color: var(--text-tertiary);
  font-size: 13px;
  font-family: 'SF Mono', 'Menlo', monospace;
}

.result-stats {
  display: flex;
  gap: 32px;
  margin-bottom: 16px;
}

.result-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.result-count {
  font-size: 32px;
  font-weight: 700;
}

.result-item.success .result-count { color: var(--apple-green); }
.result-item.error .result-count { color: var(--apple-red); }
.result-item.total .result-count { color: var(--apple-blue); }
.result-label { font-size: 13px; color: var(--text-tertiary); margin-top: 4px; }
</style>
