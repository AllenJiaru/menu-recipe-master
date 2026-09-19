/**
 * 收藏菜谱页面 - Premium Edition
 * 展示用户收藏的菜谱列表
 */
import { CommonStyle } from '../common/CommonStyle'
import { CommonConstant, RecipeTypeMap } from '../common/CommonConstant'
import { RdbUtil } from '../common/RdbUtil'
import { LogUtil } from '../common/LogUtil'
import { ImageUtil } from '../common/ImageUtil'
import { ApiService } from '../common/ApiService'
import { ToastUtil } from '../common/ToastUtil'
import { Recipe } from '../model/Recipe'
import router from '@ohos.router'

@Entry
@Component
struct FavoriteRecipePage {
  @State recipes: Recipe[] = []
  @State isLoading: boolean = true

  aboutToAppear() {
    this.loadFavoriteRecipes()
  }

  onPageShow() {
    this.loadFavoriteRecipes()
  }

  async loadFavoriteRecipes() {
    this.isLoading = true
    try {
      const res = await ApiService.getFavorites()
      if (res.code === 200 && res.data) {
        this.recipes = (res.data as object[]).map((r: object) => Recipe.fromApi(r))
      }
    } catch (err) {
      LogUtil.error('FavoriteRecipePage', `加载收藏菜谱失败: ${JSON.stringify(err)}`)
    } finally {
      this.isLoading = false
    }
  }

  async toggleFavorite(recipe: Recipe) {
    try {
      const res = await ApiService.toggleFavorite(recipe.id)
      if (res.code === 200) {
        const index = this.recipes.findIndex(r => r.id === recipe.id)
        if (index >= 0) {
          this.recipes.splice(index, 1)
        }
        ToastUtil.showSuccess('已取消收藏')
        LogUtil.info('FavoriteRecipePage', `已取消收藏: ${recipe.name}`)
      }
    } catch (err) {
      LogUtil.error('FavoriteRecipePage', `切换收藏状态失败: ${JSON.stringify(err)}`)
    }
  }

  build() {
    Column() {
      this.NavBarBuilder()
      this.ContentBuilder()
    }
    .width('100%')
    .height('100%')
    .backgroundColor(CommonStyle.BG_PAGE)
  }

  // ── Navigation Bar ──────────────────────────────────────
  @Builder
  NavBarBuilder() {
    Row() {
      Row() {
        Text('‹')
          .fontSize(28)
          .fontColor(CommonStyle.PRIMARY)
          .fontWeight(FontWeight.Light)
          .textAlign(TextAlign.Center)
          .width(36)
          .height(36)
          .backgroundColor(CommonStyle.BG_CARD)
          .borderRadius(CommonStyle.RADIUS_MD)
          .shadow({
            color: 'rgba(0,0,0,0.04)',
            offsetX: 0,
            offsetY: 1,
            radius: 4
          })
          .onClick(() => router.back())
      }
      .margin({ left: CommonStyle.SP_XL })

      Blank()

      Text('我的收藏')
        .fontSize(CommonStyle.FONT_TITLE3)
        .fontWeight(FontWeight.Bold)
        .fontColor(CommonStyle.TEXT_PRIMARY)

      Blank()

      Text(`${this.recipes.length}`)
        .fontSize(CommonStyle.FONT_CAPTION1)
        .fontWeight(FontWeight.Medium)
        .fontColor(CommonStyle.PRIMARY)
        .backgroundColor(CommonStyle.PRIMARY_BG)
        .padding({ left: CommonStyle.SP_SM, right: CommonStyle.SP_SM, top: 2, bottom: 2 })
        .borderRadius(CommonStyle.RADIUS_CHIP)
        .margin({ right: CommonStyle.SP_XL })
    }
    .width('100%')
    .height(56)
    .backgroundColor(CommonStyle.BG_PAGE)
  }

  // ── Content ──────────────────────────────────────────────
  @Builder
  ContentBuilder() {
    if (this.isLoading) {
      Column() {
        LoadingProgress()
          .width(40)
          .height(40)
          .color(CommonStyle.PRIMARY)
      }
      .layoutWeight(1)
      .justifyContent(FlexAlign.Center)
    } else if (this.recipes.length === 0) {
      this.EmptyStateBuilder()
    } else {
      List({ space: CommonStyle.SP_MD }) {
        ForEach(this.recipes, (recipe: Recipe) => {
          ListItem() {
            this.RecipeCardBuilder(recipe)
          }
          .swipeAction({
            end: this.DeleteSwipeBuilder(recipe)
          })
        }, (recipe: Recipe) => `fav_${recipe.id}`)
      }
      .width('100%')
      .layoutWeight(1)
      .padding({
        left: CommonStyle.SP_LG,
        right: CommonStyle.SP_LG,
        top: CommonStyle.SP_SM,
        bottom: CommonStyle.SP_XXL
      })
      .scrollBar(BarState.Off)
    }
  }

