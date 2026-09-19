import request from '@/utils/request'
export const exportRecipes = (params: any) => request.post('/export/recipes', null, { params, responseType: 'blob' })
export const exportOrders = (params: any) => request.post('/export/orders', null, { params, responseType: 'blob' })
