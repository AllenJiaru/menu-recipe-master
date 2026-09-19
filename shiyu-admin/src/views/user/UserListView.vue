<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon blue"><el-icon :size="22"><User /></el-icon></div>
        <div class="page-header-text">
          <h2>用户管理</h2>
          <p>管理系统用户账号与权限</p>
        </div>
      </div>
      <el-button type="primary" @click="showDialog()" v-permission="'system:user:create'">
        <el-icon><Plus /></el-icon> 新建用户
      </el-button>
    </div>

    <div class="page-toolbar">
      <div class="page-search">
        <el-input v-model="filters.keyword" placeholder="搜索用户名或昵称..." clearable @keyup.enter="loadData" @clear="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="filters.organizationId" placeholder="所属机构" clearable style="width: 160px" @change="loadData">
          <el-option v-for="org in organizations" :key="org.id" :label="org.name" :value="org.id" />
        </el-select>
      </div>
      <div class="page-chips">
        <span class="chip" :class="{ active: filters.role === '' }" @click="filters.role = ''; loadData()">全部</span>
        <span class="chip" :class="{ active: filters.role === 'admin' }" @click="filters.role = 'admin'; loadData()">
          <span class="chip-dot admin"></span>管理员
        </span>
        <span class="chip" :class="{ active: filters.role === 'chef' }" @click="filters.role = 'chef'; loadData()">
          <span class="chip-dot chef"></span>主厨
        </span>
        <span class="chip" :class="{ active: filters.role === 'diner' }" @click="filters.role = 'diner'; loadData()">
          <span class="chip-dot diner"></span>食客
        </span>
      </div>
      <div class="page-toolbar-right">
        <span class="page-count">共 <b>{{ total }}</b> 位用户</span>
      </div>
    </div>

    <div class="page-table-card" v-loading="loading">
      <transition name="batch-slide">
        <div v-if="selectedRows.length" class="batch-bar">
          <div class="batch-info">
            <el-icon><Select /></el-icon>
            已选择 <b>{{ selectedRows.length }}</b> 位用户
          </div>
          <div class="batch-actions">
            <el-button size="small" class="batch-btn" @click="handleBatchStatus(1)">
              <el-icon><Top /></el-icon> 批量启用
            </el-button>
            <el-button size="small" class="batch-btn" @click="handleBatchStatus(0)">
              <el-icon><Bottom /></el-icon> 批量禁用
            </el-button>
            <el-button size="small" type="danger" plain @click="handleBatchDelete">
              <el-icon><Delete /></el-icon> 批量删除
            </el-button>
            <el-button size="small" text @click="clearSelection">取消选择</el-button>
          </div>
        </div>
      </transition>

      <el-table ref="tableRef" :data="users" stripe style="width: 100%" row-key="id" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="46" reserve-selection />
        <el-table-column prop="id" label="#" width="56" align="center">
          <template #default="{ row }"><span class="id-badge">{{ row.id }}</span></template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <div class="user-avatar" :class="row.role">
                <img v-if="row.avatar" :src="imgUrl(row.avatar)" class="avatar-img" @error="onImgError($event, row)" />
                <span v-else class="avatar-text">{{ (row.nickname || row.username || '?')[0] }}</span>
              </div>
              <div class="user-meta">
                <span class="username">{{ row.username }}</span>
                <span class="nickname" v-if="row.nickname && row.nickname !== row.username">{{ row.nickname }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="(cmd: string) => handleQuickRole(row, cmd)" v-permission="'system:user:edit'">
              <span :class="['role-badge', row.role, 'clickable']">{{ roleMap[row.role] || row.role }}<el-icon class="el-icon--right"><ArrowDown /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="admin"><span class="chip-dot admin"></span>管理员</el-dropdown-item>
                  <el-dropdown-item command="chef"><span class="chip-dot chef"></span>主厨</el-dropdown-item>
                  <el-dropdown-item command="diner"><span class="chip-dot diner"></span>食客</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
        <el-table-column label="所属机构" min-width="140">
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="(cmd: string) => handleOrgChange(row, Number(cmd))" v-if="row.id !== authStore.userInfo?.id">
              <span v-if="row.organizationId" class="org-name clickable">{{ getOrgName(row.organizationId) }}<el-icon class="el-icon--right"><ArrowDown /></el-icon></span>
              <span v-else class="text-muted clickable">未分配<el-icon class="el-icon--right"><ArrowDown /></el-icon></span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="org in organizations" :key="org.id" :command="String(org.id)" :class="{ 'is-active': row.organizationId === org.id }">{{ org.name }}</el-dropdown-item>
                  <el-dropdown-item divided command="clear" v-if="row.organizationId"><span style="color:var(--el-color-danger)">取消分配</span></el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <span v-else class="org-name">{{ getOrgName(row.organizationId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatus(row)"
              v-permission="'system:user:edit'"
              :disabled="row.id === authStore.userInfo?.id"
            />
          </template>
        </el-table-column>
        <el-table-column label="登录次数" width="90" align="center">
          <template #default="{ row }"><span class="meta-num">{{ row.loginCount || 0 }}</span></template>
        </el-table-column>
        <el-table-column label="最后登录" width="160">
          <template #default="{ row }">
            <span class="time-cell" v-if="row.lastLoginTime">{{ formatDate(row.lastLoginTime) }}</span>
            <span class="time-cell empty" v-else>从未登录</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button text size="small" @click="showDialog(row)" v-permission="'system:user:edit'" class="action-btn">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button text size="small" @click="showRoleDialog(row)" v-permission="'system:user:edit'" class="action-btn">
                <el-icon><User /></el-icon> 角色
              </el-button>
              <el-button text size="small" @click="handleResetPwd(row)" v-permission="'system:user:resetpwd'" class="action-btn warning">
                <el-icon><Key /></el-icon> 重置
              </el-button>
              <el-popconfirm title="确定删除?" @confirm="handleDelete(row.id)">
                <template #reference>
                  <el-button text size="small" v-permission="'system:user:delete'" class="action-btn danger"
                    :disabled="row.id === authStore.userInfo?.id">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @current-change="loadData" @size-change="handleSizeChange" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editUser ? '编辑用户' : '新建用户'" width="440px" destroy-on-close>
      <el-form :model="userForm" label-width="72px" size="large">
        <el-form-item label="用户名"><el-input v-model="userForm.username" :disabled="!!editUser" placeholder="请输入用户名" /></el-form-item>
        <el-form-item label="密码" v-if="!editUser"><el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="userForm.nickname" placeholder="请输入昵称" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="userForm.email" placeholder="请输入邮箱" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="userForm.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="头像">
          <div class="avatar-preview" v-if="userForm.avatar">
            <img :src="imgUrl(userForm.avatar)" class="avatar-preview-img" />
            <el-icon class="avatar-remove" @click="userForm.avatar = ''"><Close /></el-icon>
          </div>
          <el-input v-model="userForm.avatar" placeholder="头像URL（http或/uploads/路径）" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role" style="width: 100%">
            <el-option label="管理员" value="admin" /><el-option label="主厨" value="chef" /><el-option label="食客" value="diner" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属机构">
          <el-select v-model="userForm.organizationId" placeholder="请选择机构" clearable style="width: 100%">
            <el-option v-for="org in organizations" :key="org.id" :label="org.name" :value="org.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <UserRoleDialog v-model:visible="roleDialogVisible" :user-id="roleUserId" :username="roleUsername" @saved="loadData" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUsers, createUser, updateUser, deleteUser, updateUserStatus, updateUserRole, resetPassword, batchUpdateStatus, batchDeleteUsers } from '@/api/user'
import { organizationApi } from '@/api/organization'
import { useAuthStore } from '@/stores/auth'
import { formatDate, roleMap, imgUrl } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'
import UserRoleDialog from './UserRoleDialog.vue'
import type { TableInstance } from 'element-plus'

const authStore = useAuthStore()
const tableRef = ref<TableInstance>()
const selectedRows = ref<any[]>([])
const users = ref<any[]>([])
const organizations = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const roleUserId = ref<number | null>(null)
const roleUsername = ref('')
const editUser = ref<any>(null)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const filters = reactive({ keyword: '', role: '', organizationId: null as number | null })
const userForm = reactive({ username: '', password: '', nickname: '', email: '', phone: '', avatar: '', role: 'diner', organizationId: null as number | null })

const loadData = async () => {
  loading.value = true
  try {
    const params: any = { page: page.value, size: pageSize.value }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.role) params.role = filters.role
    if (filters.organizationId) params.organizationId = filters.organizationId
    const res: any = await getUsers(params)
    users.value = res.data.records
    total.value = res.data.total
  } catch { ElMessage.error('加载用户失败') } finally { loading.value = false }
}

const loadOrganizations = async () => {
  try {
    const { data } = await organizationApi.getUserOrganizations()
    organizations.value = data
  } catch { /* ignore */ }
}

const getOrgName = (orgId: number) => {
  const org = organizations.value.find(o => o.id === orgId)
  return org?.name || '未知机构'
}

const handleQuickRole = async (row: any, role: string) => {
  if (row.role === role) return
  try {
    await updateUserRole(row.id, role)
    row.role = role
    ElMessage.success('角色已变更')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '变更失败')
  }
}

const handleOrgChange = async (row: any, orgId: number | string) => {
  const newOrgId = orgId === 'clear' ? null : Number(orgId)
  if (row.organizationId === newOrgId) return
  try {
    await updateUser(row.id, { organizationId: newOrgId })
    row.organizationId = newOrgId
    ElMessage.success('所属机构已变更')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '变更失败')
  }
}

const handleSizeChange = () => { page.value = 1; loadData() }

const onSelectionChange = (rows: any[]) => { selectedRows.value = rows }
const clearSelection = () => { tableRef.value?.clearSelection() }

const handleStatus = async (row: any) => {
  try { await updateUserStatus(row.id, row.status); ElMessage.success('操作成功') }
  catch (e: any) { ElMessage.error(e.message || '操作失败'); loadData() }
}

const onImgError = (e: Event, row: any) => {
  row.avatar = ''
}

const handleDelete = async (id: number) => {
  try { await deleteUser(id); ElMessage.success('删除成功'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '删除失败') }
}

const handleResetPwd = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定重置「${row.username}」的密码？将生成随机密码。`, '重置密码', { type: 'warning' })
    const res: any = await resetPassword(row.id)
    await ElMessageBox.alert(`新密码：${res.data}`, '密码已重置', { type: 'success', confirmButtonText: '我知道了' })
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

const handleBatchStatus = async (status: number) => {
  const ids = selectedRows.value.map(r => r.id)
  try {
    await batchUpdateStatus(ids, status)
    ElMessage.success(`已${status === 1 ? '启用' : '禁用'} ${ids.length} 位用户`)
    clearSelection(); loadData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
}

const handleBatchDelete = async () => {
  const ids = selectedRows.value.map(r => r.id)
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 位用户？`, '批量删除', { type: 'warning' })
    await batchDeleteUsers(ids)
    ElMessage.success('删除成功')
    clearSelection(); loadData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

