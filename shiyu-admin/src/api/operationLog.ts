import request from '@/utils/request'
export const getLogs = (params: any) => request.get('/logs', { params })
