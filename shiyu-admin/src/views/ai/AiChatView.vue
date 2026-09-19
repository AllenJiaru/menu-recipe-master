<template>
  <div class="page-container chat-page">
    <div class="chat-layout">
      <div class="session-sidebar">
        <div class="sidebar-header">
          <h3>会话列表</h3>
          <el-button type="primary" :icon="Plus" circle size="small" @click="createNewSession" />
        </div>
        <div class="session-list">
          <div
            v-for="s in sessions"
            :key="s.id"
            :class="['session-item', { active: currentSessionId === s.id }]"
            @click="switchSession(s.id)"
          >
            <el-icon class="session-icon"><ChatDotRound /></el-icon>
            <div class="session-info">
              <div class="session-title">{{ s.title }}</div>
              <div class="session-time">{{ formatTime(s.updateTime || s.createTime) }}</div>
            </div>
            <el-button
              class="session-delete"
              :icon="Delete"
              size="small"
              text
              @click.stop="deleteSession(s.id)"
            />
          </div>
          <div v-if="sessions.length === 0" class="session-empty">
            <el-icon :size="32"><ChatLineRound /></el-icon>
            <p>暂无会话</p>
          </div>
        </div>
      </div>

      <div class="chat-main">
        <div class="chat-header">
          <div class="chat-header-left">
            <div class="page-header-icon purple"><el-icon :size="20"><ChatDotRound /></el-icon></div>
            <div>
              <h2>{{ currentSessionTitle }}</h2>
              <p class="chat-provider">{{ providerInfo.currentProvider || '未配置' }}</p>
            </div>
          </div>
        </div>

        <div class="chat-messages" ref="messagesRef">
          <div v-if="messages.length === 0 && !loading" class="chat-empty">
            <div class="chat-empty-icon">🍳</div>
            <h3>你好！我是食遇 AI 助手</h3>
            <p>问我任何关于美食、菜谱、烹饪的问题</p>
            <div class="suggestion-chips">
              <span v-for="s in suggestions" :key="s" class="chip" @click="sendMessage(s)">{{ s }}</span>
            </div>
          </div>
          <div v-for="(msg, i) in messages" :key="msg.id || i" :class="['chat-msg', msg.role]">
            <div class="msg-avatar-wrap">
              <img v-if="msg.role === 'user' && userAvatar" :src="userAvatar" class="msg-avatar-img" />
              <div v-else-if="msg.role === 'user'" class="msg-avatar-text">{{ userInitial }}</div>
              <div v-else class="msg-avatar-ai">
                <span>🤖</span>
              </div>
            </div>
            <div class="msg-content">
              <div class="msg-bubble markdown-body" v-html="renderMarkdown(msg._displayContent || msg.content)"></div>
              <button v-if="msg.role === 'assistant' && !msg._streaming" class="msg-copy" @click="copyMessage(msg.content)" title="复制">
                <el-icon :size="12"><DocumentCopy /></el-icon>
              </button>
              <div class="msg-time" v-if="msg.createTime">{{ formatTime(msg.createTime) }}</div>
            </div>
          </div>
          <div v-if="loading && !streamingMsgId" class="chat-msg assistant">
            <div class="msg-avatar-wrap">
              <div class="msg-avatar-ai"><span>🤖</span></div>
            </div>
            <div class="msg-bubble typing"><span /><span /><span /></div>
          </div>
        </div>

        <div class="chat-input">
          <div class="input-row">
            <el-input
              v-model="input"
              placeholder="输入你想问的问题..."
              @keyup.enter="handleSend"
              :disabled="loading"
              size="large"
              maxlength="2000"
              show-word-limit
            />
            <el-button type="primary" @click="handleSend" :loading="loading" :disabled="!input.trim()" size="large">
              <el-icon><Promotion /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, computed } from 'vue'
import { Plus, Delete, ChatLineRound, DocumentCopy } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  aiInfo, aiListSessions, aiCreateSession, aiDeleteSession,
  aiListMessages, aiChatSession
} from '@/api/ai'
import { renderMarkdown } from '@/utils/markdown'
import { imgUrl } from '@/utils/format'
import { useAuthStore } from '@/stores/auth'

interface Message {
  id?: number
  sessionId?: number
  role: string
  content: string
  createTime?: string
  _displayContent?: string
  _streaming?: boolean
}

interface Session {
  id: number
  title: string
  provider?: string
  model?: string
  createTime?: string
  updateTime?: string
}

