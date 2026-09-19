import { CommonStyle, OrderStatusStyle } from '../common/CommonStyle'
import { CommonConstant, OrderStatusMap } from '../common/CommonConstant'
import { RdbUtil } from '../common/RdbUtil'
import { ApiService } from '../common/ApiService'
import { LogUtil } from '../common/LogUtil'
import { ImageUtil } from '../common/ImageUtil'
import { ToastUtil } from '../common/ToastUtil'
import { Order } from '../model/Order'
import { RolePermissionUtil } from '../common/RolePermissionUtil'
import router from '@ohos.router'

const TIMELINE_STATUSES: string[] = ['待处理', '已接单', '制作中', '已完成']

@Entry
@Component
struct OrderDetailPage {
  @State orderId: number = 0
  @State order: Order = new Order()
  @State context: Context = getContext(this)

  aboutToAppear(): void {
    const params = router.getParams() as Record<string, number>
    if (params) {
      this.orderId = params['orderId'] ?? 0
    }
  }

  onPageShow(): void {
    this.loadOrder()
  }

  async loadOrder(): Promise<void> {
    // 优先从后端加载
    try {
      const res = await ApiService.getOrderById(this.orderId)
      if (res.code === 200 && res.data) {
        this.order = Order.fromApi(res.data)
        return
      }
    } catch (err) {
      LogUtil.error(`Failed to load order from api: ${err}`)
    }
    // 回退本地数据
    const local = await RdbUtil.queryOrderById(this.orderId)
    if (local) {
      this.order = local
    }
  }

  async updateStatus(status: number): Promise<void> {
    try {
      if (ApiService.isLoggedIn()) {
        const res = await ApiService.updateOrderStatus(this.orderId, status)
        if (res.code !== 200) {
          throw new Error(res.message)
        }
      } else {
        const ok = await RdbUtil.updateOrderStatus(this.orderId, status)
        if (!ok) {
          throw new Error('本地更新失败')
        }
      }
      ToastUtil.showToast('操作成功')
      await this.loadOrder()
    } catch (err) {
      LogUtil.error(`Failed to update order status: ${err}`)
      ToastUtil.showToast('操作失败')
    }
  }

  cancelOrder(): void {
    AlertDialog.show({
      title: '提示',
      message: '确定要取消订单吗？',
      primaryButton: {
        value: '确定',
        action: () => {
          this.updateStatus(CommonConstant.ORDER_STATUS_CANCELLED)
        }
      },
      secondaryButton: {
        value: '取消',
        action: () => {}
      }
    })
  }

  deleteOrder(): void {
    AlertDialog.show({
      title: '提示',
      message: '确定要删除订单吗？此操作不可恢复',
        primaryButton: {
          value: '确定',
          action: async () => {
            try {
              if (ApiService.isLoggedIn()) {
                await ApiService.deleteOrder(this.orderId)
              }
              await RdbUtil.deleteOrder(this.orderId)
              ToastUtil.showToast('删除成功')
              router.back()
            } catch (err) {
              LogUtil.error(`Failed to delete order: ${err}`)
              ToastUtil.showToast('删除失败')
            }
          }
        },
      secondaryButton: {
        value: '取消',
        action: () => {}
      }
    })
  }

  getStatusIndex(status: number): number {
    const statusMap: Record<number, number> = {
      [CommonConstant.ORDER_STATUS_PENDING]: 0,
      [CommonConstant.ORDER_STATUS_ACCEPTED]: 1,
      [CommonConstant.ORDER_STATUS_COOKING]: 2,
      [CommonConstant.ORDER_STATUS_COMPLETED]: 3
    }
    return statusMap[status] ?? -1
  }

  getStatusColor(status: number): string {
    const colorMap: Record<number, string> = {
      [CommonConstant.ORDER_STATUS_PENDING]: OrderStatusStyle.PENDING,
      [CommonConstant.ORDER_STATUS_ACCEPTED]: OrderStatusStyle.ACCEPTED,
      [CommonConstant.ORDER_STATUS_COOKING]: OrderStatusStyle.COOKING,
      [CommonConstant.ORDER_STATUS_COMPLETED]: OrderStatusStyle.COMPLETED,
      [CommonConstant.ORDER_STATUS_CANCELLED]: OrderStatusStyle.CANCELLED
    }
    return colorMap[status] ?? OrderStatusStyle.PENDING
  }

