import request from '@/utils/request'

export const getPermissionTree = () => request.get('/permissions/tree')
export const getUserPermissions = () => request.get('/permissions/user')
export const createPermission = (data: any) => request.post('/permissions', data)
export const updatePermission = (id: number, data: any) => request.put(`/permissions/${id}`, data)
export const deletePermission = (id: number) => request.delete(`/permissions/${id}`)
export const checkPermission = (code: string) => request.get('/permissions/check', { params: { code } })
