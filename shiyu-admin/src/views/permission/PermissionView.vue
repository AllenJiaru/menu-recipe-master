<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon indigo"><el-icon :size="22"><Lock /></el-icon></div>
        <div class="page-header-text">
          <h2>权限管理</h2>
          <p>管理系统权限与菜单配置</p>
        </div>
      </div>
      <el-button type="primary" @click="showDialog()" v-permission="'system:permission:create'">
        <el-icon><Plus /></el-icon> 新建权限
      </el-button>
    </div>

    <div class="page-toolbar">
      <div class="page-search">
        <el-input v-model="searchKeyword" placeholder="搜索权限名称或编码..." clearable @input="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
      <el-select v-model="filterType" placeholder="全部类型" clearable style="width: 130px" @change="handleSearch">
        <el-option label="目录" :value="1" />
        <el-option label="菜单" :value="2" />
        <el-option label="按钮" :value="3" />
      </el-select>
      <div class="page-toolbar-right">
        <span class="page-count">共 <b>{{ flatCount }}</b> 个权限</span>
      </div>
    </div>

    <div class="perm-cards" v-loading="loading">
      <template v-if="filteredTree.length > 0">
        <div v-for="group in filteredTree" :key="group.id" class="perm-card">
          <div class="perm-card-head">
            <div class="perm-card-title">
              <div class="perm-card-icon" :style="{ background: groupColors[group.id % groupColors.length] }">
                <el-icon :size="18" color="#fff"><component :is="group.icon || 'Folder'" /></el-icon>
              </div>
              <div class="perm-card-text">
                <span class="perm-card-name">{{ group.name }}</span>
                <span class="perm-card-code">{{ group.code }}</span>
              </div>
            </div>
            <div class="perm-card-actions">
              <span class="perm-card-count">{{ countChildren(group) }} 个权限</span>
              <el-button text size="small" class="card-btn" @click="showDialog(group)" v-permission="'system:permission:edit'">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button text size="small" class="card-btn" @click="showDialog(null, group.id)" v-permission="'system:permission:create'">
                <el-icon><Plus /></el-icon>
              </el-button>
              <el-popconfirm title="确定删除?" @confirm="handleDelete(group.id)">
                <template #reference>
                  <el-button text size="small" class="card-btn danger" v-permission="'system:permission:delete'">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>

          <div class="perm-card-body" v-if="group.children?.length">
            <div v-for="menu in group.children" :key="menu.id" class="perm-row">
              <div class="perm-row-left">
                <div class="perm-row-icon menu"><el-icon :size="14"><Menu /></el-icon></div>
                <div class="perm-row-info">
                  <span class="perm-row-name">{{ menu.name }}</span>
                  <span class="perm-row-code">{{ menu.code }}</span>
                  <span v-if="menu.path" class="perm-row-path">{{ menu.path }}</span>
                </div>
              </div>
              <div class="perm-row-right">
                <span class="type-tag menu">菜单</span>
                <div class="perm-row-btns">
                  <el-button text size="small" class="card-btn" @click="showDialog(menu)" v-permission="'system:permission:edit'">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button text size="small" class="card-btn" @click="showDialog(null, menu.id)" v-permission="'system:permission:create'">
                    <el-icon><Plus /></el-icon>
                  </el-button>
                  <el-popconfirm title="确定删除?" @confirm="handleDelete(menu.id)">
                    <template #reference>
                      <el-button text size="small" class="card-btn danger" v-permission="'system:permission:delete'">
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </template>
                  </el-popconfirm>
                </div>
              </div>

              <div v-for="btn in (menu.children || [])" :key="btn.id" class="perm-row nested">
                <div class="perm-row-left">
                  <div class="perm-row-icon btn"><el-icon :size="14"><Document /></el-icon></div>
                  <div class="perm-row-info">
                    <span class="perm-row-name">{{ btn.name }}</span>
                    <span class="perm-row-code">{{ btn.code }}</span>
                  </div>
                </div>
                <div class="perm-row-right">
                  <span class="type-tag btn">按钮</span>
                  <div class="perm-row-btns">
                    <el-button text size="small" class="card-btn" @click="showDialog(btn)" v-permission="'system:permission:edit'">
                      <el-icon><Edit /></el-icon>
                    </el-button>
                    <el-popconfirm title="确定删除?" @confirm="handleDelete(btn.id)">
                      <template #reference>
                        <el-button text size="small" class="card-btn danger" v-permission="'system:permission:delete'">
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </template>
                    </el-popconfirm>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="perm-card-empty">暂无子权限</div>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="暂无权限数据" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editItem ? '编辑权限' : '新建权限'" width="520px" destroy-on-close>
      <el-form :model="form" label-width="80px" size="large">
        <el-form-item label="上级">
          <el-tree-select
            v-model="form.parentId"
            :data="permissionTree"
            :props="{ label: 'name', children: 'children', value: 'id' }"
            check-strictly clearable placeholder="无（顶级权限）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="请输入权限名称" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.code" placeholder="如 system:user:list" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio-button :value="1">目录</el-radio-button>
            <el-radio-button :value="2">菜单</el-radio-button>
            <el-radio-button :value="3">按钮</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="路由路径" v-if="form.type !== 3"><el-input v-model="form.path" placeholder="如 /users" /></el-form-item>
        <el-form-item label="组件路径" v-if="form.type === 2"><el-input v-model="form.component" placeholder="如 views/user/UserListView" /></el-form-item>
        <el-form-item label="图标" v-if="form.type !== 3"><el-input v-model="form.icon" placeholder="图标名称" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" :max="999" /></el-form-item>
        <div class="form-row">
          <el-form-item label="可见" class="form-row-item"><el-switch v-model="form.visible" :active-value="1" :inactive-value="0" /></el-form-item>
          <el-form-item label="状态" class="form-row-item"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, markRaw } from 'vue'
