import request from '@/utils/request'
export const getOrderStats = (params: any) => request.get('/statistics/orders', { params })
