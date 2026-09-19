<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon purple"><el-icon :size="22"><UserFilled /></el-icon></div>
        <div class="page-header-text">
          <h2>角色管理</h2>
          <p>管理系统角色与权限配置</p>
        </div>
      </div>
      <el-button type="primary" @click="showDialog()" v-permission="'system:role:create'">
        <el-icon><Plus /></el-icon> 新建角色
      </el-button>
    </div>

    <div class="page-toolbar">
      <div class="page-search">
        <el-input v-model="filters.keyword" placeholder="搜索角色名称..." clearable @keyup.enter="loadData" @clear="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
      <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 130px" @change="loadData">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <div class="page-toolbar-right">
        <span class="page-count">共 <b>{{ total }}</b> 个角色</span>
      </div>
    </div>

    <div class="role-grid" v-loading="loading">
      <div v-for="role in roles" :key="role.id" class="role-card" :class="{ disabled: role.status === 0 }">
        <div class="role-card-header">
          <div class="role-avatar" :style="{ background: roleColor(role.code) }">
            <span>{{ (role.name || 'R').charAt(0) }}</span>
          </div>
          <div class="role-info">
            <div class="role-name">{{ role.name }}</div>
            <div class="role-code">{{ role.code }}</div>
          </div>
          <el-switch
            class="role-status"
            v-model="role.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleToggleStatus(role)"
            v-permission="'system:role:edit'"
          />
        </div>

        <div class="role-desc" v-if="role.description">{{ role.description }}</div>
        <div class="role-desc empty" v-else>暂无描述</div>

        <div class="role-stats">
          <div class="stat-item">
            <el-icon><User /></el-icon>
            <span><b>{{ role.userCount || 0 }}</b> 用户</span>
          </div>
          <div class="stat-item">
            <el-icon><Sort /></el-icon>
            <span>排序 {{ role.sortOrder || 0 }}</span>
          </div>
        </div>

        <div class="role-actions">
          <el-button text size="small" class="action-btn" @click="showDialog(role)" v-permission="'system:role:edit'">
            <el-icon><Edit /></el-icon> 编辑
          </el-button>
          <el-button text size="small" class="action-btn primary" @click="openPermDialog(role)" v-permission="'system:role:edit'">
            <el-icon><Setting /></el-icon> 权限
          </el-button>
          <el-popconfirm title="确定删除此角色?" @confirm="handleDelete(role.id)">
            <template #reference>
              <el-button text size="small" class="action-btn danger" v-permission="'system:role:delete'">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>

      <div v-if="!loading && roles.length === 0" class="empty-state">
        <el-empty description="暂无角色数据" />
      </div>
    </div>

    <div class="page-pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="editItem ? '编辑角色' : '新建角色'" width="480px" destroy-on-close>
      <el-form :model="form" label-width="80px" size="large">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="form.code" placeholder="如 admin, chef, diner" :disabled="!!editItem" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <RolePermissionDialog v-model:visible="permDialogVisible" :role-id="permRoleId" :role-name="permRoleName" @saved="loadData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getRoles, createRole, updateRole, deleteRole } from '@/api/role'
import { ElMessage } from 'element-plus'
import RolePermissionDialog from './RolePermissionDialog.vue'

const roles = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const permDialogVisible = ref(false)
const permRoleId = ref<number | null>(null)
const permRoleName = ref('')
const editItem = ref<any>(null)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const filters = reactive({ keyword: '', status: '' as number | string })
const form = reactive({ name: '', code: '', description: '', sortOrder: 0, status: 1 })

const gradients = [
  ['#FF3B30', '#FF6B6B'], ['#FF9500', '#FFB340'], ['#007AFF', '#5AC8FA'],
  ['#5856D6', '#7B78F2'], ['#34C759', '#30D158'], ['#AF52DE', '#BF5AF2'],
  ['#FF2D55', '#FF6B8A'], ['#5AC8FA', '#64D2FF'],
]
const roleColor = (code: string) => {
  let hash = 0
  for (let i = 0; i < (code || '').length; i++) hash = ((hash << 5) - hash + code.charCodeAt(i)) | 0
  const [c1, c2] = gradients[Math.abs(hash) % gradients.length]
  return `linear-gradient(135deg, ${c1}, ${c2})`
}

