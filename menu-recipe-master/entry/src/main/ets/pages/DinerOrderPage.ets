import { CommonStyle } from '../common/CommonStyle'
import { CommonConstant, RecipeTypeMap } from '../common/CommonConstant'
import { RdbUtil } from '../common/RdbUtil'
import { ApiService } from '../common/ApiService'
import { LogUtil } from '../common/LogUtil'
import { ImageUtil } from '../common/ImageUtil'
import { ToastUtil } from '../common/ToastUtil'
import { Recipe } from '../model/Recipe'
import { Order } from '../model/Order'
import router from '@ohos.router'

@Component
struct DinerOrderPage {
  @State recipes: Recipe[] = []
  @State selectedRecipe: Recipe | null = null
  @State remark: string = ''

  aboutToAppear() {
    this.loadRecipes()
  }

  async loadRecipes() {
    try {
      if (ApiService.isLoggedIn()) {
        const res = await ApiService.getRecipes({ page: 1, size: 100, status: 1 })
        if (res.code === 200 && res.data) {
          this.recipes = (res.data.records || []).map((r: object) => Recipe.fromApi(r))
          return
        }
      }
      this.recipes = await RdbUtil.queryAllRecipes()
    } catch (e) {
      LogUtil.error('Failed to load recipes: ' + e)
    }
  }

  async submitOrder() {
    if (!this.selectedRecipe) {
      ToastUtil.showToast('请先选择菜谱')
      return
    }

    try {
      if (ApiService.isLoggedIn()) {
        const res = await ApiService.createOrder({
          recipeId: this.selectedRecipe.id,
          remark: this.remark
        })
        if (res.code !== 200) {
          ToastUtil.showToast(res.message || '下单失败，请重试')
          return
        }
      }
      ToastUtil.showToast('下单成功')
      router.back()
    } catch (e) {
      LogUtil.error('Failed to submit order: ' + e)
      ToastUtil.showToast('下单失败，请重试')
    }
  }

  build() {
    Column() {
      // Top Nav Bar
      Row() {
        Image($r('app.media.back'))
          .width(24)
          .height(24)
          .fillColor('#1C1C1E')
          .onClick(() => {
            router.back()
          })

        Text('发起点餐')
          .fontSize(17)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1C1C1E')
          .layoutWeight(1)
          .textAlign(TextAlign.Center)
          .margin({ right: 24 })
      }
      .width('100%')
      .height(56)
      .padding({ left: 16, right: 16 })
      .backgroundColor('#FFFFFF')
      .border({ width: { bottom: 0.5 }, color: '#E5E5EA' })

      // Scrollable Content
      Scroll() {
        Column() {
          // Recipe Selection Section
          Column() {
            Text('选择菜谱')
              .fontSize(14)
              .fontColor('#8E8E93')
              .margin({ bottom: 12 })

            Column() {
              ForEach(this.recipes, (item: Recipe) => {
                Row() {
                  Image(ImageUtil.getImagePath(item.getCoverImage()))
                    .width(56)
                    .height(56)
                    .borderRadius(8)
                    .objectFit(ImageFit.Cover)

                  Column() {
                    Text(item.name)
                      .fontSize(17)
                      .fontWeight(FontWeight.Medium)
                      .fontColor('#1C1C1E')
                      .margin({ bottom: 4 })

                    Text(RecipeTypeMap.get(item.getCategoryId()) || '其他')
                      .fontSize(14)
                      .fontColor('#8E8E93')
                  }
                  .layoutWeight(1)
                  .margin({ left: 12 })
                  .alignItems(HorizontalAlign.Start)

                  if (this.selectedRecipe?.id === item.id) {
                    Text('✅')
                      .fontSize(20)
                  }
                }
                .width('100%')
                .padding(12)
                .backgroundColor(this.selectedRecipe?.id === item.id ? '#FFF0F0' : '#FFFFFF')
                .borderRadius(12)
                .border({
                  width: this.selectedRecipe?.id === item.id ? 1.5 : 0.5,
                  color: this.selectedRecipe?.id === item.id ? '#FF6B6B' : '#E5E5EA'
                })
                .margin({ bottom: 8 })
                .onClick(() => {
                  this.selectedRecipe = item
                })
              }, (item: Recipe) => item.id.toString())
            }
            .width('100%')
          }
          .width('100%')
          .padding(16)

          // Remark Section
          Column() {
            Text('备注')
              .fontSize(14)
              .fontColor('#8E8E93')
              .margin({ bottom: 12 })

            TextArea({ placeholder: '请输入特殊要求（如口味、忌口等）', text: this.remark })
              .onChange((value: string) => {
                this.remark = value
              })
              .fontSize(16)
              .fontColor('#1C1C1E')
              .placeholderColor('#C7C7CC')
              .backgroundColor('#FFFFFF')
              .borderRadius(12)
              .border({ width: 0.5, color: '#E5E5EA' })
              .height(120)
              .width('100%')
              .padding(12)
          }
          .width('100%')
          .padding({ left: 16, right: 16, bottom: 16 })

          // Submit Button
          Button('确认下单')
            .fontSize(17)
            .fontWeight(FontWeight.Medium)
            .fontColor('#FFFFFF')
            .backgroundColor(this.selectedRecipe ? '#FF6B6B' : '#C7C7CC')
            .width('100%')
            .height(50)
            .borderRadius(14)
            .enabled(this.selectedRecipe !== null)
            .onClick(() => {
              this.submitOrder()
            })
            .margin({ left: 16, right: 16, bottom: 32 })
        }
        .width('100%')
      }
      .layoutWeight(1)
      .backgroundColor('#F2F2F7')
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F2F2F7')
  }
}

export default DinerOrderPage