const authStore = useAuthStore()
const messagesRef = ref<HTMLElement>()
const messages = ref<Message[]>([])
const sessions = ref<Session[]>([])
const currentSessionId = ref<number | null>(null)
const input = ref('')
const loading = ref(false)
const providerInfo = ref<any>({})
const streamingMsgId = ref<number | null>(null)
const suggestions = ['推荐今晚吃什么', '红烧肉怎么做', '番茄有什么营养', '清淡的午餐推荐']

const userAvatar = computed(() => imgUrl(authStore.userInfo?.avatar) || '')
const userInitial = computed(() => (authStore.userInfo?.nickname || authStore.userInfo?.username || '我').charAt(0))

const currentSessionTitle = computed(() => {
  if (currentSessionId.value) {
    const s = sessions.value.find(s => s.id === currentSessionId.value)
    if (s) return s.title
  }
  return 'AI 美食助手'
})

onMounted(async () => {
  try { const res: any = await aiInfo(); providerInfo.value = res.data || {} } catch { providerInfo.value = { currentProvider: '未配置' } }
  try { await authStore.getUserInfo() } catch {}
  await loadSessions()
})

const formatTime = (t?: string) => {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diffMs = now.getTime() - d.getTime()
  const diffMin = Math.floor(diffMs / 60000)
  if (diffMin < 1) return '刚刚'
  if (diffMin < 60) return diffMin + '分钟前'
  const diffHr = Math.floor(diffMin / 60)
  if (diffHr < 24) return diffHr + '小时前'
  const diffDay = Math.floor(diffHr / 24)
  if (diffDay < 7) return diffDay + '天前'
  return d.toLocaleDateString('zh-CN')
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}

const loadSessions = async () => {
  try {
    const res: any = await aiListSessions()
    sessions.value = res.data || []
    if (sessions.value.length > 0 && !currentSessionId.value) {
      await switchSession(sessions.value[0].id)
    }
  } catch { /* sessions stay empty */ }
}

const switchSession = async (id: number) => {
  currentSessionId.value = id
  try {
    const res: any = await aiListMessages(id)
    messages.value = (res.data || []).map((m: any) => ({
      id: m.id,
      sessionId: m.sessionId,
      role: m.role,
      content: m.content,
      createTime: m.createTime
    }))
    scrollToBottom()
  } catch { ElMessage.error('加载消息失败') }
}

const createNewSession = async () => {
  try {
    const res: any = await aiCreateSession()
    await loadSessions()
    currentSessionId.value = res.data.id
    messages.value = []
  } catch { ElMessage.error('创建会话失败') }
}

const deleteSession = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除此会话？', '提示', { type: 'warning' })
    await aiDeleteSession(id)
    if (currentSessionId.value === id) {
      currentSessionId.value = null
      messages.value = []
    }
    await loadSessions()
    ElMessage.success('已删除')
  } catch (e: any) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const typewriterEffect = (msg: Message, fullContent: string) => {
  msg._streaming = true
  msg._displayContent = ''
  streamingMsgId.value = msg.id || 0
  let idx = 0
  const chunkSize = 3
  const interval = 30
  const timer = setInterval(() => {
    idx += chunkSize
    if (idx >= fullContent.length) {
      msg._displayContent = fullContent
      msg._streaming = false
      streamingMsgId.value = null
      clearInterval(timer)
      scrollToBottom()
    } else {
      msg._displayContent = fullContent.substring(0, idx)
      scrollToBottom()
    }
  }, interval)
}

