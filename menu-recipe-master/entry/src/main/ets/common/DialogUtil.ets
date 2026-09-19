/**
 * 弹窗工具类
 * 封装鸿蒙AlertDialog弹窗功能
 */
import promptAction from '@ohos.promptAction'

export class DialogUtil {
  /**
   * 显示确认对话框
   */
  static showConfirm(
    title: string,
    message: string,
    onConfirm: () => void,
    onCancel?: () => void
  ): void {
    promptAction.showDialog({
      title: title,
      message: message,
      buttons: [
        { text: '取消', color: '#666666' },
        { text: '确定', color: '#FF6B6B' }
      ]
    }).then((result) => {
      if (result.index === 1) {
        onConfirm()
      } else if (onCancel) {
        onCancel()
      }
    })
  }
  
  /**
   * 显示提示对话框
   */
  static showAlertDialog(
    title: string,
    message: string,
    buttonText: string = '确定',
    onConfirm?: () => void
  ): void {
    promptAction.showDialog({
      title: title,
      message: message,
      buttons: [
        { text: buttonText, color: '#FF6B6B' }
      ]
    }).then(() => {
      if (onConfirm) {
        onConfirm()
      }
    })
  }
  
  /**
   * 显示删除确认对话框
   */
  static showDeleteConfirm(
    message: string,
    onConfirm: () => void
  ): void {
    DialogUtil.showConfirm(
      '删除确认',
      message,
      onConfirm
    )
  }
  
  /**
   * 显示操作成功提示
   */
  static showSuccessDialog(
    message: string,
    onConfirm?: () => void
  ): void {
    DialogUtil.showAlertDialog(
      '操作成功',
      message,
      '确定',
      onConfirm
    )
  }
  
  /**
   * 显示操作失败提示
   */
  static showErrorDialog(
    message: string,
    onConfirm?: () => void
  ): void {
    DialogUtil.showAlertDialog(
      '操作失败',
      message,
      '确定',
      onConfirm
    )
  }
}
