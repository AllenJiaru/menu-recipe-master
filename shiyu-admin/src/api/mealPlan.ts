import request from '@/utils/request'
export const getWeeklyPlan = (startDate: string) => request.get('/meal-plans/weekly', { params: { startDate } })
export const savePlan = (data: any) => request.post('/meal-plans', data)
export const deletePlan = (id: number) => request.delete(`/meal-plans/${id}`)
export const getPlanStats = () => request.get('/meal-plans/stats')
