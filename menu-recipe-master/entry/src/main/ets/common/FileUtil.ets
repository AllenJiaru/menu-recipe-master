/**
 * 文件工具类
 * 封装文件操作相关功能
 */
import fs from '@ohos.file.fs'
import { LogUtil } from './LogUtil'

export class FileUtil {
  private static readonly TAG: string = 'FileUtil'
  
  /**
   * 获取应用私有文件目录
   */
  static getFilesDir(context: Context): string {
    return context.filesDir
  }
  
  /**
   * 获取应用缓存目录
   */
  static getCacheDir(context: Context): string {
    return context.cacheDir
  }
  
  /**
   * 创建目录
   */
  static createDirectory(dirPath: string): boolean {
    try {
      if (!fs.accessSync(dirPath)) {
        fs.mkdirSync(dirPath)
        LogUtil.info(FileUtil.TAG, `创建目录成功: ${dirPath}`)
      }
      return true
    } catch (err) {
      LogUtil.error(FileUtil.TAG, `创建目录失败: ${err}`)
      return false
    }
  }
  
  /**
   * 判断文件是否存在
   */
  static exists(filePath: string): boolean {
    try {
      return fs.accessSync(filePath)
    } catch (err) {
      return false
    }
  }
  
  /**
   * 删除文件
   */
  static deleteFile(filePath: string): boolean {
    try {
      if (FileUtil.exists(filePath)) {
        fs.unlinkSync(filePath)
        LogUtil.info(FileUtil.TAG, `删除文件成功: ${filePath}`)
      }
      return true
    } catch (err) {
      LogUtil.error(FileUtil.TAG, `删除文件失败: ${err}`)
      return false
    }
  }
  
  /**
   * 复制文件
   */
  static copyFile(srcPath: string, destPath: string): boolean {
    try {
      fs.copyFileSync(srcPath, destPath)
      LogUtil.info(FileUtil.TAG, `复制文件成功: ${srcPath} -> ${destPath}`)
      return true
    } catch (err) {
      LogUtil.error(FileUtil.TAG, `复制文件失败: ${err}`)
      return false
    }
  }
  
  /**
   * 移动文件
   */
  static moveFile(srcPath: string, destPath: string): boolean {
    try {
      fs.moveFileSync(srcPath, destPath)
      LogUtil.info(FileUtil.TAG, `移动文件成功: ${srcPath} -> ${destPath}`)
      return true
    } catch (err) {
      LogUtil.error(FileUtil.TAG, `移动文件失败: ${err}`)
      return false
    }
  }
  
  /**
   * 获取文件大小
   */
  static getFileSize(filePath: string): number {
    try {
      const stat = fs.statSync(filePath)
      return stat.size
    } catch (err) {
      LogUtil.error(FileUtil.TAG, `获取文件大小失败: ${err}`)
      return 0
    }
  }
  
  /**
   * 读取文件内容
   */
  static readFile(filePath: string): string {
    let file: fs.File | null = null
    try {
      file = fs.openSync(filePath, fs.OpenMode.READ_ONLY)
      const stat = fs.statSync(filePath)
      const buffer = new ArrayBuffer(stat.size)
      fs.readSync(file.fd, buffer)
      return String.fromCharCode(...new Uint8Array(buffer))
    } catch (err) {
      LogUtil.error(FileUtil.TAG, `读取文件失败: ${err}`)
      return ''
    } finally {
      try { if (file) fs.closeSync(file) } catch (_) {}
    }
  }
  
  /**
   * 写入文件内容
   */
  static writeFile(filePath: string, content: string): boolean {
    let file: fs.File | null = null
    try {
      file = fs.openSync(filePath, fs.OpenMode.CREATE | fs.OpenMode.WRITE_ONLY)
      fs.writeSync(file.fd, content)
      LogUtil.info(FileUtil.TAG, `写入文件成功: ${filePath}`)
      return true
    } catch (err) {
      LogUtil.error(FileUtil.TAG, `写入文件失败: ${err}`)
      return false
    } finally {
      try { if (file) fs.closeSync(file) } catch (_) {}
    }
  }
  
  /**
   * 生成唯一文件名
   */
  static generateFileName(extension: string): string {
    const timestamp = Date.now()
    const random = Math.floor(Math.random() * 10000)
    return `${timestamp}_${random}.${extension}`
  }
}
