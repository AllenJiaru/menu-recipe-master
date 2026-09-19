/**
 * 订单数据模型
 */
export class Order {
  id: number = 0
  recipeId: number = 0
  recipeName: string = ''
  recipeImage: string = ''
  status: number = 0
  remark: string = ''
  rejectReason: string = ''
  orderTime: number = 0
  acceptTime: number = 0
  completeTime: number = 0
  userId: number = 0

  /**
   * 时间解析（兼容 ISO 字符串与毫秒时间戳）
   */
  static parseTime(value: number | string | null | undefined): number {
    if (value === null || value === undefined || value === '') return 0
    if (typeof value === 'number') return value
    const parsed = Date.parse(value as string)
    return isNaN(parsed) ? 0 : parsed
  }

  /**
   * 从后端返回的普通对象构造 Order 实例
   */
  static fromApi(raw: object): Order {
    const order = new Order()
    const data = raw as Record<string, Object>
    order.id = (data['id'] as number) || 0
    order.recipeId = (data['recipeId'] as number) || 0
    order.recipeName = (data['recipeName'] as string) || ''
    order.recipeImage = (data['recipeImage'] as string) || ''
    order.status = (data['status'] as number) || 0
    order.remark = (data['remark'] as string) || ''
    order.rejectReason = (data['rejectReason'] as string) || ''
    order.orderTime = Order.parseTime(data['orderTime'] as string)
    order.acceptTime = Order.parseTime(data['acceptTime'] as string)
    order.completeTime = Order.parseTime(data['completeTime'] as string)
    order.userId = (data['userId'] as number) || 0
    return order
  }
}
