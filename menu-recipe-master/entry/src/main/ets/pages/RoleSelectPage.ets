/**
 * 角色选择页 - 大厂风格卡片选择
 */
import { CommonStyle, RoleStyle } from '../common/CommonStyle'
import { CommonConstant } from '../common/CommonConstant'
import { RdbUtil } from '../common/RdbUtil'
import { LogUtil } from '../common/LogUtil'
import { ApiService } from '../common/ApiService'
import { CoupleConfig } from '../model/CoupleConfig'
import router from '@ohos.router'
import preferences from '@ohos.data.preferences'
import common from '@ohos.app.ability.common'
import relationalStore from '@ohos.data.relationalStore'

@Entry
@Component
struct RoleSelectPage {
  @State selectedRole: string = ''
  @State chefName: string = ''
  @State dinerName: string = ''
  @State spaceName: string = ''
  @State isSubmitting: boolean = false
  private context: common.UIAbilityContext | null = null

  aboutToAppear() {
    try {
      this.context = getContext(this) as common.UIAbilityContext
      this.loadExistingConfig()
    } catch (err) {
      LogUtil.error('RoleSelectPage', `获取上下文失败: ${err}`)
    }
  }

  async loadExistingConfig() {
    try {
      const predicates = new relationalStore.RdbPredicates('couple_config')
      const resultSet = await RdbUtil.query(predicates)
      if (resultSet.goToFirstRow()) {
        this.spaceName = resultSet.getString(resultSet.getColumnIndex('space_name'))
        this.chefName = resultSet.getString(resultSet.getColumnIndex('chef_name'))
        this.dinerName = resultSet.getString(resultSet.getColumnIndex('diner_name'))
      }
      resultSet.close()
    } catch (err) {
      LogUtil.error('RoleSelectPage', `加载配置失败: ${err}`)
    }
  }

  canSubmit(): boolean {
    return this.selectedRole !== '' &&
      this.chefName.trim() !== '' &&
      this.dinerName.trim() !== '' &&
      this.spaceName.trim() !== '' &&
      !this.isSubmitting
  }

  async submitConfig() {
    if (!this.context) return
    this.isSubmitting = true
    try {
      const predicates = new relationalStore.RdbPredicates('couple_config')
      const existing = await RdbUtil.query(predicates)
      const hasExisting = existing.rowCount > 0
      existing.close()

      const values: relationalStore.ValuesBucket = {
        'space_name': this.spaceName.trim(),
        'chef_name': this.chefName.trim(),
        'diner_name': this.dinerName.trim(),
        'create_time': Date.now()
      }

      if (hasExisting) {
        await RdbUtil.update('couple_config', values, predicates)
      } else {
        await RdbUtil.insert('couple_config', values)
      }

      const prefs = await preferences.getPreferences(this.context, 'menu_recipe_prefs')
      await prefs.put('current_role', this.selectedRole)
      await prefs.flush()

      // 自动登录云端账号，保证后续同步可用
      const loginOk = await ApiService.autoLogin(this.selectedRole)
      if (loginOk) {
        await ApiService.fetchAndSaveUserProfile()
      }

      const target = this.selectedRole === CommonConstant.ROLE_CHEF
        ? 'pages/ChefHomePage' : 'pages/DinerHomePage'
      router.replaceUrl({ url: target })
    } catch (err) {
      LogUtil.error('RoleSelectPage', `提交失败: ${err}`)
      this.isSubmitting = false
    }
  }

