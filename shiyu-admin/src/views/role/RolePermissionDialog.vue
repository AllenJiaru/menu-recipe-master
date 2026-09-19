<template>
  <el-dialog
    :model-value="visible"
    :title="`分配权限 - ${roleName}`"
    width="600px"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
  >
    <div class="perm-summary" v-if="currentPermCount > 0">
      <el-tag v-for="cat in summaryTags" :key="cat.label" size="small" :type="cat.tagType" class="perm-summary-tag">
        {{ cat.label }}: {{ cat.count }}
      </el-tag>
      <span class="perm-summary-total">共 <b>{{ currentPermCount }}</b> 项权限</span>
    </div>

    <div class="perm-dialog-toolbar">
      <el-input v-model="filterText" placeholder="搜索权限名称或编码..." clearable size="small">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <div class="perm-dialog-actions">
        <el-button text size="small" @click="handleCheckAll">全选</el-button>
        <el-button text size="small" @click="handleUncheckAll">取消全选</el-button>
        <span class="perm-selected-count">已选 <b>{{ checkedCount }}</b> 项</span>
      </div>
    </div>

    <div class="permission-tree-wrap" v-loading="loading">
      <el-tree
        ref="treeRef"
        :data="permissionTree"
        :props="{ label: 'name', children: 'children' }"
        show-checkbox
        node-key="id"
        check-strictly
        default-expand-all
        :filter-node-method="filterNode"
      >
        <template #default="{ data }">
          <div class="tree-node">
            <span class="tree-node-label">{{ data.name }}</span>
            <span class="tree-node-code">{{ data.code }}</span>
            <span class="tree-node-type" :class="typeClass(data.type)">
              {{ data.type === 1 ? '目录' : data.type === 2 ? '菜单' : '按钮' }}
            </span>
          </div>
        </template>
      </el-tree>
      <el-empty v-if="!loading && permissionTree.length === 0" description="暂无权限数据" />
    </div>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { getPermissionTree } from '@/api/permission'
import { getRolePermissions, assignPermissions } from '@/api/role'
import { ElMessage } from 'element-plus'
import type { ElTree } from 'element-plus'

const props = defineProps<{
  visible: boolean
  roleId: number | null
  roleName?: string
}>()

const emit = defineEmits<{
  'update:visible': [val: boolean]
  saved: []
}>()

const treeRef = ref<InstanceType<typeof ElTree>>()
const permissionTree = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const filterText = ref('')
const checkedCount = ref(0)

const typeClass = (type: number) => {
  return type === 1 ? 'directory' : type === 2 ? 'menu' : 'button'
}

const summaryTags = computed(() => {
  const all = flattenTree(permissionTree.value)
  const checked = treeRef.value?.getCheckedKeys(false) as number[] || []
  const dirs = all.filter(p => p.type === 1 && checked.includes(p.id))
  const menus = all.filter(p => p.type === 2 && checked.includes(p.id))
  const btns = all.filter(p => p.type === 3 && checked.includes(p.id))
  const tags = []
  if (dirs.length) tags.push({ label: '目录', count: dirs.length, tagType: '' as const })
  if (menus.length) tags.push({ label: '菜单', count: menus.length, tagType: 'success' as const })
  if (btns.length) tags.push({ label: '按钮', count: btns.length, tagType: 'warning' as const })
  return tags
})

const currentPermCount = computed(() => {
  return summaryTags.value.reduce((sum, t) => sum + t.count, 0)
})

const flattenTree = (list: any[]): any[] => {
  const result: any[] = []
  for (const item of list) {
    result.push(item)
    if (item.children?.length) result.push(...flattenTree(item.children))
  }
  return result
}

const filterNode = (value: string, data: any) => {
  if (!value) return true
  return data.name?.toLowerCase().includes(value.toLowerCase()) || data.code?.toLowerCase().includes(value.toLowerCase())
}

const handleCheckAll = () => {
  const allKeys = getAllKeys(permissionTree.value)
  treeRef.value?.setCheckedKeys(allKeys)
  updateCheckedCount()
}

const handleUncheckAll = () => {
  treeRef.value?.setCheckedKeys([])
  checkedCount.value = 0
}

const getAllKeys = (list: any[]): number[] => {
  const keys: number[] = []
  for (const item of list) {
    keys.push(item.id)
    if (item.children?.length) keys.push(...getAllKeys(item.children))
  }
  return keys
}

const updateCheckedCount = () => {
  const checked = treeRef.value?.getCheckedKeys(false) as number[] || []
  checkedCount.value = checked.length
}

const loadData = async () => {
  if (!props.roleId) return
  loading.value = true
  try {
    const [treeRes, permRes]: any[] = await Promise.all([
      getPermissionTree(),
      getRolePermissions(props.roleId)
    ])
    permissionTree.value = treeRes.data || []
    const checkedIds = (permRes.data || []).map((p: any) => p.id || p.permissionId)
    setTimeout(() => {
      treeRef.value?.setCheckedKeys(checkedIds)
      updateCheckedCount()
    }, 100)
  } catch {
    ElMessage.error('加载权限数据失败')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!props.roleId) return
  const checkedKeys = treeRef.value?.getCheckedKeys(false) as number[]
  const halfCheckedKeys = treeRef.value?.getHalfCheckedKeys() as number[]
  const permissionIds = [...checkedKeys, ...halfCheckedKeys]
  saving.value = true
  try {
    await assignPermissions(props.roleId, permissionIds)
    ElMessage.success('权限分配成功')
    emit('update:visible', false)
    emit('saved')
  } catch (e: any) {
    ElMessage.error(e.message || '分配失败')
  } finally {
    saving.value = false
  }
}

watch(() => props.visible, (val) => {
  if (val && props.roleId) loadData()
})

watch(filterText, (val) => {
  treeRef.value?.filter(val)
})
</script>

<style scoped lang="scss">
.perm-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: linear-gradient(135deg, rgba(0, 122, 255, 0.04), rgba(88, 86, 214, 0.04));
  border-radius: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.perm-summary-tag {
  font-size: 12px !important;
}

.perm-summary-total {
  font-size: 12px;
  color: #86868B;
  margin-left: auto;
  b { color: #1D1D1F; font-weight: 600; }
}

.perm-dialog-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.perm-dialog-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.perm-selected-count {
  font-size: 12px;
  color: #86868B;
  b { color: #007AFF; font-weight: 600; }
}

.permission-tree-wrap {
  max-height: 420px;
  overflow-y: auto;
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 12px;
  padding: 8px;
  background: #FAFAFA;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.tree-node-label {
  font-size: 13px;
  font-weight: 500;
  color: #1D1D1F;
}

.tree-node-code {
  font-family: 'SF Mono', 'Menlo', monospace;
  font-size: 11px;
  color: #86868B;
  background: rgba(0, 0, 0, 0.04);
  padding: 1px 6px;
  border-radius: 4px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-node-type {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 500;
  flex-shrink: 0;

  &.directory { background: rgba(0, 122, 255, 0.1); color: #007AFF; }
  &.menu { background: rgba(52, 199, 89, 0.1); color: #34C759; }
  &.button { background: rgba(255, 149, 0, 0.1); color: #FF9500; }
}
</style>
