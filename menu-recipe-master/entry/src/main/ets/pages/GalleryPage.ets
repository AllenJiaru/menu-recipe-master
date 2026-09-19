import { CommonStyle } from '../common/CommonStyle'
import { RdbUtil } from '../common/RdbUtil'
import { LogUtil } from '../common/LogUtil'
import { ImageUtil } from '../common/ImageUtil'
import { ToastUtil } from '../common/ToastUtil'
import { ApiService } from '../common/ApiService'
import { GalleryImage } from '../model/GalleryImage'
import router from '@ohos.router'
import common from '@ohos.app.ability.common'
import picker from '@ohos.file.picker'

@Entry
@Component
struct GalleryPage {
  @State images: GalleryImage[] = []
  @State isLoading: boolean = false
  context: common.UIAbilityContext = getContext(this) as common.UIAbilityContext

  aboutToAppear() {
    this.context = getContext(this) as common.UIAbilityContext
  }

  onPageShow() {
    this.loadImages()
  }

  async loadImages() {
    this.isLoading = true
    try {
      const res = await ApiService.getAllGalleryImages()
      if (res.code === 200 && res.data) {
        this.images = (res.data as object[]).map((g: object) => GalleryImage.fromApi(g))
      }
    } catch (e) {
      LogUtil.error('Failed to load images: ' + JSON.stringify(e))
    } finally {
      this.isLoading = false
    }
  }

  async addPhoto() {
    try {
      const photoPicker = new picker.PhotoViewPicker()
      const result = await photoPicker.select({
        maxSelectNumber: 9,
        isPhotoMayTake: false,
        selectionMode: picker.PhotoViewSelectionMode.Multiple
      })

      for (const uri of result.photoUris) {
        const res = await ApiService.uploadGalleryImage(uri, '')
        if (res.code !== 200) {
          LogUtil.error('Failed to upload image: ' + res.message)
        }
      }
      await this.loadImages()
      ToastUtil.showSuccess('添加成功')
    } catch (e) {
      LogUtil.error('Failed to add photo: ' + JSON.stringify(e))
      ToastUtil.showError('添加失败')
    }
  }

  async deletePhoto(id: number) {
    try {
      const res = await ApiService.deleteGalleryImage(id)
      if (res.code === 200) {
        await this.loadImages()
        ToastUtil.showSuccess('删除成功')
      } else {
        ToastUtil.showError(res.message || '删除失败')
      }
    } catch (e) {
      LogUtil.error('Failed to delete photo: ' + JSON.stringify(e))
      ToastUtil.showError('删除失败')
    }
  }

  build() {
    Column() {
      Row() {
        Image($r('app.media.ic_back'))
          .width(24)
          .height(24)
          .fillColor('#1C1C1E')
          .onClick(() => {
            router.back()
          })

        Text('美食相册')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1C1C1E')
          .layoutWeight(1)
          .textAlign(TextAlign.Center)

        Text(this.images.length.toString())
          .fontSize(14)
          .fontColor('#8E8E93')
          .margin({ right: 4 })

        Text('+')
          .fontSize(28)
          .fontColor('#FF6B6B')
          .fontWeight(FontWeight.Bold)
          .onClick(() => this.addPhoto())
      }
      .width('100%')
      .height(56)
      .padding({ left: 16, right: 16 })
      .alignItems(VerticalAlign.Center)
      .backgroundColor('#FFFFFF')

      if (this.images.length === 0) {
        Column() {
          Text('📸')
            .fontSize(64)
            .margin({ bottom: 16 })

          Text('暂无美食照片')
            .fontSize(17)
            .fontColor('#1C1C1E')
            .fontWeight(FontWeight.Medium)
            .margin({ bottom: 8 })

          Text('点击 + 添加')
            .fontSize(14)
            .fontColor('#8E8E93')
        }
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
      } else {
        Grid() {
          ForEach(this.images, (item: GalleryImage) => {
            GridItem() {
              Stack() {
                Image(ImageUtil.getDisplayPath(item.getDisplayPath()))
                  .width('100%')
                  .aspectRatio(1)
                  .borderRadius(8)
                  .objectFit(ImageFit.Cover)

                Text('✕')
                  .fontSize(12)
                  .fontColor('#FFFFFF')
                  .textAlign(TextAlign.Center)
                  .width(20)
                  .height(20)
                  .backgroundColor('#80000000')
                  .borderRadius(10)
                  .position({ x: 0, y: 0 })
                  .onClick(() => this.deletePhoto(item.id))
              }
              .width('100%')
              .aspectRatio(1)
            }
          }, (item: GalleryImage) => item.id.toString())
        }
        .columnsTemplate('1fr 1fr 1fr')
        .columnsGap(4)
        .rowsGap(4)
        .layoutWeight(1)
        .padding(12)
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F2F2F7')
  }
}
