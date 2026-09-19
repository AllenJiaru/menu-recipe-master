<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon indigo">
          <el-icon :size="22"><Setting /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>系统设置</h2>
          <p>配置应用参数和数据管理</p>
        </div>
      </div>
    </div>

    <div class="settings-grid">
      <div class="page-card" v-loading="loading">
        <div class="page-card-header">
          <div class="page-header-left">
            <div class="page-header-icon blue">
              <el-icon :size="18"><Setting /></el-icon>
            </div>
            <div>
              <h3>应用配置</h3>
              <p class="text-secondary" style="font-size:12px;margin-top:2px">管理应用基本参数</p>
            </div>
          </div>
        </div>
        <el-form :model="settings" label-position="top" size="large" class="settings-form">
          <el-form-item label="应用名称"><el-input v-model="settings.app_name" /></el-form-item>
          <el-form-item label="最大上传大小 (MB)"><el-input-number v-model.number="settings.max_upload_size_mb" :min="1" :max="100" style="width: 100%" /></el-form-item>
          <el-form-item label="允许的图片类型"><el-input v-model="settings.allowed_image_types" placeholder="jpg,png,gif" /></el-form-item>
          <el-form-item label="自动同步间隔 (秒)"><el-input-number v-model.number="settings.sync_interval" :min="30" :max="3600" style="width: 100%" /></el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave"><el-icon><Check /></el-icon> 保存设置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="page-card" v-loading="loading">
        <div class="page-card-header">
          <div class="page-header-left">
            <div class="page-header-icon red">
              <el-icon :size="18"><Delete /></el-icon>
            </div>
            <div>
              <h3>数据管理</h3>
              <p class="text-secondary" style="font-size:12px;margin-top:2px">清除和导出系统数据</p>
            </div>
          </div>
        </div>

        <div class="mgmt-section">
          <h5>清除数据</h5>
          <div class="radio-group">
            <el-radio-group v-model="clearType">
              <el-radio-button value="orders">订单数据</el-radio-button>
              <el-radio-button value="recipes">菜谱数据</el-radio-button>
              <el-radio-button value="gallery">相册数据</el-radio-button>
              <el-radio-button value="sync">同步数据</el-radio-button>
            </el-radio-group>
          </div>
          <el-button type="danger" :loading="clearing" @click="handleClearData">
            <el-icon><Delete /></el-icon> 清除选中数据
          </el-button>
          <p class="action-tip">清除所选类型的所有数据，此操作不可恢复</p>
        </div>

        <el-divider />

        <div class="mgmt-section">
          <h5>导出数据</h5>
          <div class="checkbox-group">
            <el-checkbox-group v-model="exportTypes">
              <el-checkbox value="orders">订单数据</el-checkbox>
              <el-checkbox value="recipes">菜谱数据</el-checkbox>
              <el-checkbox value="gallery">相册数据</el-checkbox>
              <el-checkbox value="couples">情侣空间</el-checkbox>
            </el-checkbox-group>
          </div>
          <el-button type="warning" :loading="exporting" :disabled="exportTypes.length === 0" @click="handleExportData">
            <el-icon><Download /></el-icon> 导出选中数据
          </el-button>
          <p class="action-tip">将选中数据导出为 JSON 文件</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getSettings, updateSetting, clearData } from '@/api/settings'
import { getOrders } from '@/api/order'
import { getRecipes } from '@/api/recipe'
import { getGalleryImages } from '@/api/gallery'
import { getCouples } from '@/api/couple'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const clearing = ref(false)
const exporting = ref(false)
const clearType = ref('orders')
const exportTypes = ref<string[]>([])

const settings = reactive<any>({
  app_name: '食遇', max_upload_size_mb: 10, allowed_image_types: 'jpg,jpeg,png,gif,webp', sync_interval: 300
})

onMounted(async () => {
  loading.value = true
  try {
    const res: any = await getSettings()
    const configs = res.data || []
    configs.forEach((c: any) => { if (c.configKey in settings) settings[c.configKey] = c.configType === 'number' ? Number(c.configValue) : c.configValue })
  } catch { ElMessage.error('加载设置失败') } finally { loading.value = false }
})

const handleSave = async () => {
  saving.value = true
  try { await Promise.all(Object.entries(settings).map(([key, value]) => updateSetting(key, String(value)))); ElMessage.success('设置已保存') }
  catch { ElMessage.error('保存失败') } finally { saving.value = false }
}

const clearDataLabels: Record<string, string> = { orders: '订单数据', recipes: '菜谱数据', gallery: '相册数据', sync: '同步数据' }

const handleClearData = async () => {
  try { await ElMessageBox.confirm(`确定清除所有${clearDataLabels[clearType.value]}？`, '确认', { type: 'warning' }) } catch { return }
  clearing.value = true
  try { await clearData(clearType.value); ElMessage.success('已清除') }
  catch { ElMessage.error('清除失败') } finally { clearing.value = false }
}

const fetchDataByType = async (type: string, page = 1, size = 100): Promise<any[]> => {
  try { let res: any; switch (type) { case 'orders': res = await getOrders({ page, size }); break; case 'recipes': res = await getRecipes({ page, size }); break; case 'gallery': res = await getGalleryImages({ page, size }); break; case 'couples': res = await getCouples({ page, size }); break; default: return [] }; return res.data?.records || [] }
  catch { return [] }
}

const handleExportData = async () => {
  exporting.value = true
  try {
    const exportData: Record<string, any[]> = {}
    for (const type of exportTypes.value) exportData[type] = await fetchDataByType(type)
    const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
    const url = URL.createObjectURL(blob); const link = document.createElement('a')
    const now = new Date(); const ts = `${now.getFullYear()}${String(now.getMonth()+1).padStart(2,'0')}${String(now.getDate()).padStart(2,'0')}`
    link.href = url; link.download = `食遇数据导出_${ts}.json`; link.click(); URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') } finally { exporting.value = false }
}
</script>

<style scoped lang="scss">
.settings-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.settings-form { max-width: 400px; }

.mgmt-section {
  margin-bottom: 0;
  h5 {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 12px;
  }
}

.radio-group, .checkbox-group { margin-bottom: 16px; }

.action-tip {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 8px;
}

@media (max-width: 900px) { .settings-grid { grid-template-columns: 1fr; } }
</style>