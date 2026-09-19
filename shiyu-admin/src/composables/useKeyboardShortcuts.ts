import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'

export function useKeyboardShortcuts() {
  const router = useRouter()
  const themeStore = useThemeStore()

  const handleKeydown = (e: KeyboardEvent) => {
    const isMod = e.metaKey || e.ctrlKey
    if (isMod && e.key === 'k') {
      e.preventDefault()
      window.dispatchEvent(new CustomEvent('focus-search'))
    }
    if (isMod && e.key === 'd') {
      e.preventDefault()
      themeStore.toggleTheme()
    }
    if (isMod && e.key === 'l') {
      e.preventDefault()
      router.push('/login')
    }
    if (isMod && e.key === 'n') {
      e.preventDefault()
      router.push('/recipes/create')
    }
    if (e.key === 'Escape') {
      window.dispatchEvent(new CustomEvent('close-dialogs'))
    }
  }

  onMounted(() => window.addEventListener('keydown', handleKeydown))
  onUnmounted(() => window.removeEventListener('keydown', handleKeydown))
}
