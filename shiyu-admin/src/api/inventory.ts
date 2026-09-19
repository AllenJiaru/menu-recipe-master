import request from '@/utils/request'
export const getInventory = (params: any) => request.get('/inventory', { params })
export const getInventoryById = (id: number) => request.get(`/inventory/${id}`)
export const createInventory = (data: any) => request.post('/inventory', data)
export const updateInventory = (id: number, data: any) => request.put(`/inventory/${id}`, data)
export const deleteInventory = (id: number) => request.delete(`/inventory/${id}`)
export const getLowStockItems = (params: any) => request.get('/inventory/low-stock', { params })
export const restock = (id: number, data: any) => request.put(`/inventory/${id}/restock`, data)
