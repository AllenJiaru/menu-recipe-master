<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon gradient"><el-icon :size="22"><Setting /></el-icon></div>
        <div class="page-header-text">
          <h2>AI 模型设置</h2>
          <p>支持 16 家主流大模型，自由切换、随时生效</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="testConnection" :loading="testing">
          <el-icon><Connection /></el-icon> 测试连接
        </el-button>
        <el-button type="primary" @click="saveConfig" :loading="saving">
          <el-icon><Check /></el-icon> 保存配置
        </el-button>
      </div>
    </div>

    <el-alert v-if="testResult" :type="testResult.success ? 'success' : 'error'" :title="testResult.success ? `连接成功！响应时间: ${testResult.responseTime}ms` : `连接失败: ${testResult.error}`" show-icon closable @close="testResult = null" style="margin-bottom:20px" />

    <div class="settings-layout">
      <div class="providers-grid">
        <div v-for="p in providerList" :key="p.id" class="provider-tile" :class="{ selected: config.provider === p.id, configured: isConfigured(p.id) }" @click="selectProvider(p.id)">
          <div class="tile-icon">{{ p.icon }}</div>
          <div class="tile-info">
            <div class="tile-name">{{ p.name }}</div>
            <div class="tile-model">{{ currentModel(p.id) }}</div>
          </div>
          <div class="tile-status">
            <span v-if="isConfigured(p.id)" class="status-dot green" title="已配置" />
            <span v-else class="status-dot gray" title="未配置" />
            <el-icon v-if="config.provider === p.id" class="tile-check"><Select /></el-icon>
          </div>
        </div>
      </div>

      <div class="config-panel">
        <div class="panel-header">
          <span class="panel-icon">{{ selectedProvider?.icon }}</span>
          <div>
            <h3>{{ selectedProvider?.name }}</h3>
            <p>{{ selectedProvider?.desc }}</p>
          </div>
          <el-tag v-if="selectedProvider" type="success" size="small">当前使用</el-tag>
        </div>

        <el-form v-if="selectedProvider" label-position="top" class="panel-form">
          <el-form-item v-if="selectedProvider.needsApiKey" label="API Key">
            <el-input v-model="getProviderConfig(selectedProvider.id).apiKey" :placeholder="selectedProvider.keyPlaceholder || '输入 API Key'" show-password clearable />
          </el-form-item>

          <el-form-item v-if="selectedProvider.needsApiSecret" label="API Secret">
            <el-input v-model="getProviderConfig(selectedProvider.id).apiSecret" placeholder="输入 API Secret" show-password clearable />
          </el-form-item>

          <el-form-item v-if="selectedProvider.needsBaseUrl" label="Base URL">
            <el-input v-model="getProviderConfig(selectedProvider.id).baseUrl" placeholder="https://..." />
          </el-form-item>

          <el-form-item label="模型">
            <el-select v-model="getProviderConfig(selectedProvider.id).model" style="width:100%" filterable allow-create default-first-option>
              <el-option v-for="m in selectedProvider.models" :key="m" :label="m" :value="m" />
            </el-select>
          </el-form-item>
        </el-form>

        <div class="panel-footer">
          <el-button @click="resetProvider" :disabled="!selectedProvider">恢复默认</el-button>
          <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
        </div>
      </div>
    </div>

    <div class="tips-card">
      <h3>💡 使用说明</h3>
      <ul>
        <li><b>点击卡片</b>选择要使用的大模型，选择后点击「保存配置」立即生效，无需重启</li>
        <li><b>国内模型</b>（DeepSeek、通义千问、智谱GLM、Kimi、文心、星火、豆包、MiniMax、混元）需要到对应官网申请 API Key</li>
        <li><b>海外模型</b>（OpenAI、Claude、Gemini、Mistral、Groq、Grok）需要海外支付方式获取 API Key</li>
        <li><b>Ollama</b>完全免费，需本地安装 Ollama 并下载模型</li>
        <li>保存后 API Key 会自动脱敏显示</li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { aiGetConfig, aiUpdateConfig, aiTestConnection } from '@/api/ai'
import { ElMessage } from 'element-plus'

const saving = ref(false)
const testing = ref(false)
const testResult = ref<any>(null)

interface ProviderMeta {
  id: string
  name: string
  icon: string
  desc: string
  models: string[]
  needsApiKey: boolean
  needsApiSecret?: boolean
  needsBaseUrl?: boolean
  keyPlaceholder?: string
}