const sendMessage = async (text: string) => {
  if (!text.trim() || loading.value) return
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  scrollToBottom()
  try {
    const res: any = await aiChatSession({
      sessionId: currentSessionId.value || undefined,
      message: text
    })
    const data = res.data
    if (!currentSessionId.value && data?.content) {
      await loadSessions()
      if (sessions.value.length > 0) currentSessionId.value = sessions.value[0].id
    }
    const assistantMsg: Message = {
      role: 'assistant',
      content: data?.content || '抱歉，暂时无法回答'
    }
    messages.value.push(assistantMsg)
    nextTick(() => typewriterEffect(assistantMsg, assistantMsg.content))
  } catch (e: any) {
    const errMsg: Message = { role: 'assistant', content: '请求失败：' + (e?.message || '请检查AI配置') }
    messages.value.push(errMsg)
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

const copyMessage = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}

const handleSend = () => sendMessage(input.value)
</script>

<style scoped lang="scss">
.chat-page {
  height: calc(100vh - 120px);
  padding: 0 !important;
}
.chat-layout {
  display: flex;
  height: 100%;
  background: var(--apple-card-bg, #fff);
  border-radius: 16px;
  border: 1px solid var(--apple-border, #e8e8e8);
  overflow: hidden;
}
.session-sidebar {
  width: 260px;
  border-right: 1px solid var(--apple-border, #e8e8e8);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--apple-border, #e8e8e8);
  h3 { margin: 0; font-size: 15px; color: var(--apple-text-primary, #1d1d1f); }
}
.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}
.session-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  &:hover { background: var(--apple-bg-secondary, #f5f5f7); }
  &.active { background: rgba(0, 122, 255, 0.08); .session-title { color: var(--apple-blue, #007aff); } }
}
.session-icon { color: var(--apple-text-secondary, #8e8e93); flex-shrink: 0; }
.session-info { flex: 1; min-width: 0; }
.session-title {
  font-size: 13px;
  color: var(--apple-text-primary, #1d1d1f);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.session-time { font-size: 11px; color: var(--apple-text-secondary, #8e8e93); margin-top: 2px; }
.session-delete {
  opacity: 0;
  transition: opacity 0.2s;
  color: var(--apple-text-secondary, #8e8e93);
  &:hover { color: var(--apple-red, #ff3b30); }
}
.session-item:hover .session-delete { opacity: 1; }
.session-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--apple-text-secondary, #8e8e93);
  p { margin: 8px 0 0; font-size: 13px; }
}
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--apple-border, #e8e8e8);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chat-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  h2 { margin: 0; font-size: 16px; color: var(--apple-text-primary, #1d1d1f); }
}
.chat-provider { margin: 2px 0 0; font-size: 12px; color: var(--apple-text-secondary, #8e8e93); }
.page-header-icon.purple { background: rgba(175, 82, 222, 0.1); color: #af52de; border-radius: 10px; width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; }
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--apple-text-secondary, #8e8e93);
}
.chat-empty-icon { font-size: 48px; margin-bottom: 12px; }
.chat-empty h3 { color: var(--apple-text-primary, #1d1d1f); margin: 0 0 8px; }
.suggestion-chips { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 12px; }
.chip {
  padding: 6px 14px; border-radius: 20px; font-size: 13px; cursor: pointer;
  background: var(--apple-bg-secondary, #f5f5f7); color: var(--apple-text-primary, #1d1d1f);
  border: 1px solid var(--apple-border, #e8e8e8); transition: all 0.2s;
  &:hover { background: var(--apple-blue, #007aff); color: #fff; border-color: var(--apple-blue); }
}
.chat-msg {
  display: flex;
  gap: 10px;
  max-width: 75%;
  &.user { align-self: flex-end; flex-direction: row-reverse; }
  &.assistant { align-self: flex-start; }
}
.msg-avatar-wrap { flex-shrink: 0; margin-top: 2px; }
.msg-avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}
.msg-avatar-text {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--apple-blue, #007aff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}
.msg-avatar-ai {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #af52de, #5856d6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}
.msg-content { position: relative; display: flex; flex-direction: column; }
.msg-copy {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: none;
  border: none;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
  color: var(--apple-text-secondary, #8e8e93);
  padding: 2px 4px;
  border-radius: 4px;
  &:hover { background: rgba(0,0,0,0.05); color: var(--apple-blue, #007aff); }
}
.msg-content:hover .msg-copy { opacity: 1; }
.msg-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  line-height: 1.7;
  font-size: 14px;
  word-break: break-word;
}
.user .msg-bubble { background: var(--apple-blue, #007aff); color: #fff; border-bottom-right-radius: 4px; }
.assistant .msg-bubble {
  background: var(--apple-bg-secondary, #f5f5f7);
  color: var(--apple-text-primary, #1d1d1f);
  border-bottom-left-radius: 4px;
}
.msg-time { font-size: 11px; color: var(--apple-text-secondary, #8e8e93); margin-top: 4px; }
.user .msg-time { text-align: right; }
.typing { display: flex; gap: 4px; padding: 16px 20px; span { width: 8px; height: 8px; border-radius: 50%; background: #999; animation: typing 1.4s infinite; &:nth-child(2) { animation-delay: 0.2s; } &:nth-child(3) { animation-delay: 0.4s; } } }
@keyframes typing { 0%, 60%, 100% { opacity: 0.3; transform: translateY(0); } 30% { opacity: 1; transform: translateY(-4px); } }
.chat-input { padding: 16px 20px; border-top: 1px solid var(--apple-border, #e8e8e8); }
.input-row { display: flex; gap: 10px; }
</style>
