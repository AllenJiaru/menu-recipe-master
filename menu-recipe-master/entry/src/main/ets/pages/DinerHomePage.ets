/**
 * 食客首页 - Premium Edition
 * 浏览菜谱、随机点菜、查看订单、美食相册
 */
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
struct DinerHomePage {
  @State currentTab: number = 0
  @State coupleConfig: CoupleConfig | null = null
  @State recipes: Recipe[] = []
  @State allRecipes: Recipe[] = []
  @State orders: Order[] = []
  @State galleryImages: GalleryImage[] = []
  @State selectedCategory: number = 0
  @State isLoading: boolean = false
  @State searchKeyword: string = ''
  @State selectedDifficulty: number = 0  // 0=全部, 1=简单, 2=较简单, 3=中等, 4=较困难, 5=困难
  @State selectedStatus: number = -1     // -1=全部, 0=下架, 1=上架
  // ── 订单多选 ──
  @State isOrderSelectionMode: boolean = false
  @State selectedOrderIds: Set<number> = new Set()
  private context: common.UIAbilityContext | null = null

  aboutToAppear() {
    this.context = getContext(this) as common.UIAbilityContext
    this.initData()
  }

  onPageShow() {
    this.loadRecipes()
    this.loadOrders()
    this.loadGallery()
  }

  async initData() {
    await RolePermissionUtil.init(this.context!)
    await this.loadCoupleConfig()
  }

  async loadCoupleConfig() {
    try {
      const res = await ApiService.getCoupleConfig()
      if (res.code === 200 && res.data) {
        const config = res.data as object
        this.coupleConfig = {
          id: (config as object)['id'] as number || 0,
          space_name: (config as object)['spaceName'] as string || '',
          chef_name: (config as object)['chefName'] as string || '',
          diner_name: (config as object)['dinerName'] as string || '',
          create_time: (config as object)['createTime'] as number || 0
        }
      }
    } catch (err) {
      LogUtil.error('DinerHomePage', `加载配置失败: ${err}`)
    }
  }

