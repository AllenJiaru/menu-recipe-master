<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon green">
          <el-icon :size="22"><Download /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>数据导出</h2>
          <p>导出菜谱或订单数据为 CSV 或 Excel 格式</p>
        </div>
      </div>
    </div>

    <div class="page-card export-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="导出类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio-button value="recipes">菜谱数据</el-radio-button>
            <el-radio-button value="orders">订单数据</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="导出格式" prop="format">
          <el-radio-group v-model="form.format">
            <el-radio-button value="csv">CSV</el-radio-button>
            <el-radio-button value="excel">Excel</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="日期范围">
          <el-date-picker v-model="form.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>

        <el-form-item v-if="form.type === 'recipes'" label="分类筛选">
          <el-select v-model="form.categoryId" placeholder="全部分类" clearable style="width: 200px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="exporting" @click="handleExport">
            <el-icon><Download /></el-icon> 导出数据
          </el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <div class="export-tips">
        <h4>导出说明</h4>
        <ul>
          <li>CSV 格式适用于大多数表格软件，文件体积小</li>
          <li>Excel 格式保留更丰富的格式信息</li>
          <li>可选择日期范围筛选导出特定时间段的数据</li>
          <li>菜谱导出包含：名称、分类、难度、烹饪时间、食材、步骤等</li>
          <li>订单导出包含：订单号、菜谱、状态、时间、备注等</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { exportRecipes, exportOrders } from '@/api/exportApi'
import { getCategories } from '@/api/recipe'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const formRef = ref<FormInstance>()
const exporting = ref(false)
const categories = ref<any[]>([])
const form = reactive({ type: 'recipes', format: 'csv', dateRange: null as any, categoryId: null as number | null })
const rules: FormRules = {
  type: [{ required: true, message: '请选择导出类型', trigger: 'change' }],
  format: [{ required: true, message: '请选择导出格式', trigger: 'change' }],
}

const handleExport = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  exporting.value = true
  try {
    const params: any = { format: form.format }
    if (form.dateRange) { params.startDate = form.dateRange[0]; params.endDate = form.dateRange[1] }
    if (form.type === 'recipes' && form.categoryId) params.categoryId = form.categoryId

    const fn = form.type === 'recipes' ? exportRecipes : exportOrders
    const res: any = await fn(params)

    const blob = res instanceof Blob ? res : new Blob([res], { type: form.format === 'csv' ? 'text/csv' : 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${form.type === 'recipes' ? '菜谱数据' : '订单数据'}.${form.format === 'csv' ? 'csv' : 'xlsx'}`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e: any) { ElMessage.error(e.message || '导出失败') } finally { exporting.value = false }
}

onMounted(async () => {
  const res: any = await getCategories().catch(() => ({ data: [] }))
  categories.value = res.data || []
})
</script>

<style scoped lang="scss">
.export-card { max-width: 640px; }

.export-tips {
  h4 { font-size: 14px; font-weight: 600; color: var(--text-primary); margin-bottom: 12px; }
  ul { padding-left: 20px; }
  li { font-size: 13px; color: var(--text-secondary); line-height: 2; }
}
</style>
