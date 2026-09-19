import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export function useUndoDelete<T extends { id: number }>(
  deleteFn: (id: number) => Promise<void>,
  restoreFn: (item: T) => Promise<void>,
  options: { duration?: number; message?: string } = {}
) {
  const { duration = 5000, message = '已删除' } = options
  const deletedItem = ref<T | null>(null)
  const timer = ref<ReturnType<typeof setTimeout> | null>(null)
  const isUndoing = ref(false)

  const softDelete = async (item: T, confirmMessage?: string) => {
    if (confirmMessage) {
      try {
        await ElMessageBox.confirm(confirmMessage, '确认删除', {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning'
        })
      } catch { return }
    }

    try {
      await deleteFn(item.id)
      deletedItem.value = item
      ElMessage({
        message: `${message} - ${duration / 1000}秒内可撤销`,
        type: 'success',
        duration: duration,
        showClose: true
      })

      timer.value = setTimeout(() => {
        deletedItem.value = null
        timer.value = null
      }, duration)
    } catch (err: any) {
      ElMessage.error(err?.response?.data?.message || '删除失败')
    }
  }

  const undo = async () => {
    if (!deletedItem.value) return
    isUndoing.value = true
    try {
      await restoreFn(deletedItem.value)
      ElMessage.success('已撤销删除')
      deletedItem.value = null
      if (timer.value) { clearTimeout(timer.value); timer.value = null }
    } catch (err: any) {
      ElMessage.error(err?.response?.data?.message || '撤销失败')
    } finally {
      isUndoing.value = false
    }
  }

  return { deletedItem, softDelete, undo, isUndoing }
}
