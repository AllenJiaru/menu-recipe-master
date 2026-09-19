import { ref, type Ref } from 'vue'

export function useDragDrop<T extends { id: number; sortOrder?: number }>(
  items: Ref<T[]>,
  updateFn: (id: number, sortOrder: number) => Promise<void>
) {
  const draggedItem = ref<T | null>(null)
  const dragOverItem = ref<T | null>(null)
  const isDragging = ref(false)

  const onDragStart = (item: T, event: DragEvent) => {
    draggedItem.value = item
    isDragging.value = true
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move'
      event.dataTransfer.setData('text/plain', String(item.id))
    }
  }

  const onDragOver = (item: T, event: DragEvent) => {
    event.preventDefault()
    if (event.dataTransfer) event.dataTransfer.dropEffect = 'move'
    dragOverItem.value = item
  }

  const onDragLeave = () => {
    dragOverItem.value = null
  }

  const onDragEnd = () => {
    draggedItem.value = null
    dragOverItem.value = null
    isDragging.value = false
  }

  const onDrop = async (targetItem: T) => {
    if (!draggedItem.value || draggedItem.value.id === targetItem.id) {
      onDragEnd()
      return
    }

    const currentItems = [...items.value]
    const draggedIdx = currentItems.findIndex(i => i.id === draggedItem.value!.id)
    const targetIdx = currentItems.findIndex(i => i.id === targetItem.id)

    const [removed] = currentItems.splice(draggedIdx, 1)
    currentItems.splice(targetIdx, 0, removed)

    const updatedItems = currentItems.map((item, idx) => ({ ...item, sortOrder: idx + 1 }))
    items.value = updatedItems as T[]

    try {
      await Promise.all(
        updatedItems.map((item, idx) => updateFn(item.id, idx + 1))
      )
    } catch (err) {
      console.error('Failed to update sort order:', err)
    }

    onDragEnd()
  }

  const isDragOver = (item: T) => dragOverItem.value?.id === item.id && draggedItem.value?.id !== item.id

  return {
    draggedItem, isDragging, isDragOver,
    onDragStart, onDragOver, onDragLeave, onDragEnd, onDrop
  }
}
