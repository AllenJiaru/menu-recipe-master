import { CommonStyle } from '../common/CommonStyle'
import { CommonConstant, RecipeTypeMap } from '../common/CommonConstant'
import { RdbUtil } from '../common/RdbUtil'
import { ApiService } from '../common/ApiService'
import { LogUtil } from '../common/LogUtil'
import { ImageUtil } from '../common/ImageUtil'
import { ToastUtil } from '../common/ToastUtil'
import { Recipe, RecipeMaterial, RecipeStep } from '../model/Recipe'
import router from '@ohos.router'
import common from '@ohos.app.ability.common'
import picker from '@ohos.file.picker'

@Entry
@Component
struct AddRecipePage {
  // 状态变量
  @State isEdit: boolean = false
  @State recipeId: number = -1
  @State recipeName: string = ''
  @State recipeDescription: string = ''
  @State recipeType: number = 0
  @State cookingTime: number = 30
  @State difficulty: number = 1
  @State recipeImage: string = ''
  @State materials: RecipeMaterial[] = []
  @State steps: RecipeStep[] = []
  @State matName: string = ''
  @State matAmount: string = ''
  @State matUnit: string = ''
  @State stepDesc: string = ''
  @State isSaving: boolean = false
  @State context: common.UIAbilityContext = {} as common.UIAbilityContext

  // 原始菜谱数据（编辑时用于保留createTime）
  private originalRecipe: Recipe | null = null

  // 获取UIAbilityContext
  private getContext(): common.UIAbilityContext {
    return this.context
  }

  aboutToAppear(): void {
    // 获取上下文
    this.context = getContext(this) as common.UIAbilityContext

    // 获取路由参数
    const params = router.getParams() as Record<string, string>
    if (params && params['recipeId']) {
      this.recipeId = Number(params['recipeId'])
      this.isEdit = true
      this.loadRecipe()
    }
  }

  // 加载菜谱数据
  private async loadRecipe(): Promise<void> {
    try {
      this.originalRecipe = await RdbUtil.queryRecipeById(this.recipeId)
      if (this.originalRecipe) {
        this.recipeName = this.originalRecipe.name
        this.recipeDescription = this.originalRecipe.description
        this.recipeType = this.originalRecipe.getCategoryId()
        this.cookingTime = this.originalRecipe.cookingTime
        this.difficulty = this.originalRecipe.difficulty
        this.recipeImage = this.originalRecipe.getCoverImage()
        this.materials = this.originalRecipe.materials || []
        this.steps = this.originalRecipe.steps || []
      }
    } catch (error) {
      LogUtil.error('Failed to load recipe: ' + JSON.stringify(error))
      ToastUtil.showToast('加载菜谱失败')
    }
  }

  // 选择图片
  private async selectImage(): Promise<void> {
    try {
      const photoPicker = new picker.PhotoViewPicker()
      const result = await photoPicker.select({
        maxSelectNumber: 1,
        mimeTypes: ['image/*'],
        isNeedToCrop: true
      })

      if (result && result.photoUris && result.photoUris.length > 0) {
        this.recipeImage = result.photoUris[0]
        LogUtil.info('Selected image: ' + this.recipeImage)
      }
    } catch (error) {
      LogUtil.error('Failed to select image: ' + JSON.stringify(error))
      ToastUtil.showToast('选择图片失败')
    }
  }

  // 添加材料
  private addMaterial(): void {
    if (this.matName.trim() === '') {
      ToastUtil.showToast('请输入材料名称')
      return
    }
    const newMaterial: RecipeMaterial = {
      name: this.matName.trim(),
      amount: this.matAmount.trim(),
      unit: this.matUnit.trim()
    }
    this.materials = [...this.materials, newMaterial]
    this.matName = ''
    this.matAmount = ''
    this.matUnit = ''
  }

  // 删除材料
  private removeMaterial(index: number): void {
    this.materials = this.materials.filter((_, i) => i !== index)
  }