  getStatusBgColor(status: number): string {
    const colorMap: Record<number, string> = {
      [CommonConstant.ORDER_STATUS_PENDING]: OrderStatusStyle.PENDING_BG,
      [CommonConstant.ORDER_STATUS_ACCEPTED]: OrderStatusStyle.ACCEPTED_BG,
      [CommonConstant.ORDER_STATUS_COOKING]: OrderStatusStyle.COOKING_BG,
      [CommonConstant.ORDER_STATUS_COMPLETED]: OrderStatusStyle.COMPLETED_BG,
      [CommonConstant.ORDER_STATUS_CANCELLED]: OrderStatusStyle.CANCELLED_BG
    }
    return colorMap[status] ?? OrderStatusStyle.PENDING_BG
  }

  build() {
    Column() {
      // 顶部导航栏
      Row() {
        Image($r('app.media.ic_back'))
          .width(24)
          .height(24)
          .onClick(() => {
            router.back()
          })

        Text('订单详情')
          .fontSize(CommonStyle.TITLE3)
          .fontWeight(FontWeight.Bold)
          .fontColor(CommonStyle.TEXT_PRIMARY)
          .layoutWeight(1)
          .textAlign(TextAlign.Center)

        Text(' ')
          .width(24)
      }
      .width('100%')
      .height(56)
      .padding({ left: CommonStyle.SP.LG, right: CommonStyle.SP.LG })
      .backgroundColor(CommonStyle.BG_CARD)

      // 内容区域
      Scroll() {
        Column() {
          // 菜品图片头部
          Stack() {
            if (this.order.recipeImage) {
              Image(ImageUtil.getImageSrc(this.order.recipeImage))
                .width('100%')
                .height(180)
                .objectFit(ImageFit.Cover)
            } else {
              Image($r('app.media.default_recipe'))
                .width('100%')
                .height(180)
                .objectFit(ImageFit.Cover)
            }

            // 状态标签
            if (this.order.status !== CommonConstant.ORDER_STATUS_CANCELLED) {
              Text(OrderStatusMap[this.order.status] ?? '未知状态')
                .fontSize(CommonStyle.FOOTNOTE)
                .fontColor('#FFFFFF')
                .backgroundColor(this.getStatusColor(this.order.status))
                .borderRadius(CommonStyle.RADIUS.SM)
                .padding({ left: CommonStyle.SP.SM, right: CommonStyle.SP.SM, top: CommonStyle.SP.XS, bottom: CommonStyle.SP.XS })
                .position({ x: CommonStyle.SP.MD, y: CommonStyle.SP.MD })
            } else {
              Text('已取消')
                .fontSize(CommonStyle.FOOTNOTE)
                .fontColor('#FFFFFF')
                .backgroundColor(OrderStatusStyle.CANCELLED)
                .borderRadius(CommonStyle.RADIUS.SM)
                .padding({ left: CommonStyle.SP.SM, right: CommonStyle.SP.SM, top: CommonStyle.SP.XS, bottom: CommonStyle.SP.XS })
                .position({ x: CommonStyle.SP.MD, y: CommonStyle.SP.MD })
            }
          }
          .width('100%')
          .height(180)
          .borderRadius({ bottomLeft: CommonStyle.RADIUS.CARD, bottomRight: CommonStyle.RADIUS.CARD })
          .clip(true)

          // 订单信息卡片
          Column() {
            // 菜品名称
            Text(this.order.recipeName ?? '未知菜品')
              .fontSize(CommonStyle.HEADLINE)
              .fontWeight(FontWeight.Bold)
              .fontColor(CommonStyle.TEXT_PRIMARY)
              .margin({ bottom: CommonStyle.SP.SM })

            // 菜品类型
            Row() {
              Text('类型')
                .fontSize(CommonStyle.SUBHEAD)
                .fontColor(CommonStyle.TEXT_SECONDARY)
                .margin({ right: CommonStyle.SP.SM })

              Text(this.order.recipeType ?? '家常菜')
                .fontSize(CommonStyle.SUBHEAD)
                .fontColor(CommonStyle.TEXT_PRIMARY)
            }
            .width('100%')
            .margin({ bottom: CommonStyle.SP.SM })

            // 备注
            if (this.order.remark) {
              Row() {
                Text('备注')
                  .fontSize(CommonStyle.SUBHEAD)
                  .fontColor(CommonStyle.TEXT_SECONDARY)
                  .margin({ right: CommonStyle.SP.SM })

                Text(this.order.remark)
                  .fontSize(CommonStyle.SUBHEAD)
                  .fontColor(CommonStyle.TEXT_PRIMARY)
                  .layoutWeight(1)
              }
              .width('100%')
              .margin({ bottom: CommonStyle.SP.SM })
            }

            // 下单时间
            Row() {
              Text('下单时间')
                .fontSize(CommonStyle.SUBHEAD)
                .fontColor(CommonStyle.TEXT_SECONDARY)
                .margin({ right: CommonStyle.SP.SM })

              Text(this.order.createTime ?? '')
                .fontSize(CommonStyle.SUBHEAD)
                .fontColor(CommonStyle.TEXT_PRIMARY)
            }
            .width('100%')
          }
          .width('100%')
          .backgroundColor(CommonStyle.BG_CARD)
          .borderRadius(CommonStyle.RADIUS.CARD)
          .padding(CommonStyle.SP.LG)
          .margin({ top: CommonStyle.SP.MD, left: CommonStyle.SP.MD, right: CommonStyle.SP.MD })

          // 状态时间线
          Column() {
            Text('订单状态')
              .fontSize(CommonStyle.HEADLINE)
              .fontWeight(FontWeight.Bold)
              .fontColor(CommonStyle.TEXT_PRIMARY)
              .margin({ bottom: CommonStyle.SP.LG })

            // 时间线
            ForEach(TIMELINE_STATUSES, (status: string, index: number) => {
              Row() {
                // 左侧圆点和线条
                Column() {
                  // 圆点
                  Stack() {
                    Circle()
                      .width(16)
                      .height(16)
                      .fill(index <= this.getStatusIndex(this.order.status)
                        ? this.getStatusColor(this.order.status)
                        : CommonStyle.TEXT_TERTIARY)

                    if (index <= this.getStatusIndex(this.order.status)) {
                      Circle()
                        .width(8)
                        .height(8)
                        .fill('#FFFFFF')
                    }
                  }
                  .width(16)
                  .height(16)

                  // 连接线
                  if (index < TIMELINE_STATUSES.length - 1) {
                    Column()
                      .width(2)
                      .height(40)
                      .backgroundColor(index < this.getStatusIndex(this.order.status)
                        ? this.getStatusColor(this.order.status)
                        : CommonStyle.TEXT_TERTIARY)
                  }
                }
                .width(16)
                .alignItems(HorizontalAlign.Center)

                // 右侧状态信息
                Column() {
                  Text(status)
                    .fontSize(CommonStyle.BODY)
                    .fontWeight(index === this.getStatusIndex(this.order.status) ? FontWeight.Bold : FontWeight.Normal)
                    .fontColor(index <= this.getStatusIndex(this.order.status)
                      ? CommonStyle.TEXT_PRIMARY
                      : CommonStyle.TEXT_SECONDARY)
                }
                .margin({ left: CommonStyle.SP.MD })
                .layoutWeight(1)
              }
              .width('100%')
              .alignItems(VerticalAlign.Top)
            }, (status: string) => status)
          }
          .width('100%')
          .backgroundColor(CommonStyle.BG_CARD)
          .borderRadius(CommonStyle.RADIUS.CARD)
          .padding(CommonStyle.SP.LG)
          .margin({ top: CommonStyle.SP.MD, left: CommonStyle.SP.MD, right: CommonStyle.SP.MD })

          // 操作按钮区域
          if (this.order.status !== CommonConstant.ORDER_STATUS_COMPLETED &&
            this.order.status !== CommonConstant.ORDER_STATUS_CANCELLED) {
            Column() {
              // 厨师操作按钮
              if (RolePermissionUtil.isChef()) {
                if (this.order.status === CommonConstant.ORDER_STATUS_PENDING) {
                  Button('接单', { type: ButtonType.Capsule })
                    .width('100%')
                    .height(48)
                    .fontSize(CommonStyle.BODY)
                    .fontWeight(FontWeight.Medium)
                    .backgroundColor(CommonStyle.PRIMARY)
                    .fontColor('#FFFFFF')
                    .margin({ bottom: CommonStyle.SP.SM })
                    .onClick(() => {
                      this.updateStatus(CommonConstant.ORDER_STATUS_ACCEPTED)
                    })
                } else if (this.order.status === CommonConstant.ORDER_STATUS_ACCEPTED) {
                  Button('开始制作', { type: ButtonType.Capsule })
                    .width('100%')
                    .height(48)
                    .fontSize(CommonStyle.BODY)
                    .fontWeight(FontWeight.Medium)
                    .backgroundColor(CommonStyle.SECONDARY)
                    .fontColor('#FFFFFF')
                    .margin({ bottom: CommonStyle.SP.SM })
                    .onClick(() => {
                      this.updateStatus(CommonConstant.ORDER_STATUS_COOKING)
                    })
                } else if (this.order.status === CommonConstant.ORDER_STATUS_COOKING) {
                  Button('完成制作', { type: ButtonType.Capsule })
                    .width('100%')
                    .height(48)
                    .fontSize(CommonStyle.BODY)
                    .fontWeight(FontWeight.Medium)
                    .backgroundColor(CommonStyle.SUCCESS)
                    .fontColor('#FFFFFF')
                    .margin({ bottom: CommonStyle.SP.SM })
                    .onClick(() => {
                      this.updateStatus(CommonConstant.ORDER_STATUS_COMPLETED)
                    })
                }

                // 取消按钮（厨师）
                if (this.order.status !== CommonConstant.ORDER_STATUS_COMPLETED) {
                  Button('取消订单', { type: ButtonType.Capsule })
                    .width('100%')
                    .height(48)
                    .fontSize(CommonStyle.BODY)
                    .fontWeight(FontWeight.Medium)
                    .backgroundColor(Color.Transparent)
                    .fontColor(CommonStyle.ERROR)
                    .borderWidth(1)
                    .borderColor(CommonStyle.ERROR)
                    .margin({ bottom: CommonStyle.SP.SM })
                    .onClick(() => {
                      this.cancelOrder()
                    })
                }
              } else {
                // 食客操作按钮
                if (this.order.status === CommonConstant.ORDER_STATUS_PENDING) {
                  Button('取消订单', { type: ButtonType.Capsule })
                    .width('100%')
                    .height(48)
                    .fontSize(CommonStyle.BODY)
                    .fontWeight(FontWeight.Medium)
                    .backgroundColor(Color.Transparent)
                    .fontColor(CommonStyle.ERROR)
                    .borderWidth(1)
                    .borderColor(CommonStyle.ERROR)
                    .onClick(() => {
                      this.cancelOrder()
                    })
                }
              }
            }
            .width('100%')
            .backgroundColor(CommonStyle.BG_CARD)
            .borderRadius(CommonStyle.RADIUS.CARD)
            .padding(CommonStyle.SP.LG)
            .margin({ top: CommonStyle.SP.MD, left: CommonStyle.SP.MD, right: CommonStyle.SP.MD })
          }

          // 删除按钮（仅已取消或已完成的订单）
          if (this.order.status === CommonConstant.ORDER_STATUS_COMPLETED ||
            this.order.status === CommonConstant.ORDER_STATUS_CANCELLED) {
            Button('删除订单', { type: ButtonType.Capsule })
              .width('calc(100% - 32px)')
              .height(48)
              .fontSize(CommonStyle.BODY)
              .fontWeight(FontWeight.Medium)
              .backgroundColor(Color.Transparent)
              .fontColor(CommonStyle.ERROR)
              .borderWidth(1)
              .borderColor(CommonStyle.ERROR)
              .margin({ top: CommonStyle.SP.MD })
              .onClick(() => {
                this.deleteOrder()
              })
          }
        }
        .width('100%')
        .padding({ bottom: CommonStyle.SP.XXL })
      }
      .layoutWeight(1)
      .backgroundColor(CommonStyle.BG_PAGE)
    }
    .width('100%')
    .height('100%')
    .backgroundColor(CommonStyle.BG_PAGE)
  }
}
