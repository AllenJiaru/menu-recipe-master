<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon green"><el-icon :size="22"><Message /></el-icon></div>
        <div class="page-header-text">
          <h2>邮件设置</h2>
          <p>配置 SMTP 邮件服务，用于发送验证码和通知</p>
        </div>
      </div>
    </div>

    <div class="email-layout">
      <div class="config-card">
        <h3>📧 SMTP 配置</h3>

        <el-form label-position="top" class="config-form">
          <el-form-item label="启用邮件服务">
            <el-switch v-model="form.enabled" active-text="开启" inactive-text="关闭" />
          </el-form-item>

          <el-form-item label="SMTP 服务器">
            <el-input v-model="form.host" placeholder="smtp.qq.com" />
          </el-form-item>

          <el-form-item label="SMTP 端口">
            <el-input-number v-model="form.port" :min="1" :max="65535" />
          </el-form-item>

          <el-form-item label="发件人邮箱（SMTP 账号）">
            <el-input v-model="form.username" placeholder="your-email@qq.com" />
          </el-form-item>

          <el-form-item label="SMTP 密码/授权码">
            <el-input v-model="form.password" type="password" show-password autocomplete="new-password" placeholder="QQ邮箱填授权码，163邮箱填密码" @input="form.passwordChanged = true" />
          </el-form-item>

          <el-form-item label="发件人名称">
            <el-input v-model="form.fromName" placeholder="食遇" />
          </el-form-item>

          <div class="form-actions">
            <el-button type="primary" @click="saveConfig" :loading="saving">
              <el-icon><Check /></el-icon> 保存配置
            </el-button>
            <el-button @click="loadConfig">
              <el-icon><RefreshRight /></el-icon> 重新加载
            </el-button>
          </div>
        </el-form>
      </div>

      <div class="test-card">
        <h3>🧪 测试发送</h3>
        <p class="test-desc">发送测试邮件验证 SMTP 配置是否正确</p>

        <el-form label-position="top">
          <el-form-item label="测试邮箱">
            <el-input v-model="testEmail" placeholder="输入接收测试邮件的邮箱" />
          </el-form-item>
          <el-button type="success" @click="sendTest" :loading="testing" :disabled="!testEmail.trim()" style="width:100%">
            <el-icon><Promotion /></el-icon> 发送测试邮件
          </el-button>
        </el-form>

        <div class="test-result" v-if="testResult">
          <el-alert :type="testResult.success ? 'success' : 'error'" :title="testResult.message" show-icon :closable="false" />
          <p v-if="testResult.responseTime" class="response-time">响应时间：{{ testResult.responseTime }}ms</p>
        </div>

        <div class="smtp-presets">
          <h4>常用 SMTP 配置</h4>
          <div class="preset-list">
            <div class="preset-item" v-for="preset in presets" :key="preset.name" @click="applyPreset(preset)">
              <span class="preset-name">{{ preset.name }}</span>
              <span class="preset-host">{{ preset.host }}:{{ preset.port }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const saving = ref(false)
const testing = ref(false)
const testEmail = ref('')
const testResult = ref<{ success: boolean; message: string; responseTime?: number } | null>(null)

const form = reactive({
  enabled: false,
  host: 'smtp.qq.com',
  port: 587,
  username: '',
  password: '',
  passwordChanged: false,
  fromName: '食遇'
})

const presets: { name: string; host: string; port: number }[] = [
  { name: 'QQ 邮箱', host: 'smtp.qq.com', port: 587 },
  { name: '163 邮箱', host: 'smtp.163.com', port: 465 },
  { name: 'Gmail', host: 'smtp.gmail.com', port: 587 },
  { name: 'Outlook', host: 'smtp.office365.com', port: 587 },
  { name: '阿里云企业邮箱', host: 'smtp.mxhichina.com', port: 465 },
]

const loadConfig = async () => {
  try {
    const res: any = await request.get('/email/config')
    const data = res.data || {}
    form.enabled = data.enabled ?? false
    form.host = data.host || 'smtp.qq.com'
    form.port = data.port || 587
    form.username = data.username || ''
    form.password = ''
    form.fromName = data.fromName || '食遇'
  } catch (e: any) {
    ElMessage.error('加载配置失败')
  }
}

const saveConfig = async () => {
  saving.value = true
  try {
    const payload: any = {
      enabled: form.enabled,
      host: form.host,
      port: form.port,
      username: form.username,
      fromName: form.fromName
    }
    if (form.passwordChanged && form.password) {
      payload.password = form.password
    }
    await request.post('/email/config', payload)
    ElMessage.success('邮件配置已保存')
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally { saving.value = false }
}

const sendTest = async () => {
  testing.value = true
  testResult.value = null
  try {
    const res: any = await request.post('/email/test', { email: testEmail.value })
    testResult.value = res.data || { success: false, message: '未知错误' }
  } catch (e: any) {
    testResult.value = { success: false, message: e.message || '发送失败' }
  } finally { testing.value = false }
}

const applyPreset = (preset: { name: string; host: string; port: number }) => {
  form.host = preset.host
  form.port = preset.port
  ElMessage.info(`已应用 ${preset.name} 配置`)
}

onMounted(loadConfig)
</script>

<style scoped lang="scss">
.email-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.config-card, .test-card {
  background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px;
  border: 1px solid var(--apple-border, #e8e8e8);
}
.config-card h3, .test-card h3 { margin: 0 0 20px; font-size: 16px; }
.config-form { max-width: 400px; }
.form-actions { display: flex; gap: 12px; margin-top: 8px; }
.test-desc { color: var(--apple-text-secondary, #999); font-size: 13px; margin: -8px 0 20px; }
.test-result { margin-top: 20px; }
.response-time { font-size: 12px; color: var(--apple-text-secondary, #999); margin-top: 8px; }
.smtp-presets { margin-top: 32px; }
.smtp-presets h4 { font-size: 14px; margin-bottom: 12px; color: var(--apple-text-secondary, #666); }
.preset-list { display: flex; flex-direction: column; gap: 8px; }
.preset-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 14px; background: var(--apple-bg, #f5f5f5); border-radius: 8px;
  cursor: pointer; transition: all 0.15s;
}
.preset-item:hover { background: var(--apple-hover, #e8e8e8); }
.preset-name { font-weight: 500; }
.preset-host { font-size: 12px; color: var(--apple-text-secondary, #999); }
@media (max-width: 900px) { .email-layout { grid-template-columns: 1fr; } }
</style>
