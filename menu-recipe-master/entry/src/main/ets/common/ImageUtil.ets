/**
 * 图片工具类
 * 封装图片处理相关功能
 */
import image from '@ohos.multimedia.image'
import fs from '@ohos.file.fs'
import { FileUtil } from './FileUtil'
import { LogUtil } from './LogUtil'
import { CommonConstant } from './CommonConstant'
import { HttpUtil } from './HttpUtil'

// 定义缩放比例接口
interface ScaleResult {
  scaleX: number
  scaleY: number
}

export class ImageUtil {
  private static readonly TAG: string = 'ImageUtil'
  
  /**
   * 获取图片保存目录
   */
  static getImageDir(context: Context): string {
    const imageDir = `${context.filesDir}/images`
    FileUtil.createDirectory(imageDir)
    return imageDir
  }
  
  /**
   * 保存图片到应用私有目录
   */
  static async saveImage(
    context: Context,
    pixelMap: image.PixelMap,
    fileName?: string
  ): Promise<string> {
    try {
      const imageDir = ImageUtil.getImageDir(context)
      const name = fileName || FileUtil.generateFileName('jpg')
      const filePath = `${imageDir}/${name}`
      
      // 创建图像打包器
      const imagePackerApi = image.createImagePacker()
      const packOpts: image.PackingOption = {
        format: 'image/jpeg',
        quality: CommonConstant.IMAGE_QUALITY
      }
      
      // 打包图片
      const data = await imagePackerApi.packing(pixelMap, packOpts)
      
      // 写入文件
      const file = fs.openSync(filePath, fs.OpenMode.CREATE | fs.OpenMode.WRITE_ONLY)
      fs.writeSync(file.fd, data)
      fs.closeSync(file)
      
      LogUtil.info(ImageUtil.TAG, `保存图片成功: ${filePath}`)
      return filePath
    } catch (err) {
      LogUtil.error(ImageUtil.TAG, `保存图片失败: ${err}`)
      return ''
    }
  }
  
  /**
   * 压缩图片
   */
  static async compressImage(
    pixelMap: image.PixelMap,
    quality: number = CommonConstant.IMAGE_QUALITY
  ): Promise<ArrayBuffer> {
    try {
      const imagePackerApi = image.createImagePacker()
      const packOpts: image.PackingOption = {
        format: 'image/jpeg',
        quality: quality
      }
      
      const data = await imagePackerApi.packing(pixelMap, packOpts)
      LogUtil.info(ImageUtil.TAG, '压缩图片成功')
      return data
    } catch (err) {
      LogUtil.error(ImageUtil.TAG, `压缩图片失败: ${err}`)
      const error = new Error(`压缩图片失败: ${err}`)
      throw error
    }
  }
  
  /**
   * 缩放图片
   */
  static async scaleImage(
    pixelMap: image.PixelMap,
    scaleX: number,
    scaleY: number
  ): Promise<image.PixelMap> {
    try {
      await pixelMap.scale(scaleX, scaleY)
      LogUtil.info(ImageUtil.TAG, '缩放图片成功')
      return pixelMap
    } catch (err) {
      LogUtil.error(ImageUtil.TAG, `缩放图片失败: ${err}`)
      const error = new Error(`缩放图片失败: ${err}`)
      throw error
    }
  }
  
  /**
   * 裁剪图片
   */
  static async cropImage(
    pixelMap: image.PixelMap,
    region: image.Region
  ): Promise<image.PixelMap> {
    try {
      await pixelMap.crop(region)
      LogUtil.info(ImageUtil.TAG, '裁剪图片成功')
      return pixelMap
    } catch (err) {
      LogUtil.error(ImageUtil.TAG, `裁剪图片失败: ${err}`)
      const error = new Error(`裁剪图片失败: ${err}`)
      throw error
    }
  }
  
  /**
   * 获取图片信息
   */
  static async getImageInfo(pixelMap: image.PixelMap): Promise<image.ImageInfo> {
    try {
      const info = await pixelMap.getImageInfo()
      return info
    } catch (err) {
      LogUtil.error(ImageUtil.TAG, `获取图片信息失败: ${err}`)
      const error = new Error(`获取图片信息失败: ${err}`)
      throw error
    }
  }
  
