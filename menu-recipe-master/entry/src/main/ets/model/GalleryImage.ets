/**
 * 相册图片数据模型
 */
export class GalleryImage {
  id: number = 0
  imagePath: string = ''
  imageUrl: string = ''
  description: string = ''
  orderId: number = 0
  createTime: number = 0

  /**
   * 获取图片路径（兼容后端 imageUrl 与本地 imagePath）
   */
  getDisplayPath(): string {
    if (this.imageUrl) return this.imageUrl
    return this.imagePath
  }

  /**
   * 从后端返回的普通对象构造 GalleryImage 实例
   */
  static fromApi(raw: object): GalleryImage {
    const image = new GalleryImage()
    const data = raw as Record<string, Object>
    image.id = (data['id'] as number) || 0
    image.imageUrl = (data['imageUrl'] as string) || ''
    image.imagePath = (data['imagePath'] as string) || ''
    image.description = (data['description'] as string) || ''
    image.orderId = (data['orderId'] as number) || 0
    const createTime = data['createTime']
    if (typeof createTime === 'number') {
      image.createTime = createTime
    } else if (typeof createTime === 'string' && createTime) {
      const parsed = Date.parse(createTime as string)
      image.createTime = isNaN(parsed) ? 0 : parsed
    }
    return image
  }
}