  build() {
    Column() {
      // 顶部大标题区
      Column() {
        Text('👋')
          .fontSize(48)
          .margin({ bottom: 12 })
        Text('欢迎来到食遇')
          .fontSize(CommonStyle.FONT_TITLE1)
          .fontWeight(FontWeight.Bold)
          .fontColor(CommonStyle.TEXT_PRIMARY)
        Text('选择你的角色，开始美食之旅')
          .fontSize(CommonStyle.FONT_SUBHEAD)
          .fontColor(CommonStyle.TEXT_SECONDARY)
          .margin({ top: 6 })
      }
      .width('100%')
      .padding({ top: 60, bottom: 32 })
      .alignItems(HorizontalAlign.Center)

      // 角色卡片
      Row() {
        // 主厨卡片
        this.RoleCard({
          role: CommonConstant.ROLE_CHEF,
          emoji: '👨‍🍳',
          title: '我是主厨',
          desc: '创建菜谱\n接受点餐',
          color: RoleStyle.CHEF_PRIMARY,
          bgColor: RoleStyle.CHEF_LIGHT
        })

        Blank().width(16)

        // 食客卡片
        this.RoleCard({
          role: CommonConstant.ROLE_DINER,
          emoji: '🍽',
          title: '我是食客',
          desc: '浏览菜谱\n发起点餐',
          color: RoleStyle.DINER_PRIMARY,
          bgColor: RoleStyle.DINER_LIGHT
        })
      }
      .width('100%')
      .padding({ left: 24, right: 24 })
      .justifyContent(FlexAlign.Center)

      // 信息输入区
      if (this.selectedRole !== '') {
        Column() {
          // 空间名称
          this.InputField('你们的小窝', '例如：我们的厨房', (v: string) => { this.spaceName = v }, this.spaceName)

          // 主厨昵称
          this.InputField('主厨昵称', '谁是大厨？', (v: string) => { this.chefName = v }, this.chefName)

          // 食客昵称
          this.InputField('食客昵称', '谁来品尝？', (v: string) => { this.dinerName = v }, this.dinerName)
        }
        .width('100%')
        .padding({ left: 24, right: 24 })
        .margin({ top: 32 })
        .animation({ duration: 300, curve: Curve.EaseOut })
      }

      Blank().layoutWeight(1)

      // 确认按钮
      Button(this.isSubmitting ? '正在创建...' : '开始美食之旅')
        .width('85%').height(52)
        .fontSize(CommonStyle.FONT_HEADLINE)
        .fontWeight(FontWeight.Medium)
        .fontColor(Color.White)
        .backgroundColor(this.canSubmit() ? CommonStyle.PRIMARY : CommonStyle.TEXT_TERTIARY)
        .borderRadius(CommonStyle.RADIUS_BUTTON)
        .enabled(this.canSubmit())
        .margin({ bottom: 48 })
        .onClick(() => this.submitConfig())
    }
    .width('100%').height('100%')
    .backgroundColor(CommonStyle.BG_PAGE)
  }

  @Builder
  RoleCard(params: { role: string; emoji: string; title: string; desc: string; color: string; bgColor: string }) {
    Column() {
      Text(params.emoji)
        .fontSize(40)
        .margin({ bottom: 12 })

      Text(params.title)
        .fontSize(CommonStyle.FONT_HEADLINE)
        .fontWeight(FontWeight.Bold)
        .fontColor(params.color)

      Text(params.desc)
        .fontSize(CommonStyle.FONT_CAPTION1)
        .fontColor(CommonStyle.TEXT_SECONDARY)
        .textAlign(TextAlign.Center)
        .margin({ top: 8 })
        .lineHeight(18)
    }
    .width('42%').height(160)
    .justifyContent(FlexAlign.Center)
    .alignItems(HorizontalAlign.Center)
    .backgroundColor(params.bgColor)
    .borderRadius(CommonStyle.RADIUS_CARD)
    .border({
      width: this.selectedRole === params.role ? 2.5 : 0,
      color: params.color
    })
    .shadow({
      radius: this.selectedRole === params.role ? 16 : 4,
      color: this.selectedRole === params.role ? `${params.color}30` : '#00000008',
      offsetX: 0,
      offsetY: this.selectedRole === params.role ? 4 : 2
    })
    .scale({
      x: this.selectedRole === params.role ? 1.02 : 1,
      y: this.selectedRole === params.role ? 1.02 : 1
    })
    .animation({ duration: 250, curve: Curve.EaseOut })
    .onClick(() => { this.selectedRole = params.role })
  }

  @Builder
  InputField(label: string, placeholder: string, onChange: (v: string) => void, value: string) {
    Column() {
      Text(label)
        .fontSize(CommonStyle.FONT_FOOTNOTE)
        .fontWeight(FontWeight.Medium)
        .fontColor(CommonStyle.TEXT_SECONDARY)
        .width('100%')
        .margin({ bottom: 8 })

      TextInput({ placeholder: placeholder, text: value })
        .width('100%').height(48)
        .fontSize(CommonStyle.FONT_BODY)
        .fontColor(CommonStyle.TEXT_PRIMARY)
        .placeholderColor(CommonStyle.TEXT_TERTIARY)
        .backgroundColor(CommonStyle.BG_CARD)
        .borderRadius(CommonStyle.RADIUS_INPUT)
        .padding({ left: 16, right: 16 })
        .onChange(onChange)
    }
    .width('100%')
    .margin({ bottom: 16 })
    .alignItems(HorizontalAlign.Start)
  }
}
