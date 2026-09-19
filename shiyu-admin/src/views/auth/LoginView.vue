<template>
  <div class="login-page">
    <!-- Left visual panel -->
    <div class="left-panel">
      <div class="left-content">
        <div class="brand">
          <div class="brand-icon">
            <svg viewBox="0 0 48 48" fill="none">
              <rect width="48" height="48" rx="14" fill="rgba(255,255,255,0.2)"/>
              <path d="M24 11L12 17.5L24 24L36 17.5L24 11Z" fill="#fff" opacity=".9"/>
              <path d="M12 25.5l12 6.5 12-6.5" stroke="#fff" stroke-width="2" stroke-linecap="round" opacity=".6"/>
              <path d="M12 31l12 6.5 12-6.5" stroke="#fff" stroke-width="2" stroke-linecap="round" opacity=".35"/>
            </svg>
          </div>
          <h1>食遇</h1>
          <p>情侣菜谱点餐管理平台</p>
        </div>

        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25"/></svg>
            </div>
            <div>
              <h3>智能菜谱管理</h3>
              <p>113+ 精选菜谱，图文步骤一键管理</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z"/></svg>
            </div>
            <div>
              <h3>多人协作点餐</h3>
              <p>情侣双方实时下单，厨师接单制作</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3.75 3v11.25A2.25 2.25 0 006 16.5h2.25M3.75 3h-1.5m1.5 0h16.5m0 0h1.5m-1.5 0v11.25A2.25 2.25 0 0118 16.5h-2.25m-7.5 0h7.5m-7.5 0l-1 3m8.5-3l1 3m0 0l.5 1.5m-.5-1.5h-9.5m0 0l-.5 1.5"/></svg>
            </div>
            <div>
              <h3>数据统计分析</h3>
              <p>订单趋势、菜品排行一目了然</p>
            </div>
          </div>
        </div>

        <div class="left-footer">
          <p>基于 Spring Boot + Vue 3 构建</p>
        </div>
      </div>

      <!-- Decorative circles -->
      <div class="deco deco-1"></div>
      <div class="deco deco-2"></div>
      <div class="deco deco-3"></div>
    </div>

    <!-- Right form panel -->
    <div class="right-panel">
      <transition name="view-switch" mode="out-in">
        <!-- Login -->
        <form v-if="view === 'login'" key="login" class="form-card" @submit.prevent="doLogin">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p>登录你的账号以继续</p>
          </div>

          <div class="field">
            <label>用户名</label>
            <div class="input-wrap">
              <svg class="input-icon" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/></svg>
              <input v-model="lf.username" placeholder="请输入用户名" />
            </div>
            <em v-if="le.username">{{ le.username }}</em>
          </div>

          <div class="field">
            <label>密码</label>
            <div class="input-wrap">
              <svg class="input-icon" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd"/></svg>
              <input v-model="lf.password" :type="showP ? 'text' : 'password'" placeholder="请输入密码" />
              <button type="button" class="eye" @click="showP = !showP">
                <svg v-if="!showP" viewBox="0 0 20 20" fill="currentColor"><path d="M10 12a2 2 0 100-4 2 2 0 000 4z"/><path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"/></svg>
                <svg v-else viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" clip-rule="evenodd"/><path d="M12.454 16.697L9.75 13.992a4 4 0 01-3.742-3.741L2.335 6.578A9.98 9.98 0 00.458 10c1.274 4.057 5.065 7 9.542 7 .847 0 1.669-.105 2.454-.303z"/></svg>
              </button>
            </div>
            <em v-if="le.password">{{ le.password }}</em>
          </div>

          <div class="row-between">
            <span></span>
            <a class="link-sm" @click="view = 'forgot'">忘记密码？</a>
          </div>

          <button class="btn-primary" type="submit" :disabled="loading">
            <span v-if="loading" class="spin"></span>
            <span v-else>登 录</span>
          </button>

          <p class="alt-hint">还没有账号？<a @click="view = 'register'">立即注册</a></p>
        </form>

        <!-- Register -->
        <form v-else-if="view === 'register'" key="reg" class="form-card" @submit.prevent="doRegister">
          <div class="form-header">
            <h2>创建账号</h2>
            <p>注册一个新的食遇账号</p>
          </div>

          <div class="field">
            <label>用户名</label>
            <div class="input-wrap">
              <svg class="input-icon" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/></svg>
              <input v-model="rf.username" placeholder="设置用户名" />
            </div>
            <em v-if="re.username">{{ re.username }}</em>
          </div>

          <div class="field">
            <label>密码</label>
            <div class="input-wrap">
              <svg class="input-icon" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd"/></svg>
              <input v-model="rf.password" :type="showRP ? 'text' : 'password'" placeholder="至少6位" />
              <button type="button" class="eye" @click="showRP = !showRP">
                <svg v-if="!showRP" viewBox="0 0 20 20" fill="currentColor"><path d="M10 12a2 2 0 100-4 2 2 0 000 4z"/><path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"/></svg>
                <svg v-else viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" clip-rule="evenodd"/><path d="M12.454 16.697L9.75 13.992a4 4 0 01-3.742-3.741L2.335 6.578A9.98 9.98 0 00.458 10c1.274 4.057 5.065 7 9.542 7 .847 0 1.669-.105 2.454-.303z"/></svg>
              </button>
            </div>
            <em v-if="re.password">{{ re.password }}</em>
          </div>

          <div class="field">
            <label>昵称</label>
            <div class="input-wrap">
              <svg class="input-icon" viewBox="0 0 20 20" fill="currentColor"><path d="M17.982 18.725A7.488 7.488 0 0012 15.75a7.488 7.488 0 00-5.982 2.975m11.963 0a9 9 0 10-11.963 0m11.963 0A8.966 8.966 0 0112 21a8.966 8.966 0 01-5.982-2.275M15 9.75a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
              <input v-model="rf.nickname" placeholder="给自己取个昵称" />
            </div>
            <em v-if="re.nickname">{{ re.nickname }}</em>
          </div>

          <button class="btn-primary" type="submit" :disabled="regLoading">
            <span v-if="regLoading" class="spin"></span>
            <span v-else>注 册</span>
          </button>

          <p class="alt-hint">已有账号？<a @click="view = 'login'">返回登录</a></p>
        </form>

        <!-- Forgot -->
        <form v-else key="forgot" class="form-card" @submit.prevent="doReset">
          <div class="form-header">
            <h2>重置密码</h2>
            <p>输入用户名和新密码</p>
          </div>

          <div class="field">
            <label>用户名</label>
            <div class="input-wrap">
              <svg class="input-icon" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/></svg>
              <input v-model="ff.username" placeholder="输入注册用户名" />
            </div>
            <em v-if="fe.username">{{ fe.username }}</em>
          </div>

          <div class="field">
            <label>新密码</label>
            <div class="input-wrap">
              <svg class="input-icon" viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd"/></svg>
              <input v-model="ff.newPassword" :type="showFP ? 'text' : 'password'" placeholder="设置新密码（至少6位）" />
              <button type="button" class="eye" @click="showFP = !showFP">
                <svg v-if="!showFP" viewBox="0 0 20 20" fill="currentColor"><path d="M10 12a2 2 0 100-4 2 2 0 000 4z"/><path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"/></svg>
                <svg v-else viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" clip-rule="evenodd"/><path d="M12.454 16.697L9.75 13.992a4 4 0 01-3.742-3.741L2.335 6.578A9.98 9.98 0 00.458 10c1.274 4.057 5.065 7 9.542 7 .847 0 1.669-.105 2.454-.303z"/></svg>
              </button>
            </div>
            <em v-if="fe.newPassword">{{ fe.newPassword }}</em>
          </div>

          <button class="btn-primary" type="submit" :disabled="resetLoading">
            <span v-if="resetLoading" class="spin"></span>
            <span v-else>重置密码</span>
          </button>

          <p class="alt-hint">想起密码了？<a @click="view = 'login'">返回登录</a></p>
        </form>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { register, resetPassword } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const view = ref<'login' | 'register' | 'forgot'>('login')
