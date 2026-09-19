import request from '@/utils/request'
export const getCategories = (params: any) => request.get('/categories', { params })
export const getCategoryById = (id: number) => request.get(`/categories/${id}`)
export const createCategory = (data: any) => request.post('/categories', data)
export const updateCategory = (id: number, data: any) => request.put(`/categories/${id}`, data)
export const deleteCategory = (id: number) => request.delete(`/categories/${id}`)
