import { CommonStyle, RoleStyle, OrderStatusStyle } from '../common/CommonStyle'
import { CommonConstant, RecipeTypeMap, OrderStatusMap } from '../common/CommonConstant'
import { RdbUtil } from '../common/RdbUtil'
import { LogUtil } from '../common/LogUtil'
import { ImageUtil } from '../common/ImageUtil'
import { RolePermissionUtil } from '../common/RolePermissionUtil'
import { ToastUtil } from '../common/ToastUtil'
import { ApiService } from '../common/ApiService'
import { CoupleConfig } from '../model/CoupleConfig'
import { Recipe } from '../model/Recipe'
import { Order } from '../model/Order'
import { GalleryImage } from '../model/GalleryImage'
import router from '@ohos.router'
import common from '@ohos.app.ability.common'
import relationalStore from '@ohos.data.relationalStore'
import picker from '@ohos.file.picker'

@Entry
@Component
struct ChefHomePage {
  @State currentTab: number = 0
  @State coupleConfig: CoupleConfig = new CoupleConfig()
  @State pendingOrderCount: number = 0
  @State recipeCount: number = 0
  @State recipes: Recipe[] = []
  @State allRecipes: Recipe[] = []
  @State orders: Order[] = []
  @State galleryImages: GalleryImage[] = []
  @State isLoading: boolean = false
  @State selectedCategory: number = 0
  @State searchKeyword: string = ''
  @State selectedDifficulty: number = 0  // 0=全部, 1=简单, 2=较简单, 3=中等, 4=较困难, 5=困难
  @State selectedStatus: number = -1     // -1=全部, 0=下架, 1=上架
  // ── 订单多选 ──
  @State isOrderSelectionMode: boolean = false
  @State selectedOrderIds: Set<number> = new Set()
  @State showBatchDialog: boolean = false
  @State batchAction: string = ''
  private context: common.UIAbilityContext = {} as common.UIAbilityContext

  aboutToAppear(): void {
    this.context = getContext(this) as common.UIAbilityContext
    this.initData()
  }

  onPageShow(): void {
    this.loadRecipes()
    this.loadOrders()
    this.loadGallery()
    this.loadPendingOrderCount()
    this.loadRecipeCount()
  }

  async initData(): Promise<void> {
    RolePermissionUtil.init(this.context)
    await this.loadCoupleConfig()
  }

  async loadCoupleConfig(): Promise<void> {
    try {
      const res = await ApiService.getCoupleConfig()
      if (res.code === 200 && res.data) {
        const config = res.data as Record<string, Object>
        this.coupleConfig.space_name = (config['spaceName'] as string) || ''
        this.coupleConfig.chef_name = (config['chefName'] as string) || ''
        this.coupleConfig.diner_name = (config['dinerName'] as string) || ''
      }
    } catch (e) {
      LogUtil.error('loadCoupleConfig error: ' + e)
    }
  }

  async loadRecipes(): Promise<void> {
    try {
      this.isLoading = true
      const res = await ApiService.getRecipes({ page: 1, size: 100 })
      if (res.code === 200 && res.data) {
        this.allRecipes = (res.data.records || []).map((r: object) => Recipe.fromApi(r))
        this.recipeCount = res.data.total || 0
        this.filterRecipes()
      }
    } catch (e) {
      LogUtil.error('loadRecipes error: ' + e)
    } finally {
      this.isLoading = false
    }
  }

  filterRecipes() {
    let filtered = [...this.allRecipes]

    // 按分类筛选 - 使用 getCategoryId() 兼容不同字段名
    if (this.selectedCategory !== 0) {
      filtered = filtered.filter(r => r.getCategoryId() === this.selectedCategory)
    }

    // 按难度筛选
    if (this.selectedDifficulty !== 0) {
      filtered = filtered.filter(r => r.difficulty === this.selectedDifficulty)
    }

    // 按状态筛选
    if (this.selectedStatus !== -1) {
      filtered = filtered.filter(r => r.status === this.selectedStatus)
    }

    // 按关键词搜索
    if (this.searchKeyword.trim()) {
      const keyword = this.searchKeyword.trim().toLowerCase()
      filtered = filtered.filter(r =>
        r.name.toLowerCase().includes(keyword) ||
        (r.description && r.description.toLowerCase().includes(keyword))
      )
    }

    this.recipes = filtered
  }

  async loadOrders(): Promise<void> {
    try {
      const res = await ApiService.getAllOrders()
      if (res.code === 200 && res.data) {
        this.orders = (res.data as object[]).map((o: object) => Order.fromApi(o))
      }
    } catch (e) {
      LogUtil.error('loadOrders error: ' + e)
    }
  }

  async loadGallery(): Promise<void> {
    try {
      const res = await ApiService.getAllGalleryImages()
      if (res.code === 200 && res.data) {
        this.galleryImages = (res.data as object[]).map((g: object) => GalleryImage.fromApi(g))
      }
    } catch (e) {
      LogUtil.error('loadGallery error: ' + e)
    }
  }

