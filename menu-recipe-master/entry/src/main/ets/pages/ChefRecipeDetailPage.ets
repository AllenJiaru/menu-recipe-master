import { CommonStyle } from '../common/CommonStyle'
import { CommonConstant, RecipeTypeMap } from '../common/CommonConstant'
import { RdbUtil } from '../common/RdbUtil'
import { ApiService } from '../common/ApiService'
import { LogUtil } from '../common/LogUtil'
import { ImageUtil } from '../common/ImageUtil'
import { ToastUtil } from '../common/ToastUtil'
import { Recipe, RecipeMaterial, RecipeStep } from '../model/Recipe'
import router from '@ohos.router'

interface RouterParams {
  recipeId: number
  fromDiner?: boolean
}

@Entry
@Component
struct ChefRecipeDetailPage {
  @State recipe: Recipe = new Recipe()
  @State recipeId: number = 0
  @State fromDiner: boolean = false
  @State isFavorite: boolean = false
  @State context: Context = getContext(this)
  @State showDeleteDialog: boolean = false

  aboutToAppear(): void {
    const params = router.getParams() as RouterParams
    if (params) {
      this.recipeId = params.recipeId
      this.fromDiner = params.fromDiner ?? false
    }
  }

  onPageShow(): void {
    this.loadRecipe()
  }

  async loadRecipe(): Promise<void> {
    // 优先从后端加载
    try {
      const res = await ApiService.getRecipeById(this.recipeId)
      if (res.code === 200 && res.data) {
        this.recipe = Recipe.fromApi(res.data)
        this.isFavorite = this.recipe.isFavorite === 1
        return
      }
    } catch (err) {
      LogUtil.error(`Failed to load recipe from api: ${err}`)
    }
    // 后端不可用时回退本地数据
    RdbUtil.queryRecipeById(this.recipeId).then((recipe: Recipe) => {
      if (recipe) {
        this.recipe = recipe
        this.isFavorite = recipe.isFavorite === 1
      }
    }).catch((err: Error) => {
      LogUtil.error(`Failed to load recipe: ${err.message}`)
    })
  }

  async toggleFavorite(): Promise<void> {
    const nextFavorite = !this.isFavorite
    this.isFavorite = nextFavorite
    this.recipe.isFavorite = nextFavorite ? 1 : 0
    try {
      if (ApiService.isLoggedIn()) {
        const res = await ApiService.toggleFavorite(this.recipeId)
        if (res.code !== 200) {
          throw new Error(res.message)
        }
      } else {
        await RdbUtil.toggleFavorite(this.recipeId)
      }
      ToastUtil.showToast(nextFavorite ? '已收藏' : '已取消收藏')
    } catch (err) {
      LogUtil.error(`Failed to toggle favorite: ${err}`)
      this.isFavorite = !nextFavorite
      this.recipe.isFavorite = nextFavorite ? 0 : 1
      ToastUtil.showToast('操作失败，请重试')
    }
  }

  deleteRecipe(): void {
    this.showDeleteDialog = true
  }

  async confirmDelete(): Promise<void> {
    this.showDeleteDialog = false
    try {
      if (ApiService.isLoggedIn()) {
        await ApiService.deleteRecipe(this.recipeId)
      }
      await RdbUtil.deleteRecipe(this.recipeId)
      ToastUtil.showToast('删除成功')
      router.back()
    } catch (err) {
      LogUtil.error(`Failed to delete recipe: ${err}`)
      ToastUtil.showToast('删除失败，请重试')
    }
  }

  @Builder
  MaterialItem(material: RecipeMaterial) {
    Row() {
      Column() {
        Text(material.name)
          .fontSize(CommonStyle.FONT_BODY)
          .fontColor(CommonStyle.TEXT_PRIMARY)
          .fontWeight(FontWeight.Medium)
      }
      .layoutWeight(1)
      .alignItems(HorizontalAlign.Start)

      Text(`${material.amount}${material.unit}`)
        .fontSize(CommonStyle.FONT_CALLOUT)
        .fontColor(CommonStyle.TEXT_SECONDARY)
    }
    .width('100%')
    .padding({ left: CommonStyle.SP_LG, right: CommonStyle.SP_LG, top: CommonStyle.SP_MD, bottom: CommonStyle.SP_MD })
  }

