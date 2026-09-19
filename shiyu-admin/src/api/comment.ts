import request from '@/utils/request'
export const getComments = (recipeId: number, params: any) => request.get(`/comments/recipe/${recipeId}`, { params })
export const addComment = (data: any) => request.post('/comments', data)
export const deleteComment = (id: number) => request.delete(`/comments/${id}`)
export const getCommentStats = (recipeId: number) => request.get(`/comments/stats/${recipeId}`)
