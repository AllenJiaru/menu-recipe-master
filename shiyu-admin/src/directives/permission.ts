import type { App, Directive, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/stores/auth'

const hasPermission = (code: string): boolean => {
  const authStore = useAuthStore()
  if (authStore.userInfo?.role === 'super_admin' || authStore.userInfo?.role === 'admin') return true
  if (!authStore.permissions || authStore.permissions.length === 0) return true
  return authStore.permissions.some(p => p.code === code || code.startsWith(p.code + ':'))
}

const vPermission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    if (value && !hasPermission(value)) {
      el.parentNode?.removeChild(el)
    }
  }
}

const vPermissionOr: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    if (value && Array.isArray(value)) {
      const hasAny = value.some((code: string) => hasPermission(code))
      if (!hasAny) {
        el.parentNode?.removeChild(el)
      }
    }
  }
}

export function setupPermissionDirectives(app: App) {
  app.directive('permission', vPermission)
  app.directive('permission-or', vPermissionOr)
}
