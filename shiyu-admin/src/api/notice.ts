import request from '@/utils/request'
export const getNotices = (params: any) => request.get('/notices', { params })
export const getNoticeById = (id: number) => request.get(`/notices/${id}`)
export const createNotice = (data: any) => request.post('/notices', data)
export const updateNotice = (id: number, data: any) => request.put(`/notices/${id}`, data)
export const deleteNotice = (id: number) => request.delete(`/notices/${id}`)
export const publishNotice = (id: number) => request.put(`/notices/${id}/publish`)
export const getPublishedNotices = (params: any) => request.get('/notices/published', { params })
