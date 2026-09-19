import request from '@/utils/request'
export const toggleFavorite = (recipeId: number, recipeName: string, recipeCover: string) => request.post('/favorites/toggle', { recipeId, recipeName, recipeCover })
export const checkFavorite = (recipeId: number) => request.get(`/favorites/check/${recipeId}`)
export const getFavorites = (params: any) => request.get('/favorites', { params })
