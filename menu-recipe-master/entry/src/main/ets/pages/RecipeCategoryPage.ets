/**
 * 菜谱分类页面 - Premium Design
 * 横向滚动分类标签 + 菜谱卡片列表
 */
import { CommonStyle } from '../common/CommonStyle'
import { CommonConstant, RecipeTypeMap } from '../common/CommonConstant'
import { RdbUtil } from '../common/RdbUtil'
import { ApiService } from '../common/ApiService'
import { LogUtil } from '../common/LogUtil'
import { ImageUtil } from '../common/ImageUtil'
import { Recipe } from '../model/Recipe'
import router from '@ohos.router'

@Entry
@Component
struct RecipeCategoryPage {
  @State selectedType: number = CommonConstant.RECIPE_TYPE_MEAT
  @State recipes: Recipe[] = []
  @State isLoading: boolean = false
  @State recipeTypes: [number, string][] = []

  aboutToAppear() {
    this.recipeTypes = Array.from(RecipeTypeMap.entries())
    this.loadRecipes()
  }

  build() {
    Column() {
      this.NavigationBar()
      this.CategoryTabs()
      this.RecipeList()
    }
    .width('100%')
    .height('100%')
    .backgroundColor(CommonStyle.BG_PAGE)
  }

  @Builder
  NavigationBar() {
    Row() {
      Row() {
        Text('<')
          .fontSize(22)
          .fontColor(CommonStyle.PRIMARY)
          .fontWeight(FontWeight.Medium)
        Text('返回')
          .fontSize(CommonStyle.FONT_BODY)
          .fontColor(CommonStyle.PRIMARY)
          .margin({ left: 4 })
      }
      .onClick(() => {
        router.back()
      })

      Blank()

      Text('菜谱分类')
        .fontSize(CommonStyle.FONT_TITLE3)
        .fontWeight(FontWeight.Bold)
        .fontColor(CommonStyle.TEXT_PRIMARY)

      Blank()

      Text('')
        .width(64)
    }
    .width('100%')
    .height(56)
    .padding({ left: CommonStyle.SP_LG, right: CommonStyle.SP_LG })
    .backgroundColor(CommonStyle.BG_CARD)
    .border({ width: { bottom: 0.5 }, color: CommonStyle.SEPARATOR })
  }

  @Builder
  CategoryTabs() {
    Scroll() {
      Row() {
        ForEach(this.recipeTypes, (item: [number, string]) => {
          Column() {
            Text(item[1])
              .fontSize(CommonStyle.FONT_SUBHEAD)
              .fontWeight(this.selectedType === item[0] ? FontWeight.Bold : FontWeight.Normal)
              .fontColor(this.selectedType === item[0] ? Color.White : CommonStyle.TEXT_PRIMARY)
              .padding({
                left: CommonStyle.SP_LG + 4,
                right: CommonStyle.SP_LG + 4,
                top: CommonStyle.SP_SM + 2,
                bottom: CommonStyle.SP_SM + 2
              })
              .borderRadius(CommonStyle.RADIUS_CHIP)
              .backgroundColor(this.selectedType === item[0] ? CommonStyle.PRIMARY : '#F0F0F5')
          }
          .margin({ right: CommonStyle.SP_SM })
          .onClick(() => {
            this.selectedType = item[0]
            this.loadRecipes()
          })
        }, (item: [number, string]) => `type_${item[0]}`)
      }
      .padding({ left: CommonStyle.SP_LG, right: CommonStyle.SP_LG })
      .height(52)
    }
    .scrollable(ScrollDirection.Horizontal)
    .scrollBar(BarState.Off)
    .width('100%')
    .backgroundColor(CommonStyle.BG_CARD)
    .border({ width: { bottom: 0.5 }, color: CommonStyle.SEPARATOR })
  }

  @Builder
  RecipeList() {
    if (this.isLoading) {
      Column() {
        LoadingProgress()
          .width(44)
          .height(44)
          .color(CommonStyle.PRIMARY)

        Text('加载中...')
          .fontSize(CommonStyle.FONT_SUBHEAD)
          .fontColor(CommonStyle.TEXT_SECONDARY)
          .margin({ top: CommonStyle.SP_MD })
      }
      .width('100%')
      .layoutWeight(1)
      .justifyContent(FlexAlign.Center)
    } else if (this.recipes.length > 0) {
      List() {
        ForEach(this.recipes, (recipe: Recipe) => {
          ListItem() {
            this.RecipeCard(recipe)
          }
          .margin({ bottom: CommonStyle.SP_SM })
        }, (recipe: Recipe) => `recipe_${recipe.id}`)
      }
      .width('100%')
      .layoutWeight(1)
      .padding({ top: CommonStyle.SP_MD, left: CommonStyle.SP_LG, right: CommonStyle.SP_LG, bottom: CommonStyle.SP_LG })
      .scrollBar(BarState.Off)
    } else {
      this.EmptyState()
    }
  }

