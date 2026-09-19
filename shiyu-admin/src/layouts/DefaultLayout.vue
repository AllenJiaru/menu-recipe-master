<template>
  <div class="layout">
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <!-- Logo -->
      <div class="sidebar-logo" @click="router.push('/dashboard')">
        <div class="logo-icon">
          <svg viewBox="0 0 36 36" fill="none">
            <defs>
              <linearGradient id="logoGrad" x1="0" y1="0" x2="36" y2="36">
                <stop offset="0%" stop-color="#007AFF"/>
                <stop offset="100%" stop-color="#5856D6"/>
              </linearGradient>
            </defs>
            <rect width="36" height="36" rx="10" fill="url(#logoGrad)"/>
            <path d="M18 8L9 13L18 18L27 13L18 8Z" fill="#fff" opacity=".9"/>
            <path d="M9 19.5l9 5 9-5" stroke="#fff" stroke-width="1.5" stroke-linecap="round" opacity=".6"/>
            <path d="M9 23.5l9 5 9-5" stroke="#fff" stroke-width="1.5" stroke-linecap="round" opacity=".35"/>
          </svg>
        </div>
        <transition name="fade">
          <div v-if="!sidebarCollapsed" class="logo-text">
            <span class="logo-name">食遇</span>
            <span class="logo-desc">管理平台</span>
          </div>
        </transition>
      </div>

      <!-- Navigation -->
      <nav v-if="authStore.permissionsLoaded" class="sidebar-nav">
        <div v-for="group in menuGroups" :key="group.key" class="nav-group">
          <!-- Collapsed: show icons only -->
          <template v-if="sidebarCollapsed">
            <router-link v-for="item in group.items" :key="item.path" :to="item.path" class="nav-item-icon" :class="{ active: isItemActive(item.path) }" :title="item.title">
              <el-icon :size="18"><component :is="item.icon" /></el-icon>
            </router-link>
          </template>

          <!-- Expanded: group header + items -->
          <template v-else>
            <div class="group-header" :class="{ open: isGroupExpanded(group.key) }" @click="toggleGroup(group.key)">
              <el-icon :size="15" class="group-icon"><component :is="group.icon" /></el-icon>
              <span class="group-title">{{ group.title }}</span>
              <span class="group-count">{{ group.items.length }}</span>
              <el-icon :size="12" class="group-arrow"><ArrowDown /></el-icon>
            </div>
            <transition name="slide">
              <div v-show="isGroupExpanded(group.key)" class="group-items">
                <router-link v-for="item in group.items" :key="item.path" :to="item.path" class="nav-item" :class="{ active: isItemActive(item.path) }">
                  <el-icon :size="16"><component :is="item.icon" /></el-icon>
                  <span class="nav-label">{{ item.title }}</span>
                  <span v-if="item.badge" class="nav-badge">{{ item.badge > 99 ? '99+' : item.badge }}</span>
                </router-link>
              </div>
            </transition>
          </template>
        </div>
      </nav>

      <!-- Footer -->
      <div class="sidebar-footer">
        <div class="nav-item-icon collapse-btn" @click="appStore.toggleSidebar">
          <el-icon :size="18"><Fold v-if="!sidebarCollapsed" /><Expand v-else /></el-icon>
        </div>
      </div>
    </aside>

    <div class="main-wrapper">
      <header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">{{ item.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <div class="header-action" @click="toggleFullscreen" title="全屏">
            <el-icon :size="16"><FullScreen /></el-icon>
          </div>
          <div class="header-action" @click="themeStore.toggleTheme" :title="themeStore.isDark ? '浅色模式' : '深色模式'">
            <el-icon :size="16"><Sunny v-if="themeStore.isDark" /><Moon v-else /></el-icon>
          </div>
          <div class="header-action bell" title="通知">
            <el-badge :value="pendingBadge" :hidden="!pendingBadge" :max="99"><el-icon :size="16"><Bell /></el-icon></el-badge>
          </div>
          <div class="header-divider"></div>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-profile">
              <div class="user-avatar-wrap">
                <img v-if="userInfo?.avatar" :src="imgUrl(userInfo.avatar)" class="user-avatar-img" />
                <div v-else class="user-avatar-text">{{ (userInfo?.nickname || userInfo?.username || 'A').charAt(0) }}</div>
              </div>
              <div class="user-info">
                <div class="user-name">{{ userInfo?.nickname || userInfo?.username || 'Admin' }}</div>
                <div class="user-role">{{ roleMap[userInfo?.role || 'admin'] }}</div>
              </div>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item icon="User" command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item icon="Setting" command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout" icon="SwitchButton">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { imgUrl, roleMap } from '@/utils/format'