  async loadPendingOrderCount(): Promise<void> {
    try {
      const res = await ApiService.getOrders({ status: 0 })
      if (res.code === 200 && res.data) {
        this.pendingOrderCount = res.data.total || 0
      }
    } catch (e) {
      LogUtil.error('loadPendingOrderCount error: ' + e)
    }
  }

  async loadRecipeCount(): Promise<void> {
    try {
      const res = await ApiService.getRecipes({ page: 1, size: 1 })
      if (res.code === 200 && res.data) {
        this.recipeCount = res.data.total || 0
      }
    } catch (e) {
      LogUtil.error('loadRecipeCount error: ' + e)
    }
  }

  async addGalleryPhoto(): Promise<void> {
    try {
      const photoPicker = new picker.PhotoViewPicker()
      const result = await photoPicker.select({
        maxSelectNumber: 9,
        isPhotoTakingEnable: true,
        isCrop: false
      })
      if (result && result.photoUris && result.photoUris.length > 0) {
        for (const uri of result.photoUris) {
          const res = await ApiService.uploadGalleryImage(uri, '')
          if (res.code !== 200) {
            LogUtil.error('addGalleryPhoto upload failed: ' + res.message)
          }
        }
        ToastUtil.showToast('添加成功')
        await this.loadGallery()
      }
    } catch (e) {
      LogUtil.error('addGalleryPhoto error: ' + e)
      ToastUtil.showToast('添加失败')
    }
  }

  async deleteGalleryPhoto(id: number): Promise<void> {
    try {
      const res = await ApiService.deleteGalleryImage(id)
      if (res.code === 200) {
        ToastUtil.showToast('已删除')
        await this.loadGallery()
      } else {
        ToastUtil.showToast(res.message || '删除失败')
      }
    } catch (e) {
      LogUtil.error('deleteGalleryPhoto error: ' + e)
    }
  }

  @Builder
  HeaderBuilder() {
    Row() {
      Column() {
        Text('主厨工作台')
          .fontSize(34)
          .fontWeight(FontWeight.Bold)
          .fontColor(CommonStyle.TEXT_PRIMARY)
        Text('主厨：' + this.coupleConfig.chef_name)
          .fontSize(15)
          .fontColor(CommonStyle.TEXT_SECONDARY)
          .margin({ top: SP_XS })
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1)

      Text('⚙')
        .fontSize(24)
        .fontColor(CommonStyle.TEXT_PRIMARY)
        .width(44)
        .height(44)
        .textAlign(TextAlign.Center)
        .backgroundColor(CommonStyle.BG_CARD)
        .borderRadius(RADIUS_MD)
        .shadow({
          color: 'rgba(0,0,0,0.04)',
          offsetX: 0,
          offsetY: 2,
          radius: 8
        })
        .onClick(() => {
          router.pushUrl({ url: 'pages/SettingsPage' })
        })
    }
    .width('100%')
    .padding({ left: SP_XL, right: SP_XL, top: SP_XXXL, bottom: SP_MD })
  }

  @Builder
  PendingOrderBannerBuilder() {
    if (this.pendingOrderCount > 0) {
      Row() {
        Text('🔔')
          .fontSize(20)
          .margin({ right: SP_SM })
        Text(`有 ${this.pendingOrderCount} 个待处理订单`)
          .fontSize(15)
          .fontWeight(FontWeight.Medium)
          .fontColor('#FFFFFF')
        Text(' >')
          .fontSize(15)
          .fontColor('#FFFFFF')
          .layoutWeight(1)
          .textAlign(TextAlign.Right)
      }
      .width('100%')
      .height(52)
      .padding({ left: SP_LG, right: SP_LG })
      .linearGradient({
        direction: GradientDirection.Right,
        colors: [['#FF6B6B', 0], ['#FF8E8E', 1]]
      })
      .borderRadius(RADIUS_CARD)
      .margin({ left: SP_XL, right: SP_XL, bottom: SP_MD })
      .onClick(() => {
        this.currentTab = 1
      })
    }
  }

