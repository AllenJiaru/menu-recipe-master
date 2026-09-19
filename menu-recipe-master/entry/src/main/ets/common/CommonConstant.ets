/**
 * 全局常量配置
 */

export class CommonConstant {
  static readonly APP_NAME: string = '食遇'
  static readonly APP_VERSION: string = '1.0.0'
  static readonly DB_NAME: string = 'MenuRecipe.db'
  static readonly DB_VERSION: number = 1

  static readonly ROLE_CHEF: string = 'chef'
  static readonly ROLE_DINER: string = 'diner'

  static readonly ORDER_STATUS_PENDING: number = 0
  static readonly ORDER_STATUS_ACCEPTED: number = 1
  static readonly ORDER_STATUS_COOKING: number = 2
  static readonly ORDER_STATUS_COMPLETED: number = 3
  static readonly ORDER_STATUS_CANCELLED: number = 4

  static readonly RECIPE_TYPE_MEAT: number = 1
  static readonly RECIPE_TYPE_VEGETABLE: number = 2
  static readonly RECIPE_TYPE_SOUP: number = 3
  static readonly RECIPE_TYPE_DESSERT: number = 4
  static readonly RECIPE_TYPE_STEAMED: number = 5
  static readonly RECIPE_TYPE_STEWED: number = 6
  static readonly RECIPE_TYPE_COLD: number = 7
  static readonly RECIPE_TYPE_STIR_FRY: number = 8
  static readonly RECIPE_TYPE_BRAISED: number = 9
  static readonly RECIPE_TYPE_OTHER: number = 10

  static readonly PAGE_SPLASH: string = 'pages/SplashPage'
  static readonly PAGE_ROLE_SELECT: string = 'pages/RoleSelectPage'
  static readonly PAGE_CHEF_HOME: string = 'pages/ChefHomePage'
  static readonly PAGE_DINER_HOME: string = 'pages/DinerHomePage'
  static readonly PAGE_CHEF_RECIPE_DETAIL: string = 'pages/ChefRecipeDetailPage'
  static readonly PAGE_ADD_RECIPE: string = 'pages/AddRecipePage'
  static readonly PAGE_DINER_ORDER: string = 'pages/DinerOrderPage'
  static readonly PAGE_ORDER_DETAIL: string = 'pages/OrderDetailPage'
  static readonly PAGE_FAVORITE_RECIPE: string = 'pages/FavoriteRecipePage'
  static readonly PAGE_GALLERY: string = 'pages/GalleryPage'
  static readonly PAGE_SETTINGS: string = 'pages/SettingsPage'
  static readonly PAGE_RECIPE_CATEGORY: string = 'pages/RecipeCategoryPage'

  static readonly STORAGE_COUPLE_CONFIG: string = 'couple_config'
  static readonly STORAGE_CURRENT_ROLE: string = 'current_role'
  static readonly STORAGE_USER_INFO: string = 'user_info'

  static readonly IMAGE_QUALITY: number = 80
  static readonly MAX_IMAGE_SIZE: number = 1024 * 1024
  static readonly PAGE_SIZE: number = 20
}

/**
 * 菜谱类型映射 (带 emoji)
 */
export const RecipeTypeMap = new Map<number, string>([
  [CommonConstant.RECIPE_TYPE_MEAT, '荤菜'],
  [CommonConstant.RECIPE_TYPE_VEGETABLE, '素菜'],
  [CommonConstant.RECIPE_TYPE_SOUP, '汤类'],
  [CommonConstant.RECIPE_TYPE_DESSERT, '甜点'],
  [CommonConstant.RECIPE_TYPE_STEAMED, '蒸菜'],
  [CommonConstant.RECIPE_TYPE_STEWED, '炖菜'],
  [CommonConstant.RECIPE_TYPE_COLD, '凉菜'],
  [CommonConstant.RECIPE_TYPE_STIR_FRY, '炒菜'],
  [CommonConstant.RECIPE_TYPE_BRAISED, '红烧'],
  [CommonConstant.RECIPE_TYPE_OTHER, '其他']
])

export const RecipeTypeEmoji = new Map<number, string>([
  [CommonConstant.RECIPE_TYPE_MEAT, '🥩'],
  [CommonConstant.RECIPE_TYPE_VEGETABLE, '🥬'],
  [CommonConstant.RECIPE_TYPE_SOUP, '🍲'],
  [CommonConstant.RECIPE_TYPE_DESSERT, '🍰'],
  [CommonConstant.RECIPE_TYPE_STEAMED, '🥟'],
  [CommonConstant.RECIPE_TYPE_STEWED, '🫕'],
  [CommonConstant.RECIPE_TYPE_COLD, '🥗'],
  [CommonConstant.RECIPE_TYPE_STIR_FRY, '🍳'],
  [CommonConstant.RECIPE_TYPE_BRAISED, '🍖'],
  [CommonConstant.RECIPE_TYPE_OTHER, '🍽']
])

/**
 * 订单状态映射
 */
export const OrderStatusMap = new Map<number, string>([
  [CommonConstant.ORDER_STATUS_PENDING, '待处理'],
  [CommonConstant.ORDER_STATUS_ACCEPTED, '已接单'],
  [CommonConstant.ORDER_STATUS_COOKING, '制作中'],
  [CommonConstant.ORDER_STATUS_COMPLETED, '已完成'],
  [CommonConstant.ORDER_STATUS_CANCELLED, '已取消']
])