  @Builder
  StepItem(step: RecipeStep, index: number) {
    Row() {
      Column() {
        Text(`${index + 1}`)
          .fontSize(CommonStyle.FONT_HEADLINE)
          .fontColor('#FFFFFF')
          .fontWeight(FontWeight.Bold)
          .textAlign(TextAlign.Center)
          .width(28)
          .height(28)
          .borderRadius(14)
          .backgroundColor(CommonStyle.SECONDARY)
      }
      .width(36)
      .alignItems(HorizontalAlign.Center)

      Column() {
        Text(step.description)
          .fontSize(CommonStyle.FONT_BODY)
          .fontColor(CommonStyle.TEXT_PRIMARY)
          .lineHeight(CommonStyle.FONT_BODY * 1.5)
          .width('100%')

        if (step.image) {
          Image(step.image)
            .width('100%')
            .height(160)
            .borderRadius(CommonStyle.RADIUS_SM)
            .objectFit(ImageFit.Cover)
            .margin({ top: CommonStyle.SP_SM })
        }
      }
      .layoutWeight(1)
      .margin({ left: CommonStyle.SP_SM })
      .alignItems(HorizontalAlign.Start)
    }
    .width('100%')
    .padding({ left: CommonStyle.SP_LG, right: CommonStyle.SP_LG, top: CommonStyle.SP_MD, bottom: CommonStyle.SP_MD })
  }

