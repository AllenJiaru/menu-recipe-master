import request from '@/utils/request'
export const saveVersion = (recipeId: number, changeNote: string) => request.post(`/recipe-versions/${recipeId}`, null, { params: { changeNote } })
export const getVersionHistory = (recipeId: number) => request.get(`/recipe-versions/${recipeId}`)
export const getVersionDetail = (id: number) => request.get(`/recipe-versions/detail/${id}`)
export const restoreVersion = (id: number) => request.post(`/recipe-versions/${id}/restore`)