const providerList: ProviderMeta[] = [
  { id: 'openai', name: 'OpenAI', icon: '🟢', desc: 'GPT 系列，能力最强，付费使用', models: ['gpt-4o', 'gpt-4o-mini', 'gpt-4-turbo', 'gpt-3.5-turbo', 'o1', 'o1-mini'], needsApiKey: true, keyPlaceholder: 'sk-...' },
  { id: 'deepseek', name: 'DeepSeek', icon: '🐋', desc: '深度求索，性价比极高，支持思考模式', models: ['deepseek-chat', 'deepseek-reasoner'], needsApiKey: true, keyPlaceholder: 'sk-...' },
  { id: 'qwen', name: '通义千问', icon: '☁️', desc: '阿里云百炼平台，中文能力强', models: ['qwen-max', 'qwen-plus', 'qwen-turbo', 'qwen-long', 'qwen2.5-72b-instruct'], needsApiKey: true, keyPlaceholder: 'sk-...' },
  { id: 'zhipu', name: '智谱GLM', icon: '🧠', desc: '清华智谱 AI，GLM 系列模型', models: ['glm-4-plus', 'glm-4-air', 'glm-4-flash', 'glm-4-long'], needsApiKey: true },
  { id: 'moonshot', name: 'Kimi', icon: '🌙', desc: '月之暗面，超长上下文', models: ['moonshot-v1-8k', 'moonshot-v1-32k', 'moonshot-v1-128k', 'kimi-latest'], needsApiKey: true, keyPlaceholder: 'sk-...' },
  { id: 'ernie', name: '文心一言', icon: '🐻', desc: '百度千帆平台，中文场景丰富', models: ['ernie-4.0-8k', 'ernie-4.0-turbo-8k', 'ernie-3.5-8k', 'ernie-speed-8k'], needsApiKey: true },
  { id: 'spark', name: '讯飞星火', icon: '🔥', desc: '科大讯飞，需 API Key + Secret', models: ['4.0Ultra', 'generalv4.0', 'generalv3.5'], needsApiKey: true, needsApiSecret: true },
  { id: 'doubao', name: '豆包', icon: '🎯', desc: '字节跳动火山方舟平台', models: ['doubao-pro-32k', 'doubao-lite-32k', 'doubao-1.5-pro-32k'], needsApiKey: true },
  { id: 'minimax', name: 'MiniMax', icon: '💎', desc: 'MiniMax 大模型平台', models: ['abab6.5s-chat', 'abab6.5-chat', 'MiniMax-Text-01'], needsApiKey: true },
  { id: 'mistral', name: 'Mistral', icon: '🌪️', desc: '欧洲开源大模型，性能出色', models: ['mistral-large-latest', 'mistral-small-latest', 'mistral-medium-latest'], needsApiKey: true },
  { id: 'groq', name: 'Groq', icon: '⚡', desc: '极速推理，Llama 系列，免费额度', models: ['llama-3.3-70b-versatile', 'llama-3.1-8b-instant', 'llama-3.2-90b-vision-preview'], needsApiKey: true },
  { id: 'grok', name: 'Grok', icon: '🚀', desc: 'xAI 出品，马斯克旗下', models: ['grok-beta', 'grok-2-latest', 'grok-2-vision-latest'], needsApiKey: true },
  { id: 'hunyuan', name: '腾讯混元', icon: '🐧', desc: '腾讯云大模型，生态丰富', models: ['hunyuan-pro', 'hunyuan-standard', 'hunyuan-turbo', 'hunyuan-lite'], needsApiKey: true },
  { id: 'claude', name: 'Claude', icon: '🟠', desc: 'Anthropic，长文分析与写作强', models: ['claude-sonnet-4-20250514', 'claude-3-5-sonnet-20241022', 'claude-3-5-haiku-20241022', 'claude-3-opus-20240229'], needsApiKey: true, keyPlaceholder: 'sk-ant-...' },
  { id: 'gemini', name: 'Gemini', icon: '🔵', desc: 'Google 多模态大模型', models: ['gemini-2.0-flash', 'gemini-2.0-flash-lite', 'gemini-1.5-flash', 'gemini-1.5-pro'], needsApiKey: true, keyPlaceholder: 'AIza...' },
  { id: 'ollama', name: 'Ollama', icon: '🦙', desc: '本地部署，完全免费，隐私安全', models: ['qwen2.5:7b', 'qwen2.5:14b', 'qwen2.5:32b', 'llama3.1:8b', 'llama3.1:70b', 'deepseek-r1:7b', 'deepseek-r1:32b', 'gemma2:9b', 'mistral:7b'], needsApiKey: false, needsBaseUrl: true }
]

const config = reactive<any>({
  provider: 'openai',
  providers: {} as Record<string, any>
})

const selectedProvider = computed(() => providerList.find(p => p.id === config.provider))