const showDialog = (user?: any) => {
  editUser.value = user || null
  if (user) { Object.assign(userForm, { username: user.username, password: '', nickname: user.nickname, email: user.email || '', phone: user.phone || '', avatar: user.avatar || '', role: user.role, organizationId: user.organizationId || null }) }
  else { Object.assign(userForm, { username: '', password: '', nickname: '', email: '', phone: '', avatar: '', role: 'diner', organizationId: null }) }
  dialogVisible.value = true
}

const showRoleDialog = (user: any) => {
  roleUserId.value = user.id
  roleUsername.value = user.username
  roleDialogVisible.value = true
}

const handleSave = async () => {
  if (!userForm.username.trim()) { ElMessage.warning('请输入用户名'); return }
  if (!editUser.value && !userForm.password) { ElMessage.warning('请输入密码'); return }
  if (userForm.password && userForm.password.length < 6) { ElMessage.warning('密码至少需要6位'); return }
  if (!userForm.nickname.trim()) { ElMessage.warning('请输入昵称'); return }
  saving.value = true
  try {
    if (editUser.value) { await updateUser(editUser.value.id, userForm) }
    else { await createUser(userForm) }
    ElMessage.success(editUser.value ? '更新成功' : '创建成功')
    dialogVisible.value = false; loadData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
  finally { saving.value = false }
}

onMounted(() => {
  loadData()
  loadOrganizations()
})
</script>

<style scoped lang="scss">
.page-toolbar { flex-wrap: wrap; }

.chip-dot {
  width: 7px; height: 7px; border-radius: 50%; flex-shrink: 0;
  &.admin { background: var(--apple-red); }
  &.chef { background: var(--apple-orange); }
  &.diner { background: var(--apple-blue); }
}

.id-badge {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 26px; height: 22px; padding: 0 6px;
  border-radius: var(--radius-xs); background: var(--bg-page); color: var(--text-tertiary);
  font-size: 12px; font-weight: 600; font-family: 'SF Mono', 'Menlo', monospace;
}

.user-cell { display: flex; align-items: center; gap: 10px; }

.user-avatar {
  width: 34px; height: 34px; border-radius: var(--radius-sm);
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 600; color: #fff; flex-shrink: 0;
  overflow: hidden; background: var(--apple-blue);
  &.admin { background: linear-gradient(135deg, #FF3B30, #FF6B6B); }
  &.chef { background: linear-gradient(135deg, #FF9500, #FFB340); }
  &.diner { background: linear-gradient(135deg, #007AFF, #5AC8FA); }
}

.avatar-img {
  width: 100%; height: 100%; object-fit: cover; border-radius: inherit;
}

.avatar-text { color: #fff; }

.user-meta { display: flex; flex-direction: column; min-width: 0; }
.username { font-size: 14px; font-weight: 500; color: var(--text-primary); line-height: 1.3; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.nickname { font-size: 12px; color: var(--text-tertiary); line-height: 1.3; }

.role-badge {
  display: inline-flex; align-items: center; justify-content: center;
  height: 26px; padding: 0 10px; border-radius: var(--radius-full);
  font-size: 12px; font-weight: 500;
  &.admin { background: rgba(255, 59, 48, 0.08); color: #FF3B30; }
  &.chef { background: rgba(255, 149, 0, 0.08); color: #FF9500; }
  &.diner { background: rgba(0, 122, 255, 0.08); color: #007AFF; }
  &.clickable { cursor: pointer; transition: opacity 0.2s; &:hover { opacity: 0.75; } }
}

.org-name { font-size: 13px; color: var(--text-secondary); }
.clickable { cursor: pointer; transition: opacity 0.2s; &:hover { opacity: 0.75; } }
.text-muted { color: var(--text-quaternary); font-size: 13px; }

.meta-num { font-size: 13px; color: var(--text-secondary); font-family: 'SF Mono', 'Menlo', monospace; }

.time-cell {
  color: var(--text-tertiary); font-size: 13px; font-family: 'SF Mono', 'Menlo', monospace;
  &.empty { color: var(--text-quaternary); font-style: italic; }
}

.action-group { display: flex; align-items: center; justify-content: flex-end; gap: 2px; }

.action-btn {
  font-size: 13px !important; color: var(--text-secondary) !important;
  height: 30px !important; padding: 0 8px !important; border-radius: var(--radius-xs) !important;
  &:hover { color: var(--apple-blue) !important; background: rgba(0, 122, 255, 0.06) !important; }
  &.warning:hover { color: var(--apple-orange) !important; background: rgba(255, 149, 0, 0.06) !important; }
  &.danger:hover { color: var(--apple-red) !important; background: rgba(255, 59, 48, 0.06) !important; }
  .el-icon { margin-right: 3px; }
}

.avatar-preview {
  position: relative; display: inline-flex; margin-bottom: 8px;
  width: 56px; height: 56px; border-radius: var(--radius-sm); overflow: hidden;
  border: 2px solid var(--border-secondary);
}
.avatar-preview-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-remove {
  position: absolute; top: 2px; right: 2px; width: 18px; height: 18px;
  background: rgba(0,0,0,0.5); color: #fff; border-radius: 50%;
  display: flex; align-items: center; justify-content: center; cursor: pointer;
  font-size: 12px; transition: background 0.2s;
  &:hover { background: rgba(255,59,48,0.8); }
}
</style>
