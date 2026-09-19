/**
 * 设置页 - Premium iOS 风格
 * 修复: 头像显示、UI排版、交互体验
 */
import { CommonStyle, RoleStyle } from '../common/CommonStyle'
import { CommonConstant } from '../common/CommonConstant'
import { RdbUtil } from '../common/RdbUtil'
import { HttpUtil } from '../common/HttpUtil'
import { ApiService } from '../common/ApiService'
import { LogUtil } from '../common/LogUtil'
import { ImageUtil } from '../common/ImageUtil'
import { ToastUtil } from '../common/ToastUtil'
import { GlobalStateManager, GlobalStateKey } from '../common/GlobalStateManager'
import router from '@ohos.router'
import relationalStore from '@ohos.data.relationalStore'
import preferences from '@ohos.data.preferences'
import common from '@ohos.app.ability.common'
import picker from '@ohos.file.picker'

@Entry
@Component
struct SettingsPage {
  @State spaceName: string = ''
  @State chefName: string = ''
  @State dinerName: string = ''
  @State currentRole: string = ''
  @State userAvatar: string = ''
  @State serverUrl: string = ''
  @State isSaving: boolean = false
  @State showRoleSwitchHint: boolean = false
  private context: common.UIAbilityContext | null = null

  aboutToAppear(): void {
    try {
      this.context = getContext(this) as common.UIAbilityContext
    } catch (err) {
      LogUtil.error('SettingsPage', `获取上下文失败: ${err}`)
    }
    this.loadConfig()
    this.loadRole()
  }

  async loadConfig(): Promise<void> {
    try {
      const predicates = new relationalStore.RdbPredicates('couple_config')
      const resultSet = await RdbUtil.query(predicates)
      try {
        if (resultSet.goToFirstRow()) {
          this.spaceName = resultSet.getString(resultSet.getColumnIndex('space_name')) || ''
          this.chefName = resultSet.getString(resultSet.getColumnIndex('chef_name')) || ''
          this.dinerName = resultSet.getString(resultSet.getColumnIndex('diner_name')) || ''
        }
      } finally {
        resultSet.close()
      }
      // 从 GlobalStateManager 加载头像
      const avatar = GlobalStateManager.getUserAvatar()
      this.userAvatar = avatar
      // 头像为空时从后端获取
      if (!avatar && ApiService.isLoggedIn()) {
        await ApiService.fetchAndSaveUserProfile()
        this.userAvatar = GlobalStateManager.getUserAvatar()
      }
      // 加载服务器地址
      this.serverUrl = AppStorage.get<string>('serverUrl') || HttpUtil.getServerBaseUrl()
      LogUtil.info('SettingsPage', `加载头像路径: ${avatar}`)
    } catch (e) {
      LogUtil.error('SettingsPage', `加载配置失败: ${JSON.stringify(e)}`)
    }
  }

  async loadRole(): Promise<void> {
    try {
      if (this.context) {
        const prefs = await preferences.getPreferences(this.context, 'menu_recipe_prefs')
        this.currentRole = await prefs.get('current_role', '') as string
      }
    } catch (e) {
      LogUtil.error('SettingsPage', `加载角色失败: ${JSON.stringify(e)}`)
    }
  }

  async saveConfig(): Promise<void> {
    if (!this.spaceName.trim()) {
      ToastUtil.showWarning('请输入空间名称')
      return
    }
    if (!this.chefName.trim()) {
      ToastUtil.showWarning('请输入主厨名称')
      return
    }
    if (!this.dinerName.trim()) {
      ToastUtil.showWarning('请输入食客名称')
      return
    }
    this.isSaving = true
    try {
      const predicates = new relationalStore.RdbPredicates('couple_config')
      const resultSet = await RdbUtil.query(predicates)
      try {
        if (resultSet.goToFirstRow()) {
          const id = resultSet.getLong(resultSet.getColumnIndex('id'))
          const p = new relationalStore.RdbPredicates('couple_config')
          p.equalTo('id', id)
          await RdbUtil.update('couple_config', {
            'space_name': this.spaceName.trim(),
            'chef_name': this.chefName.trim(),
            'diner_name': this.dinerName.trim()
          }, p)
        }
      } finally {
        resultSet.close()
      }
      // 同步更新 GlobalStateManager
      const config = GlobalStateManager.getCoupleConfig()
      config.space_name = this.spaceName.trim()
      config.chef_name = this.chefName.trim()
      config.diner_name = this.dinerName.trim()
      GlobalStateManager.setCoupleConfig(config)
      GlobalStateManager.updateUserInfoByRole(this.currentRole)

      // 保存服务器地址并重新登录
      const normalizedUrl = this.serverUrl.trim()
      if (normalizedUrl) {
        HttpUtil.setServerUrl(normalizedUrl)
        await ApiService.autoLogin(this.currentRole)
      }

      ToastUtil.showSuccess('保存成功')
    } catch (e) {
      LogUtil.error('SettingsPage', `保存失败: ${JSON.stringify(e)}`)
      ToastUtil.showError('保存失败')
    } finally {
      this.isSaving = false
    }
  }