import { getOrders } from '@/api/order'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const userInfo = computed(() => authStore.userInfo)

const pendingBadge = ref(0)

const expandedGroups = ref<string[]>(
  JSON.parse(localStorage.getItem('sidebar-expanded') || '["overview","recipe","order"]')
)

const toggleGroup = (key: string) => {
  const idx = expandedGroups.value.indexOf(key)
  if (idx >= 0) expandedGroups.value.splice(idx, 1)
  else expandedGroups.value.push(key)
  localStorage.setItem('sidebar-expanded', JSON.stringify(expandedGroups.value))
}

const isGroupExpanded = (key: string) => {
  if (sidebarCollapsed.value) return false
  return expandedGroups.value.includes(key)
}

interface MenuItem { path: string; title: string; icon: string; badge?: number }
interface MenuGroup { key: string; title: string; icon: string; items: MenuItem[] }

const menuGroups = computed<MenuGroup[]>(() => {
  const allGroups: MenuGroup[] = [
    {
      key: 'overview', title: '概览', icon: 'Odometer',
      items: [
        { path: '/dashboard', title: '仪表盘', icon: 'Odometer' },
      ]
    },
    {
      key: 'recipe', title: '菜谱中心', icon: 'Notebook',
      items: [
        { path: '/recipes', title: '菜谱管理', icon: 'Notebook' },
        { path: '/categories', title: '分类管理', icon: 'Grid' },
        { path: '/reviews', title: '菜谱审核', icon: 'CircleCheck' },
      ]
    },
    {
      key: 'order', title: '订单与库存', icon: 'ShoppingCart',
      items: [
        { path: '/orders', title: '订单管理', icon: 'ShoppingCart', badge: pendingBadge.value || undefined },
        { path: '/inventory', title: '库存管理', icon: 'Box' },
        { path: '/suppliers', title: '供应商管理', icon: 'Van' },
      ]
    },
    {
      key: 'planning', title: '智能规划', icon: 'MagicStick',
      items: [
        { path: '/meal-plans', title: '周菜谱规划', icon: 'Calendar' },
        { path: '/shopping', title: '采购清单', icon: 'ShoppingBag' },
      ]
    },
    {
      key: 'space', title: '我的空间', icon: 'User',
      items: [
        { path: '/favorites', title: '我的收藏', icon: 'Star' },
        { path: '/gallery', title: '美食相册', icon: 'Picture' },
        { path: '/couples', title: '情侣空间', icon: 'Connection' },
      ]
    },
    {
      key: 'analytics', title: '数据分析', icon: 'DataAnalysis',
      items: [
        { path: '/statistics', title: '数据统计', icon: 'TrendCharts' },
        { path: '/reports', title: '报表分析', icon: 'DataLine' },
        { path: '/export', title: '数据导出', icon: 'Download' },
      ]
    },
    {
      key: 'system', title: '系统管理', icon: 'Setting',
      items: [
        { path: '/organizations', title: '组织管理', icon: 'OfficeBuilding' },
        { path: '/users', title: '用户管理', icon: 'User' },
        { path: '/roles', title: '角色管理', icon: 'UserFilled' },
        { path: '/permissions', title: '权限管理', icon: 'Lock' },
        { path: '/notices', title: '公告管理', icon: 'Bell' },
        { path: '/settings', title: '系统设置', icon: 'Setting' },
      ]
    },
    {
      key: 'ai', title: 'AI 智能', icon: 'MagicStick',
      items: [
        { path: '/ai', title: 'AI 助手', icon: 'ChatDotRound' },
        { path: '/ai/recommend', title: '智能推荐', icon: 'Sunny' },
        { path: '/ai/ingredient', title: '食材变菜谱', icon: 'Food' },
        { path: '/ai/nutrition', title: '营养分析', icon: 'DataAnalysis' },
        { path: '/ai/meal-plan', title: 'AI 周菜谱', icon: 'Calendar' },
        { path: '/ai/recognize', title: '菜品识别', icon: 'Camera' },
        { path: '/ai/shopping', title: '智能购物', icon: 'ShoppingBag' },
        { path: '/ai/leftover', title: '剩菜妙招', icon: 'Bowl' },
        { path: '/ai/score', title: 'AI 评分', icon: 'Star' },
        { path: '/ai/cooking-qa', title: '烹饪问答', icon: 'ChatLineRound' },
        { path: '/ai/health', title: '健康报告', icon: 'FirstAidKit' },
        { path: '/ai/translate', title: '菜谱翻译', icon: 'EditPen' },
        { path: '/ai/settings', title: 'AI 设置', icon: 'Setting' },
      ]
    },
    {
      key: 'monitor', title: '系统监控', icon: 'Monitor',
      items: [
        { path: '/logs', title: '操作日志', icon: 'Document' },
        { path: '/sync-logs', title: '同步日志', icon: 'Refresh' },
        { path: '/health', title: '系统健康', icon: 'Monitor' },
      ]
    }
  ]

  const role = authStore.userInfo?.role
  if (role === 'super_admin' || role === 'admin') return allGroups

  return allGroups.map(group => ({
    ...group,
    items: group.items.filter(item => {
      const code = pathToPermissionCode[item.path]
      return !code || authStore.hasPermission(code)
    })
  })).filter(group => group.items.length > 0)
})