  /**
   * 计算缩放比例（保持宽高比）
   */
  static calculateScale(
    originalWidth: number,
    originalHeight: number,
    targetWidth: number,
    targetHeight: number
  ): ScaleResult {
    const widthScale = targetWidth / originalWidth
    const heightScale = targetHeight / originalHeight
    const scale = Math.min(widthScale, heightScale)
    
    const result: ScaleResult = {
      scaleX: scale,
      scaleY: scale
    }
    return result
  }
  
  /**
   * 生成缩略图
   */
  static async generateThumbnail(
    pixelMap: image.PixelMap,
    thumbnailSize: number = 200
  ): Promise<image.PixelMap> {
    try {
      const info = await pixelMap.getImageInfo()
      const scale = ImageUtil.calculateScale(
        info.size.width,
        info.size.height,
        thumbnailSize,
        thumbnailSize
      )
      
      await pixelMap.scale(scale.scaleX, scale.scaleY)
      LogUtil.info(ImageUtil.TAG, '生成缩略图成功')
      return pixelMap
    } catch (err) {
      LogUtil.error(ImageUtil.TAG, `生成缩略图失败: ${err}`)
      const error = new Error(`生成缩略图失败: ${err}`)
      throw error
    }
  }
  
  /**
   * 从文件路径保存图片
   * 用于从相册选择图片后保存到应用私有目录
   */
  static async saveImageFromPath(
    context: Context,
    sourcePath: string,
    fileName?: string
  ): Promise<string> {
    let sourceFile: fs.File | null = null
    let destFile: fs.File | null = null
    try {
      const imageDir = ImageUtil.getImageDir(context)
      const name = fileName || FileUtil.generateFileName('jpg')
      const destPath = `${imageDir}/${name}`
      
      sourceFile = fs.openSync(sourcePath, fs.OpenMode.READ_ONLY)
      destFile = fs.openSync(destPath, fs.OpenMode.CREATE | fs.OpenMode.WRITE_ONLY)
      
      const stat = fs.statSync(sourcePath)
      const buffer = new ArrayBuffer(stat.size)
      fs.readSync(sourceFile.fd, buffer)
      fs.writeSync(destFile.fd, buffer)
      
      LogUtil.info(ImageUtil.TAG, `保存图片成功: ${destPath}`)
      return destPath
    } catch (err) {
      LogUtil.error(ImageUtil.TAG, `保存图片失败: ${err}`)
      return ''
    } finally {
      try { if (sourceFile) fs.closeSync(sourceFile) } catch (_) {}
      try { if (destFile) fs.closeSync(destFile) } catch (_) {}
    }
  }
  
  /**
   * 获取可显示的图片路径
   * - http(s) URL 直接返回
   * - 后端上传路径（/uploads/...）转换为文件预览接口 URL
   * - 本地绝对路径添加 file:// 前缀
   */
  static getDisplayPath(filePath: string): string {
    if (!filePath) {
      return ''
    }
    // 网络地址直接使用
    if (filePath.startsWith('http://') || filePath.startsWith('https://')) {
      return filePath
    }
    // 本地文件协议直接使用
    if (filePath.startsWith('file://')) {
      return filePath
    }
    // 后端上传的相对路径，走文件预览接口
    if (filePath.startsWith('/uploads/') || filePath.startsWith('uploads/')) {
      const baseUrl = HttpUtil.getServerBaseUrl()
      return `${baseUrl}/api/files/preview?path=${encodeURIComponent(filePath)}`
    }
    // 本地绝对路径添加 file:// 前缀
    if (filePath.startsWith('/')) {
      return `file://${filePath}`
    }
    // 其他相对路径按后端路径处理
    const baseUrl = HttpUtil.getServerBaseUrl()
    return `${baseUrl}/api/files/preview?path=${encodeURIComponent(filePath)}`
  }
}