const loadData = async () => {
  loading.value = true
  try {
    const params: any = { page: page.value, size: pageSize.value }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.status !== '' && filters.status !== null) params.status = filters.status
    const res: any = await getRoles(params)
    roles.value = res.data.records
    total.value = res.data.total
  } catch { ElMessage.error('加载角色失败') } finally { loading.value = false }
}

const handleSizeChange = () => { page.value = 1; loadData() }

const handleDelete = async (id: number) => {
  try { await deleteRole(id); ElMessage.success('删除成功'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '删除失败') }
}

const handleToggleStatus = async (role: any) => {
  try {
    await updateRole(role.id, { name: role.name, code: role.code, description: role.description, sortOrder: role.sortOrder, status: role.status })
    ElMessage.success(`已${role.status === 1 ? '启用' : '禁用'}`)
  } catch (e: any) { ElMessage.error(e.message || '操作失败'); role.status = role.status === 1 ? 0 : 1 }
}

const showDialog = (item?: any) => {
  editItem.value = item || null
  if (item) {
    Object.assign(form, { name: item.name, code: item.code, description: item.description, sortOrder: item.sortOrder, status: item.status })
  } else {
    Object.assign(form, { name: '', code: '', description: '', sortOrder: 0, status: 1 })
  }
  dialogVisible.value = true
}

const openPermDialog = (row: any) => {
  permRoleId.value = row.id
  permRoleName.value = row.name || ''
  permDialogVisible.value = true
}

const handleSave = async () => {
  if (!form.name.trim()) { ElMessage.warning('请输入角色名称'); return }
  if (!form.code.trim()) { ElMessage.warning('请输入角色编码'); return }
  saving.value = true
  try {
    if (editItem.value) { await updateRole(editItem.value.id, form) }
    else { await createRole(form) }
    ElMessage.success(editItem.value ? '更新成功' : '创建成功')
    dialogVisible.value = false; loadData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
  finally { saving.value = false }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.role-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.role-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #007AFF, #5856D6);
    opacity: 0;
    transition: opacity 0.25s;
  }

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
    &::before { opacity: 1; }
  }

  &.disabled {
    opacity: 0.55;
    &:hover { opacity: 0.75; }
  }
}

.role-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.role-avatar {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.role-info { flex: 1; min-width: 0; }

.role-name {
  font-weight: 600;
  font-size: 16px;
  color: #1D1D1F;
  line-height: 1.3;
}

.role-code {
  font-family: 'SF Mono', 'Menlo', monospace;
  font-size: 12px;
  color: #86868B;
  margin-top: 2px;
}

.role-status { flex-shrink: 0; }

.role-desc {
  font-size: 13px;
  color: #6E6E73;
  line-height: 1.5;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;

  &.empty { color: #C7C7CC; font-style: italic; }
}

.role-stats {
  display: flex;
  gap: 16px;
  padding: 12px 0;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  margin-bottom: 12px;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #86868B;

    .el-icon { font-size: 14px; }

    b { color: #1D1D1F; font-weight: 600; }
  }
}

.role-actions {
  display: flex;
  gap: 4px;
}

.action-btn {
  font-size: 13px !important;
  color: #6E6E73 !important;
  height: 30px !important;
  padding: 0 10px !important;
  border-radius: 8px !important;
  font-weight: 500 !important;

  &:hover {
    color: #007AFF !important;
    background: rgba(0, 122, 255, 0.08) !important;
  }

  &.primary:hover {
    color: #5856D6 !important;
    background: rgba(88, 86, 214, 0.08) !important;
  }

  &.danger:hover {
    color: #FF3B30 !important;
    background: rgba(255, 59, 48, 0.08) !important;
  }

  .el-icon { margin-right: 4px; }
}

.empty-state {
  grid-column: 1 / -1;
}
</style>
