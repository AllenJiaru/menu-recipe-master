import dayjs from 'dayjs'

export const formatDate = (date?: string | Date | null) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}
export const formatDateShort = (date?: string | Date | null) => {
  if (!date) return '-'
  return dayjs(date).format('MM-DD HH:mm')
}

export const orderStatusMap: Record<number, string> = {
  0: '待处理', 1: '已接受', 2: '制作中', 3: '已完成', 4: '已取消'
}

export const orderStatusType: Record<number, string> = {
  0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger'
}

export const recipeTypeMap: Record<number, string> = {
  1: '荤菜', 2: '素菜', 3: '汤类', 4: '甜点', 5: '蒸菜',
  6: '炖菜', 7: '凉菜', 8: '炒菜', 9: '红烧', 10: '其他'
}

export const difficultyMap: Record<number, string> = {
  1: '简单', 2: '较简单', 3: '中等', 4: '较难', 5: '困难'
}

export const roleMap: Record<string, string> = {
  admin: '管理员', chef: '主厨', diner: '食客'
}

export const roleType: Record<string, string> = {
  admin: 'danger', chef: 'warning', diner: 'success'
}

export function imgUrl(url?: string | null): string {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const path = url.replace(/^\/uploads\//, '')
  return `/api/files/preview?path=${encodeURIComponent(path)}`
}