  async selectAvatar(): Promise<void> {
    try {
      const photoPicker = new picker.PhotoViewPicker()
      const result = await photoPicker.select({
        MIMEType: picker.PhotoViewMIMETypes.IMAGE_TYPE,
        maxSelectNumber: 1
      })
      if (result?.photoUris && result.photoUris.length > 0 && this.context) {
        const saved = await ImageUtil.saveImageFromPath(this.context, result.photoUris[0], 'avatar.jpg')
        if (saved) {
          this.userAvatar = saved
          GlobalStateManager.setUserAvatar(saved)
          LogUtil.info('SettingsPage', `头像已保存: ${saved}`)
          ToastUtil.showSuccess('头像已更新')
        } else {
          ToastUtil.showError('保存头像失败')
        }
      }
    } catch (err) {
      LogUtil.error('SettingsPage', `选择头像失败: ${err}`)
      ToastUtil.showError('选择头像失败')
    }
  }

  async switchRole(role: string): Promise<void> {
    if (role === this.currentRole) return
    this.currentRole = role
    GlobalStateManager.setCurrentRole(role)
    GlobalStateManager.updateUserInfoByRole(role)
    try {
      if (this.context) {
        const prefs = await preferences.getPreferences(this.context, 'menu_recipe_prefs')
        await prefs.put('current_role', role)
        await prefs.flush()
      }
    } catch (e) {
      LogUtil.error('SettingsPage', `切换角色失败: ${e}`)
    }
    ToastUtil.showSuccess(role === CommonConstant.ROLE_CHEF ? '已切换为主厨' : '已切换为食客')
  }

  getRoleName(): string {
    return this.currentRole === CommonConstant.ROLE_CHEF ? '主厨' : '食客'
  }

  getRoleColor(): string {
    return this.currentRole === CommonConstant.ROLE_CHEF ? RoleStyle.CHEF_PRIMARY : RoleStyle.DINER_PRIMARY
  }

  getRoleBg(): string {
    return this.currentRole === CommonConstant.ROLE_CHEF ? RoleStyle.CHEF_LIGHT : RoleStyle.DINER_LIGHT
  }

  getUserName(): string {
    return this.currentRole === CommonConstant.ROLE_CHEF ? this.chefName : this.dinerName
  }

  getAvatarDisplayPath(): string {
    if (!this.userAvatar) return ''
    // 确保路径正确
    return ImageUtil.getDisplayPath(this.userAvatar)
  }

