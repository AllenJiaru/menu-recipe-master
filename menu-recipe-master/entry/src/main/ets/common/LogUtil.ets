/**
 * 日志工具类
 * 封装鸿蒙HiLog日志系统
 */
import hilog from '@ohos.hilog'

export class LogUtil {
  private static readonly DOMAIN: number = 0x0001
  private static readonly PREFIX: string = 'MenuRecipe'

  static debug(tag: string, format: string): void {
    hilog.debug(LogUtil.DOMAIN, `${LogUtil.PREFIX}_${tag}`, format)
  }

  static info(tag: string, format: string): void {
    hilog.info(LogUtil.DOMAIN, `${LogUtil.PREFIX}_${tag}`, format)
  }

  static warn(tag: string, format: string): void {
    hilog.warn(LogUtil.DOMAIN, `${LogUtil.PREFIX}_${tag}`, format)
  }

  static error(tag: string, format: string): void {
    hilog.error(LogUtil.DOMAIN, `${LogUtil.PREFIX}_${tag}`, format)
  }

  static fatal(tag: string, format: string): void {
    hilog.fatal(LogUtil.DOMAIN, `${LogUtil.PREFIX}_${tag}`, format)
  }
}
