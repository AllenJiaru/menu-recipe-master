/**
 * 全局设计系统
 * 食遇 - 情侣菜谱点餐 App
 * 参考 Apple HIG + Material Design 3 融合风格
 */

export class CommonStyle {
  // ── 主题色 ──
  static readonly PRIMARY: string = '#FF6B6B'
  static readonly PRIMARY_LIGHT: string = '#FF8E8E'
  static readonly PRIMARY_DARK: string = '#E85555'
  static readonly PRIMARY_BG: string = '#FFF0F0'

  // ── 辅助色 ──
  static readonly SECONDARY: string = '#4ECDC4'
  static readonly SECONDARY_LIGHT: string = '#E0F7F4'
  static readonly ACCENT: string = '#FF9500'
  static readonly ACCENT_LIGHT: string = '#FFF4E5'

  // ── 语义色 ──
  static readonly SUCCESS: string = '#34C759'
  static readonly SUCCESS_BG: string = '#E8F9ED'
  static readonly WARNING: string = '#FF9500'
  static readonly WARNING_BG: string = '#FFF4E5'
  static readonly ERROR: string = '#FF3B30'
  static readonly ERROR_BG: string = '#FFEBEA'
  static readonly INFO: string = '#007AFF'
  static readonly INFO_BG: string = '#E5F2FF'

  // ── 背景色 ──
  static readonly BG_PAGE: string = '#F2F2F7'
  static readonly BG_CARD: string = '#FFFFFF'
  static readonly BG_SECONDARY: string = '#F7F7FA'
  static readonly BG_GROUPED: string = '#F2F2F7'
  static readonly BG_ELEVATED: string = '#FFFFFF'

  // ── 文字色 ──
  static readonly TEXT_PRIMARY: string = '#1C1C1E'
  static readonly TEXT_SECONDARY: string = '#8E8E93'
  static readonly TEXT_TERTIARY: string = '#C7C7CC'
  static readonly TEXT_ON_PRIMARY: string = '#FFFFFF'
  static readonly TEXT_DESTRUCTIVE: string = '#FF3B30'

  // ── 分隔线 ──
  static readonly SEPARATOR: string = '#E5E5EA'
  static readonly SEPARATOR_OPAQUE: string = '#3C3C4340'

  // ── 字体尺寸 (SF Pro Scale) ──
  static readonly FONT_LARGE_TITLE: number = 34
  static readonly FONT_TITLE1: number = 28
  static readonly FONT_TITLE2: number = 22
  static readonly FONT_TITLE3: number = 20
  static readonly FONT_HEADLINE: number = 17
  static readonly FONT_BODY: number = 16
  static readonly FONT_CALLOUT: number = 15
  static readonly FONT_SUBHEAD: number = 14
  static readonly FONT_FOOTNOTE: number = 13
  static readonly FONT_CAPTION1: number = 12
  static readonly FONT_CAPTION2: number = 11

  // ── 间距 (8px Grid) ──
  static readonly SP_XS: number = 4
  static readonly SP_SM: number = 8
  static readonly SP_MD: number = 12
  static readonly SP_LG: number = 16
  static readonly SP_XL: number = 20
  static readonly SP_XXL: number = 24
  static readonly SP_XXXL: number = 32

  // ── 圆角 ──
  static readonly RADIUS_SM: number = 8
  static readonly RADIUS_MD: number = 12
  static readonly RADIUS_LG: number = 16
  static readonly RADIUS_XL: number = 20
  static readonly RADIUS_CARD: number = 16
  static readonly RADIUS_BUTTON: number = 14
  static readonly RADIUS_INPUT: number = 12
  static readonly RADIUS_CHIP: number = 20
  static readonly RADIUS_ROUND: number = 999

  // ── 阴影 ──
  static readonly SHADOW_SM: string = '0 2px 8px rgba(0,0,0,0.04)'
  static readonly SHADOW_MD: string = '0 4px 16px rgba(0,0,0,0.08)'
  static readonly SHADOW_LG: string = '0 8px 32px rgba(0,0,0,0.12)'

  // ── 动画 ──
  static readonly ANIM_FAST: number = 200
  static readonly ANIM_NORMAL: number = 350
  static readonly ANIM_SLOW: number = 500
}

/**
 * 角色主题色
 */
export class RoleStyle {
  static readonly CHEF_PRIMARY: string = '#FF6B6B'
  static readonly CHEF_LIGHT: string = '#FFF0F0'
  static readonly CHEF_GRADIENT: string[] = ['#FF6B6B', '#FF8E5A']

  static readonly DINER_PRIMARY: string = '#4ECDC4'
  static readonly DINER_LIGHT: string = '#E0F7F4'
  static readonly DINER_GRADIENT: string[] = ['#4ECDC4', '#44B09E']
}

/**
 * 订单状态样式
 */
export class OrderStatusStyle {
  static readonly PENDING: string = '#FF9500'
  static readonly PENDING_BG: string = '#FFF4E5'
  static readonly ACCEPTED: string = '#007AFF'
  static readonly ACCEPTED_BG: string = '#E5F2FF'
  static readonly COOKING: string = '#AF52DE'
  static readonly COOKING_BG: string = '#F3E8FF'
  static readonly COMPLETED: string = '#34C759'
  static readonly COMPLETED_BG: string = '#E8F9ED'
  static readonly CANCELLED: string = '#FF3B30'
  static readonly CANCELLED_BG: string = '#FFEBEA'

  static getStatusColor(status: number): string {
    if (status === 0) return OrderStatusStyle.PENDING
    if (status === 1) return OrderStatusStyle.ACCEPTED
    if (status === 2) return OrderStatusStyle.COOKING
    if (status === 3) return OrderStatusStyle.COMPLETED
    return OrderStatusStyle.CANCELLED
  }

  static getStatusBg(status: number): string {
    if (status === 0) return OrderStatusStyle.PENDING_BG
    if (status === 1) return OrderStatusStyle.ACCEPTED_BG
    if (status === 2) return OrderStatusStyle.COOKING_BG
    if (status === 3) return OrderStatusStyle.COMPLETED_BG
    return OrderStatusStyle.CANCELLED_BG
  }
}
