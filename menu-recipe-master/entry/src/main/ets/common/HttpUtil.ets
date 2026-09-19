/**
 * HTTP 工具类
 * 封装与后端 API 的通信
 */
import http from '@ohos.net.http'
import { LogUtil } from './LogUtil'
import { GlobalStateManager, GlobalStateKey } from './GlobalStateManager'

export class HttpUtil {
  private static readonly TAG: string = 'HttpUtil'
  // 后端 API 基础地址默认值（模拟器 10.0.2.2，真机需在设置中改为电脑 IP）
  private static readonly DEFAULT_BASE_URL: string = 'http://10.0.2.2:8081'
  private static readonly API_PREFIX: string = '/api'

  /**
   * 获取当前后端地址（优先读取全局配置）
   */
  private static getBaseUrl(): string {
    const saved = AppStorage.get<string>('serverUrl') || ''
    if (saved) {
      return saved.replace(/\/+$/, '')
    }
    return this.DEFAULT_BASE_URL
  }

  /**
   * 设置后端地址
   */
  static setServerUrl(url: string): void {
    const normalized = url.trim().replace(/\/+$/, '')
    AppStorage.setOrCreate('serverUrl', normalized)
  }

  /**
   * 获取后端地址（供图片 URL 拼接使用）
   */
  static getServerBaseUrl(): string {
    return this.getBaseUrl()
  }

  /**
   * 获取完整的 API URL
   */
  private static getUrl(path: string): string {
    return `${this.getBaseUrl()}${this.API_PREFIX}${path}`
  }