  @Builder
  TabBarBuilder() {
    Row() {
      ForEach(['菜谱管理', '订单管理', '美食相册'], (item: string, index: number) => {
        Column() {
          Text(item)
            .fontSize(15)
            .fontWeight(this.currentTab === index ? FontWeight.Bold : FontWeight.Normal)
            .fontColor(this.currentTab === index ? CommonStyle.PRIMARY : CommonStyle.TEXT_SECONDARY)
            .padding({ top: SP_SM, bottom: SP_SM })

          Divider()
            .strokeWidth(2)
            .color(this.currentTab === index ? CommonStyle.PRIMARY : 'transparent')
            .width(40)
            .margin({ top: 4 })
            .borderRadius(1)
        }
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Center)
        .onClick(() => {
          this.currentTab = index
        })
      }, (item: string) => item)
    }
    .width('100%')
    .height(48)
    .padding({ left: SP_XL, right: SP_XL })
    .backgroundColor(CommonStyle.BG_PAGE)
  }

  @Builder
  RecipeQuickActionsBuilder() {
    Column() {
      // 统计卡片行
      Row() {
        // 总菜谱卡片
        Column() {
          Text(`${this.recipeCount}`)
            .fontSize(28)
            .fontWeight(FontWeight.Bold)
            .fontColor('#FF6B6B')
          Text('总菜谱')
            .fontSize(12)
            .fontColor(CommonStyle.TEXT_SECONDARY)
            .margin({ top: 4 })
        }
        .layoutWeight(1)
        .height(80)
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
        .backgroundColor('#FFF5F5')
        .borderRadius(RADIUS_MD)

        Blank().width(12)

        // 我的收藏卡片
        Column() {
          Text('0')
            .fontSize(28)
            .fontWeight(FontWeight.Bold)
            .fontColor('#FF9500')
          Text('我的收藏')
            .fontSize(12)
            .fontColor(CommonStyle.TEXT_SECONDARY)
            .margin({ top: 4 })
        }
        .layoutWeight(1)
        .height(80)
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
        .backgroundColor('#FFF8E1')
        .borderRadius(RADIUS_MD)

        Blank().width(12)

        // 新建按钮
        Column() {
          Text('+')
            .fontSize(28)
            .fontWeight(FontWeight.Bold)
            .fontColor(Color.White)
          Text('新建')
            .fontSize(12)
            .fontColor(Color.White)
            .margin({ top: 4 })
        }
        .layoutWeight(1)
        .height(80)
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
        .linearGradient({ direction: GradientDirection.Right, colors: [['#FF6B6B', 0], ['#FF8E5A', 1]] })
        .borderRadius(RADIUS_MD)
        .shadow({ radius: 8, color: '#FF6B6B40', offsetX: 0, offsetY: 4 })
        .onClick(() => {
          router.pushUrl({ url: 'pages/CreateRecipePage' })
        })
      }
      .width('100%')

      // 搜索栏
      Row() {
        Text('🔍')
          .fontSize(16)
          .margin({ right: 8 })
        TextInput({ placeholder: '搜索菜谱名称...', text: this.searchKeyword })
          .fontSize(14)
          .layoutWeight(1)
          .backgroundColor('transparent')
          .onChange((value: string) => {
            this.searchKeyword = value
            this.filterRecipes()
          })
        if (this.searchKeyword) {
          Text('✕')
            .fontSize(16)
            .fontColor(CommonStyle.TEXT_TERTIARY)
            .onClick(() => {
              this.searchKeyword = ''
              this.filterRecipes()
            })
        }
      }
      .width('100%')
      .height(44)
      .padding({ left: 12, right: 12 })
      .backgroundColor(CommonStyle.BG_CARD)
      .borderRadius(RADIUS_MD)
      .margin({ top: 12 })

      // 筛选栏
      Row() {
        // 难度筛选
        Select(this.getDifficultyOptions())
          .value(this.getDifficultyLabel())
          .fontSize(13)
          .fontColor(CommonStyle.TEXT_PRIMARY)
          .selectedOptionFontColor(CommonStyle.PRIMARY)
          .onSelect((_index: number, value: string) => {
            this.selectedDifficulty = this.getDifficultyValue(value)
            this.filterRecipes()
          })

        Blank().width(12)

        // 状态筛选
        Select(this.getStatusOptions())
          .value(this.getStatusLabel())
          .fontSize(13)
          .fontColor(CommonStyle.TEXT_PRIMARY)
          .selectedOptionFontColor(CommonStyle.PRIMARY)
          .onSelect((_index: number, value: string) => {
            this.selectedStatus = this.getStatusValue(value)
            this.filterRecipes()
          })

        Blank().width(12)

        // 重置按钮
        Text('↻ 重置')
          .fontSize(13)
          .fontColor(CommonStyle.TEXT_SECONDARY)
          .onClick(() => {
            this.searchKeyword = ''
            this.selectedDifficulty = 0
            this.selectedStatus = -1
            this.selectedCategory = 0
            this.filterRecipes()
          })
      }
      .width('100%')
      .padding({ top: 8 })
      .justifyContent(FlexAlign.Start)

      // 筛选结果提示
      if (this.searchKeyword || this.selectedDifficulty !== 0 || this.selectedStatus !== -1 || this.selectedCategory !== 0) {
        Row() {
          Text(`筛选结果: ${this.recipes.length} 道菜谱`)
            .fontSize(12)
            .fontColor(CommonStyle.TEXT_SECONDARY)
          Blank()
          Text('清除筛选')
            .fontSize(12)
            .fontColor(CommonStyle.PRIMARY)
            .onClick(() => {
              this.searchKeyword = ''
              this.selectedDifficulty = 0
              this.selectedStatus = -1
              this.selectedCategory = 0
              this.filterRecipes()
            })
        }
        .width('100%')
        .padding({ top: 8 })
      }

      // 分类筛选标签
      Scroll() {
        Row() {
          this.CategoryTab('全部', 0)
          ForEach(Array.from(RecipeTypeMap.entries()), (item: [number, string]) => {
            this.CategoryTab(item[1], item[0])
          }, (item: [number, string]) => item[0].toString())
        }
        .width('100%')
        .justifyContent(FlexAlign.Start)
      }
      .scrollable(ScrollDirection.Horizontal)
      .scrollBar(BarState.Off)
      .width('100%')
      .margin({ top: 8 })
    }
    .width('100%')
    .padding({ left: SP_XL, right: SP_XL, top: SP_MD, bottom: SP_MD })
  }

  // ── Helper Methods for Filters ──
  getDifficultyOptions(): SelectOption[] {
    return [
      { value: '全部难度' },
      { value: '简单' },
      { value: '较简单' },
      { value: '中等' },
      { value: '较困难' },
      { value: '困难' }
    ]
  }

  getDifficultyLabel(): string {
    const labels = ['全部难度', '简单', '较简单', '中等', '较困难', '困难']
    return labels[this.selectedDifficulty]
  }

  getDifficultyValue(label: string): number {
    const map: Record<string, number> = {
      '全部难度': 0,
      '简单': 1,
      '较简单': 2,
      '中等': 3,
      '较困难': 4,
      '困难': 5
    }
    return map[label] || 0
  }

  getStatusOptions(): SelectOption[] {
    return [
      { value: '全部状态' },
      { value: '上架' },
      { value: '下架' }
    ]
  }

  getStatusLabel(): string {
    if (this.selectedStatus === -1) return '全部状态'
    return this.selectedStatus === 1 ? '上架' : '下架'
  }

  getStatusValue(label: string): number {
    if (label === '上架') return 1
    if (label === '下架') return 0
    return -1
  }

  @Builder
  CategoryTab(label: string, index: number) {
    Text(label)
      .fontSize(13)
      .fontWeight(this.selectedCategory === index ? FontWeight.Medium : FontWeight.Normal)
      .fontColor(this.selectedCategory === index ? Color.White : CommonStyle.TEXT_PRIMARY)
      .backgroundColor(this.selectedCategory === index ? '#FF6B6B' : CommonStyle.BG_SECONDARY)
      .padding({ left: 14, right: 14, top: 6, bottom: 6 })
      .borderRadius(RADIUS_CHIP)
      .margin({ right: 8 })
      .onClick(() => {
        this.selectedCategory = index
        this.filterRecipes()
      })
  }

  @Builder
  SectionHeaderBuilder(title: string, count?: number) {
    Row() {
      Text(title)
        .fontSize(18)
        .fontWeight(FontWeight.Bold)
        .fontColor(CommonStyle.TEXT_PRIMARY)
      if (count !== undefined) {
        Text(`共 ${count} 个`)
          .fontSize(13)
          .fontColor(CommonStyle.TEXT_SECONDARY)
          .margin({ left: 8 })
      }
    }
    .width('100%')
    .padding({ left: SP_XL, right: SP_XL, top: SP_MD, bottom: SP_SM })
  }

  @Builder
  RecipeCardBuilder(recipe: Recipe) {
    Column() {
      // Hero Image with Type Badge Overlay
      Stack({ alignContent: Alignment.TopStart }) {
        if (recipe.getCoverImage()) {
          Image(ImageUtil.getDisplayPath(recipe.getCoverImage()))
            .width('100%')
            .height(180)
            .borderRadius({ topLeft: RADIUS_CARD, topRight: RADIUS_CARD })
            .objectFit(ImageFit.Cover)
        } else {
          Column() {
            Text('🍽')
              .fontSize(48)
            Text('暂无图片')
              .fontSize(13)
              .fontColor(CommonStyle.TEXT_TERTIARY)
              .margin({ top: SP_SM })
          }
          .width('100%')
          .height(180)
          .backgroundColor('#F8F8FA')
          .justifyContent(FlexAlign.Center)
          .alignItems(HorizontalAlign.Center)
          .borderRadius({ topLeft: RADIUS_CARD, topRight: RADIUS_CARD })
        }

        // Type Badge (top-left)
        Text(RecipeTypeMap.get(recipe.getCategoryId()) || '其他')
          .fontSize(11)
          .fontColor(CommonStyle.PRIMARY)
          .backgroundColor('#FFFFFFDD')
          .padding({ left: 8, right: 8, top: 3, bottom: 3 })
          .borderRadius(RADIUS_CHIP)
          .margin({ left: SP_SM, top: SP_SM })
          .fontWeight(FontWeight.Medium)

        // Difficulty Badge (top-right)
        Row() {
          Text(recipe.difficulty === '简单' ? '🟢' : recipe.difficulty === '困难' ? '🔴' : '🟡')
            .fontSize(8)
          Text(recipe.difficulty || '普通')
            .fontSize(10)
            .fontColor(Color.White)
        }
        .backgroundColor(recipe.difficulty === '简单' ? CommonStyle.SUCCESS :
          recipe.difficulty === '困难' ? CommonStyle.ERROR : CommonStyle.ACCENT)
        .padding({ left: 6, right: 6, top: 2, bottom: 2 })
        .borderRadius(RADIUS_SM)
        .position({ right: SP_SM, top: SP_SM })
      }
      .width('100%')

      // Content Section
      Column() {
        // Recipe Name
        Text(recipe.name)
          .fontSize(17)
          .fontWeight(FontWeight.Bold)
          .fontColor(CommonStyle.TEXT_PRIMARY)
          .maxLines(1)
          .textOverflow({ overflow: TextOverflow.Ellipsis })
          .width('100%')

        // Description
        if (recipe.description) {
          Text(recipe.description)
            .fontSize(13)
            .fontColor(CommonStyle.TEXT_SECONDARY)
            .maxLines(2)
            .textOverflow({ overflow: TextOverflow.Ellipsis })
            .margin({ top: SP_XS })
            .width('100%')
        }

        // Cooking Info Row
        Row() {
          // Cooking Time
          Row() {
            Text('⏱')
              .fontSize(12)
            Text(recipe.cookingTime || '未知')
              .fontSize(12)
              .fontColor(CommonStyle.TEXT_SECONDARY)
              .margin({ left: 2 })
          }

          Text('  ·  ')
            .fontSize(12)
            .fontColor(CommonStyle.TEXT_TERTIARY)

          // Ingredients Count
          Row() {
            Text('🥬')
              .fontSize(12)
            Text(this.getIngredientCount(recipe.ingredients))
              .fontSize(12)
              .fontColor(CommonStyle.TEXT_SECONDARY)
              .margin({ left: 2 })
          }

          Blank()

          // Arrow Icon
          Text('›')
            .fontSize(18)
            .fontColor(CommonStyle.TEXT_TERTIARY)
            .fontWeight(FontWeight.Light)
        }
        .width('100%')
        .margin({ top: SP_SM })
      }
      .padding({ left: SP_MD, right: SP_MD, top: SP_SM, bottom: SP_MD })
      .alignItems(HorizontalAlign.Start)
    }
    .width('100%')
    .backgroundColor(CommonStyle.BG_CARD)
    .borderRadius(RADIUS_CARD)
    .shadow({
      color: 'rgba(0,0,0,0.08)',
      offsetX: 0,
      offsetY: 4,
      radius: 12
    })
    .margin({ left: SP_XL, right: SP_XL, bottom: SP_MD })
    .clip(true)
    .onClick(() => {
      router.pushUrl({ url: 'pages/RecipeDetailPage', params: { recipeId: recipe.id } })
    })
  }

  private getIngredientCount(ingredients: string): string {
    if (!ingredients) return '0种食材'
    const list = ingredients.split(/[,，、]/)
    return `${list.length}种食材`
  }

  @Builder
  OrderCardBuilder(order: Order) {
    Row() {
      // 选择框
      if (this.isOrderSelectionMode) {
        Column() {
          Text(this.isOrderSelected(order.id) ? '☑' : '☐')
            .fontSize(20)
            .fontColor(this.isOrderSelected(order.id) ? CommonStyle.PRIMARY : CommonStyle.TEXT_TERTIARY)
        }
        .width(32).height(60)
        .justifyContent(FlexAlign.Center)
        .onClick(() => {
          this.toggleOrderSelection(order.id)
        })
      }

      // 图片
      if (order.recipeImage) {
        Image(ImageUtil.getDisplayPath(order.recipeImage))
          .width(60).height(60)
          .borderRadius(8)
          .objectFit(ImageFit.Cover)
      } else {
        Column() { Text('🍽').fontSize(24) }
          .width(60).height(60).borderRadius(8)
          .backgroundColor('#F5F5F5').justifyContent(FlexAlign.Center)
      }

      // 订单信息
      Column() {
        Row() {
          Text(order.recipeName)
            .fontSize(15).fontWeight(FontWeight.Medium)
            .fontColor(CommonStyle.TEXT_PRIMARY)
            .layoutWeight(1)
            .maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })

          Text(OrderStatusMap.get(order.status) || '未知')
            .fontSize(11)
            .fontColor(OrderStatusStyle.getStatusColor(order.status))
            .backgroundColor(OrderStatusStyle.getStatusBg(order.status))
            .padding({ left: 8, right: 8, top: 3, bottom: 3 })
            .borderRadius(12)
        }
        .width('100%')

        if (order.remark) {
          Text(order.remark)
            .fontSize(13).fontColor(CommonStyle.TEXT_SECONDARY)
            .maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
            .margin({ top: 4 }).width('100%')
        }

        Row() {
          Text(this.formatTime(order.orderTime))
            .fontSize(12).fontColor(CommonStyle.TEXT_TERTIARY)
          if (!this.isOrderSelectionMode) {
            Text(' ›')
              .fontSize(14).fontColor(CommonStyle.TEXT_TERTIARY)
          }
        }
        .width('100%')
        .margin({ top: 4 })
      }
      .layoutWeight(1).alignItems(HorizontalAlign.Start)
      .margin({ left: 12 })
    }
    .width('100%')
    .padding(12)
    .backgroundColor(this.isOrderSelected(order.id) ? '#FFF0F0' : CommonStyle.BG_CARD)
    .borderRadius(12)
    .shadow({ radius: 4, color: '#00000008', offsetX: 0, offsetY: 2 })
    .margin({ left: 16, right: 16, bottom: 8 })
    .onClick(() => {
      if (this.isOrderSelectionMode) {
        this.toggleOrderSelection(order.id)
      } else {
        router.pushUrl({ url: 'pages/OrderDetailPage', params: { orderId: order.id } })
      }
    })
    .onLongPress(() => {
      if (!this.isOrderSelectionMode) {
        this.enterOrderSelectionMode(order.id)
      }
    })
  }

  @Builder
  GalleryGridBuilder() {
    Column() {
      Row() {
        Text('美食相册')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor(CommonStyle.TEXT_PRIMARY)
          .layoutWeight(1)

        Text('+')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor(CommonStyle.PRIMARY)
          .width(32)
          .height(32)
          .textAlign(TextAlign.Center)
          .backgroundColor(CommonStyle.CHEF_LIGHT)
          .borderRadius(RADIUS_ROUND)
          .onClick(() => {
            this.addGalleryPhoto()
          })
      }
      .width('100%')
      .padding({ left: SP_XL, right: SP_XL, top: SP_MD, bottom: SP_MD })

      if (this.galleryImages.length === 0) {
        Column() {
          Text('📷')
            .fontSize(48)
            .margin({ bottom: SP_MD })
          Text('还没有照片')
            .fontSize(15)
            .fontColor(CommonStyle.TEXT_SECONDARY)
          Text('点击右上角 + 添加')
            .fontSize(13)
            .fontColor(CommonStyle.TEXT_TERTIARY)
            .margin({ top: SP_XS })
        }
        .width('100%')
        .height(200)
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
      } else {
        Flex({ wrap: FlexWrap.Wrap }) {
          ForEach(this.galleryImages, (image: GalleryImage) => {
            Stack() {
              Image(ImageUtil.getDisplayPath(image.getDisplayPath()))
                .width('31%')
                .aspectRatio(1)
                .borderRadius(RADIUS_SM)
                .objectFit(ImageFit.Cover)

              Column() {
                Text('✕')
                  .fontSize(12)
                  .fontColor('#FFFFFF')
                  .width(20)
                  .height(20)
                  .textAlign(TextAlign.Center)
                  .backgroundColor('rgba(0,0,0,0.5)')
                  .borderRadius(RADIUS_ROUND)
              }
              .position({ x: 0, y: 0 })
              .padding(4)
              .onClick(() => {
                this.deleteGalleryPhoto(image.id)
              })
            }
            .width('31%')
            .aspectRatio(1)
            .margin({ left: '1%', right: '1%', bottom: SP_SM })
          }, (image: GalleryImage) => image.id.toString())
        }
        .width('100%')
        .padding({ left: SP_XL, right: SP_XL })
      }
    }
    .width('100%')
  }

  @Builder
  RecipeTabBuilder() {
    Column() {
      this.RecipeQuickActionsBuilder()

      this.SectionHeaderBuilder('我的菜谱', this.recipeCount)

      if (this.recipes.length === 0) {
        Column() {
          Text('🍳')
            .fontSize(48)
            .margin({ bottom: SP_MD })
          Text('还没有菜谱')
            .fontSize(15)
            .fontColor(CommonStyle.TEXT_SECONDARY)
            .fontWeight(FontWeight.Medium)
          Text('点击上方新建你的第一个菜谱')
            .fontSize(13)
            .fontColor(CommonStyle.TEXT_TERTIARY)
            .margin({ top: SP_XS })
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
      } else {
        List() {
          ForEach(this.recipes, (recipe: Recipe) => {
            ListItem() {
              this.RecipeCardBuilder(recipe)
            }
          }, (recipe: Recipe) => recipe.id.toString())
        }
        .width('100%')
        .layoutWeight(1)
        .scrollBar(BarState.Off)
        .edgeEffect(EdgeEffect.Spring)
      }
    }
    .width('100%')
    .layoutWeight(1)
  }

  @Builder
  OrderTabBuilder() {
    Column() {
      // 选择模式头部
      if (this.isOrderSelectionMode) {
        Row() {
          Text('取消')
            .fontSize(15).fontColor(CommonStyle.PRIMARY)
            .onClick(() => this.exitOrderSelectionMode())

          Blank()

          Text(`已选 ${this.selectedOrderIds.size} 个`)
            .fontSize(15).fontWeight(FontWeight.Medium)
            .fontColor(CommonStyle.TEXT_PRIMARY)

          Blank()

          Text(this.selectedOrderIds.size === this.orders.length ? '取消全选' : '全选')
            .fontSize(15).fontColor(CommonStyle.PRIMARY)
            .onClick(() => {
              if (this.selectedOrderIds.size === this.orders.length) {
                this.deselectAllOrders()
              } else {
                this.selectAllOrders()
              }
            })
        }
        .width('100%').height(48)
        .padding({ left: 16, right: 16 })
        .backgroundColor(CommonStyle.BG_CARD)
      } else {
        // 普通头部
        Row() {
          Text('订单管理')
            .fontSize(20).fontWeight(FontWeight.Bold)
            .fontColor(CommonStyle.TEXT_PRIMARY)
          if (this.pendingOrderCount > 0) {
            Text(`${this.pendingOrderCount}`)
              .fontSize(13).fontColor(Color.White)
              .backgroundColor(CommonStyle.ERROR)
              .padding({ left: 6, right: 6, top: 2, bottom: 2 })
              .borderRadius(10)
              .margin({ left: 8 })
          }
          Blank()
          if (this.orders.length > 0) {
            Text('多选')
              .fontSize(14).fontColor(CommonStyle.PRIMARY)
              .onClick(() => {
                this.isOrderSelectionMode = true
              })
          }
        }
        .width('100%').height(48)
        .padding({ left: 16, right: 16 })
        .backgroundColor(CommonStyle.BG_CARD)
      }

      // 订单列表
      if (this.orders.length === 0) {
        Column() {
          Text('📋').fontSize(48).margin({ bottom: 12 })
          Text('暂无订单').fontSize(15).fontColor(CommonStyle.TEXT_SECONDARY)
          Text('等待对方下单吧').fontSize(13).fontColor(CommonStyle.TEXT_TERTIARY).margin({ top: 4 })
        }
        .layoutWeight(1).justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
      } else {
        List() {
          ForEach(this.orders, (order: Order) => {
            ListItem() {
              this.SwipeableOrderCard(order)
            }
          }, (order: Order) => `order_${order.id}`)
        }
        .width('100%').layoutWeight(1)
        .scrollBar(BarState.Off)
        .padding({ top: 8 })
      }

      // 底部批量操作栏
      if (this.isOrderSelectionMode && this.selectedOrderIds.size > 0) {
        Column() {
          Divider().color(CommonStyle.SEPARATOR)

          // 批量操作按钮行（根据选中订单状态智能显示）
          Row() {
            if (this.canBatchAccept()) {
              this.BatchActionButton('接单', '#007AFF', () => this.executeBatchAction('accept'))
            }
            if (this.canBatchCooking()) {
              this.BatchActionButton('制作', '#AF52DE', () => this.executeBatchAction('cooking'))
            }
            if (this.canBatchComplete()) {
              this.BatchActionButton('完成', '#34C759', () => this.executeBatchAction('complete'))
            }
            if (this.canBatchCancel()) {
              this.BatchActionButton('取消', '#FF9500', () => this.executeBatchAction('cancel'))
            }
            if (this.canBatchDelete()) {
              this.BatchActionButton('删除', '#FF3B30', () => this.executeBatchAction('delete'))
            }
          }
          .width('100%')
          .padding({ left: 12, right: 12, top: 10, bottom: 10 })
          .justifyContent(FlexAlign.SpaceEvenly)
        }
        .width('100%')
        .backgroundColor(CommonStyle.BG_CARD)
      }
    }
    .width('100%').height('100%')
  }

  @Builder
  BatchActionButton(label: string, color: string, onClick: () => void) {
    Column() {
      Text(label)
        .fontSize(13).fontColor(color).fontWeight(FontWeight.Medium)
    }
    .width(56).height(40)
    .justifyContent(FlexAlign.Center)
    .alignItems(HorizontalAlign.Center)
    .backgroundColor(`${color}15`)
    .borderRadius(8)
    .onClick(onClick)
  }

  @Builder
  SwipeDeleteButton(orderId: number) {
    Column() {
      Text('删除')
        .fontSize(14).fontColor(Color.White).fontWeight(FontWeight.Medium)
    }
    .width(72).height('100%')
    .backgroundColor('#FF3B30')
    .justifyContent(FlexAlign.Center)
    .alignItems(HorizontalAlign.Center)
    .onClick(() => {
      AlertDialog.show({
        title: '确认删除',
        message: '确定要删除这个订单吗？',
        primaryButton: {
          value: '删除',
          action: () => {
            this.swipeDeleteOrder(orderId)
          }
        },
        secondaryButton: {
          value: '取消',
          action: () => {}
        }
      })
    })
  }

  @Builder
  GalleryTabBuilder() {
    Column() {
      this.GalleryGridBuilder()
    }
    .width('100%')
    .layoutWeight(1)
  }

  @Builder
  TabContentBuilder() {
    Column() {
      if (this.currentTab === 0) {
        this.RecipeTabBuilder()
      } else if (this.currentTab === 1) {
        this.OrderTabBuilder()
      } else {
        this.GalleryTabBuilder()
      }
    }
    .width('100%')
    .layoutWeight(1)
  }

  private getStatusColor(status: number): string {
    if (status === 0) return CommonStyle.WARNING
    if (status === 1) return CommonStyle.SECONDARY
    if (status === 2) return CommonStyle.SUCCESS
    if (status === 3) return CommonStyle.PRIMARY
    return CommonStyle.TEXT_SECONDARY
  }

  private getStatusBg(status: number): string {
    if (status === 0) return '#FFF3E0'
    if (status === 1) return '#E0F7FA'
    if (status === 2) return '#E8F5E9'
    if (status === 3) return CommonStyle.CHEF_LIGHT
    return '#F5F5F5'
  }

  private formatTime(timestamp: number): string {
    const date = new Date(timestamp)
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${month}-${day} ${hours}:${minutes}`
  }

  // ── 订单多选方法 ──

  toggleOrderSelection(orderId: number): void {
    const newSet = new Set(this.selectedOrderIds)
    if (newSet.has(orderId)) {
      newSet.delete(orderId)
    } else {
      newSet.add(orderId)
    }
    this.selectedOrderIds = newSet
    if (this.selectedOrderIds.size === 0) {
      this.isOrderSelectionMode = false
    }
  }

  selectAllOrders(): void {
    const newSet = new Set<number>()
    for (const order of this.orders) {
      newSet.add(order.id)
    }
    this.selectedOrderIds = newSet
  }

  deselectAllOrders(): void {
    this.selectedOrderIds = new Set()
    this.isOrderSelectionMode = false
  }

  enterOrderSelectionMode(orderId: number): void {
    this.isOrderSelectionMode = true
    this.selectedOrderIds = new Set([orderId])
  }

  exitOrderSelectionMode(): void {
    this.isOrderSelectionMode = false
    this.selectedOrderIds = new Set()
  }

  isOrderSelected(orderId: number): boolean {
    return this.selectedOrderIds.has(orderId)
  }

  getSelectedOrderIds(): number[] {
    return Array.from(this.selectedOrderIds)
  }

  // 获取选中订单的主要状态
  getSelectedOrdersMainStatus(): number {
    if (this.selectedOrderIds.size === 0) return -1
    const selectedOrders = this.orders.filter(o => this.selectedOrderIds.has(o.id))
    if (selectedOrders.length === 0) return -1
    // 如果所有选中订单状态相同，返回该状态；否则返回-1（混合状态）
    const firstStatus = selectedOrders[0].status
    const allSame = selectedOrders.every(o => o.status === firstStatus)
    return allSame ? firstStatus : -1
  }

  // 根据选中订单状态判断显示哪些批量操作
  canBatchAccept(): boolean {
    return this.orders.filter(o => this.selectedOrderIds.has(o.id) && o.status === 0).length > 0
  }

  canBatchCooking(): boolean {
    return this.orders.filter(o => this.selectedOrderIds.has(o.id) && o.status === 1).length > 0
  }

  canBatchComplete(): boolean {
    return this.orders.filter(o => this.selectedOrderIds.has(o.id) && o.status === 2).length > 0
  }

  canBatchCancel(): boolean {
    return this.orders.filter(o => this.selectedOrderIds.has(o.id) && (o.status === 0 || o.status === 1 || o.status === 2)).length > 0
  }

  canBatchDelete(): boolean {
    return this.selectedOrderIds.size > 0
  }

  async swipeDeleteOrder(orderId: number): Promise<void> {
    try {
      const res = await ApiService.deleteOrder(orderId)
      if (res.code === 200) {
        ToastUtil.showSuccess('已删除')
        this.loadOrders()
        this.loadPendingOrderCount()
      } else {
        ToastUtil.showError(res.message || '删除失败')
      }
    } catch (e) {
      LogUtil.error('swipeDeleteOrder error: ' + e)
      ToastUtil.showError('删除失败')
    }
  }

  async executeBatchAction(action: string): Promise<void> {
    const ids = this.getSelectedOrderIds()
    if (ids.length === 0) {
      ToastUtil.showWarning('请先选择订单')
      return
    }

    let successCount = 0
    switch (action) {
      case 'accept':
        try {
          const res = await ApiService.batchUpdateOrderStatus(ids, CommonConstant.ORDER_STATUS_ACCEPTED)
          successCount = res.code === 200 ? ids.length : 0
        } catch (e) { LogUtil.error('batch accept error: ' + e) }
        ToastUtil.showSuccess(`已接单 ${successCount} 个`)
        break
      case 'cooking':
        try {
          const res = await ApiService.batchUpdateOrderStatus(ids, CommonConstant.ORDER_STATUS_COOKING)
          successCount = res.code === 200 ? ids.length : 0
        } catch (e) { LogUtil.error('batch cooking error: ' + e) }
        ToastUtil.showSuccess(`已开始制作 ${successCount} 个`)
        break
      case 'complete':
        try {
          const res = await ApiService.batchUpdateOrderStatus(ids, CommonConstant.ORDER_STATUS_COMPLETED)
          successCount = res.code === 200 ? ids.length : 0
        } catch (e) { LogUtil.error('batch complete error: ' + e) }
        ToastUtil.showSuccess(`已完成 ${successCount} 个`)
        break
      case 'cancel':
        try {
          const res = await ApiService.batchUpdateOrderStatus(ids, CommonConstant.ORDER_STATUS_CANCELLED)
          successCount = res.code === 200 ? ids.length : 0
        } catch (e) { LogUtil.error('batch cancel error: ' + e) }
        ToastUtil.showSuccess(`已取消 ${successCount} 个`)
        break
      case 'delete':
        try {
          const res = await ApiService.batchDeleteOrders(ids)
          successCount = res.code === 200 ? ids.length : 0
        } catch (e) { LogUtil.error('batch delete error: ' + e) }
        ToastUtil.showSuccess(`已删除 ${successCount} 个`)
        break
    }

    this.exitOrderSelectionMode()
    this.loadOrders()
    this.loadPendingOrderCount()
  }

  build() {
    Column() {
      this.HeaderBuilder()
      this.PendingOrderBannerBuilder()
      this.TabBarBuilder()
      this.TabContentBuilder()
    }
    .width('100%')
    .height('100%')
    .backgroundColor(CommonStyle.BG_PAGE)
  }
}
