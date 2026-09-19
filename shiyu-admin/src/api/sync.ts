import request from '@/utils/request'
export const syncPull = (data: any) => request.post('/sync/pull', data)
export const syncPush = (data: any) => request.post('/sync/push', data)
export const getSyncLogs = (params: any) => request.get('/sync/logs', { params })
