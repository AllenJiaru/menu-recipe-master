export interface User {
  id: number
  username: string
  nickname?: string
  avatar?: string
  role: string
  coupleId?: number
  status: number
  lastLoginTime?: string
  createTime: string
}

export interface CoupleConfig {
  id: number
  spaceName: string
  chefName: string
  dinerName: string
  spaceAvatar?: string
  status: number
  createTime: string
}

export interface RecipeCategory {
  id: number
  name: string
  icon?: string
  sortOrder: number
  status: number
}

export interface RecipeMaterial {
  id?: number
  recipeId?: number
  name: string
  amount: string
  unit: string
  sortOrder: number
}

export interface RecipeStep {
  id?: number
  recipeId?: number
  stepNumber: number
  description: string
  imageUrl?: string
}

export interface Recipe {
  id: number
  coupleId?: number
  name: string
  categoryId?: number
  type: number
  description?: string
  coverImage?: string
  cookingTime?: number
  difficulty: number
  isFavorite: number
  orderCount: number
  status: number
  createTime: string
  materials?: RecipeMaterial[]
  steps?: RecipeStep[]
}

export interface OrderRecord {
  id: number
  coupleId: number
  recipeId: number
  recipeName: string
  recipeImage?: string
  status: number
  remark?: string
  rejectReason?: string
  orderTime: string
  acceptTime?: string
  completeTime?: string
  createTime: string
}

export interface GalleryImage {
  id: number
  coupleId: number
  imageUrl: string
  thumbnailUrl?: string
  description?: string
  orderId?: number
  createTime: string
}

export interface DashboardData {
  totalRecipes: number
  totalOrders: number
  todayOrders: number
  pendingOrders: number
  completedOrders: number
  recentOrders: OrderRecord[]
  popularRecipes: Recipe[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}
