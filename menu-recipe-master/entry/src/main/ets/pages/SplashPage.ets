/**
 * 闪屏页 - 品牌展示 + 初始化
 */
import { CommonStyle } from '../common/CommonStyle'
import { CommonConstant } from '../common/CommonConstant'
import { RdbUtil } from '../common/RdbUtil'
import { LogUtil } from '../common/LogUtil'
import { HttpUtil } from '../common/HttpUtil'
import { ApiService } from '../common/ApiService'
import router from '@ohos.router'
import preferences from '@ohos.data.preferences'
import common from '@ohos.app.ability.common'

@Entry
@Component
struct SplashPage {
  @State logoScale: number = 0.5
  @State logoOpacity: number = 0
  @State titleOpacity: number = 0
  @State progress: number = 0
  @State statusText: string = '正在初始化...'
  private context: common.UIAbilityContext | null = null

  aboutToAppear() {
    try {
      this.context = getContext(this) as common.UIAbilityContext
    } catch (err) {
      LogUtil.error('SplashPage', `获取上下文失败: ${err}`)
    }
  }

  onPageShow() {
    this.logoScale = 0.5
    this.logoOpacity = 0
    this.titleOpacity = 0

    animateTo({ duration: 600, curve: Curve.EaseOut }, () => {
      this.logoScale = 1
      this.logoOpacity = 1
    })

    setTimeout(() => {
      animateTo({ duration: 400, curve: Curve.EaseOut }, () => {
        this.titleOpacity = 1
      })
    }, 300)

    if (this.context) {
      this.initApp()
    }
  }

  async initApp() {
    try {
      this.progress = 10
      this.statusText = '正在检查服务器...'
      await this.sleep(200)

      // 检查后端连接
      const isServerAlive = await HttpUtil.checkHealth()
      if (!isServerAlive) {
        this.statusText = '服务器连接失败，请检查网络'
        await this.sleep(2000)
        // 继续启动，使用本地数据
      }

      this.progress = 30
      this.statusText = '正在初始化数据库...'
      await RdbUtil.init(this.context!)
      this.progress = 50
      await this.sleep(150)

      this.statusText = '正在加载配置...'
      const currentRole = await this.getCurrentRole()
      this.progress = 70

      // 服务器可用时按角色自动登录，保证云端同步可用
      if (isServerAlive && currentRole) {
        this.statusText = '正在登录云端账号...'
        const loginOk = await ApiService.autoLogin(currentRole)
        if (loginOk) {
          await ApiService.fetchAndSaveUserProfile()
        }
      }
      this.progress = 80
      await this.sleep(150)

      this.progress = 100
      this.statusText = '加载完成'
      await this.sleep(400)

      if (currentRole) {
        const target = currentRole === CommonConstant.ROLE_CHEF
          ? 'pages/ChefHomePage' : 'pages/DinerHomePage'
        router.replaceUrl({ url: target })
      } else {
        router.replaceUrl({ url: 'pages/RoleSelectPage' })
      }
    } catch (err) {
      LogUtil.error('SplashPage', `初始化失败: ${err}`)
      this.statusText = '初始化失败'
      this.progress = 100
      await this.sleep(500)
      router.replaceUrl({ url: 'pages/RoleSelectPage' })
    }
  }

  async getCurrentRole(): Promise<string> {
    try {
      const prefs = await preferences.getPreferences(this.context!, 'menu_recipe_prefs')
      return await prefs.get('current_role', '') as string
    } catch (err) {
      return ''
    }
  }

  sleep(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms))
  }

  build() {
    Stack() {
      // 渐变背景
      Column()
        .width('100%').height('100%')
        .linearGradient({ direction: GradientDirection.Bottom, colors: [['#FF6B6B', 0], ['#FF8E5A', 0.5], ['#FFA07A', 1]] })

      Column() {
        Blank().layoutWeight(1)

        // Logo
        Column() {
          Text('🍽')
            .fontSize(72)
            .scale({ x: this.logoScale, y: this.logoScale })
            .opacity(this.logoOpacity)

          Text('食遇')
            .fontSize(36)
            .fontWeight(FontWeight.Bold)
            .fontColor(Color.White)
            .opacity(this.titleOpacity)
            .margin({ top: 16 })

          Text('为爱下厨，每一餐都是情书')
            .fontSize(14)
            .fontColor('#FFFFFFCC')
            .opacity(this.titleOpacity)
            .margin({ top: 8 })
        }
        .alignItems(HorizontalAlign.Center)

        Blank().layoutWeight(1)

        // 加载进度
        Column() {
          Text(this.statusText)
            .fontSize(12)
            .fontColor('#FFFFFFCC')
            .margin({ bottom: 8 })

          Progress({ value: this.progress, total: 100, type: ProgressType.Linear })
            .width(160).height(3)
            .color(Color.White)
            .backgroundColor('#FFFFFF40')
            .borderRadius(2)

          Text(`${this.progress}%`)
            .fontSize(11).fontColor('#FFFFFF99')
            .margin({ top: 8 })
        }
        .margin({ bottom: 60 })
      }
      .width('100%').height('100%')
    }
    .width('100%').height('100%')
  }
}
