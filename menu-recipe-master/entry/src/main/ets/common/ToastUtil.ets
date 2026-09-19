/**
 * 吐司提示工具类
 * 封装鸿蒙Toast提示功能
 */
import promptAction from '@ohos.promptAction'

export class ToastUtil {
  /**
   * 显示短时长吐司
   */
  static showShort(message: string): void {
    promptAction.showToast({
      message: message,
      duration: 1500
    })
  }
  
  /**
   * 显示长时长吐司
   */
  static showLong(message: string): void {
    promptAction.showToast({
      message: message,
      duration: 3000
    })
  }
  
  /**
   * 显示成功提示
   */
  static showSuccess(message: string): void {
    promptAction.showToast({
      message: `✓ ${message}`,
      duration: 1500
    })
  }
  
  /**
   * 显示错误提示
   */
  static showError(message: string): void {
    promptAction.showToast({
      message: `✗ ${message}`,
      duration: 2000
    })
  }
  
  /**
   * 显示警告提示
   */
  static showWarning(message: string): void {
    promptAction.showToast({
      message: `⚠ ${message}`,
      duration: 2000
    })
  }
}