import { getPermissionTree, createPermission, updatePermission, deletePermission } from '@/api/permission'
import { ElMessage } from 'element-plus'
import { Folder, Menu, Document, Edit, Plus, Delete, Search } from '@element-plus/icons-vue'

const permissionTree = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editItem = ref<any>(null)
const searchKeyword = ref('')
const filterType = ref<number | ''>('')
const form = reactive({
  parentId: null as number | null,
  name: '', code: '', type: 2 as number,
  path: '', component: '', icon: '',
  sortOrder: 0, visible: 1, status: 1
})

const groupColors = [
  'linear-gradient(135deg, #007AFF, #5AC8FA)',
  'linear-gradient(135deg, #34C759, #30D158)',
  'linear-gradient(135deg, #FF9500, #FFB340)',
  'linear-gradient(135deg, #5856D6, #7B78F2)',
  'linear-gradient(135deg, #FF3B30, #FF6B6B)',
  'linear-gradient(135deg, #AF52DE, #BF5AF2)',
]

const countChildren = (group: any) => {
  let count = 0
  const walk = (list: any[]) => {
    for (const item of list) {
      count++
      if (item.children?.length) walk(item.children)
    }
  }
  if (group.children?.length) walk(group.children)
  return count
}

const flatCount = computed(() => {
  let count = 0
  const walk = (list: any[]) => {
    for (const item of list) { count++; if (item.children?.length) walk(item.children) }
  }
  walk(permissionTree.value)
  return count
})

const filteredTree = computed(() => {
  let tree = permissionTree.value
  if (filterType.value !== '' && filterType.value !== null) {
    tree = tree.filter((item: any) => item.type === filterType.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    const filterNodes = (nodes: any[]): any[] => {
      return nodes.filter(node => {
        const match = node.name?.toLowerCase().includes(kw) || node.code?.toLowerCase().includes(kw)
        if (node.children?.length) {
          const fc = filterNodes(node.children)
          return fc.length > 0 || match
        }
        return match
      })
    }
    tree = filterNodes(JSON.parse(JSON.stringify(tree)))
  }
  return tree
})

const handleSearch = () => {}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getPermissionTree()
    permissionTree.value = res.data || []
  } catch { ElMessage.error('加载权限失败') } finally { loading.value = false }
}

