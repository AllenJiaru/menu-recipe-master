import request from '@/utils/request'
export const getCouples = (params: any) => request.get('/couples', { params })
export const getCoupleById = (id: number) => request.get(`/couples/${id}`)
export const createCouple = (data: any) => request.post('/couples', data)
export const updateCouple = (id: number, data: any) => request.put(`/couples/${id}`, data)
export const deleteCouple = (id: number) => request.delete(`/couples/${id}`)