const loading = ref(false)
const regLoading = ref(false)
const resetLoading = ref(false)
const showP = ref(false)
const showRP = ref(false)
const showFP = ref(false)

const lf = reactive({ username: '', password: '' })
const le = reactive({ username: '', password: '' })
const rf = reactive({ username: '', password: '', nickname: '' })
const re = reactive({ username: '', password: '', nickname: '' })
const ff = reactive({ username: '', newPassword: '' })
const fe = reactive({ username: '', newPassword: '' })

function clearErr() {
  Object.assign(le, { username: '', password: '' })
  Object.assign(re, { username: '', password: '', nickname: '' })
  Object.assign(fe, { username: '', newPassword: '' })
}

function vLogin(): boolean {
  let ok = true
  le.username = ''; le.password = ''
  if (!lf.username.trim()) { le.username = '请输入用户名'; ok = false }
  if (!lf.password) { le.password = '请输入密码'; ok = false }
  return ok
}
async function doLogin() {
  if (!vLogin()) return
  loading.value = true
  try {
    await authStore.login(lf.username, lf.password)
    ElMessage.success('欢迎回来！')
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } catch (e: any) {
    ElMessage.error(e.message || '登录失败')
  } finally { loading.value = false }
}

function vRegister(): boolean {
  let ok = true
  re.username = ''; re.password = ''; re.nickname = ''
  if (!rf.username.trim()) { re.username = '请输入用户名'; ok = false }
  if (!rf.password || rf.password.length < 6) { re.password = '密码至少需要6位'; ok = false }
  if (!rf.nickname.trim()) { re.nickname = '请输入昵称'; ok = false }
  return ok
}
async function doRegister() {
  if (!vRegister()) return
  regLoading.value = true
  try {
    await register({ username: rf.username, password: rf.password, nickname: rf.nickname })
    ElMessage.success('注册成功！请登录')
    view.value = 'login'
    lf.username = rf.username
    lf.password = ''
    rf.username = ''; rf.password = ''; rf.nickname = ''
  } catch (e: any) {
    ElMessage.error(e.message || '注册失败')
  } finally { regLoading.value = false }
}

