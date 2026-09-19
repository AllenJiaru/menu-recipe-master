/**
 * API 服务层
 * 封装所有后端 API 接口
 */
import { HttpUtil, ApiResponse, PageResponse } from './HttpUtil'
import { LogUtil } from './LogUtil'
import { Recipe } from '../model/Recipe'
import { Order } from '../model/Order'
import { GalleryImage } from '../model/GalleryImage'

export class ApiService {
  private static readonly TAG: string = 'ApiService'

  // ======================== Auth ========================

  /**
   * 用户登录
   */
  static async login(username: string, password: string): Promise<ApiResponse<object>> {
    return HttpUtil.post<object>('/auth/login', { username, password })
  }

  /**
   * 获取当前用户信息
   */
  static async getCurrentUser(): Promise<ApiResponse<object>> {
    return HttpUtil.get<object>('/auth/info')
  }

  /**
   * 按角色自动登录（主厨/食客使用默认测试账号）
   */
  static async autoLogin(role: string): Promise<boolean> {
    const accounts: Record<string, string[]> = {
      'chef': ['testchef', '123456'],
      'diner': ['diner_a', '123456']
    }
    const account = accounts[role]
    if (!account) {
      return false
    }
    const res = await this.login(account[0], account[1])
    if (res.code === 200 && res.data) {
      const token = (res.data as Record<string, Object>)['token'] as string
      if (token) {
        HttpUtil.setAuthToken(token)
        LogUtil.info(this.TAG, `自动登录成功: ${account[0]}`)
        return true
      }
    }
    LogUtil.error(this.TAG, `自动登录失败: ${res.message}`)
    return false
  }

  /**
   * 获取当前用户信息并保存头像到 AppStorage
   */
  static async fetchAndSaveUserProfile(): Promise<void> {
    try {
      const res = await this.getCurrentUser()
      if (res.code === 200 && res.data) {
        const data = res.data as Record<string, Object>
        const avatar = data['avatar'] as string
        if (avatar) {
          AppStorage.setOrCreate('userAvatar', avatar)
          LogUtil.info(this.TAG, `保存用户头像: ${avatar}`)
        }
        const nickname = data['nickname'] as string
        if (nickname) {
          AppStorage.setOrCreate('userNickname', nickname)
        }
      }
    } catch (err) {
      LogUtil.error(this.TAG, `获取用户信息失败: ${err}`)
    }
  }

  /**
   * 是否已登录
   */
  static isLoggedIn(): boolean {
    return !!AppStorage.get<string>('authToken')
  }

  // ======================== Recipes ========================

  /**
   * 获取菜谱列表
   */
  static async getRecipes(params?: {
    page?: number
    size?: number
    categoryId?: number
    keyword?: string
    difficulty?: number
    coupleId?: number
    status?: number
  }): Promise<ApiResponse<PageResponse<Recipe>>> {
    const queryParams: Record<string, string> = {}
    if (params?.page) queryParams['page'] = params.page.toString()
    if (params?.size) queryParams['size'] = params.size.toString()
    if (params?.categoryId) queryParams['categoryId'] = params.categoryId.toString()
    if (params?.keyword) queryParams['keyword'] = params.keyword
    if (params?.difficulty) queryParams['difficulty'] = params.difficulty.toString()
    if (params?.coupleId) queryParams['coupleId'] = params.coupleId.toString()
    if (params?.status !== undefined) queryParams['status'] = params.status.toString()
    return HttpUtil.get<PageResponse<Recipe>>('/recipes', queryParams)
  }

  /**
   * 获取菜谱详情
   */
  static async getRecipeById(id: number): Promise<ApiResponse<Recipe>> {
    return HttpUtil.get<Recipe>(`/recipes/${id}`)
  }

  /**
   * 创建菜谱
   */
  static async createRecipe(recipe: Partial<Recipe>): Promise<ApiResponse<Recipe>> {
    return HttpUtil.post<Recipe>('/recipes', recipe)
  }

  /**
   * 更新菜谱
   */
  static async updateRecipe(id: number, recipe: Partial<Recipe>): Promise<ApiResponse<Recipe>> {
    return HttpUtil.put<Recipe>(`/recipes/${id}`, recipe)
  }