  build() {
    Column() {
      // ── 顶部导航栏 ──
      Row() {
        Text('‹ 返回')
          .fontSize(17)
          .fontColor(CommonStyle.PRIMARY)
          .onClick(() => router.back())
        Blank()
        Text('设置')
          .fontSize(17)
          .fontWeight(FontWeight.Bold)
          .fontColor(CommonStyle.TEXT_PRIMARY)
        Blank()
        Text('      ')
          .fontSize(17)
      }
      .width('100%').height(56)
      .padding({ left: 16, right: 16 })
      .backgroundColor(CommonStyle.BG_CARD)

      // ── 内容区 ──
      Scroll() {
        Column() {

          // ═══ 头像卡片 ═══
          Column() {
            Row() {
              // 头像
              Stack({ alignContent: Alignment.BottomEnd }) {
                // 头像图片或默认头像
                if (this.userAvatar) {
                  Image(this.getAvatarDisplayPath())
                    .width(72).height(72).borderRadius(36)
                    .objectFit(ImageFit.Cover)
                    .backgroundColor(CommonStyle.BG_SECONDARY)
                } else {
                  Column() {
                    Text(this.getUserName().charAt(0) || '?')
                      .fontSize(28).fontColor(Color.White).fontWeight(FontWeight.Bold)
                  }
                  .width(72).height(72).borderRadius(36)
                  .backgroundColor(this.getRoleColor())
                  .justifyContent(FlexAlign.Center)
                  .alignItems(HorizontalAlign.Center)
                }

                // 相机图标
                Column() {
                  Text('📷')
                    .fontSize(14)
                }
                .width(24).height(24).borderRadius(12)
                .backgroundColor(CommonStyle.BG_CARD)
                .justifyContent(FlexAlign.Center)
                .alignItems(HorizontalAlign.Center)
                .shadow({ radius: 4, color: '#00000020', offsetX: 0, offsetY: 2 })
              }
              .margin({ right: 16 })

              // 用户信息
              Column() {
                Text(this.getUserName() || '未设置昵称')
                  .fontSize(20).fontWeight(FontWeight.Bold)
                  .fontColor(CommonStyle.TEXT_PRIMARY)
                Row() {
                  Text(this.getRoleName())
                    .fontSize(12)
                    .fontColor(this.getRoleColor())
                    .backgroundColor(this.getRoleBg())
                    .padding({ left: 8, right: 8, top: 3, bottom: 3 })
                    .borderRadius(CommonStyle.RADIUS_CHIP)
                }
                .margin({ top: 6 })
              }
              .alignItems(HorizontalAlign.Start)

              Blank()

              Text('›')
                .fontSize(20).fontColor(CommonStyle.TEXT_TERTIARY)
            }
            .width('100%')
            .padding(16)
            .onClick(() => this.selectAvatar())
          }
          .width('100%')
          .backgroundColor(CommonStyle.BG_CARD)
          .borderRadius(CommonStyle.RADIUS_CARD)
          .margin({ left: 16, right: 16, top: 12 })
          .shadow({ radius: 8, color: '#00000008', offsetX: 0, offsetY: 2 })

          // ═══ 角色切换 ═══
          Column() {
            Text('角色切换')
              .fontSize(13)
              .fontColor(CommonStyle.TEXT_SECONDARY)
              .width('100%')
              .margin({ bottom: 12 })

            Row() {
              // 主厨
              Column() {
                Text('👨‍🍳').fontSize(28).margin({ bottom: 6 })
                Text('主厨')
                  .fontSize(15).fontWeight(FontWeight.Medium)
                  .fontColor(this.currentRole === CommonConstant.ROLE_CHEF ? Color.White : CommonStyle.TEXT_PRIMARY)
                Text('管理菜谱与订单')
                  .fontSize(11)
                  .fontColor(this.currentRole === CommonConstant.ROLE_CHEF ? '#FFFFFFCC' : CommonStyle.TEXT_TERTIARY)
                  .margin({ top: 2 })
              }
              .layoutWeight(1).height(88)
              .justifyContent(FlexAlign.Center)
              .alignItems(HorizontalAlign.Center)
              .backgroundColor(this.currentRole === CommonConstant.ROLE_CHEF ? RoleStyle.CHEF_PRIMARY : CommonStyle.BG_SECONDARY)
              .borderRadius(CommonStyle.RADIUS_MD)
              .onClick(() => this.switchRole(CommonConstant.ROLE_CHEF))

              Blank().width(12)

              // 食客
              Column() {
                Text('🍽').fontSize(28).margin({ bottom: 6 })
                Text('食客')
                  .fontSize(15).fontWeight(FontWeight.Medium)
                  .fontColor(this.currentRole === CommonConstant.ROLE_DINER ? Color.White : CommonStyle.TEXT_PRIMARY)
                Text('浏览与下单')
                  .fontSize(11)
                  .fontColor(this.currentRole === CommonConstant.ROLE_DINER ? '#FFFFFFCC' : CommonStyle.TEXT_TERTIARY)
                  .margin({ top: 2 })
              }
              .layoutWeight(1).height(88)
              .justifyContent(FlexAlign.Center)
              .alignItems(HorizontalAlign.Center)
              .backgroundColor(this.currentRole === CommonConstant.ROLE_DINER ? RoleStyle.DINER_PRIMARY : CommonStyle.BG_SECONDARY)
              .borderRadius(CommonStyle.RADIUS_MD)
              .onClick(() => this.switchRole(CommonConstant.ROLE_DINER))
            }
            .width('100%')
          }
          .width('100%')
          .padding(16)
          .backgroundColor(CommonStyle.BG_CARD)
          .borderRadius(CommonStyle.RADIUS_CARD)
          .margin({ left: 16, right: 16, top: 12 })
          .shadow({ radius: 8, color: '#00000008', offsetX: 0, offsetY: 2 })

          // ═══ 个人信息 ═══
          Column() {
            Text('个人信息')
              .fontSize(13)
              .fontColor(CommonStyle.TEXT_SECONDARY)
              .width('100%')
              .margin({ bottom: 8 })

            Column() {
              // 空间名称
              this.SettingsRow('🏠', '空间名称', this.spaceName, (v: string) => { this.spaceName = v })
              Divider().color(CommonStyle.SEPARATOR).margin({ left: 44 })

              // 主厨昵称
              this.SettingsRow('👨‍🍳', '主厨昵称', this.chefName, (v: string) => { this.chefName = v })
              Divider().color(CommonStyle.SEPARATOR).margin({ left: 44 })

              // 食客昵称
              this.SettingsRow('🍽', '食客昵称', this.dinerName, (v: string) => { this.dinerName = v })
            }
            .width('100%')
            .backgroundColor(CommonStyle.BG_CARD)
            .borderRadius(CommonStyle.RADIUS_MD)
          }
          .width('100%')
          .padding({ left: 16, right: 16 })
          .margin({ top: 12 })

          // ═══ 服务器设置 ═══
          Column() {
            Text('服务器设置')
              .fontSize(13)
              .fontColor(CommonStyle.TEXT_SECONDARY)
              .width('100%')
              .margin({ bottom: 8 })

            Column() {
              this.SettingsRow('🌐', '服务器地址', this.serverUrl, (v: string) => { this.serverUrl = v })
            }
            .width('100%')
            .backgroundColor(CommonStyle.BG_CARD)
            .borderRadius(CommonStyle.RADIUS_MD)
          }
          .width('100%')
          .padding({ left: 16, right: 16 })
          .margin({ top: 12 })

          // ═══ 关于 ═══
          Column() {
            Text('关于')
              .fontSize(13)
              .fontColor(CommonStyle.TEXT_SECONDARY)
              .width('100%')
              .margin({ bottom: 8 })

            Column() {
              this.InfoRow('🍽', '应用名称', '食遇')
              Divider().color(CommonStyle.SEPARATOR).margin({ left: 44 })
              this.InfoRow('📱', '版本', 'v1.0.0')
              Divider().color(CommonStyle.SEPARATOR).margin({ left: 44 })
              this.InfoRow('💻', '开发者', '小智个人开发')
            }
            .width('100%')
            .backgroundColor(CommonStyle.BG_CARD)
            .borderRadius(CommonStyle.RADIUS_MD)
          }
          .width('100%')
          .padding({ left: 16, right: 16 })
          .margin({ top: 12 })

          // ═══ 保存按钮 ═══
          Button(this.isSaving ? '保存中...' : '保存设置')
            .width('100%').height(52)
            .fontSize(17).fontWeight(FontWeight.Medium)
            .fontColor(Color.White)
            .backgroundColor(CommonStyle.PRIMARY)
            .borderRadius(CommonStyle.RADIUS_BUTTON)
            .margin({ left: 16, right: 16, top: 32, bottom: 40 })
            .enabled(!this.isSaving)
            .onClick(() => this.saveConfig())
        }
        .width('100%')
        .padding({ bottom: 20 })
      }
      .layoutWeight(1)
      .backgroundColor(CommonStyle.BG_PAGE)
    }
    .width('100%').height('100%')
    .backgroundColor(CommonStyle.BG_PAGE)
  }

