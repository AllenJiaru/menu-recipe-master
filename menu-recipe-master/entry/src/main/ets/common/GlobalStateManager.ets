/**
 * 全局状态管理
 * 使用AppStorage管理应用全局状态
 */
import { CommonConstant } from './CommonConstant'
import { CoupleConfig } from '../model/CoupleConfig'

/**
 * 全局状态键名
 */
export class GlobalStateKey {
  static readonly CURRENT_ROLE: string = 'currentRole'           // 当前角色
  static readonly COUPLE_CONFIG: string = 'coupleConfig'         // 情侣配置
  static readonly IS_INITIALIZED: string = 'isInitialized'       // 是否初始化
  static readonly USER_NAME: string = 'userName'                 // 当前用户名
  static readonly USER_AVATAR: string = 'userAvatar'             // 当前用户头像
}

/**
 * 全局状态管理类
 */
export class GlobalStateManager {
  /**
   * 初始化全局状态
   */
  static init(): void {
    // 初始化当前角色
    AppStorage.setOrCreate(GlobalStateKey.CURRENT_ROLE, '')
    
    // 初始化情侣配置
    AppStorage.setOrCreate(GlobalStateKey.COUPLE_CONFIG, new CoupleConfig())
    
    // 初始化状态标记
    AppStorage.setOrCreate(GlobalStateKey.IS_INITIALIZED, false)
    
    // 初始化用户信息
    AppStorage.setOrCreate(GlobalStateKey.USER_NAME, '')
    AppStorage.setOrCreate(GlobalStateKey.USER_AVATAR, '')
  }
  
  /**
   * 设置当前角色
   */
  static setCurrentRole(role: string): void {
    AppStorage.setOrCreate(GlobalStateKey.CURRENT_ROLE, role)
  }
  
  /**
   * 获取当前角色
   */
  static getCurrentRole(): string {
    return AppStorage.get<string>(GlobalStateKey.CURRENT_ROLE) || ''
  }
  
  /**
   * 设置情侣配置
   */
  static setCoupleConfig(config: CoupleConfig): void {
    AppStorage.setOrCreate(GlobalStateKey.COUPLE_CONFIG, config)
  }
  
  /**
   * 获取情侣配置
   */
  static getCoupleConfig(): CoupleConfig {
    return AppStorage.get<CoupleConfig>(GlobalStateKey.COUPLE_CONFIG) || new CoupleConfig()
  }
  
  /**
   * 设置初始化状态
   */
  static setInitialized(initialized: boolean): void {
    AppStorage.setOrCreate(GlobalStateKey.IS_INITIALIZED, initialized)
  }
  
  /**
   * 获取初始化状态
   */
  static isInitialized(): boolean {
    return AppStorage.get<boolean>(GlobalStateKey.IS_INITIALIZED) || false
  }
  
  /**
   * 设置当前用户名
   */
  static setUserName(name: string): void {
    AppStorage.setOrCreate(GlobalStateKey.USER_NAME, name)
  }
  
  /**
   * 获取当前用户名
   */
  static getUserName(): string {
    return AppStorage.get<string>(GlobalStateKey.USER_NAME) || ''
  }
  
  /**
   * 设置当前用户头像
   */
  static setUserAvatar(avatar: string): void {
    AppStorage.setOrCreate(GlobalStateKey.USER_AVATAR, avatar)
  }
  
  /**
   * 获取当前用户头像
   */
  static getUserAvatar(): string {
    return AppStorage.get<string>(GlobalStateKey.USER_AVATAR) || ''
  }
  
  /**
   * 根据角色更新用户信息
   */
  static updateUserInfoByRole(role: string): void {
    const config = GlobalStateManager.getCoupleConfig()
    if (role === CommonConstant.ROLE_CHEF) {
      GlobalStateManager.setUserName(config.chef_name)
    } else if (role === CommonConstant.ROLE_DINER) {
      GlobalStateManager.setUserName(config.diner_name)
    }
  }
  
  /**
   * 清空全局状态
   */
  static clear(): void {
    GlobalStateManager.setCurrentRole('')
    GlobalStateManager.setCoupleConfig(new CoupleConfig())
    GlobalStateManager.setInitialized(false)
    GlobalStateManager.setUserName('')
    GlobalStateManager.setUserAvatar('')
  }
}
