import request from '@/utils/request'
export const getGalleryImages = (params: any) => request.get('/gallery', { params })
export const getGalleryImageById = (id: number) => request.get(`/gallery/${id}`)
export const uploadGalleryImage = (formData: FormData) => request.post('/gallery', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
export const updateGalleryImage = (id: number, data: any) => request.put(`/gallery/${id}`, data)
export const deleteGalleryImage = (id: number) => request.delete(`/gallery/${id}`)
export const batchDeleteGallery = (ids: number[]) => request.post('/gallery/batch', ids)
