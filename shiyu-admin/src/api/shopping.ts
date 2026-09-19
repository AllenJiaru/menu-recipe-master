import request from '@/utils/request'
export const getShoppingList = (listName: string) => request.get('/shopping', { params: { listName } })
export const addItem = (data: any) => request.post('/shopping', data)
export const addFromRecipe = (recipeId: number) => request.post(`/shopping/from-recipe/${recipeId}`)
export const toggleItem = (id: number) => request.put(`/shopping/${id}/toggle`)
export const deleteItem = (id: number) => request.delete(`/shopping/${id}`)
export const clearChecked = () => request.delete('/shopping/clear-checked')
export const getShoppingStats = () => request.get('/shopping/stats')