  // 添加步骤
  private addStep(): void {
    if (this.stepDesc.trim() === '') {
      ToastUtil.showToast('请输入步骤描述')
      return
    }
    const newStep: RecipeStep = {
      stepNumber: this.steps.length + 1,
      description: this.stepDesc.trim()
    }
    this.steps = [...this.steps, newStep]
    this.stepDesc = ''
  }

  // 删除步骤
  private removeStep(index: number): void {
    this.steps = this.steps.filter((_, i) => i !== index)
    this.renumberSteps()
  }

  // 重新编号步骤
  private renumberSteps(): void {
    this.steps = this.steps.map((step, index) => ({
      stepNumber: index + 1,
      description: step.description
    }))
  }

  // 保存菜谱
  private async saveRecipe(): Promise<void> {
    if (this.recipeName.trim() === '') {
      ToastUtil.showToast('请输入菜谱名称')
      return
    }

    this.isSaving = true
    try {
      // 本地图片先上传到服务器，换取可访问的封面路径
      let coverImage: string = this.recipeImage
      if (coverImage && !coverImage.startsWith('http')) {
        const uploadRes = await ApiService.uploadFile(coverImage, 'recipe.jpg')
        if (uploadRes.code === 200 && uploadRes.data) {
          coverImage = uploadRes.data
        }
      }

      const recipe: Recipe = {
        name: this.recipeName.trim(),
        description: this.recipeDescription.trim(),
        type: this.recipeType,
        categoryId: this.recipeType + 10,
        cookingTime: this.cookingTime,
        difficulty: this.difficulty,
        coverImage: coverImage,
        imagePath: coverImage,
        materials: this.materials,
        steps: this.steps
      }

      // 编辑时保留原创建时间
      if (this.isEdit && this.originalRecipe) {
        recipe.id = this.originalRecipe.id
        recipe.createTime = this.originalRecipe.createTime
      }

      // 优先同步到后端
      if (ApiService.isLoggedIn()) {
        if (this.isEdit && recipe.id > 0) {
          await ApiService.updateRecipe(recipe.id, recipe)
        } else {
          const res = await ApiService.createRecipe(recipe)
          if (res.code === 200 && res.data) {
            const created = res.data as Recipe
            if (created.id) {
              recipe.id = created.id
            }
          }
        }
      }

      // 写入本地缓存
      if (this.isEdit) {
        await RdbUtil.updateRecipe(recipe)
        ToastUtil.showToast('菜谱修改成功')
      } else {
        await RdbUtil.insertRecipe(recipe)
        ToastUtil.showToast('菜谱保存成功')
      }
      router.back()
    } catch (error) {
      LogUtil.error('Failed to save recipe: ' + JSON.stringify(error))
      ToastUtil.showToast('保存失败，请重试')
    } finally {
      this.isSaving = false
    }
  }

  // 返回按钮
  private goBack(): void {
    router.back()
  }

  // 获取分类标签颜色
  private getCategoryColor(type: number): ResourceColor {
    const colors: ResourceColor[] = [
      '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7',
      '#DDA0DD', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9'
    ]
    return colors[type % colors.length]
  }

  @Builder
  FormField(title: string, @Builder content: () => void) {
    Column() {
      Text(title)
        .fontSize(15)
        .fontWeight(FontWeight.Medium)
        .fontColor('#8E8E93')
        .margin({ bottom: 8 })

      content()
    }
    .width('100%')
    .alignItems(HorizontalAlign.Start)
    .margin({ bottom: 20 })
  }