  /**
   * 获取认证头
   */
  private static getAuthHeaders(): Record<string, string> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json'
    }
    const token = AppStorage.get<string>('authToken') || ''
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
    return headers
  }

  /**
   * 发送 GET 请求
   */
  static async get<T>(path: string, params?: Record<string, string>): Promise<ApiResponse<T>> {
    let url = this.getUrl(path)
    if (params) {
      const queryString = Object.entries(params)
        .filter(([_, value]) => value !== undefined && value !== null && value !== '')
        .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
        .join('&')
      if (queryString) {
        url += `?${queryString}`
      }
    }

    try {
      const httpRequest = http.createHttp()
      const response = await httpRequest.request(url, {
        method: http.RequestMethod.GET,
        header: this.getAuthHeaders(),
        connectTimeout: 10000,
        readTimeout: 10000
      })

      httpRequest.destroy()

      if (response.responseCode === 200) {
        const result = JSON.parse(response.result as string) as ApiResponse<T>
        return result
      } else {
        LogUtil.error(this.TAG, `GET ${path} failed: ${response.responseCode}`)
        return { code: response.responseCode, message: '请求失败', data: null as T }
      }
    } catch (err) {
      LogUtil.error(this.TAG, `GET ${path} error: ${err}`)
      return { code: -1, message: `网络错误: ${err}`, data: null as T }
    }
  }

  /**
   * 发送 POST 请求
   */
  static async post<T>(path: string, body?: object): Promise<ApiResponse<T>> {
    const url = this.getUrl(path)
    try {
      const httpRequest = http.createHttp()
      const response = await httpRequest.request(url, {
        method: http.RequestMethod.POST,
        header: this.getAuthHeaders(),
        extraData: body ? JSON.stringify(body) : undefined,
        connectTimeout: 10000,
        readTimeout: 10000
      })

      httpRequest.destroy()

      if (response.responseCode === 200) {
        const result = JSON.parse(response.result as string) as ApiResponse<T>
        return result
      } else {
        LogUtil.error(this.TAG, `POST ${path} failed: ${response.responseCode}`)
        return { code: response.responseCode, message: '请求失败', data: null as T }
      }
    } catch (err) {
      LogUtil.error(this.TAG, `POST ${path} error: ${err}`)
      return { code: -1, message: `网络错误: ${err}`, data: null as T }
    }
  }

  /**
   * 发送 PUT 请求
   */
  static async put<T>(path: string, body?: object): Promise<ApiResponse<T>> {
    const url = this.getUrl(path)
    try {
      const httpRequest = http.createHttp()
      const response = await httpRequest.request(url, {
        method: http.RequestMethod.PUT,
        header: this.getAuthHeaders(),
        extraData: body ? JSON.stringify(body) : undefined,
        connectTimeout: 10000,
        readTimeout: 10000
      })

      httpRequest.destroy()

      if (response.responseCode === 200) {
        const result = JSON.parse(response.result as string) as ApiResponse<T>
        return result
      } else {
        LogUtil.error(this.TAG, `PUT ${path} failed: ${response.responseCode}`)
        return { code: response.responseCode, message: '请求失败', data: null as T }
      }
    } catch (err) {
      LogUtil.error(this.TAG, `PUT ${path} error: ${err}`)
      return { code: -1, message: `网络错误: ${err}`, data: null as T }
    }
  }

  /**
   * 发送 DELETE 请求
   */
  static async delete<T>(path: string): Promise<ApiResponse<T>> {
    const url = this.getUrl(path)
    try {
      const httpRequest = http.createHttp()
      const response = await httpRequest.request(url, {
        method: http.RequestMethod.DELETE,
        header: this.getAuthHeaders(),
        connectTimeout: 10000,
        readTimeout: 10000
      })

      httpRequest.destroy()

      if (response.responseCode === 200) {
        const result = JSON.parse(response.result as string) as ApiResponse<T>
        return result
      } else {
        LogUtil.error(this.TAG, `DELETE ${path} failed: ${response.responseCode}`)
        return { code: response.responseCode, message: '请求失败', data: null as T }
      }
    } catch (err) {
      LogUtil.error(this.TAG, `DELETE ${path} error: ${err}`)
      return { code: -1, message: `网络错误: ${err}`, data: null as T }
    }
  }

  /**
   * 上传文件
   */
  static async upload<T>(path: string, filePath: string, fileName: string): Promise<ApiResponse<T>> {
    const url = this.getUrl(path)
    try {
      const httpRequest = http.createHttp()
      const multiFormDataList: http.MultiFormData[] = [
        {
          name: 'file',
          contentType: 'application/octet-stream',
          remoteFileName: fileName,
          filePath: filePath
        }
      ]
      const response = await httpRequest.request(url, {
        method: http.RequestMethod.POST,
        header: {
          'Authorization': `Bearer ${AppStorage.get<string>('authToken') || ''}`
        },
        extraData: multiFormDataList,
        connectTimeout: 30000,
        readTimeout: 30000
      })

      httpRequest.destroy()

      if (response.responseCode === 200) {
        const result = JSON.parse(response.result as string) as ApiResponse<T>
        return result
      } else {
        LogUtil.error(this.TAG, `UPLOAD ${path} failed: ${response.responseCode}`)
        return { code: response.responseCode, message: '上传失败', data: null as T }
      }
    } catch (err) {
      LogUtil.error(this.TAG, `UPLOAD ${path} error: ${err}`)
      return { code: -1, message: `网络错误: ${err}`, data: null as T }
    }
  }

  /**
   * 设置认证令牌
   */
  static setAuthToken(token: string): void {
    AppStorage.setOrCreate('authToken', token)
  }

  /**
   * 清除认证令牌
   */
  static clearAuthToken(): void {
    AppStorage.delete('authToken')
  }

  /**
   * 检查后端连接状态
   */
  static async checkHealth(): Promise<boolean> {
    try {
      const httpRequest = http.createHttp()
      const response = await httpRequest.request(`${this.getBaseUrl()}/api/health`, {
        method: http.RequestMethod.GET,
        connectTimeout: 5000,
        readTimeout: 5000
      })
      httpRequest.destroy()
      return response.responseCode === 200
    } catch (err) {
      LogUtil.error(this.TAG, `Health check failed: ${err}`)
      return false
    }
  }
}

/**
 * API 响应接口
 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

/**
 * 分页响应接口
 */
export interface PageResponse<T> {
  records: T[]
  total: number
  page: number
  size: number
}