const getProviderConfig = (id: string): any => {
  if (!config.providers[id]) {
    const meta = providerList.find(p => p.id === id)
    config.providers[id] = reactive({
      apiKey: '',
      apiSecret: '',
      baseUrl: meta?.id === 'ollama' ? 'http://localhost:11434' : '',
      model: meta?.models[0] || ''
    })
  }
  return config.providers[id]
}

const isConfigured = (id: string) => {
  const p = config.providers[id]
  if (!p) return false
  return !!p.apiKey && p.apiKey !== '****'
}

const currentModel = (id: string) => {
  const p = config.providers[id]
  return p?.model || providerList.find(m => m.id === id)?.models[0] || ''
}

const selectProvider = (id: string) => {
  config.provider = id
}

const resetProvider = () => {
  const meta = selectedProvider.value
  if (!meta) return
  const pc = getProviderConfig(meta.id)
  pc.model = meta.models[0]
  ElMessage.success(`已恢复 ${meta.name} 默认模型`)
}

const saveConfig = async () => {
  saving.value = true
  try {
    const body = {
      provider: config.provider,
      providers: config.providers
    }
    await aiUpdateConfig(body)
    ElMessage.success('配置已保存，立即生效')
    await loadConfig()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally { saving.value = false }
}

const testConnection = async () => {
  testing.value = true
  testResult.value = null
  try {
    const res: any = await aiTestConnection()
    testResult.value = res.data
  } catch (e: any) {
    testResult.value = { success: false, error: e?.message || '请求失败' }
  } finally { testing.value = false }
}

const loadConfig = async () => {
  try {
    const res: any = await aiGetConfig()
    const data = res.data || {}
    config.provider = data.provider || 'openai'
    if (data.providers) {
      for (const [id, p] of Object.entries<any>(data.providers)) {
        const meta = providerList.find(m => m.id === id)
        config.providers[id] = reactive({
          apiKey: p.apiKey || '',
          apiSecret: p.apiSecret || '',
          baseUrl: p.baseUrl || (meta?.id === 'ollama' ? 'http://localhost:11434' : ''),
          model: p.model || meta?.models[0] || ''
        })
      }
    }
    if (!config.providers[config.provider]) {
      getProviderConfig(config.provider)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载配置失败')
  }
}

onMounted(loadConfig)
</script>

<style scoped lang="scss">
.settings-layout {
  display: grid;
  grid-template-columns: 1fr 420px;
  gap: 20px;
  align-items: start;
}
.providers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 12px;
}
.provider-tile {
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--apple-card-bg, #fff);
  border-radius: 14px;
  padding: 14px 16px;
  border: 2px solid var(--apple-border, #e8e8e8);
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  &:hover { border-color: var(--apple-blue, #007aff); transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0,0,0,0.06); }
  &.selected { border-color: var(--apple-blue, #007aff); background: rgba(0, 122, 255, 0.04); }
}
.tile-icon { font-size: 26px; flex-shrink: 0; }
.tile-info { flex: 1; min-width: 0; }
.tile-name { font-size: 14px; font-weight: 600; color: var(--apple-text-primary, #1d1d1f); }
.tile-model { font-size: 11px; color: var(--apple-text-secondary, #8e8e93); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tile-status { display: flex; align-items: center; gap: 6px; flex-shrink: 0; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; &.green { background: var(--apple-green, #34c759); } &.gray { background: #c7c7cc; } }
.tile-check { color: var(--apple-blue, #007aff); font-size: 16px; }
.config-panel {
  background: var(--apple-card-bg, #fff);
  border-radius: 16px;
  border: 1px solid var(--apple-border, #e8e8e8);
  padding: 24px;
  position: sticky;
  top: 84px;
}
.panel-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  .panel-icon { font-size: 30px; }
  h3 { margin: 0; font-size: 18px; color: var(--apple-text-primary, #1d1d1f); }
  p { margin: 2px 0 0; font-size: 12px; color: var(--apple-text-secondary, #8e8e93); }
  div { flex: 1; }
}
.panel-form { margin-top: 8px; }
.panel-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
.tips-card {
  margin-top: 20px;
  background: var(--apple-bg-secondary, #f5f5f7);
  border-radius: 16px;
  padding: 20px 24px;
  h3 { margin: 0 0 10px; font-size: 15px; }
  ul { margin: 0; padding-left: 20px; font-size: 13px; line-height: 2; color: var(--apple-text-secondary, #8e8e93); b { color: var(--apple-text-primary, #1d1d1f); } }
}
.page-header-icon.gradient { background: linear-gradient(135deg, #007aff, #5856d6); color: #fff; }
.header-actions { display: flex; gap: 8px; }
@media (max-width: 1100px) {
  .settings-layout { grid-template-columns: 1fr; }
  .config-panel { position: static; }
}
</style>
