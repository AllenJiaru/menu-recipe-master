import request from '@/utils/request'
export const getSuppliers = (params: any) => request.get('/suppliers', { params })
export const getSupplierById = (id: number) => request.get(`/suppliers/${id}`)
export const createSupplier = (data: any) => request.post('/suppliers', data)
export const updateSupplier = (id: number, data: any) => request.put(`/suppliers/${id}`, data)
export const deleteSupplier = (id: number) => request.delete(`/suppliers/${id}`)
