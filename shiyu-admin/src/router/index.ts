import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/DashboardView.vue'), meta: { title: '仪表盘', icon: 'Odometer' } },
      { path: 'recipes', name: 'RecipeList', component: () => import('@/views/recipe/RecipeListView.vue'), meta: { title: '菜谱管理', icon: 'Notebook' } },
      { path: 'recipes/create', name: 'RecipeCreate', component: () => import('@/views/recipe/RecipeFormView.vue'), meta: { title: '新建菜谱', hidden: true } },
      { path: 'recipes/:id/edit', name: 'RecipeEdit', component: () => import('@/views/recipe/RecipeFormView.vue'), meta: { title: '编辑菜谱', hidden: true } },
      { path: 'recipes/:id', name: 'RecipeDetail', component: () => import('@/views/recipe/RecipeDetailView.vue'), meta: { title: '菜谱详情', hidden: true } },
      { path: 'recipes/:id/print', name: 'RecipePrint', component: () => import('@/views/recipe/RecipePrintView.vue'), meta: { title: '打印菜谱', hidden: true } },
      { path: 'recipes/:id/versions', name: 'RecipeVersions', component: () => import('@/views/recipe/RecipeVersionView.vue'), meta: { title: '版本历史', hidden: true } },
      { path: 'recipes/batch-import', name: 'BatchImport', component: () => import('@/views/recipe/BatchImportView.vue'), meta: { title: '批量导入', hidden: true } },
      { path: 'favorites', name: 'Favorites', component: () => import('@/views/recipe/FavoriteListView.vue'), meta: { title: '我的收藏', icon: 'Star' } },
      { path: 'orders', name: 'OrderList', component: () => import('@/views/order/OrderListView.vue'), meta: { title: '订单管理', icon: 'ShoppingCart' } },
      { path: 'orders/:id', name: 'OrderDetail', component: () => import('@/views/order/OrderDetailView.vue'), meta: { title: '订单详情', hidden: true } },
      { path: 'gallery', name: 'Gallery', component: () => import('@/views/gallery/GalleryView.vue'), meta: { title: '美食相册', icon: 'Picture' } },
      { path: 'organizations', name: 'OrganizationManagement', component: () => import('@/views/organization/OrganizationManagement.vue'), meta: { title: '组织管理', icon: 'OfficeBuilding' } },
      { path: 'couples', name: 'CoupleList', component: () => import('@/views/couple/CoupleListView.vue'), meta: { title: '情侣空间', icon: 'Connection' } },
      { path: 'users', name: 'UserList', component: () => import('@/views/user/UserListView.vue'), meta: { title: '用户管理', icon: 'User' } },
      { path: 'roles', name: 'Roles', component: () => import('@/views/role/RoleView.vue'), meta: { title: '角色管理', icon: 'UserFilled' } },
      { path: 'permissions', name: 'Permissions', component: () => import('@/views/permission/PermissionView.vue'), meta: { title: '权限管理', icon: 'Lock' } },
      { path: 'categories', name: 'Categories', component: () => import('@/views/category/CategoryView.vue'), meta: { title: '分类管理', icon: 'Grid' } },
      { path: 'notices', name: 'Notices', component: () => import('@/views/notice/NoticeView.vue'), meta: { title: '公告管理', icon: 'Bell' } },
      { path: 'inventory', name: 'Inventory', component: () => import('@/views/inventory/InventoryView.vue'), meta: { title: '库存管理', icon: 'Box' } },
      { path: 'reviews', name: 'Reviews', component: () => import('@/views/review/ReviewView.vue'), meta: { title: '菜谱审核', icon: 'CircleCheck' } },
      { path: 'statistics', name: 'Statistics', component: () => import('@/views/statistics/StatisticsView.vue'), meta: { title: '数据统计', icon: 'TrendCharts' } },
      { path: 'reports', name: 'Reports', component: () => import('@/views/reports/ReportsView.vue'), meta: { title: '报表分析', icon: 'DataLine' } },
      { path: 'suppliers', name: 'Suppliers', component: () => import('@/views/supplier/SupplierView.vue'), meta: { title: '供应商管理', icon: 'Van' } },
      { path: 'meal-plans', name: 'MealPlans', component: () => import('@/views/mealplan/MealPlanView.vue'), meta: { title: '周菜谱规划', icon: 'Calendar' } },
      { path: 'shopping', name: 'Shopping', component: () => import('@/views/shopping/ShoppingView.vue'), meta: { title: '采购清单', icon: 'ShoppingBag' } },
      { path: 'export', name: 'Export', component: () => import('@/views/export/ExportView.vue'), meta: { title: '数据导出', icon: 'Download' } },
      { path: 'logs', name: 'OperationLogs', component: () => import('@/views/logs/OperationLogView.vue'), meta: { title: '操作日志', icon: 'Document' } },
      { path: 'sync-logs', name: 'SyncLogs', component: () => import('@/views/sync/SyncLogView.vue'), meta: { title: '同步日志', icon: 'Refresh' } },
      { path: 'health', name: 'Health', component: () => import('@/views/health/HealthView.vue'), meta: { title: '系统监控', icon: 'Monitor' } },
      { path: 'settings', name: 'Settings', component: () => import('@/views/settings/SettingsView.vue'), meta: { title: '系统设置', icon: 'Setting' } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/profile/ProfileView.vue'), meta: { title: '个人中心', hidden: true } },
      { path: 'ai', name: 'AiChat', component: () => import('@/views/ai/AiChatView.vue'), meta: { title: 'AI 助手', icon: 'MagicStick' } },
      { path: 'ai/recommend', name: 'AiRecommend', component: () => import('@/views/ai/AiRecommendView.vue'), meta: { title: '智能推荐', icon: 'Sunny' } },
      { path: 'ai/ingredient', name: 'AiIngredient', component: () => import('@/views/ai/AiIngredientView.vue'), meta: { title: '食材变菜谱', icon: 'Food' } },
      { path: 'ai/nutrition', name: 'AiNutrition', component: () => import('@/views/ai/AiNutritionView.vue'), meta: { title: '营养分析', icon: 'DataAnalysis' } },
      { path: 'ai/meal-plan', name: 'AiMealPlan', component: () => import('@/views/ai/AiMealPlanView.vue'), meta: { title: 'AI 周菜谱', icon: 'Calendar' } },
      { path: 'ai/recognize', name: 'AiRecognize', component: () => import('@/views/ai/AiRecognizeView.vue'), meta: { title: '菜品识别', icon: 'Camera' } },
      { path: 'ai/shopping', name: 'AiShopping', component: () => import('@/views/ai/AiShoppingListView.vue'), meta: { title: '智能购物', icon: 'ShoppingBag' } },
      { path: 'ai/leftover', name: 'AiLeftover', component: () => import('@/views/ai/AiLeftoverView.vue'), meta: { title: '剩菜妙招', icon: 'Bowl' } },
      { path: 'ai/score', name: 'AiScore', component: () => import('@/views/ai/AiScoreView.vue'), meta: { title: 'AI 评分', icon: 'Star' } },
      { path: 'ai/cooking-qa', name: 'AiCookingQA', component: () => import('@/views/ai/AiCookingQAView.vue'), meta: { title: '烹饪问答', icon: 'ChatDotRound' } },
      { path: 'ai/health', name: 'AiHealth', component: () => import('@/views/ai/AiHealthReportView.vue'), meta: { title: '健康报告', icon: 'FirstAidKit' } },
      { path: 'ai/translate', name: 'AiTranslate', component: () => import('@/views/ai/AiTranslateView.vue'), meta: { title: '菜谱翻译', icon: 'Translate' } },
      { path: 'ai/settings', name: 'AiSettings', component: () => import('@/views/ai/AiSettingsView.vue'), meta: { title: 'AI 设置', icon: 'Setting' } }
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/error/NotFoundView.vue'), meta: { requiresAuth: false } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
