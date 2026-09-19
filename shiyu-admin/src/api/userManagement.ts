import request from '@/utils/request'

export const getUsersWithRoles = (params: any) => request.get('/user-management', { params })
export const assignRoles = (userId: number, roleIds: number[]) => request.put(`/user-management/${userId}/roles`, roleIds)
export const getUserRoles = (userId: number) => request.get(`/user-management/${userId}/roles`)
