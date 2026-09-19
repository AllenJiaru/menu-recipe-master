import request from '@/utils/request'

export const getRoles = (params: any) => request.get('/roles', { params })
export const getAllRoles = () => request.get('/roles/all')
export const getRoleById = (id: number) => request.get(`/roles/${id}`)
export const createRole = (data: any) => request.post('/roles', data)
export const updateRole = (id: number, data: any) => request.put(`/roles/${id}`, data)
export const deleteRole = (id: number) => request.delete(`/roles/${id}`)
export const getRolePermissions = (roleId: number) => request.get(`/roles/${roleId}/permissions`)
export const assignPermissions = (roleId: number, permissionIds: number[]) => request.put(`/roles/${roleId}/permissions`, permissionIds)