  /**
   * 删除菜谱
   */
  static async deleteRecipe(id: number): Promise<ApiResponse<void>> {
    return HttpUtil.delete<void>(`/recipes/${id}`)
  }

  /**
   * 切换收藏状态
   */
  static async toggleFavorite(id: number): Promise<ApiResponse<void>> {
    return HttpUtil.put<void>(`/recipes/${id}/favorite`)
  }

  /**
   * 更新菜谱状态
   */
  static async updateRecipeStatus(id: number, status: number): Promise<ApiResponse<void>> {
    return HttpUtil.put<void>(`/recipes/${id}/status`, { status })
  }

  /**
   * 批量删除菜谱
   */
  static async batchDeleteRecipes(ids: number[]): Promise<ApiResponse<void>> {
    return HttpUtil.post<void>('/recipes/batch/delete', { ids })
  }

  /**
   * 批量更新菜谱状态
   */
  static async batchUpdateRecipeStatus(ids: number[], status: number): Promise<ApiResponse<void>> {
    return HttpUtil.post<void>('/recipes/batch/status', { ids, status })
  }

  // ======================== Orders ========================

  /**
   * 获取订单列表
   */
  static async getOrders(params?: {
    page?: number
    size?: number
    status?: number
    createdBy?: string
  }): Promise<ApiResponse<PageResponse<Order>>> {
    const queryParams: Record<string, string> = {}
    if (params?.page) queryParams['page'] = params.page.toString()
    if (params?.size) queryParams['size'] = params.size.toString()
    if (params?.status !== undefined) queryParams['status'] = params.status.toString()
    if (params?.createdBy) queryParams['createdBy'] = params.createdBy
    return HttpUtil.get<PageResponse<Order>>('/orders', queryParams)
  }

  /**
   * 获取所有订单（后端分页接口，一次取全量）
   */
  static async getAllOrders(): Promise<ApiResponse<Order[]>> {
    const res = await HttpUtil.get<PageResponse<Order>>('/orders', { page: '1', size: '500' })
    if (res.code === 200 && res.data) {
      return { code: 200, message: res.message, data: res.data.records || [] }
    }
    return { code: res.code, message: res.message, data: [] }
  }

  /**
   * 获取订单详情
   */
  static async getOrderById(id: number): Promise<ApiResponse<Order>> {
    return HttpUtil.get<Order>(`/orders/${id}`)
  }

  /**
   * 创建订单
   */
  static async createOrder(order: Partial<Order>): Promise<ApiResponse<Order>> {
    return HttpUtil.post<Order>('/orders', order)
  }

  /**
   * 更新订单状态（映射到后端 accept/cooking/complete/cancel 接口）
   */
  static async updateOrderStatus(id: number, status: number): Promise<ApiResponse<void>> {
    switch (status) {
      case 1:
        return HttpUtil.put<void>(`/orders/${id}/accept`)
      case 2:
        return HttpUtil.put<void>(`/orders/${id}/cooking`)
      case 3:
        return HttpUtil.put<void>(`/orders/${id}/complete`)
      case 4:
        return HttpUtil.put<void>(`/orders/${id}/cancel`, { reason: '' })
      default:
        return { code: 400, message: '无效的订单状态', data: undefined as void }
    }
  }

  /**
   * 删除订单
   */
  static async deleteOrder(id: number): Promise<ApiResponse<void>> {
    return HttpUtil.delete<void>(`/orders/${id}`)
  }

  /**
   * 批量删除订单
   */
  static async batchDeleteOrders(ids: number[]): Promise<ApiResponse<void>> {
    return HttpUtil.post<void>('/orders/batch/delete', { ids })
  }

  /**
   * 批量更新订单状态（逐个调用后端状态接口）
   */
  static async batchUpdateOrderStatus(ids: number[], status: number): Promise<ApiResponse<void>> {
    for (const id of ids) {
      const res = await this.updateOrderStatus(id, status)
      if (res.code !== 200) {
        return res
      }
    }
    return { code: 200, message: 'Success', data: undefined as void }
  }

  // ======================== Gallery ========================

