import request from '@/utils/request'
export const login = (data: any) => request.post('/auth/login', data)
export const register = (data: any) => request.post('/auth/register', data)
export const getUserInfo = () => request.get('/auth/info')
export const changePassword = (data: any) => request.put('/auth/password', data)
export const resetPassword = (data: { username: string; newPassword: string }) => request.post('/auth/reset-password', data)
export const updateProfile = (data: { nickname?: string; avatar?: string }) => request.put('/auth/profile', data)