  @Builder
  MaterialsSection() {
    Column() {
      Text('材料清单')
        .fontSize(15)
        .fontWeight(FontWeight.Medium)
        .fontColor('#8E8E93')
        .margin({ bottom: 12 })

      // 已添加的材料列表
      ForEach(this.materials, (item: RecipeMaterial, index: number) => {
        Row() {
          Text(`${index + 1}`)
            .fontSize(12)
            .fontColor('#FFFFFF')
            .backgroundColor('#4ECDC4')
            .borderRadius(10)
            .width(20)
            .height(20)
            .textAlign(TextAlign.Center)

          Column() {
            Text(item.name)
              .fontSize(16)
              .fontColor('#1C1C1E')
              .fontWeight(FontWeight.Medium)

            Row() {
              if (item.amount) {
                Text(`${item.amount}`)
                  .fontSize(14)
                  .fontColor('#8E8E93')
                  .margin({ right: 4 })
              }
              if (item.unit) {
                Text(`${item.unit}`)
                  .fontSize(14)
                  .fontColor('#8E8E93')
              }
            }
            .margin({ top: 2 })
          }
          .layoutWeight(1)
          .alignItems(HorizontalAlign.Start)
          .margin({ left: 12 })

          Button() {
            Image($r('sys.media.ohos_ic_public_close'))
              .width(16)
              .height(16)
              .fillColor('#C7C7CC')
          }
          .type(ButtonType.Circle)
          .width(28)
          .height(28)
          .backgroundColor('#F2F2F7')
          .onClick(() => this.removeMaterial(index))
        }
        .width('100%')
        .padding({ left: 12, right: 12, top: 12, bottom: 12 })
        .backgroundColor('#FFFFFF')
        .borderRadius(12)
        .margin({ bottom: 8 })
      }, (item: RecipeMaterial, index: number) => `material_${index}`)

      // 添加材料表单
      Row() {
        TextInput({ placeholder: '材料名称', text: this.matName })
          .layoutWeight(1)
          .height(40)
          .fontSize(15)
          .backgroundColor('#F2F2F7')
          .borderRadius(12)
          .padding({ left: 12, right: 12 })
          .onChange((value: string) => {
            this.matName = value
          })

        TextInput({ placeholder: '用量', text: this.matAmount })
          .width(70)
          .height(40)
          .fontSize(15)
          .backgroundColor('#F2F2F7')
          .borderRadius(12)
          .padding({ left: 12, right: 12 })
          .margin({ left: 8 })
          .onChange((value: string) => {
            this.matAmount = value
          })

        TextInput({ placeholder: '单位', text: this.matUnit })
          .width(60)
          .height(40)
          .fontSize(15)
          .backgroundColor('#F2F2F7')
          .borderRadius(12)
          .padding({ left: 12, right: 12 })
          .margin({ left: 8 })
          .onChange((value: string) => {
            this.matUnit = value
          })

        Button() {
          Image($r('sys.media.ohos_ic_public_add'))
            .width(20)
            .height(20)
            .fillColor('#FFFFFF')
        }
        .type(ButtonType.Circle)
        .width(40)
        .height(40)
        .backgroundColor('#4ECDC4')
        .margin({ left: 8 })
        .onClick(() => this.addMaterial())
      }
      .width('100%')
      .alignItems(VerticalAlign.Center)
    }
    .width('100%')
    .alignItems(HorizontalAlign.Start)
    .margin({ bottom: 20 })
  }

  @Builder
  StepsSection() {
    Column() {
      Text('烹饪步骤')
        .fontSize(15)
        .fontWeight(FontWeight.Medium)
        .fontColor('#8E8E93')
        .margin({ bottom: 12 })

      // 已添加的步骤列表
      ForEach(this.steps, (item: RecipeStep, index: number) => {
        Row() {
          Text(`${item.stepNumber}`)
            .fontSize(14)
            .fontColor('#FFFFFF')
            .backgroundColor('#FF6B6B')
            .borderRadius(12)
            .width(24)
            .height(24)
            .textAlign(TextAlign.Center)

          Column() {
            Text(item.description)
              .fontSize(15)
              .fontColor('#1C1C1E')
              .lineHeight(22)
          }
          .layoutWeight(1)
          .alignItems(HorizontalAlign.Start)
          .margin({ left: 12 })

          Button() {
            Image($r('sys.media.ohos_ic_public_close'))
              .width(16)
              .height(16)
              .fillColor('#C7C7CC')
          }
          .type(ButtonType.Circle)
          .width(28)
          .height(28)
          .backgroundColor('#F2F2F7')
          .onClick(() => this.removeStep(index))
        }
        .width('100%')
        .padding({ left: 12, right: 12, top: 12, bottom: 12 })
        .backgroundColor('#FFFFFF')
        .borderRadius(12)
        .margin({ bottom: 8 })
      }, (item: RecipeStep, index: number) => `step_${item.stepNumber}`)

      // 添加步骤表单
      Column() {
        TextArea({ placeholder: '请输入烹饪步骤描述...', text: this.stepDesc })
          .width('100%')
          .height(80)
          .fontSize(15)
          .backgroundColor('#F2F2F7')
          .borderRadius(12)
          .padding(12)
          .onChange((value: string) => {
            this.stepDesc = value
          })

        Button('添加步骤')
          .width('100%')
          .height(44)
          .fontSize(16)
          .fontColor('#4ECDC4')
          .backgroundColor('#FFFFFF')
          .borderWidth(1)
          .borderColor('#4ECDC4')
          .borderRadius(12)
          .margin({ top: 12 })
          .onClick(() => this.addStep())
      }
      .width('100%')
      .alignItems(HorizontalAlign.Start)
    }
    .width('100%')
    .alignItems(HorizontalAlign.Start)
  }