  async loadRecipes() {
    this.isLoading = true
    try {
      const res = await ApiService.getRecipes({ page: 1, size: 100, status: 1 })
      if (res.code === 200 && res.data) {
        this.allRecipes = (res.data.records || []).map((r: object) => Recipe.fromApi(r))
        this.filterRecipes()
      }
    } catch (err) {
      LogUtil.error('DinerHomePage', `加载菜谱失败: ${err}`)
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

  async loadOrders() {
    try {
      const res = await ApiService.getAllOrders()
      if (res.code === 200 && res.data) {
        this.orders = (res.data as object[]).map((o: object) => Order.fromApi(o))
      }
    } catch (err) {
      LogUtil.error('DinerHomePage', `加载订单失败: ${err}`)
    }
  }

  async loadGallery() {
    try {
      const res = await ApiService.getAllGalleryImages()
      if (res.code === 200 && res.data) {
        this.galleryImages = (res.data as object[]).map((g: object) => GalleryImage.fromApi(g))
      }
    } catch (err) {
      LogUtil.error('DinerHomePage', `加载相册失败: ${err}`)
    }
  }

  async randomOrder() {
    if (this.allRecipes.length === 0) {
      ToastUtil.showWarning('暂无可用菜谱')
      return
    }
    const randomIndex = Math.floor(Math.random() * this.allRecipes.length)
    const recipe = this.allRecipes[randomIndex]

    try {
      const res = await ApiService.createOrder({
        recipeId: recipe.id,
        recipeName: recipe.name,
        recipeImage: recipe.getCoverImage(),
        status: CommonConstant.ORDER_STATUS_PENDING,
        remark: '🎲 随机点菜'
      })
      if (res.code === 200) {
        ToastUtil.showSuccess(`已下单：${recipe.name}`)
        this.loadOrders()
      } else {
        ToastUtil.showError(res.message || '下单失败')
      }
    } catch (err) {
      LogUtil.error('DinerHomePage', `下单失败: ${err}`)
      ToastUtil.showError('下单失败')
    }
  }

  async addGalleryPhoto() {
    try {
      const photoPicker = new picker.PhotoViewPicker()
      const result = await photoPicker.select({
        MIMEType: picker.PhotoViewMIMETypes.IMAGE_TYPE,
        maxSelectNumber: 9
      })
      if (result?.photoUris && result.photoUris.length > 0) {
        for (const uri of result.photoUris) {
          const res = await ApiService.uploadGalleryImage(uri, '')
          if (res.code !== 200) {
            LogUtil.error('DinerHomePage', `上传照片失败: ${res.message}`)
          }
        }
        await this.loadGallery()
        ToastUtil.showSuccess('照片已添加')
      }
    } catch (err) {
      LogUtil.error('DinerHomePage', `添加照片失败: ${err}`)
    }
  }

  async deleteGalleryPhoto(id: number) {
    try {
      const res = await ApiService.deleteGalleryImage(id)
      if (res.code === 200) {
        await this.loadGallery()
        ToastUtil.showSuccess('已删除')
      } else {
        ToastUtil.showError(res.message || '删除失败')
      }
    } catch (err) {
      LogUtil.error('DinerHomePage', `删除照片失败: ${err}`)
    }
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

  selectAllOrders(): void {
    const newSet = new Set<number>()
    for (const order of this.orders) {
      newSet.add(order.id)
    }
    this.selectedOrderIds = newSet
  }

  // 食客只能批量取消待处理订单和批量删除
  canBatchCancel(): boolean {
    return this.orders.filter(o => this.selectedOrderIds.has(o.id) && o.status === 0).length > 0
  }

  canBatchDelete(): boolean {
    return this.selectedOrderIds.size > 0
  }

  async executeBatchAction(action: string): Promise<void> {
    const ids = Array.from(this.selectedOrderIds)
    if (ids.length === 0) {
      ToastUtil.showWarning('请先选择订单')
      return
    }

    let successCount = 0
    try {
      if (action === 'cancel') {
        const res = await ApiService.batchUpdateOrderStatus(ids, CommonConstant.ORDER_STATUS_CANCELLED)
        successCount = res.code === 200 ? ids.length : 0
        ToastUtil.showSuccess(`已取消 ${successCount} 个订单`)
      } else if (action === 'delete') {
        const res = await ApiService.batchDeleteOrders(ids)
        successCount = res.code === 200 ? ids.length : 0
        ToastUtil.showSuccess(`已删除 ${successCount} 个订单`)
      }
    } catch (e) {
      LogUtil.error('DinerHomePage', `批量操作失败: ${e}`)
      ToastUtil.showError('操作失败')
    }

    this.exitOrderSelectionMode()
    this.loadOrders()
  }

  async swipeDeleteOrder(orderId: number): Promise<void> {
    try {
      const res = await ApiService.deleteOrder(orderId)
      if (res.code === 200) {
        ToastUtil.showSuccess('已删除')
        this.loadOrders()
      } else {
        ToastUtil.showError(res.message || '删除失败')
      }
    } catch (e) {
      LogUtil.error('DinerHomePage', `删除订单失败: ${e}`)
      ToastUtil.showError('删除失败')
    }
  }

  formatTime(ts: number): string {
    if (!ts) return ''
    const d = new Date(ts)
    const month = (d.getMonth() + 1).toString().padStart(2, '0')
    const day = d.getDate().toString().padStart(2, '0')
    const hours = d.getHours().toString().padStart(2, '0')
    const minutes = d.getMinutes().toString().padStart(2, '0')
    return `${month}-${day} ${hours}:${minutes}`
  }

  build() {
    Column() {
      this.HeaderBuilder()
      this.CustomTabBarBuilder()
      Column() {
        if (this.currentTab === 0) {
          this.BrowseTabBuilder()
        } else if (this.currentTab === 1) {
          this.OrdersTabBuilder()
        } else {
          this.GalleryTabBuilder()
        }
      }
      .layoutWeight(1)
      .width('100%')
    }
    .width('100%')
    .height('100%')
    .backgroundColor(CommonStyle.BG_PAGE)
  }

  // ── Header ──────────────────────────────────────────────────
  @Builder
  HeaderBuilder() {
    Row() {
      Column() {
        Text('美食浏览')
          .fontSize(CommonStyle.FONT_LARGE_TITLE)
          .fontWeight(FontWeight.Bold)
          .fontColor(CommonStyle.TEXT_PRIMARY)
        Text(`食客：${this.coupleConfig?.diner_name || '未命名'}`)
          .fontSize(CommonStyle.FONT_CALLOUT)
          .fontColor(CommonStyle.TEXT_SECONDARY)
          .margin({ top: CommonStyle.SP_XS })
      }
      .alignItems(HorizontalAlign.Start)
      .layoutWeight(1)

      Text('⚙')
        .fontSize(CommonStyle.FONT_TITLE1)
        .fontColor(CommonStyle.TEXT_PRIMARY)
        .textAlign(TextAlign.Center)
        .width(44)
        .height(44)
        .backgroundColor(CommonStyle.BG_CARD)
        .borderRadius(CommonStyle.RADIUS_MD)
        .shadow({
          color: 'rgba(0,0,0,0.04)',
          offsetX: 0,
          offsetY: 2,
          radius: 8
        })
        .onClick(() => router.pushUrl({ url: CommonConstant.PAGE_SETTINGS }))
    }
    .width('100%')
    .padding({ left: CommonStyle.SP_XL, right: CommonStyle.SP_XL, top: CommonStyle.SP_XXL, bottom: CommonStyle.SP_MD })
  }

  // ── Custom TabBar ───────────────────────────────────────────
  @Builder
  CustomTabBarBuilder() {
    Row() {
      ForEach(['浏览菜谱', '我的订单', '美食相册'], (item: string, index: number) => {
        Column() {
          Text(item)
            .fontSize(CommonStyle.FONT_HEADLINE)
            .fontWeight(this.currentTab === index ? FontWeight.Bold : FontWeight.Normal)
            .fontColor(this.currentTab === index ? RoleStyle.DINER_PRIMARY : CommonStyle.TEXT_SECONDARY)
            .padding({ top: CommonStyle.SP_SM, bottom: CommonStyle.SP_SM })
          Divider()
            .strokeWidth(2.5)
            .color(this.currentTab === index ? RoleStyle.DINER_PRIMARY : 'transparent')
            .width(32)
            .margin({ top: CommonStyle.SP_XS })
            .borderRadius(1.25)
        }
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Center)
        .onClick(() => { this.currentTab = index })
      }, (item: string) => item)
    }
    .width('100%')
    .height(44)
    .backgroundColor(CommonStyle.BG_PAGE)
  }

  // ════════════════════════════════════════════════════════════
  //  BROWSE TAB
  // ════════════════════════════════════════════════════════════
  @Builder
  BrowseTabBuilder() {
    Column() {
      // ── 搜索栏 ──
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
      .borderRadius(CommonStyle.RADIUS_MD)
      .margin({ left: CommonStyle.SP_XL, right: CommonStyle.SP_XL, top: CommonStyle.SP_MD })

      // ── 筛选栏 ──
      Row() {
        // 难度筛选
        Select(this.getDifficultyOptions())
          .value(this.getDifficultyLabel())
          .fontSize(13)
          .fontColor(CommonStyle.TEXT_PRIMARY)
          .selectedOptionFontColor(RoleStyle.DINER_PRIMARY)
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
          .selectedOptionFontColor(RoleStyle.DINER_PRIMARY)
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
      .padding({ left: CommonStyle.SP_XL, right: CommonStyle.SP_XL, top: CommonStyle.SP_SM })
      .justifyContent(FlexAlign.Start)

      // ── 分类筛选标签 ──
      Scroll() {
        Row({ space: CommonStyle.SP_SM }) {
          this.CategoryChipBuilder('全部', 0)
          ForEach(Array.from(RecipeTypeMap.entries()), (item: [number, string]) => {
            this.CategoryChipBuilder(item[1], item[0])
          }, (item: [number, string]) => `cat_${item[0]}`)
        }
        .padding({ left: CommonStyle.SP_XL, right: CommonStyle.SP_XL })
      }
      .scrollable(ScrollDirection.Horizontal)
      .scrollBar(BarState.Off)
      .width('100%')
      .backgroundColor(CommonStyle.BG_CARD)
      .padding({ top: CommonStyle.SP_SM, bottom: CommonStyle.SP_SM })

      // ── 筛选结果提示 ──
      if (this.searchKeyword || this.selectedDifficulty !== 0 || this.selectedStatus !== -1 || this.selectedCategory !== 0) {
        Row() {
          Text(`筛选结果: ${this.recipes.length} 道菜谱`)
            .fontSize(12)
            .fontColor(CommonStyle.TEXT_SECONDARY)
          Blank()
          Text('清除筛选')
            .fontSize(12)
            .fontColor(RoleStyle.DINER_PRIMARY)
            .onClick(() => {
              this.searchKeyword = ''
              this.selectedDifficulty = 0
              this.selectedStatus = -1
              this.selectedCategory = 0
              this.filterRecipes()
            })
        }
        .width('100%')
        .padding({ left: CommonStyle.SP_XL, right: CommonStyle.SP_XL, top: CommonStyle.SP_SM, bottom: CommonStyle.SP_SM })
      }

      // ── 随机点菜按钮 ──
      if (this.recipes.length > 0) {
        Text('🎲 随机点菜')
          .fontSize(CommonStyle.FONT_HEADLINE)
          .fontWeight(FontWeight.Medium)
          .fontColor(CommonStyle.TEXT_ON_PRIMARY)
          .textAlign(TextAlign.Center)
          .width('100%')
          .height(48)
          .linearGradient({
            direction: GradientDirection.Right,
            colors: [RoleStyle.DINER_GRADIENT[0], RoleStyle.DINER_GRADIENT[1]]
          })
          .borderRadius(CommonStyle.RADIUS_BUTTON)
          .margin({ left: CommonStyle.SP_XL, right: CommonStyle.SP_XL, top: CommonStyle.SP_MD, bottom: CommonStyle.SP_MD })
          .shadow({
            color: 'rgba(78,205,196,0.3)',
            offsetX: 0,
            offsetY: 4,
            radius: 12
          })
          .onClick(() => this.randomOrder())
      }

      // ── Section Header ──
      Row() {
        Text('主厨的菜谱')
          .fontSize(CommonStyle.FONT_TITLE3)
          .fontWeight(FontWeight.Bold)
          .fontColor(CommonStyle.TEXT_PRIMARY)
        Text(`${this.recipes.length}`)
          .fontSize(CommonStyle.FONT_CAPTION1)
          .fontColor(RoleStyle.DINER_PRIMARY)
          .backgroundColor(RoleStyle.DINER_LIGHT)
          .padding({ left: CommonStyle.SP_SM, right: CommonStyle.SP_SM, top: 2, bottom: 2 })
          .borderRadius(CommonStyle.RADIUS_CHIP)
          .margin({ left: CommonStyle.SP_SM })
          .fontWeight(FontWeight.Medium)
      }
      .width('100%')
      .padding({ left: CommonStyle.SP_XL, right: CommonStyle.SP_XL, top: CommonStyle.SP_SM, bottom: CommonStyle.SP_SM })

      // ── Content ──
      if (this.isLoading) {
        Column() {
          LoadingProgress()
            .width(40)
            .height(40)
            .color(RoleStyle.DINER_PRIMARY)
        }
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else if (this.recipes.length === 0) {
        this.EmptyStateBuilder('🍳', '暂无菜谱', '等待主厨创建美味菜谱')
      } else {
        List({ space: CommonStyle.SP_MD }) {
          ForEach(this.recipes, (recipe: Recipe) => {
            ListItem() {
              this.RecipeCardBuilder(recipe)
            }
          }, (recipe: Recipe) => `recipe_${recipe.id}`)
        }
        .width('100%')
        .layoutWeight(1)
        .padding({ left: CommonStyle.SP_XL, right: CommonStyle.SP_XL, bottom: CommonStyle.SP_XL })
        .scrollBar(BarState.Off)
      }
    }
    .width('100%')
    .height('100%')
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

  // ── Category Chip ──
  @Builder
  CategoryChipBuilder(title: string, category: number) {
    Text(title)
      .fontSize(CommonStyle.FONT_CAPTION1)
      .fontWeight(this.selectedCategory === category ? FontWeight.Medium : FontWeight.Normal)
      .fontColor(this.selectedCategory === category ? Color.White : CommonStyle.TEXT_SECONDARY)
      .backgroundColor(this.selectedCategory === category ? RoleStyle.DINER_PRIMARY : CommonStyle.BG_SECONDARY)
      .borderRadius(CommonStyle.RADIUS_CHIP)
      .padding({ left: CommonStyle.SP_LG, right: CommonStyle.SP_LG, top: CommonStyle.SP_SM, bottom: CommonStyle.SP_SM })
      .onClick(() => {
        this.selectedCategory = category
        this.filterRecipes()
      })
  }

  // ── Recipe Card ──
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
        Row() {
          Text(recipe.name)
            .fontSize(CommonStyle.FONT_HEADLINE)
            .fontWeight(FontWeight.Bold)
            .fontColor(CommonStyle.TEXT_PRIMARY)
            .maxLines(1)
            .textOverflow({ overflow: TextOverflow.Ellipsis })
            .layoutWeight(1)

          if (recipe.isFavorite === 1) {
            Text('❤️')
              .fontSize(CommonStyle.FONT_FOOTNOTE)
          }
        }
        .width('100%')

        Text(RecipeTypeMap.get(recipe.getCategoryId()) || '其他')
          .fontSize(CommonStyle.FONT_CAPTION2)
          .fontColor(RoleStyle.DINER_PRIMARY)
          .backgroundColor(RoleStyle.DINER_LIGHT)
          .padding({ left: CommonStyle.SP_SM, right: CommonStyle.SP_SM, top: 2, bottom: 2 })
          .borderRadius(CommonStyle.RADIUS_CHIP)
          .margin({ top: CommonStyle.SP_XS })

        if (recipe.description) {
          Text(recipe.description)
            .fontSize(CommonStyle.FONT_CAPTION1)
            .fontColor(CommonStyle.TEXT_SECONDARY)
            .maxLines(2)
            .textOverflow({ overflow: TextOverflow.Ellipsis })
            .margin({ top: CommonStyle.SP_XS })
            .width('100%')
        }

        Row({ space: CommonStyle.SP_MD }) {
          Text(`⏱ ${recipe.cookingTime}分钟`)
            .fontSize(CommonStyle.FONT_CAPTION2)
            .fontColor(CommonStyle.TEXT_TERTIARY)
          Text(`📊 难度${recipe.difficulty}`)
            .fontSize(CommonStyle.FONT_CAPTION2)
            .fontColor(CommonStyle.TEXT_TERTIARY)
        }
        .margin({ top: CommonStyle.SP_SM })
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

  // ════════════════════════════════════════════════════════════
  //  ORDERS TAB
  // ════════════════════════════════════════════════════════════
  @Builder
  OrdersTabBuilder() {
    Column() {
      // 选择模式头部
      if (this.isOrderSelectionMode) {
        Row() {
          Text('取消')
            .fontSize(15).fontColor(RoleStyle.DINER_PRIMARY)
            .onClick(() => this.exitOrderSelectionMode())

          Blank()

          Text(`已选 ${this.selectedOrderIds.size} 个`)
            .fontSize(15).fontWeight(FontWeight.Medium)
            .fontColor(CommonStyle.TEXT_PRIMARY)

          Blank()

          Text(this.selectedOrderIds.size === this.orders.length ? '取消全选' : '全选')
            .fontSize(15).fontColor(RoleStyle.DINER_PRIMARY)
            .onClick(() => {
              if (this.selectedOrderIds.size === this.orders.length) {
                this.exitOrderSelectionMode()
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
          Text('我的订单')
            .fontSize(20).fontWeight(FontWeight.Bold)
            .fontColor(CommonStyle.TEXT_PRIMARY)
          Text(`${this.orders.length}`)
            .fontSize(12).fontColor(RoleStyle.DINER_PRIMARY)
            .backgroundColor(RoleStyle.DINER_LIGHT)
            .padding({ left: 6, right: 6, top: 2, bottom: 2 })
            .borderRadius(10)
            .margin({ left: 8 })
          Blank()
          if (this.orders.length > 0) {
            Text('多选')
              .fontSize(14).fontColor(RoleStyle.DINER_PRIMARY)
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
          Text('浏览菜谱并发起点餐').fontSize(13).fontColor(CommonStyle.TEXT_TERTIARY).margin({ top: 4 })
        }
        .layoutWeight(1).justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
      } else {
        List() {
          ForEach(this.orders, (order: Order) => {
            ListItem() {
              this.OrderCardBuilder(order)
            }
            .swipeAction({
              end: this.SwipeDeleteButton(order.id)
            })
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

          Row() {
            if (this.canBatchCancel()) {
              this.BatchActionButton('取消订单', '#FF9500', () => this.executeBatchAction('cancel'))
            }
            if (this.canBatchDelete()) {
              this.BatchActionButton('删除订单', '#FF3B30', () => this.executeBatchAction('delete'))
            }
          }
          .width('100%')
          .padding({ left: 16, right: 16, top: 12, bottom: 12 })
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
        .fontSize(14).fontColor(color).fontWeight(FontWeight.Medium)
    }
    .width(120).height(44)
    .justifyContent(FlexAlign.Center)
    .alignItems(HorizontalAlign.Center)
    .backgroundColor(`${color}15`)
    .borderRadius(12)
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
          action: async () => {
            await this.swipeDeleteOrder(orderId)
          }
        },
        secondaryButton: {
          value: '取消',
          action: () => {}
        }
      })
    })
  }

  // ── Order Card ──
  @Builder
  OrderCardBuilder(order: Order) {
    Row() {
      // 选择框
      if (this.isOrderSelectionMode) {
        Column() {
          Text(this.isOrderSelected(order.id) ? '☑' : '☐')
            .fontSize(20)
            .fontColor(this.isOrderSelected(order.id) ? RoleStyle.DINER_PRIMARY : CommonStyle.TEXT_TERTIARY)
        }
        .width(32).height(56)
        .justifyContent(FlexAlign.Center)
        .onClick(() => {
          this.toggleOrderSelection(order.id)
        })
      }

      // 图片
      if (order.recipeImage) {
        Image(ImageUtil.getDisplayPath(order.recipeImage))
          .width(56).height(56).borderRadius(8).objectFit(ImageFit.Cover)
      } else {
        Column() { Text('🍽').fontSize(22) }
          .width(56).height(56).borderRadius(8)
          .backgroundColor(CommonStyle.BG_SECONDARY).justifyContent(FlexAlign.Center)
      }

      // 订单信息
      Column() {
        Row() {
          Text(order.recipeName)
            .fontSize(15).fontWeight(FontWeight.Medium)
            .fontColor(CommonStyle.TEXT_PRIMARY)
            .maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
            .layoutWeight(1)

          Text(OrderStatusMap.get(order.status) || '未知')
            .fontSize(11)
            .fontColor(OrderStatusStyle.getStatusColor(order.status))
            .backgroundColor(OrderStatusStyle.getStatusBg(order.status))
            .padding({ left: 6, right: 6, top: 2, bottom: 2 })
            .borderRadius(8)
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
      .margin({ left: 10 })
    }
    .width('100%')
    .padding(12)
    .backgroundColor(this.isOrderSelected(order.id) ? RoleStyle.DINER_LIGHT : CommonStyle.BG_CARD)
    .borderRadius(12)
    .shadow({ radius: 4, color: '#00000008', offsetX: 0, offsetY: 2 })
    .margin({ left: 16, right: 16, bottom: 8 })
    .onClick(() => {
      if (this.isOrderSelectionMode) {
        this.toggleOrderSelection(order.id)
      } else {
        router.pushUrl({ url: CommonConstant.PAGE_ORDER_DETAIL, params: { orderId: order.id } })
      }
    })
    .onLongPress(() => {
      if (!this.isOrderSelectionMode) {
        this.enterOrderSelectionMode(order.id)
      }
    })
  }

  // ════════════════════════════════════════════════════════════
  //  GALLERY TAB
  // ════════════════════════════════════════════════════════════
  @Builder
  GalleryTabBuilder() {
    Column() {
      // ── Header with count and add button ──
      Row() {
        Text('美食相册')
          .fontSize(CommonStyle.FONT_TITLE3)
          .fontWeight(FontWeight.Bold)
          .fontColor(CommonStyle.TEXT_PRIMARY)
        Text(`${this.galleryImages.length} 张`)
          .fontSize(CommonStyle.FONT_CAPTION1)
          .fontColor(CommonStyle.TEXT_SECONDARY)
          .margin({ left: CommonStyle.SP_SM })
          .layoutWeight(1)

        Text('+')
          .fontSize(CommonStyle.FONT_TITLE2)
          .fontWeight(FontWeight.Bold)
          .fontColor(RoleStyle.DINER_PRIMARY)
          .width(36)
          .height(36)
          .textAlign(TextAlign.Center)
          .backgroundColor(RoleStyle.DINER_LIGHT)
          .borderRadius(CommonStyle.RADIUS_ROUND)
          .shadow({
            color: 'rgba(78,205,196,0.15)',
            offsetX: 0,
            offsetY: 2,
            radius: 8
          })
          .onClick(() => this.addGalleryPhoto())
      }
      .width('100%')
      .padding({ left: CommonStyle.SP_XL, right: CommonStyle.SP_XL, top: CommonStyle.SP_MD, bottom: CommonStyle.SP_MD })

      // ── Content ──
      if (this.galleryImages.length === 0) {
        this.EmptyStateBuilder('📸', '暂无照片', '点击右上角 + 添加')
      } else {
        Grid() {
          ForEach(this.galleryImages, (img: GalleryImage) => {
            GridItem() {
              Stack() {
                Image(ImageUtil.getDisplayPath(img.getDisplayPath()))
                  .width('100%')
                  .height('100%')
                  .objectFit(ImageFit.Cover)
                  .borderRadius(CommonStyle.RADIUS_SM)

                Column() {
                  Text('✕')
                    .fontSize(10)
                    .fontColor(Color.White)
                    .textAlign(TextAlign.Center)
                }
                .width(22)
                .height(22)
                .borderRadius(CommonStyle.RADIUS_ROUND)
                .backgroundColor('rgba(0,0,0,0.45)')
                .position({ x: 0, y: 0 })
                .padding(2)
                .onClick(() => this.deleteGalleryPhoto(img.id))
              }
              .width('100%')
              .aspectRatio(1)
            }
          }, (img: GalleryImage) => `gallery_${img.id}`)
        }
        .columnsTemplate('1fr 1fr 1fr')
        .columnsGap(CommonStyle.SP_SM)
        .rowsGap(CommonStyle.SP_SM)
        .padding({ left: CommonStyle.SP_XL, right: CommonStyle.SP_XL })
        .layoutWeight(1)
      }
    }
    .width('100%')
    .height('100%')
  }

  // ── Empty State ──
  @Builder
  EmptyStateBuilder(emoji: string, title: string, subtitle: string) {
    Column() {
      Text(emoji)
        .fontSize(52)
        .margin({ bottom: CommonStyle.SP_MD })
      Text(title)
        .fontSize(CommonStyle.FONT_CALLOUT)
        .fontColor(CommonStyle.TEXT_SECONDARY)
        .fontWeight(FontWeight.Medium)
      Text(subtitle)
        .fontSize(CommonStyle.FONT_CAPTION1)
        .fontColor(CommonStyle.TEXT_TERTIARY)
        .margin({ top: CommonStyle.SP_XS })
    }
    .width('100%')
    .layoutWeight(1)
    .justifyContent(FlexAlign.Center)
    .alignItems(HorizontalAlign.Center)
  }
}