const isItemActive = (path: string) => {
  if (path === '/dashboard') return route.path === '/dashboard'
  if (path === '/ai') return route.path === '/ai'
  return route.path === path || route.path.startsWith(path + '/')
}

const pathToPermissionCode: Record<string, string> = {
  '/dashboard': 'overview:dashboard',
  '/recipes': 'content:recipe',
  '/categories': 'content:category',
  '/reviews': 'content:review',
  '/orders': 'business:order',
  '/inventory': 'business:inventory',
  '/suppliers': 'business:supplier',
  '/meal-plans': 'business:mealplan',
  '/shopping': 'business:shopping',
  '/favorites': 'content:favorite',
  '/gallery': 'content:gallery',
  '/organizations': 'business:organization',
  '/couples': 'business:couple',
  '/statistics': 'system:statistics',
  '/reports': 'system:reports',
  '/export': 'system:export',
  '/users': 'system:user',
  '/roles': 'system:role',
  '/permissions': 'system:permission',
  '/notices': 'system:notice',
  '/settings': 'logs:settings',
  '/logs': 'logs:operation',
  '/sync-logs': 'logs:sync',
  '/health': 'logs:health'
}
const breadcrumbs = computed(() => route.matched.filter(r => r.meta?.title).map(r => ({ path: r.path, title: r.meta.title as string })))

const toggleFullscreen = () => {
  try {
    if (!document.fullscreenElement) document.documentElement.requestFullscreen()
    else document.exitFullscreen()
  } catch { /* unsupported */ }
}

const handleCommand = (cmd: string) => {
  if (cmd === 'logout') { authStore.logout(); router.push('/login') }
  else if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'settings') router.push('/settings')
}

async function fetchPendingCount() {
  try {
    const res: any = await getOrders({ status: 0, page: 1, size: 1 })
    pendingBadge.value = res.data?.total || 0
  } catch { /* silent */ }
}

onMounted(async () => {
  if (!authStore.userInfo) {
    try { await authStore.getUserInfo() } catch { router.push('/login'); return }
  }
  if (authStore.permissionCodes.length === 0) {
    await authStore.loadPermissions()
  }
  fetchPendingCount()
})
</script>

<style scoped lang="scss">
.layout { display: flex; height: 100vh; overflow: hidden; }

/* ═══ Sidebar ═══ */
.sidebar {
  width: 220px;
  background: var(--bg-card);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: width 0.28s var(--ease-default);
  position: relative;
  z-index: 100;
  flex-shrink: 0;

  &.collapsed { width: 60px; }
}

/* ── Logo ── */
.sidebar-logo {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 18px;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.15s;
  border-bottom: 1px solid var(--border-light);

  &:hover { background: var(--bg-hover); }

  .logo-icon {
    width: 34px;
    height: 34px;
    flex-shrink: 0;
    svg { width: 100%; height: 100%; }
  }
}

.logo-text {
  display: flex;
  flex-direction: column;
  margin-left: 10px;
  line-height: 1.2;
}

.logo-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.5px;
}

.logo-desc {
  font-size: 10px;
  color: var(--text-tertiary);
  margin-top: 1px;
  letter-spacing: 0.3px;
}

/* ── Nav ── */
.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 10px 10px;
}

.nav-group {
  margin-bottom: 2px;
}

/* ── Group Header (expandable) ── */
.group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  border-radius: var(--radius-sm);
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all 0.15s var(--ease-default);
  user-select: none;

  &:hover {
    color: var(--text-secondary);
    background: var(--bg-hover);
  }

  &.open {
    color: var(--text-secondary);
  }
}

.group-icon {
  flex-shrink: 0;
  transition: color 0.15s;

  .group-header:hover & { color: var(--text-secondary); }
  .group-header.open & { color: var(--text-primary); }
}

.group-title {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.3px;
  white-space: nowrap;
  flex: 1;
}