  build() {
    Column() {
      // 顶部导航栏
      Row() {
        Button() {
          Image($r('sys.media.ohos_ic_public_arrow_left'))
            .width(24)
            .height(24)
            .fillColor('#1C1C1E')
        }
        .type(ButtonType.Circle)
        .width(40)
        .height(40)
        .backgroundColor('transparent')
        .onClick(() => this.goBack())

        Text(this.isEdit ? '编辑菜谱' : '新建菜谱')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#1C1C1E')
          .layoutWeight(1)
          .textAlign(TextAlign.Center)

        // 占位
        Column()
          .width(40)
      }
      .width('100%')
      .height(56)
      .padding({ left: 16, right: 16 })
      .backgroundColor('#FFFFFF')
      .borderRadius({ bottomLeft: 20, bottomRight: 20 })
      .shadow({
        radius: 12,
        color: 'rgba(0, 0, 0, 0.08)',
        offsetX: 0,
        offsetY: 4
      })

      // 主内容区域
      Scroll() {
        Column() {
          // 图片选择区域
          this.FormField('菜谱封面') {
            Column() {
              if (this.recipeImage) {
                // 已选择图片
                Stack({ alignContent: Alignment.TopEnd }) {
                  Image(this.recipeImage)
                    .width('100%')
                    .height(200)
                    .objectFit(ImageFit.Cover)
                    .borderRadius(16)

                  Button() {
                    Image($r('sys.media.ohos_ic_public_close'))
                      .width(18)
                      .height(18)
                      .fillColor('#FFFFFF')
                  }
                  .type(ButtonType.Circle)
                  .width(32)
                  .height(32)
                  .backgroundColor('rgba(0, 0, 0, 0.5)')
                  .offset({ x: -8, y: 8 })
                  .onClick(() => {
                    this.recipeImage = ''
                  })
                }
                .width('100%')
              } else {
                // 未选择图片，显示添加区域
                Column() {
                  Image($r('sys.media.ohos_ic_public_add'))
                    .width(48)
                    .height(48)
                    .fillColor('#C7C7CC')
                    .margin({ bottom: 12 })

                  Text('添加菜谱封面')
                    .fontSize(16)
                    .fontColor('#8E8E93')
                    .margin({ bottom: 4 })

                  Text('支持 JPG、PNG 格式')
                    .fontSize(13)
                    .fontColor('#C7C7CC')
                }
                .width('100%')
                .height(200)
                .justifyContent(FlexAlign.Center)
                .backgroundColor('#F8F8FA')
                .borderRadius(16)
                .borderWidth(2)
                .borderStyle(BorderStyle.Dashed)
                .borderColor('#E5E5EA')
              }
            }
            .width('100%')
            .onClick(() => this.selectImage())
          }

          // 菜谱名称
          this.FormField('菜谱名称') {
            TextInput({ placeholder: '请输入菜谱名称', text: this.recipeName })
              .width('100%')
              .height(48)
              .fontSize(16)
              .backgroundColor('#FFFFFF')
              .borderRadius(12)
              .padding({ left: 16, right: 16 })
              .onChange((value: string) => {
                this.recipeName = value
              })
          }

          // 菜谱描述
          this.FormField('菜谱描述') {
            TextArea({ placeholder: '请输入菜谱描述...', text: this.recipeDescription })
              .width('100%')
              .height(100)
              .fontSize(15)
              .backgroundColor('#FFFFFF')
              .borderRadius(12)
              .padding(16)
              .onChange((value: string) => {
                this.recipeDescription = value
              })
          }

          // 分类选择
          this.FormField('菜谱分类') {
            Flex({ wrap: FlexWrap.Wrap, space: { main: new LengthMetrics(8), cross: new LengthMetrics(8) } }) {
              ForEach(Array.from(RecipeTypeMap.entries()), (item: [number, string]) => {
                Text(item[1])
                  .fontSize(14)
                  .fontColor(this.recipeType === item[0] ? '#FFFFFF' : '#1C1C1E')
                  .backgroundColor(this.recipeType === item[0] ?
                    this.getCategoryColor(item[0]) : '#F2F2F7')
                  .borderRadius(20)
                  .padding({ left: 16, right: 16, top: 8, bottom: 8 })
                  .onClick(() => {
                    this.recipeType = item[0]
                  })
              }, (item: [number, string]) => `category_${item[0]}`)
            }
            .width('100%')
          }

          // 烹饪时间
          this.FormField('烹饪时间') {
            Column() {
              Row() {
                Text('5分钟')
                  .fontSize(13)
                  .fontColor('#8E8E93')

                Text(`${this.cookingTime}分钟`)
                  .fontSize(20)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#FF6B6B')
                  .layoutWeight(1)
                  .textAlign(TextAlign.Center)

                Text('180分钟')
                  .fontSize(13)
                  .fontColor('#8E8E93')
              }
              .width('100%')
              .margin({ bottom: 8 })

              Slider({
                value: this.cookingTime,
                min: 5,
                max: 180,
                step: 5
              })
                .width('100%')
                .blockColor('#FF6B6B')
                .trackColor('#E5E5EA')
                .selectedColor('#FF6B6B')
                .onChange((value: number) => {
                  this.cookingTime = value
                })
            }
            .width('100%')
          }

          // 难度选择
          this.FormField('难度等级') {
            Row() {
              ForEach([1, 2, 3, 4, 5], (level: number) => {
                Column() {
                  Text(`${level}`)
                    .fontSize(16)
                    .fontWeight(FontWeight.Medium)
                    .fontColor(this.difficulty === level ? '#FFFFFF' : '#8E8E93')

                  Text(level === 1 ? '简单' : level === 2 ? '较易' :
                    level === 3 ? '中等' : level === 4 ? '较难' : '困难')
                    .fontSize(11)
                    .fontColor(this.difficulty === level ? '#FFFFFF' : '#C7C7CC')
                    .margin({ top: 2 })
                }
                .layoutWeight(1)
                .height(56)
                .justifyContent(FlexAlign.Center)
                .backgroundColor(this.difficulty === level ? '#FF6B6B' : '#F2F2F7')
                .borderRadius(12)
                .onClick(() => {
                  this.difficulty = level
                })
              }, (level: number) => `difficulty_${level}`)
            }
            .width('100%')
            .space({ main: new LengthMetrics(8) })
          }

          // 材料清单
          this.FormField('') {
            this.MaterialsSection()
          }

          // 烹饪步骤
          this.FormField('') {
            this.StepsSection()
          }
        }
        .width('100%')
        .padding({ left: 16, right: 16, top: 16, bottom: 100 })
      }
      .layoutWeight(1)
      .scrollBar(BarState.Off)

      // 底部保存按钮
      Button(this.isEdit ? '保存修改' : '保存菜谱')
        .width('100%')
        .height(52)
        .fontSize(18)
        .fontWeight(FontWeight.Medium)
        .fontColor('#FFFFFF')
        .backgroundColor('#FF6B6B')
        .borderRadius(14)
        .margin({ left: 16, right: 16, bottom: 20 })
        .shadow({
          radius: 16,
          color: 'rgba(255, 107, 107, 0.4)',
          offsetX: 0,
          offsetY: 6
        })
        .onClick(() => this.saveRecipe())
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F2F2F7')
  }
}
