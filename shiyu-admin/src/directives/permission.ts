import type { App, Directive, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/stores/auth'

const hasPermission = (code: string): boolean => {
  const authStore = useAuthStore()
  return authStore.hasPermission(code)
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