  // ── Recipe Card ──────────────────────────────────────────
  @Builder
  RecipeCardBuilder(recipe: Recipe) {
    Row() {
      if (recipe.getCoverImage()) {
        Image(ImageUtil.getDisplayPath(recipe.getCoverImage()))
          .width(80)
          .height(80)
          .borderRadius(CommonStyle.RADIUS_MD)
          .objectFit(ImageFit.Cover)
      } else {
        Column() {
          Text('🍳').fontSize(28)
        }
        .width(80)
        .height(80)
        .borderRadius(CommonStyle.RADIUS_MD)
        .backgroundColor(CommonStyle.BG_SECONDARY)
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
      }

      Column() {
        Text(recipe.name)
          .fontSize(CommonStyle.FONT_HEADLINE)
          .fontWeight(FontWeight.Bold)
          .fontColor(CommonStyle.TEXT_PRIMARY)
          .maxLines(1)
          .textOverflow({ overflow: TextOverflow.Ellipsis })
          .width('100%')

        Text(RecipeTypeMap.get(recipe.getCategoryId()) || '其他')
          .fontSize(CommonStyle.FONT_CAPTION2)
          .fontWeight(FontWeight.Medium)
          .fontColor(CommonStyle.PRIMARY)
          .backgroundColor(CommonStyle.PRIMARY_BG)
          .padding({ left: CommonStyle.SP_SM, right: CommonStyle.SP_SM, top: 2, bottom: 2 })
          .borderRadius(CommonStyle.RADIUS_CHIP)
          .margin({ top: CommonStyle.SP_XS })

        Row({ space: CommonStyle.SP_MD }) {
          Text(`⏱ ${recipe.cookingTime}分钟`)
            .fontSize(CommonStyle.FONT_CAPTION2)
            .fontColor(CommonStyle.TEXT_TERTIARY)

          Row({ space: CommonStyle.SP_XS }) {
            ForEach(this.getDifficultyStars(recipe.difficulty), (filled: boolean, idx: number) => {
              Text(filled ? '★' : '☆')
                .fontSize(10)
                .fontColor(filled ? CommonStyle.ACCENT : CommonStyle.TEXT_TERTIARY)
            }, (item: boolean, idx: number) => `star_${idx}`)
          }
        }
        .margin({ top: CommonStyle.SP_SM })
        .alignItems(VerticalAlign.Center)
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1)
      .margin({ left: CommonStyle.SP_MD })
    }
    .width('100%')
    .padding(CommonStyle.SP_LG)
    .backgroundColor(CommonStyle.BG_CARD)
    .borderRadius(CommonStyle.RADIUS_CARD)
    .shadow({
      color: 'rgba(0,0,0,0.06)',
      offsetX: 0,
      offsetY: 2,
      radius: 12
    })
    .onClick(() => {
      router.pushUrl({
        url: CommonConstant.PAGE_CHEF_RECIPE_DETAIL,
        params: { recipeId: recipe.id, fromDiner: true }
      })
    })
  }

  // ── Delete Swipe Action ──────────────────────────────────
  @Builder
  DeleteSwipeBuilder(recipe: Recipe) {
    Row() {
      Text('取消收藏')
        .fontSize(CommonStyle.FONT_SUBHEAD)
        .fontColor(Color.White)
        .fontWeight(FontWeight.Medium)
    }
    .width(80)
    .height('100%')
    .backgroundColor(CommonStyle.ERROR)
    .borderRadius({ topRight: CommonStyle.RADIUS_CARD, bottomRight: CommonStyle.RADIUS_CARD })
    .justifyContent(FlexAlign.Center)
    .onClick(() => this.toggleFavorite(recipe))
  }

  // ── Empty State ──────────────────────────────────────────
  @Builder
  EmptyStateBuilder() {
    Column() {
      Text('❤️')
        .fontSize(64)
        .margin({ bottom: CommonStyle.SP_LG })

      Text('还没有收藏的菜谱')
        .fontSize(CommonStyle.FONT_CALLOUT)
        .fontWeight(FontWeight.Medium)
        .fontColor(CommonStyle.TEXT_PRIMARY)

      Text('收藏喜欢的菜谱，随时查看')
        .fontSize(CommonStyle.FONT_FOOTNOTE)
        .fontColor(CommonStyle.TEXT_SECONDARY)
        .margin({ top: CommonStyle.SP_XS })

      Text('去浏览')
        .fontSize(CommonStyle.FONT_HEADLINE)
        .fontWeight(FontWeight.Medium)
        .fontColor(Color.White)
        .textAlign(TextAlign.Center)
        .width(140)
        .height(48)
        .backgroundColor(CommonStyle.PRIMARY)
        .borderRadius(CommonStyle.RADIUS_BUTTON)
        .margin({ top: CommonStyle.SP_XXL })
        .shadow({
          color: 'rgba(255,107,107,0.3)',
          offsetX: 0,
          offsetY: 4,
          radius: 12
        })
        .onClick(() => {
          router.pushUrl({ url: CommonConstant.PAGE_RECIPE_CATEGORY })
        })
    }
    .width('100%')
    .layoutWeight(1)
    .justifyContent(FlexAlign.Center)
    .alignItems(HorizontalAlign.Center)
  }

  // ── Helper ──────────────────────────────────────────────
  getDifficultyStars(level: number): boolean[] {
    const stars: boolean[] = []
    for (let i = 1; i <= 5; i++) {
      stars.push(i <= level)
    }
    return stars
  }
}
