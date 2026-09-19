import request from '@/utils/request'
export const getReviews = (params: any) => request.get('/reviews', { params })
export const createReview = (data: any) => request.post('/reviews', data)
export const updateReview = (id: number, data: any) => request.put(`/reviews/${id}`, data)
export const deleteReview = (id: number) => request.delete(`/reviews/${id}`)
export const getPendingReviews = (params: any) => request.get('/reviews/pending', { params })
export const batchCreateReviews = (ids: number[]) => request.post('/reviews/batch', { ids })
export const batchAuditReviews = (ids: number[], status: number, comment = '') => request.post('/reviews/batch/audit', { ids, status, comment })
export const batchDeleteReviews = (ids: number[]) => request.post('/reviews/batch/delete', { ids })