const handleDelete = async (id: number) => {
  try { await deletePermission(id); ElMessage.success('删除成功'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '删除失败') }
}

const showDialog = (item?: any, parentId?: number) => {
  editItem.value = item || null
  if (item) {
    Object.assign(form, {
      parentId: item.parentId || null,
      name: item.name, code: item.code, type: item.type,
      path: item.path || '', component: item.component || '',
      icon: item.icon || '', sortOrder: item.sortOrder || 0,
      visible: item.visible ?? 1, status: item.status ?? 1
    })
  } else {
    Object.assign(form, {
      parentId: parentId || null,
      name: '', code: '', type: 2,
      path: '', component: '', icon: '',
      sortOrder: 0, visible: 1, status: 1
    })
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.name.trim()) { ElMessage.warning('请输入权限名称'); return }
  if (!form.code.trim()) { ElMessage.warning('请输入权限编码'); return }
  saving.value = true
  try {
    if (editItem.value) { await updatePermission(editItem.value.id, form) }
    else { await createPermission(form) }
    ElMessage.success(editItem.value ? '更新成功' : '创建成功')
    dialogVisible.value = false; loadData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
  finally { saving.value = false }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.perm-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.perm-card {
  background: var(--bg-card, #fff);
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: box-shadow 0.2s;
  &:hover { box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06); }
}

.perm-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.perm-card-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.perm-card-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.perm-card-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.perm-card-name {
  font-weight: 600;
  font-size: 15px;
  color: #1D1D1F;
}

.perm-card-code {
  font-family: 'SF Mono', 'Menlo', monospace;
  font-size: 11px;
  color: #AEAEB2;
}

.perm-card-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.perm-card-count {
  font-size: 12px;
  color: #86868B;
  margin-right: 8px;
}

.card-btn {
  width: 30px !important;
  height: 30px !important;
  padding: 0 !important;
  border-radius: 8px !important;
  color: #86868B !important;
  &:hover { color: #007AFF !important; background: rgba(0, 122, 255, 0.08) !important; }
  &.danger:hover { color: #FF3B30 !important; background: rgba(255, 59, 48, 0.08) !important; }
}

.perm-card-body { padding: 4px 0; }

.perm-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  transition: background 0.12s;
  &:hover { background: rgba(0, 0, 0, 0.015); }
  &.nested { padding-left: 52px; }
}

.perm-row-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.perm-row-icon {
  width: 28px;
  height: 28px;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.menu { background: rgba(52, 199, 89, 0.1); color: #34C759; }
  &.btn { background: rgba(255, 149, 0, 0.1); color: #FF9500; }
}

.perm-row-info {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex-wrap: wrap;
}

.perm-row-name {
  font-weight: 500;
  font-size: 13px;
  color: #1D1D1F;
}

.perm-row-code {
  font-family: 'SF Mono', 'Menlo', monospace;
  font-size: 11px;
  color: #86868B;
  background: #F5F5F7;
  padding: 1px 6px;
  border-radius: 4px;
}

.perm-row-path {
  font-family: 'SF Mono', 'Menlo', monospace;
  font-size: 11px;
  color: #AEAEB2;
}

.perm-row-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.type-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 6px;
  font-weight: 500;
  &.menu { background: rgba(52, 199, 89, 0.1); color: #34C759; }
  &.btn { background: rgba(255, 149, 0, 0.1); color: #FF9500; }
}

.perm-row-btns {
  display: flex;
  gap: 2px;
}

.perm-card-empty {
  padding: 24px;
  text-align: center;
  font-size: 13px;
  color: #C7C7CC;
}

.form-row {
  display: flex;
  gap: 24px;
  .form-row-item { flex: 1; }
}
</style>
