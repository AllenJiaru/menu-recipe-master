import request from '@/utils/request'
export const getOrders = (params: any) => request.get('/orders', { params })
export const getOrderById = (id: number) => request.get(`/orders/${id}`)
export const createOrder = (data: any) => request.post('/orders', data)
export const acceptOrder = (id: number) => request.put(`/orders/${id}/accept`)
export const cookingOrder = (id: number) => request.put(`/orders/${id}/cooking`)
export const completeOrder = (id: number) => request.put(`/orders/${id}/complete`)
export const cancelOrder = (id: number, reason?: string) => request.put(`/orders/${id}/cancel`, { reason })
export const getOrderStats = (params: any) => request.get('/orders/stats', { params })