  /**
   * 获取相册列表
   */
  static async getGalleryImages(params?: {
    page?: number
    size?: number
    createdBy?: string
  }): Promise<ApiResponse<PageResponse<GalleryImage>>> {
    const queryParams: Record<string, string> = {}
    if (params?.page) queryParams['page'] = params.page.toString()
    if (params?.size) queryParams['size'] = params.size.toString()
    if (params?.createdBy) queryParams['createdBy'] = params.createdBy
    return HttpUtil.get<PageResponse<GalleryImage>>('/gallery', queryParams)
  }

  /**
   * 获取所有相册图片（后端分页接口，一次取全量）
   */
  static async getAllGalleryImages(): Promise<ApiResponse<GalleryImage[]>> {
    const res = await HttpUtil.get<PageResponse<GalleryImage>>('/gallery', { page: '1', size: '500' })
    if (res.code === 200 && res.data) {
      return { code: 200, message: res.message, data: res.data.records || [] }
    }
    return { code: res.code, message: res.message, data: [] }
  }

  /**
   * 上传相册图片
   */
  static async uploadGalleryImage(filePath: string, description: string): Promise<ApiResponse<GalleryImage>> {
    return HttpUtil.upload<GalleryImage>('/gallery', filePath, 'gallery.jpg')
  }

  /**
   * 删除相册图片
   */
  static async deleteGalleryImage(id: number): Promise<ApiResponse<void>> {
    return HttpUtil.delete<void>(`/gallery/${id}`)
  }

  // ======================== Categories ========================

  /**
   * 获取分类列表
   */
  static async getCategories(): Promise<ApiResponse<object[]>> {
    return HttpUtil.get<object[]>('/categories')
  }

  // ======================== Couple Config ========================

  /**
   * 获取情侣配置（取后端列表第一条）
   */
  static async getCoupleConfig(): Promise<ApiResponse<object>> {
    const res = await HttpUtil.get<object>('/couples', { page: '1', size: '1' })
    if (res.code === 200 && res.data) {
      const records = (res.data as Record<string, Object>)['records'] as object[]
      if (records && records.length > 0) {
        return { code: 200, message: res.message, data: records[0] }
      }
      return { code: 404, message: '暂无情侣配置', data: undefined as object }
    }
    return { code: res.code, message: res.message, data: undefined as object }
  }

  /**
   * 更新情侣配置
   */
  static async updateCoupleConfig(config: object): Promise<ApiResponse<void>> {
    const id = (config as Record<string, Object>)['id'] as number
    if (!id) {
      return { code: 400, message: '缺少配置ID', data: undefined as void }
    }
    return HttpUtil.put<void>(`/couples/${id}`, config)
  }

  // ======================== Favorites ========================

  /**
   * 获取收藏列表（从菜谱列表中筛选 isFavorite = 1）
   */
  static async getFavorites(): Promise<ApiResponse<Recipe[]>> {
    const res = await HttpUtil.get<PageResponse<Recipe>>('/recipes', { page: '1', size: '500' })
    if (res.code === 200 && res.data) {
      const records = res.data.records || []
      const favorites = records.filter(r => r.isFavorite === 1)
      return { code: 200, message: res.message, data: favorites }
    }
    return { code: res.code, message: res.message, data: [] }
  }

  /**
   * 检查菜谱是否已收藏
   */
  static async isFavorited(recipeId: number): Promise<ApiResponse<boolean>> {
    const res = await HttpUtil.get<Recipe>(`/recipes/${recipeId}`)
    if (res.code === 200 && res.data) {
      return { code: 200, message: res.message, data: res.data.isFavorite === 1 }
    }
    return { code: res.code, message: res.message, data: false }
  }

  // ======================== Files ========================

  /**
   * 上传文件，返回后端存储路径
   */
  static async uploadFile(filePath: string, fileName: string): Promise<ApiResponse<string>> {
    return HttpUtil.upload<string>('/files/upload', filePath, fileName)
  }

  // ======================== Health ========================

  /**
   * 健康检查
   */
  static async healthCheck(): Promise<ApiResponse<object>> {
    return HttpUtil.get<object>('/health')
  }
}
