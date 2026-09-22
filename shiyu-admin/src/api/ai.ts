import request from '@/utils/request'

export const aiChat = (data: any) => request.post('/ai/chat', data)
export const aiRecommend = (userId?: string) => request.get('/ai/recommend', { params: { userId } })
export const aiIngredientToRecipe = (data: any) => request.post('/ai/ingredient-to-recipe', data)
export const aiNutrition = (data: any) => request.post('/ai/nutrition', data)
export const aiMealPlan = (data: any) => request.post('/ai/meal-plan', data)
export const aiRecognize = (data: any) => request.post('/ai/recognize', data)
export const aiShoppingList = (data: any) => request.post('/ai/shopping-list', data)
export const aiLeftover = (data: any) => request.post('/ai/leftover', data)
export const aiScore = (data: any) => request.post('/ai/score', data)
export const aiCookingQA = (data: any) => request.post('/ai/cooking-qa', data)
export const aiHealthReport = (userId?: string) => request.get('/ai/health-report', { params: { userId } })
export const aiTranslate = (data: any) => request.post('/ai/translate', data)
export const aiInfo = () => request.get('/ai/info')
export const aiGetConfig = () => request.get('/ai/config')
export const aiUpdateConfig = (data: any) => request.post('/ai/config', data)
export const aiTestConnection = () => request.post('/ai/test')

export const aiListSessions = () => request.get('/ai/sessions')
export const aiCreateSession = (data?: { title?: string }) => request.post('/ai/sessions', data || {})
export const aiDeleteSession = (id: number) => request.delete(`/ai/sessions/${id}`)
export const aiListMessages = (sessionId: number) => request.get(`/ai/sessions/${sessionId}/messages`)
export const aiChatSession = (data: { sessionId?: number; message: string }) => request.post('/ai/chat-session', data)

// P0: 核心 AI 功能
export const aiSemanticSearch = (data: any) => request.post('/ai/semantic-search', data)
export const aiSmartOrder = (data: any) => request.post('/ai/smart-order', data)
export const aiRecipeAssist = (data: any) => request.post('/ai/recipe-assist', data)
export const aiInventoryAdvisor = (data: any) => request.post('/ai/inventory-advisor', data)

// P1: 效率提升 AI 功能
export const aiInventoryPredict = (data: any) => request.post('/ai/inventory-predict', data)
export const aiSceneMenu = (data: any) => request.post('/ai/scene-menu', data)
export const aiDataInsight = (data: any) => request.post('/ai/data-insight', data)
export const aiCopywriting = (data: any) => request.post('/ai/copywriting', data)

// P2: 差异化 AI 功能
export const aiSmartSchedule = (data: any) => request.post('/ai/smart-schedule', data)
export const aiUserProfile = (data: any) => request.post('/ai/user-profile', data)
export const aiTrendPredict = (data: any) => request.post('/ai/trend-predict', data)
export const aiMenuAnalysis = (data: any) => request.post('/ai/menu-analysis', data)
export const aiOrderAnalysis = (data: any) => request.post('/ai/order-analysis', data)

// AI 历史记录
export const aiGetHistory = (params?: { page?: number; size?: number; feature?: string }) => request.get('/ai/history', { params })
export const aiGetStats = () => request.get('/ai/stats')
export const aiDeleteHistory = (id: number) => request.delete(`/ai/history/${id}`)