  @Builder
  SettingsRow(emoji: string, label: string, value: string, onChange: (v: string) => void) {
    Row() {
      Text(emoji)
        .fontSize(18).width(28)
      Text(label)
        .fontSize(16).fontColor(CommonStyle.TEXT_PRIMARY)
        .layoutWeight(1)
        .margin({ left: 8 })
      TextInput({ placeholder: '请输入', text: value })
        .fontSize(16)
        .fontColor(CommonStyle.TEXT_PRIMARY)
        .placeholderColor(CommonStyle.TEXT_TERTIARY)
        .backgroundColor('transparent')
        .textAlign(TextAlign.End)
        .layoutWeight(1)
        .onChange(onChange)
    }
    .width('100%').height(52)
    .padding({ left: 12, right: 12 })
    .alignItems(VerticalAlign.Center)
  }

  @Builder
  InfoRow(emoji: string, label: string, value: string) {
    Row() {
      Text(emoji)
        .fontSize(18).width(28)
      Text(label)
        .fontSize(16).fontColor(CommonStyle.TEXT_PRIMARY)
        .layoutWeight(1)
        .margin({ left: 8 })
      Text(value)
        .fontSize(16).fontColor(CommonStyle.TEXT_SECONDARY)
    }
    .width('100%').height(52)
    .padding({ left: 12, right: 12 })
    .alignItems(VerticalAlign.Center)
  }
}