  build() {
    Column() {
      Stack({ alignContent: Alignment.Bottom }) {
        // Hero Image
        Image(this.recipe.getCoverImage())
          .width('100%')
          .height(250)
          .objectFit(ImageFit.Cover)

        // Gradient Overlay
        Column() {
          // Recipe Info
          Column() {
            // Type Tag
            if (this.recipe.getCategoryId()) {
              Text(RecipeTypeMap.get(this.recipe.getCategoryId()) || '其他')
                .fontSize(CommonStyle.FONT_CAPTION1)
                .fontColor('#FFFFFF')
                .backgroundColor(CommonStyle.PRIMARY)
                .borderRadius(CommonStyle.RADIUS_CHIP)
                .padding({ left: CommonStyle.SP_SM, right: CommonStyle.SP_SM, top: 2, bottom: 2 })
            }

            // Recipe Name
            Text(this.recipe.name || '未知菜名')
              .fontSize(CommonStyle.FONT_LARGE_TITLE)
              .fontColor('#FFFFFF')
              .fontWeight(FontWeight.Bold)
              .margin({ top: CommonStyle.SP_SM })

            // Info Row
            Row() {
              // Cooking Time
              if (this.recipe.cookingTime) {
                Row() {
                  Image($r('sys.media.ohos_ic_public_clock'))
                    .width(14)
                    .height(14)
                    .fillColor('#FFFFFF')
                  Text(`${this.recipe.cookingTime}分钟`)
                    .fontSize(CommonStyle.FONT_FOOTNOTE)
                    .fontColor('#FFFFFF')
                }
                .margin({ right: CommonStyle.SP_MD })
              }

              // Difficulty Stars
              if (this.recipe.difficulty) {
                Row() {
                  ForEach([1, 2, 3, 4, 5], (star: number) => {
                    Image(star <= (this.recipe.difficulty ?? 0) ? $r('sys.media.ohos_ic_public_star_filled') : $r('sys.media.ohos_ic_public_star'))
                      .width(14)
                      .height(14)
                      .fillColor('#FFD700')
                  }, (star: number) => `star-${star}`)
                }
              }
            }
            .margin({ top: CommonStyle.SP_SM })
            .alignItems(VerticalAlign.Center)
          }
          .width('100%')
          .padding(CommonStyle.SP_LG)
          .linearGradient({
            direction: GradientDirection.Bottom,
            colors: [['#00000000', 0], ['#CC000000', 1]]
          })
        }
        .width('100%')
        .alignItems(HorizontalAlign.Start)
      }
      .width('100%')

      // Scrollable Content
      Scroll() {
        Column() {
          // Description Card
          if (this.recipe.description) {
            Column() {
              Text('简介')
                .fontSize(CommonStyle.FONT_TITLE3)
                .fontColor(CommonStyle.TEXT_PRIMARY)
                .fontWeight(FontWeight.Bold)
                .margin({ bottom: CommonStyle.SP_SM })

              Text(this.recipe.description)
                .fontSize(CommonStyle.FONT_BODY)
                .fontColor(CommonStyle.TEXT_SECONDARY)
                .lineHeight(CommonStyle.FONT_BODY * 1.6)
            }
            .width('100%')
            .backgroundColor(CommonStyle.BG_CARD)
            .borderRadius(CommonStyle.RADIUS_CARD)
            .padding(CommonStyle.SP_LG)
            .margin({ left: CommonStyle.SP_LG, right: CommonStyle.SP_LG, top: CommonStyle.SP_LG })
          }

          // Materials Section
          if (this.recipe.materials && this.recipe.materials.length > 0) {
            Column() {
              Text('食材')
                .fontSize(CommonStyle.FONT_TITLE3)
                .fontColor(CommonStyle.TEXT_PRIMARY)
                .fontWeight(FontWeight.Bold)
                .margin({ bottom: CommonStyle.SP_SM })
                .width('100%')
                .padding({ left: CommonStyle.SP_LG, right: CommonStyle.SP_LG, top: CommonStyle.SP_LG })

              Column() {
                ForEach(this.recipe.materials, (material: RecipeMaterial) => {
                  Column() {
                    this.MaterialItem(material)
                    Divider()
                      .color(CommonStyle.SEPARATOR)
                      .margin({ left: CommonStyle.SP_LG })
                  }
                }, (material: RecipeMaterial, index: number) => `material-${index}`)
              }
              .width('100%')
              .backgroundColor(CommonStyle.BG_CARD)
              .borderRadius(CommonStyle.RADIUS_CARD)
              .margin({ left: CommonStyle.SP_LG, right: CommonStyle.SP_LG, top: CommonStyle.SP_SM })
            }
          }

          // Steps Section
          if (this.recipe.steps && this.recipe.steps.length > 0) {
            Column() {
              Text('步骤')
                .fontSize(CommonStyle.FONT_TITLE3)
                .fontColor(CommonStyle.TEXT_PRIMARY)
                .fontWeight(FontWeight.Bold)
                .margin({ bottom: CommonStyle.SP_SM })
                .width('100%')
                .padding({ left: CommonStyle.SP_LG, right: CommonStyle.SP_LG, top: CommonStyle.SP_LG })

              Column() {
                ForEach(this.recipe.steps, (step: RecipeStep, index: number) => {
                  Column() {
                    this.StepItem(step, index)
                    if (index < (this.recipe.steps?.length ?? 0) - 1) {
                      Divider()
                        .color(CommonStyle.SEPARATOR)
                        .margin({ left: CommonStyle.SP_XXL + CommonStyle.SP_LG })
                    }
                  }
                }, (step: RecipeStep, index: number) => `step-${index}`)
              }
              .width('100%')
              .backgroundColor(CommonStyle.BG_CARD)
              .borderRadius(CommonStyle.RADIUS_CARD)
              .margin({ left: CommonStyle.SP_LG, right: CommonStyle.SP_LG, top: CommonStyle.SP_SM, bottom: CommonStyle.SP_XXXL })
            }
          }
        }
        .width('100%')
      }
      .layoutWeight(1)
      .scrollBar(BarState.Off)

      // Bottom Action Bar
      Column() {
        Divider()
          .color(CommonStyle.SEPARATOR)

        Row() {
          if (this.fromDiner) {
            // Favorite Button
            Button() {
              Row() {
                Image(this.isFavorite ? $r('sys.media.ohos_ic_public_heart_filled') : $r('sys.media.ohos_ic_public_heart'))
                  .width(22)
                  .height(22)
                  .fillColor(this.isFavorite ? CommonStyle.ERROR : CommonStyle.TEXT_SECONDARY)

                Text(this.isFavorite ? '已收藏' : '收藏')
                  .fontSize(CommonStyle.FONT_FOOTNOTE)
                  .fontColor(this.isFavorite ? CommonStyle.ERROR : CommonStyle.TEXT_SECONDARY)
                  .margin({ left: CommonStyle.SP_XS })
              }
            }
            .type(ButtonType.Normal)
            .backgroundColor('transparent')
            .borderWidth(1)
            .borderColor(CommonStyle.SEPARATOR)
            .borderRadius(CommonStyle.RADIUS_BUTTON)
            .height(48)
            .layoutWeight(1)
            .margin({ right: CommonStyle.SP_SM })
            .onClick(() => this.toggleFavorite())

            // Order Button
            Button('下单')
              .fontSize(CommonStyle.FONT_HEADLINE)
              .fontColor('#FFFFFF')
              .fontWeight(FontWeight.Bold)
              .backgroundColor(CommonStyle.PRIMARY)
              .borderRadius(CommonStyle.RADIUS_BUTTON)
              .height(48)
              .layoutWeight(2)
              .onClick(() => {
                // Handle order action
              })
          } else {
            // Edit Button
            Button('编辑')
              .fontSize(CommonStyle.FONT_HEADLINE)
              .fontColor('#FFFFFF')
              .fontWeight(FontWeight.Bold)
              .backgroundColor(CommonStyle.SECONDARY)
              .borderRadius(CommonStyle.RADIUS_BUTTON)
              .height(48)
              .layoutWeight(1)
              .margin({ right: CommonStyle.SP_SM })
              .onClick(() => {
                // Handle edit action
              })

            // Delete Button
            Button('删除')
              .fontSize(CommonStyle.FONT_HEADLINE)
              .fontColor('#FFFFFF')
              .fontWeight(FontWeight.Bold)
              .backgroundColor(CommonStyle.ERROR)
              .borderRadius(CommonStyle.RADIUS_BUTTON)
              .height(48)
              .layoutWeight(1)
              .onClick(() => this.deleteRecipe())
          }
        }
        .width('100%')
        .padding({ left: CommonStyle.SP_LG, right: CommonStyle.SP_LG, top: CommonStyle.SP_MD, bottom: CommonStyle.SP_MD })
      }
      .width('100%')
      .backgroundColor(CommonStyle.BG_CARD)
    }
    .width('100%')
    .height('100%')
    .backgroundColor(CommonStyle.BG_PAGE)

    // Delete Confirmation Dialog
    if (this.showDeleteDialog) {
      Column() {
        Column() {
          Text('确认删除')
            .fontSize(CommonStyle.FONT_TITLE2)
            .fontColor(CommonStyle.TEXT_PRIMARY)
            .fontWeight(FontWeight.Bold)
            .margin({ bottom: CommonStyle.SP_MD })

          Text('确定要删除这个菜谱吗？此操作不可撤销。')
            .fontSize(CommonStyle.FONT_BODY)
            .fontColor(CommonStyle.TEXT_SECONDARY)
            .textAlign(TextAlign.Center)
            .lineHeight(CommonStyle.FONT_BODY * 1.5)

          Row() {
            Button('取消')
              .fontSize(CommonStyle.FONT_HEADLINE)
              .fontColor(CommonStyle.TEXT_PRIMARY)
              .backgroundColor(CommonStyle.SEPARATOR)
              .borderRadius(CommonStyle.RADIUS_BUTTON)
              .height(44)
              .layoutWeight(1)
              .margin({ right: CommonStyle.SP_SM })
              .onClick(() => {
                this.showDeleteDialog = false
              })

            Button('删除')
              .fontSize(CommonStyle.FONT_HEADLINE)
              .fontColor('#FFFFFF')
              .fontWeight(FontWeight.Bold)
              .backgroundColor(CommonStyle.ERROR)
              .borderRadius(CommonStyle.RADIUS_BUTTON)
              .height(44)
              .layoutWeight(1)
              .onClick(() => this.confirmDelete())
          }
          .width('100%')
          .margin({ top: CommonStyle.SP_LG })
        }
        .width('100%')
        .backgroundColor(CommonStyle.BG_CARD)
        .borderRadius(CommonStyle.RADIUS_CARD)
        .padding(CommonStyle.SP_XL)
      }
      .width('100%')
      .height('100%')
      .backgroundColor('#80000000')
      .justifyContent(FlexAlign.Center)
      .onClick(() => {
        this.showDeleteDialog = false
      })
    }
  }
}