function vForgot(): boolean {
  let ok = true
  fe.username = ''; fe.newPassword = ''
  if (!ff.username.trim()) { fe.username = '请输入用户名'; ok = false }
  if (!ff.newPassword || ff.newPassword.length < 6) { fe.newPassword = '密码至少需要6位'; ok = false }
  return ok
}
async function doReset() {
  if (!vForgot()) return
  resetLoading.value = true
  try {
    await resetPassword({ username: ff.username, newPassword: ff.newPassword })
    ElMessage.success('密码已重置，请使用新密码登录')
    view.value = 'login'
    lf.username = ff.username
    lf.password = ''
    ff.username = ''; ff.newPassword = ''
  } catch (e: any) {
    ElMessage.error(e.message || '重置失败')
  } finally { resetLoading.value = false }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
}

/* ═══ Left Panel ═══ */
.left-panel {
  flex: 1;
  background: linear-gradient(135deg, #007AFF 0%, #5856D6 50%, #AF52DE 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 60px;

  @media (max-width: 900px) {
    display: none;
  }
}

.left-content {
  position: relative;
  z-index: 2;
  max-width: 440px;
  color: #fff;
}

.brand {
  margin-bottom: 56px;

  .brand-icon {
    display: inline-flex;
    margin-bottom: 20px;
    svg { width: 56px; height: 56px; }
  }

  h1 {
    font-size: 36px;
    font-weight: 700;
    letter-spacing: 2px;
    margin-bottom: 8px;
  }

  p {
    font-size: 16px;
    opacity: 0.7;
    font-weight: 400;
  }
}

.features {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;

  .feature-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.15);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    backdrop-filter: blur(8px);
    svg { width: 20px; height: 20px; }
  }

  h3 {
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 3px;
  }

  p {
    font-size: 13px;
    opacity: 0.65;
    line-height: 1.4;
  }
}