.group-count {
  font-size: 10px;
  color: var(--text-quaternary);
  background: var(--bg-page);
  padding: 0 5px;
  border-radius: var(--radius-full);
  line-height: 1.6;
  font-weight: 500;
}

.group-arrow {
  transition: transform 0.2s var(--ease-default);
  flex-shrink: 0;

  .group-header.open & {
    transform: rotate(0deg);
  }
  .group-header:not(.open) & {
    transform: rotate(-90deg);
  }
}

.group-items {
  overflow: hidden;
  padding-left: 4px;
}

/* Slide transition */
.slide-enter-active {
  transition: all 0.25s var(--ease-default);
  max-height: 500px;
  opacity: 1;
}
.slide-leave-active {
  transition: all 0.2s var(--ease-default);
  max-height: 500px;
  opacity: 1;
}
.slide-enter-from {
  max-height: 0;
  opacity: 0;
}
.slide-leave-to {
  max-height: 0;
  opacity: 0;
}

/* ── Nav Item (expanded) ── */
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s var(--ease-default);
  margin-bottom: 2px;
  position: relative;
  text-decoration: none;
  font-size: 13.5px;
  font-weight: 400;

  .el-icon { flex-shrink: 0; color: var(--text-tertiary); transition: color 0.15s; }

  &:hover {
    color: var(--text-primary);
    background: var(--bg-hover);
    .el-icon { color: var(--text-secondary); }
  }

  &.active {
    color: var(--apple-blue);
    background: rgba(0, 122, 255, 0.08);
    font-weight: 500;

    .el-icon { color: var(--apple-blue); }

    &::before {
      content: '';
      position: absolute;
      left: -10px;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 18px;
      border-radius: 0 3px 3px 0;
      background: var(--apple-blue);
    }
  }
}

/* ── Nav Item (collapsed) ── */
.nav-item-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 36px;
  border-radius: var(--radius-sm);
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all 0.15s var(--ease-default);
  margin: 0 auto 2px;
  position: relative;

  &:hover {
    color: var(--text-primary);
    background: var(--bg-hover);
  }

  &.active {
    color: var(--apple-blue);
    background: rgba(0, 122, 255, 0.08);

    &::before {
      content: '';
      position: absolute;
      left: -10px;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 18px;
      border-radius: 0 3px 3px 0;
      background: var(--apple-blue);
    }
  }
}

.nav-label {
  font-size: 13.5px;
  font-weight: 400;
  white-space: nowrap;
  flex: 1;
}

.nav-badge {
  margin-left: auto;
  background: var(--apple-red);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: var(--radius-full);
  min-width: 18px;
  text-align: center;
  line-height: 1.4;
}

/* ── Footer ── */
.sidebar-footer {
  padding: 10px;
  border-top: 1px solid var(--border-light);
  display: flex;
  justify-content: center;
  flex-shrink: 0;
}

.collapse-btn {
  width: 36px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    color: var(--text-secondary);
    background: var(--bg-hover);
  }
}

/* ═══ Main ═══ */
.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow-y: auto;
  position: relative;
}

.header {
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 50;
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  background: rgba(255, 255, 255, 0.72);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.header-left { display: flex; align-items: center; gap: 16px; }
.header-right { display: flex; align-items: center; gap: 4px; }

.header-action {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--text-tertiary);
  transition: all 0.15s var(--ease-default);

  &:hover {
    background: var(--bg-hover);
    color: var(--text-primary);
  }
}

.bell { position: relative; }

.header-divider {
  width: 1px;
  height: 20px;
  background: var(--border-color);
  margin: 0 8px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.15s var(--ease-default);

  &:hover { background: var(--bg-hover); }
}

.user-avatar-wrap {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #007AFF, #5856D6);
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.25);
}

.user-avatar-img { width: 100%; height: 100%; object-fit: cover; }

.user-avatar-text {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 12px;
}

.user-info { line-height: 1.3; }
.user-name { font-size: 13px; font-weight: 500; color: var(--text-primary); }
.user-role { font-size: 11px; color: var(--text-tertiary); }
.arrow { font-size: 10px; color: var(--text-tertiary); margin-left: 2px; }

.main-content {
  flex: 1;
  padding: 24px;
  background: var(--bg-page);
}

/* Transitions */
.fade-enter-active, .fade-leave-active { transition: opacity 0.15s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.page-enter-active { animation: pageIn 0.3s var(--ease-default); }
.page-leave-active { animation: pageOut 0.15s ease-in reverse; }

@keyframes pageIn { from { opacity: 0; transform: translateY(6px); } to { opacity: 1; transform: translateY(0); } }
@keyframes pageOut { from { opacity: 1; } to { opacity: 0; } }
</style>
