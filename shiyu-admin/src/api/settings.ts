import request from '@/utils/request'
export const getSettings = () => request.get('/settings')
export const updateSetting = (key: string, value: string) => request.put(`/settings/${key}`, { value })
export const clearData = (type: string) => request.post('/settings/clear-data', { type })
