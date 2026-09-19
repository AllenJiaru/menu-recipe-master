/**
 * 角色权限控制工具
 * 用于判断当前角色权限，控制页面功能显示
 */
import { CommonConstant } from './CommonConstant'
import { LogUtil } from './LogUtil'
import preferences from '@ohos.data.preferences'
import common from '@ohos.app.ability.common'

export class RolePermissionUtil {
  private static readonly TAG: string = 'RolePermissionUtil'
  private static currentRole: string = ''
  private static context: common.UIAbilityContext | null = null
  
  /**
   * 初始化权限工具
   */
  static async init(context: common.UIAbilityContext): Promise<void> {
    RolePermissionUtil.context = context
    await RolePermissionUtil.loadCurrentRole()
  }
  
  /**
   * 加载当前角色
   */
  private static async loadCurrentRole(): Promise<void> {
    if (!RolePermissionUtil.context) {
      LogUtil.error(RolePermissionUtil.TAG, '上下文为空')
      return
    }
    
    try {
      const prefs = await preferences.getPreferences(
        RolePermissionUtil.context, 
        'menu_recipe_prefs'
      )
      const role = await prefs.get('current_role', '') as string
      RolePermissionUtil.currentRole = role
      LogUtil.info(RolePermissionUtil.TAG, `当前角色: ${role}`)
    } catch (err) {
      LogUtil.error(RolePermissionUtil.TAG, `加载角色失败: ${err}`)
    }
  }
  
  /**
   * 获取当前角色
   */
  static getCurrentRole(): string {
    return RolePermissionUtil.currentRole
  }
  
  /**
   * 设置当前角色
   */
  static async setCurrentRole(role: string): Promise<void> {
    if (!RolePermissionUtil.context) {
      LogUtil.error(RolePermissionUtil.TAG, '上下文为空')
      return
    }
    
    try {
      const prefs = await preferences.getPreferences(
        RolePermissionUtil.context, 
        'menu_recipe_prefs'
      )
      await prefs.put('current_role', role)
      await prefs.flush()
      RolePermissionUtil.currentRole = role
      LogUtil.info(RolePermissionUtil.TAG, `角色已更新: ${role}`)
    } catch (err) {
      LogUtil.error(RolePermissionUtil.TAG, `设置角色失败: ${err}`)
    }
  }
  
  /**
   * 判断是否是主厨
   */
  static isChef(): boolean {
    return RolePermissionUtil.currentRole === CommonConstant.ROLE_CHEF
  }
  
  /**
   * 判断是否是食客
   */
  static isDiner(): boolean {
    return RolePermissionUtil.currentRole === CommonConstant.ROLE_DINER
  }
  
  /**
   * 判断是否有权限（主厨权限）
   * 用于控制菜谱管理、订单处理等功能
   */
  static hasChefPermission(): boolean {
    return RolePermissionUtil.isChef()
  }
  
  /**
   * 判断是否有权限（食客权限）
   * 用于控制点餐、浏览等功能
   */
  static hasDinerPermission(): boolean {
    return RolePermissionUtil.isDiner()
  }
  
  /**
   * 判断是否可以管理菜谱
   * 只有主厨可以管理菜谱
   */
  static canManageRecipe(): boolean {
    return RolePermissionUtil.isChef()
  }
  
  /**
   * 判断是否可以点餐
   * 只有食客可以点餐
   */
  static canOrderRecipe(): boolean {
    return RolePermissionUtil.isDiner()
  }
  
  /**
   * 判断是否可以处理订单
   * 只有主厨可以处理订单
   */
  static canHandleOrder(): boolean {
    return RolePermissionUtil.isChef()
  }
  
  /**
   * 判断是否可以查看所有订单
   * 主厨可以查看所有订单，食客只能查看自己的订单
   */
  static canViewAllOrders(): boolean {
    return RolePermissionUtil.isChef()
  }
  
  /**
   * 判断是否可以上传相册
   * 主厨和食客都可以上传
   */
  static canUploadGallery(): boolean {
    return RolePermissionUtil.isChef() || RolePermissionUtil.isDiner()
  }
  
  /**
   * 获取角色显示名称
   */
  static getRoleDisplayName(): string {
    if (RolePermissionUtil.isChef()) {
      return '主厨'
    } else if (RolePermissionUtil.isDiner()) {
      return '食客'
    }
    return '未知'
  }
  
  /**
   * 获取角色颜色
   */
  static getRoleColor(): string {
    if (RolePermissionUtil.isChef()) {
      return '#FF6B6B'  // 主厨红色
    } else if (RolePermissionUtil.isDiner()) {
      return '#4ECDC4'  // 食客青色
    }
    return '#999999'
  }
  
  /**
   * 检查是否已初始化角色
   */
  static hasRole(): boolean {
    return RolePermissionUtil.currentRole !== ''
  }
  
  /**
   * 清除角色信息
   */
  static async clearRole(): Promise<void> {
    if (!RolePermissionUtil.context) {
      LogUtil.error(RolePermissionUtil.TAG, '上下文为空')
      return
    }
    
    try {
      const prefs = await preferences.getPreferences(
        RolePermissionUtil.context, 
        'menu_recipe_prefs'
      )
      await prefs.delete('current_role')
      await prefs.flush()
      RolePermissionUtil.currentRole = ''
      LogUtil.info(RolePermissionUtil.TAG, '角色已清除')
    } catch (err) {
      LogUtil.error(RolePermissionUtil.TAG, `清除角色失败: ${err}`)
    }
  }
}