.left-footer {
  margin-top: 56px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  p { font-size: 12px; opacity: 0.4; }
}

/* Decorative circles */
.deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  animation: float 20s ease-in-out infinite;
}

.deco-1 {
  width: 300px;
  height: 300px;
  top: -80px;
  right: -60px;
  animation-delay: 0s;
}

.deco-2 {
  width: 200px;
  height: 200px;
  bottom: -40px;
  left: -40px;
  animation-delay: -7s;
}

.deco-3 {
  width: 120px;
  height: 120px;
  top: 40%;
  right: 20%;
  animation-delay: -14s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25% { transform: translate(15px, -20px) scale(1.05); }
  50% { transform: translate(-10px, 15px) scale(0.95); }
  75% { transform: translate(20px, 10px) scale(1.02); }
}

/* ═══ Right Panel ═══ */
.right-panel {
  width: 480px;
  min-width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-card);
  padding: 48px;
  position: relative;

  @media (max-width: 900px) {
    width: 100%;
    min-width: 0;
  }
}

.form-card {
  width: 100%;
  max-width: 360px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-header {
  margin-bottom: 8px;

  h2 {
    font-size: 26px;
    font-weight: 600;
    color: var(--text-primary);
    letter-spacing: -0.5px;
    margin-bottom: 6px;
  }

  p {
    font-size: 14px;
    color: var(--text-tertiary);
  }
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;

  label {
    font-size: 13px;
    font-weight: 500;
    color: var(--text-primary);
  }

  em {
    font-size: 12px;
    color: var(--apple-red);
    font-style: normal;
    padding-left: 2px;
  }
}

.input-wrap {
  display: flex;
  align-items: center;
  height: 44px;
  background: var(--bg-page);
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-sm);
  padding: 0 14px;
  transition: all 0.2s var(--ease-default);

  &:hover {
    border-color: var(--text-quaternary);
  }

  &:focus-within {
    border-color: var(--apple-blue);
    background: #fff;
    box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.08);
  }

  .input-icon {
    width: 16px;
    height: 16px;
    color: var(--text-quaternary);
    margin-right: 10px;
    flex-shrink: 0;
  }

  input {
    flex: 1;
    background: none;
    border: none;
    outline: none;
    color: var(--text-primary);
    font-size: 14px;
    height: 100%;
    &::placeholder { color: var(--text-quaternary); }
  }
}

.eye {
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  color: var(--text-tertiary);
  display: flex;
  transition: color 0.15s;
  &:hover { color: var(--text-secondary); }
  svg { width: 16px; height: 16px; }
}

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.link-sm {
  font-size: 13px;
  color: var(--apple-blue);
  cursor: pointer;
  text-decoration: none;
  &:hover { text-decoration: underline; }
}

.btn-primary {
  width: 100%;
  height: 46px;
  background: var(--apple-blue);
  border: none;
  border-radius: var(--radius-sm);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s var(--ease-default);
  margin-top: 4px;
  letter-spacing: 1px;

  &:hover:not(:disabled) {
    background: var(--apple-blue-hover);
    box-shadow: 0 4px 16px rgba(0, 122, 255, 0.3);
    transform: translateY(-1px);
  }

  &:active:not(:disabled) {
    transform: scale(0.98) translateY(0);
    box-shadow: none;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.spin {
  display: inline-block;
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.alt-hint {
  text-align: center;
  font-size: 13px;
  color: var(--text-tertiary);
  margin-top: 4px;
  a {
    color: var(--apple-blue);
    cursor: pointer;
    text-decoration: none;
    font-weight: 500;
    &:hover { text-decoration: underline; }
  }
}

/* View transition */
.view-switch-enter-active { transition: all 0.3s var(--ease-default); }
.view-switch-leave-active { transition: all 0.15s ease; }
.view-switch-enter-from { opacity: 0; transform: translateY(8px); }
.view-switch-leave-to { opacity: 0; transform: translateY(-8px); }
</style>
