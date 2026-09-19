import request from '@/utils/request'
export const uploadFile = (formData: FormData) => request.post('/files/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
export const deleteFile = (filename: string) => request.delete(`/files/${filename}`)