  @Builder
  EmptyState() {
    Column() {
      Text('🍽')
        .fontSize(56)

      Text('暂无该分类的菜谱')
        .fontSize(CommonStyle.FONT_BODY)
        .fontColor(CommonStyle.TEXT_SECONDARY)
        .margin({ top: CommonStyle.SP_LG })

      Text('快去添加一道美味佳肴吧')
        .fontSize(CommonStyle.FONT_FOOTNOTE)
        .fontColor(CommonStyle.TEXT_TERTIARY)
        .margin({ top: CommonStyle.SP_SM })

      Button('去添加')
        .fontSize(CommonStyle.FONT_SUBHEAD)
        .fontWeight(FontWeight.Medium)
        .height(40)
        .borderRadius(CommonStyle.RADIUS_BUTTON)
        .backgroundColor(CommonStyle.PRIMARY)
        .fontColor(Color.White)
        .margin({ top: CommonStyle.SP_XL })
        .onClick(() => {
          router.pushUrl({ url: 'pages/AddRecipePage' })
        })
    }
    .width('100%')
    .layoutWeight(1)
    .justifyContent(FlexAlign.Center)
  }

  @Builder
  RecipeCard(recipe: Recipe) {
    Row() {
      if (recipe.getCoverImage()) {
        Image(ImageUtil.getDisplayPath(recipe.getCoverImage()))
          .width(80)
          .height(80)
          .borderRadius(CommonStyle.RADIUS_MD)
          .objectFit(ImageFit.Cover)
      } else {
        Column() {
          Text('📷')
            .fontSize(28)
        }
        .width(80)
        .height(80)
        .borderRadius(CommonStyle.RADIUS_MD)
        .backgroundColor('#F5F5FA')
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
      }

      Column() {
        Row() {
          Text(recipe.name)
            .fontSize(CommonStyle.FONT_HEADLINE)
            .fontWeight(FontWeight.Bold)
            .fontColor(CommonStyle.TEXT_PRIMARY)
            .maxLines(1)
            .textOverflow({ overflow: TextOverflow.Ellipsis })
            .layoutWeight(1)

          if (recipe.isFavorite === 1) {
            Text('♥')
              .fontSize(18)
              .fontColor(CommonStyle.PRIMARY)
          }
        }
        .width('100%')
        .alignItems(VerticalAlign.Center)

        if (recipe.description) {
          Text(recipe.description)
            .fontSize(CommonStyle.FONT_CAPTION1)
            .fontColor(CommonStyle.TEXT_SECONDARY)
            .margin({ top: 4 })
            .maxLines(2)
            .textOverflow({ overflow: TextOverflow.Ellipsis })
        }

        Row() {
          Row() {
            Text('⏱')
              .fontSize(12)
            Text(`${recipe.cookingTime}分钟`)
              .fontSize(CommonStyle.FONT_FOOTNOTE)
              .fontColor(CommonStyle.TEXT_SECONDARY)
              .margin({ left: 3 })
          }
          .alignItems(VerticalAlign.Center)

          Column() {
            Divider()
              .vertical(true)
              .height(12)
              .color(CommonStyle.SEPARATOR)
          }
          .margin({ left: CommonStyle.SP_MD })
          .height(16)
          .alignItems(HorizontalAlign.Center)

          Row() {
            Text('🔥')
              .fontSize(12)
            Text(this.getDifficultyText(recipe.difficulty))
              .fontSize(CommonStyle.FONT_FOOTNOTE)
              .fontColor(CommonStyle.TEXT_SECONDARY)
              .margin({ left: 3 })
          }
          .margin({ left: CommonStyle.SP_MD })
          .alignItems(VerticalAlign.Center)

          Text(RecipeTypeMap.get(recipe.getCategoryId()) || '其他')
            .fontSize(CommonStyle.FONT_CAPTION1)
            .fontColor(CommonStyle.SECONDARY)
            .backgroundColor(CommonStyle.SECONDARY_LIGHT)
            .padding({ left: 6, right: 6, top: 2, bottom: 2 })
            .borderRadius(4)
            .margin({ left: CommonStyle.SP_MD })
        }
        .margin({ top: CommonStyle.SP_SM })
        .alignItems(VerticalAlign.Center)
      }
      .layoutWeight(1)
      .alignItems(HorizontalAlign.Start)
      .justifyContent(FlexAlign.SpaceBetween)
      .height(80)
      .padding({ left: CommonStyle.SP_MD })
    }
    .width('100%')
    .padding(CommonStyle.SP_MD)
    .backgroundColor(CommonStyle.BG_CARD)
    .borderRadius(CommonStyle.RADIUS_CARD)
    .shadow({
      radius: 12,
      color: '#0D000000',
      offsetX: 0,
      offsetY: 2
    })
    .onClick(() => {
      router.pushUrl({ url: 'pages/ChefRecipeDetailPage', params: { recipeId: recipe.id } })
    })
  }

  getDifficultyText(difficulty: number): string {
    if (difficulty <= 1) return '简单'
    if (difficulty <= 2) return '一般'
    if (difficulty <= 3) return '中等'
    if (difficulty <= 4) return '较难'
    return '困难'
  }

  async loadRecipes() {
    this.isLoading = true
    try {
      // 优先从后端按分类加载
      if (ApiService.isLoggedIn()) {
        const res = await ApiService.getRecipes({
          page: 1,
          size: 100,
          categoryId: this.selectedType + 10
        })
        if (res.code === 200 && res.data) {
          this.recipes = (res.data.records || []).map((r: object) => Recipe.fromApi(r))
          return
        }
      }
      // 后端不可用时回退本地数据
      this.recipes = await RdbUtil.queryRecipesByType(this.selectedType)
    } catch (err) {
      LogUtil.error('RecipeCategoryPage', `加载菜谱失败: ${JSON.stringify(err)}`)
    } finally {
      this.isLoading = false
    }
  }
}
