import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'
import type { User } from '@/types/model'

interface Permission {
  id: number
  parentId: number
  name: string
  code: string
  type: number
  path: string | null
  component: string | null
  icon: string | null
  sortOrder: number
  visible: number
  status: number
  children?: Permission[]
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<User | null>(null)
  const permissions = ref<Permission[]>([])
  const permissionCodes = ref<string[]>([])
  const permissionsLoaded = ref(false)

  async function login(username: string, password: string) {
    const res: any = await request.post('/auth/login', { username, password })
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    userInfo.value = res.data
    return res
  }

  async function getUserInfo() {
    const res: any = await request.get('/auth/info')
    userInfo.value = res.data
    return res.data
  }

  async function loadPermissions() {
    try {
      const res: any = await request.get('/auth/permissions')
      permissions.value = res.data || []
      permissionCodes.value = extractCodes(res.data || [])
    } catch {
      permissions.value = []
      permissionCodes.value = []
    } finally {
      permissionsLoaded.value = true
    }
  }

  function extractCodes(list: Permission[]): string[] {
    const codes: string[] = []
    const traverse = (items: Permission[]) => {
      for (const item of items) {
        if (item.code) codes.push(item.code)
        if (item.children) traverse(item.children)
      }
    }
    traverse(list)
    return codes
  }

  function hasPermission(code: string): boolean {
    if (userInfo.value?.role === 'super_admin' || userInfo.value?.role === 'admin') return true
    if (permissionCodes.value.length === 0) return false
    return permissionCodes.value.includes(code) || permissionCodes.value.some(c => code.startsWith(c + ':'))
  }

  function hasAnyPermission(codes: string[]): boolean {
    return codes.some(c => hasPermission(c))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    permissionCodes.value = []
    permissionsLoaded.value = false
    localStorage.removeItem('token')
  }

  return { token, userInfo, permissions, permissionCodes, permissionsLoaded, login, getUserInfo, loadPermissions, hasPermission, hasAnyPermission, logout }
})
