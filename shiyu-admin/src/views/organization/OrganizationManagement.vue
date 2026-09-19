<template>
  <div class="organization-management">
    <div class="page-header">
      <h2>机构管理</h2>
      <div class="header-actions">
        <el-select v-model="filterType" placeholder="机构类型" clearable style="width: 140px" @change="loadTree">
          <el-option label="家庭" value="family" />
          <el-option label="餐厅" value="restaurant" />
          <el-option label="酒店" value="hotel" />
          <el-option label="企业" value="company" />
          <el-option label="其他" value="other" />
        </el-select>
        <el-button type="primary" @click="showCreateDialog()">
          <el-icon><Plus /></el-icon>
          新建机构
        </el-button>
      </div>
    </div>

    <div class="content-layout">
      <!-- 左侧：机构树 -->
      <div class="org-tree-panel">
        <div class="panel-header">
          <span>机构树</span>
          <el-button type="primary" link @click="expandAll">全部展开</el-button>
        </div>
        <div v-loading="treeLoading" class="tree-container">
          <el-tree
            ref="treeRef"
            :data="orgTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <el-icon class="node-icon"><OfficeBuilding /></el-icon>
                <span class="node-label">{{ node.label }}</span>
                <el-tag :type="getTypeTagType(data.type)" size="small" class="node-tag">
                  {{ getTypeLabel(data.type) }}
                </el-tag>
              </div>
            </template>
          </el-tree>
        </div>
      </div>

      <!-- 右侧：机构详情 -->
      <div class="org-detail-panel">
        <template v-if="currentOrg">
          <!-- 机构头部 -->
          <div class="detail-header">
            <div class="header-left">
              <div class="org-avatar">
                {{ currentOrg.name?.charAt(0) }}
              </div>
              <div class="org-info">
                <h3>{{ currentOrg.name }}</h3>
                <p>{{ currentOrg.description || '暂无描述' }}</p>
                <div class="org-meta">
                  <el-tag :type="getTypeTagType(currentOrg.type)" size="small">
                    {{ getTypeLabel(currentOrg.type) }}
                  </el-tag>
                  <el-tag :type="currentOrg.status === 1 ? 'success' : 'danger'" size="small">
                    {{ currentOrg.status === 1 ? '正常' : '禁用' }}
                  </el-tag>
                  <span class="meta-text">层级 L{{ currentOrg.level }}</span>
                  <span class="meta-text">创建于 {{ formatDate(currentOrg.createTime) }}</span>
                </div>
              </div>
            </div>
            <div class="header-actions">
              <el-button @click="showEditDialog(currentOrg)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="primary" @click="showCreateDialog(currentOrg.id)">
                <el-icon><Plus /></el-icon>
                添加子机构
              </el-button>
              <el-button type="danger" @click="deleteOrganization(currentOrg)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>

          <!-- 基本信息 -->
          <div class="info-section">
            <h4>基本信息</h4>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="机构名称">{{ currentOrg.name }}</el-descriptions-item>
              <el-descriptions-item label="机构类型">
                <el-tag :type="getTypeTagType(currentOrg.type)" size="small">
                  {{ getTypeLabel(currentOrg.type) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="联系人">{{ currentOrg.contactName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ currentOrg.contactPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系邮箱">{{ currentOrg.contactEmail || '-' }}</el-descriptions-item>
              <el-descriptions-item label="地址" :span="2">{{ currentOrg.address || '-' }}</el-descriptions-item>
              <el-descriptions-item label="描述" :span="2">{{ currentOrg.description || '-' }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 子机构列表 -->
          <div class="children-section">
            <div class="section-header">
              <h4>子机构</h4>
              <el-button type="primary" size="small" @click="showCreateDialog(currentOrg.id)">
                <el-icon><Plus /></el-icon>
                添加子机构
              </el-button>
            </div>
            <el-table :data="childrenList" v-loading="childrenLoading" stripe>
              <el-table-column label="机构名称" min-width="200">
                <template #default="{ row }">
                  <div class="org-name-cell" @click="handleNodeClick(row)">
                    <el-icon><OfficeBuilding /></el-icon>
                    <span>{{ row.name }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="getTypeTagType(row.type)" size="small">
                    {{ getTypeLabel(row.type) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                    {{ row.status === 1 ? '正常' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="contactName" label="联系人" width="120" />
              <el-table-column prop="contactPhone" label="联系电话" width="140" />
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="showEditDialog(row)">编辑</el-button>
                  <el-button type="danger" link size="small" @click="deleteOrganization(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 成员管理 -->
          <div class="member-section">
            <div class="section-header">
              <h4>机构成员</h4>
              <el-button type="primary" size="small" @click="showAddMemberDialog">
                <el-icon><Plus /></el-icon>
                添加成员
              </el-button>
            </div>
            <el-table :data="members" v-loading="membersLoading" stripe>
              <el-table-column label="用户信息" min-width="180">
                <template #default="{ row }">
                  <div class="user-cell">
                    <el-avatar :size="32" :src="row.avatar">
                      {{ (row.nickname || row.username || 'U').charAt(0) }}
                    </el-avatar>
                    <div class="user-info">
                      <div class="user-name">{{ row.nickname || row.username }}</div>
                      <div class="user-account">@{{ row.username }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="角色" width="100">
                <template #default="{ row }">
                  <el-tag :type="getRoleTagType(row.role)" size="small">
                    {{ getRoleLabel(row.role) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="joinTime" label="加入时间" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.joinTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button 
                    v-if="row.role !== 'owner'" 
                    type="primary" 
                    link 
                    size="small"
                    @click="showChangeRoleDialog(row)"
                  >
                    变更角色
                  </el-button>
                  <el-button 
                    v-if="row.role !== 'owner'" 
                    type="danger" 
                    link 
                    size="small"
                    @click="removeMember(row)"
                  >
                    移除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>

        <div v-else class="no-org-selected">
          <el-empty description="请选择一个机构">
            <el-button type="primary" @click="showCreateDialog()">创建机构</el-button>
          </el-empty>
        </div>
      </div>
    </div>

    <!-- 创建/编辑机构对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="editingOrg ? '编辑机构' : '新建机构'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="机构名称" required>
          <el-input v-model="form.name" placeholder="请输入机构名称" />
        </el-form-item>
        <el-form-item label="机构类型">
          <el-select v-model="form.type" placeholder="请选择类型">
            <el-option label="家庭" value="family" />
            <el-option label="餐厅" value="restaurant" />
            <el-option label="酒店" value="hotel" />
            <el-option label="企业" value="company" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="上级机构">
          <el-tree-select
            v-model="form.parentId"
            :data="orgTree"
            :props="{ label: 'name', children: 'children', value: 'id' }"
            placeholder="留空为顶级机构"
            clearable
            check-strictly
          />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactName" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="form.contactEmail" placeholder="请输入联系邮箱" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入描述" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveOrganization" :loading="saving">
          {{ editingOrg ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 添加成员对话框 -->
    <el-dialog 
      v-model="addMemberDialogVisible" 
      title="添加成员"
      width="500px"
    >
      <div class="add-member-content">
        <el-form label-width="80px">
          <el-form-item label="搜索用户">
            <el-input 
              v-model="userSearchKeyword" 
              placeholder="输入用户名或昵称搜索"
              @input="searchUsers"
              clearable
            />
          </el-form-item>
        </el-form>
        
        <div v-if="searchResults.length > 0" class="search-results">
          <div 
            v-for="user in searchResults" 
            :key="user.id" 
            class="search-result-item"
            @click="selectUser(user)"
          >
            <el-avatar :size="32" :src="user.avatar">
              {{ (user.nickname || user.username || 'U').charAt(0) }}
            </el-avatar>
            <div class="result-info">
              <div class="result-name">{{ user.nickname || user.username }}</div>
              <div class="result-account">@{{ user.username }}</div>
            </div>
            <el-button type="primary" size="small">选择</el-button>
          </div>
        </div>

        <el-form v-if="selectedUser" label-width="80px" style="margin-top: 16px;">
          <el-form-item label="已选用户">
            <div class="selected-user">
              <el-avatar :size="24" :src="selectedUser.avatar">
                {{ (selectedUser.nickname || selectedUser.username || 'U').charAt(0) }}
              </el-avatar>
              <span>{{ selectedUser.nickname || selectedUser.username }}</span>
              <el-button type="danger" link @click="selectedUser = null">取消</el-button>
            </div>
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="addMemberRole" placeholder="请选择角色">
              <el-option label="成员" value="member" />
              <el-option label="管理员" value="admin" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addMember" :loading="addingMember" :disabled="!selectedUser">
          添加
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, OfficeBuilding } from '@element-plus/icons-vue'
import { 
  organizationApi, 
  type Organization, 
  type OrganizationTreeNode,
  type OrganizationMember,
  type UserSearchResult
} from '@/api'

const treeLoading = ref(false)
const saving = ref(false)
const childrenLoading = ref(false)
const membersLoading = ref(false)
const addingMember = ref(false)

const orgTree = ref<OrganizationTreeNode[]>([])
const currentOrg = ref<Organization | null>(null)
const childrenList = ref<Organization[]>([])
const members = ref<OrganizationMember[]>([])
const filterType = ref('')

const treeRef = ref()
const dialogVisible = ref(false)
const addMemberDialogVisible = ref(false)
const editingOrg = ref<Organization | null>(null)
const form = ref({
  name: '',
  type: 'family',
  parentId: null as number | null,
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  sortOrder: 0,
  description: '',
  status: 1
} as Partial<Organization>)

const userSearchKeyword = ref('')
const searchResults = ref<UserSearchResult[]>([])
const selectedUser = ref<UserSearchResult | null>(null)
const addMemberRole = ref('member')

let searchTimeout: ReturnType<typeof setTimeout> | null = null

onMounted(() => {
  loadTree()
})

onUnmounted(() => {
  if (searchTimeout) clearTimeout(searchTimeout)
})

async function loadTree() {
  treeLoading.value = true
  try {
    if (filterType.value) {
      const { data } = await organizationApi.getOrganizationsByType(filterType.value)
      orgTree.value = buildTreeFromList(data)
    } else {
      const { data } = await organizationApi.getOrganizationTree()
      orgTree.value = data
    }
  } catch (error) {
    ElMessage.error('加载机构树失败')
  } finally {
    treeLoading.value = false
  }
}

function buildTreeFromList(list: Organization[]): OrganizationTreeNode[] {
  const map = new Map<number, OrganizationTreeNode>()
  const roots: OrganizationTreeNode[] = []
  
  list.forEach(item => {
    map.set(item.id, { ...item, children: [] })
  })
  
  list.forEach(item => {
    const node = map.get(item.id)!
    if (item.parentId && item.parentId > 0) {
      const parent = map.get(item.parentId)
      if (parent) {
        parent.children.push(node)
      } else {
        roots.push(node)
      }
    } else {
      roots.push(node)
    }
  })
  
  return roots
}

async function handleNodeClick(data: OrganizationTreeNode | Organization) {
  try {
    const { data: org } = await organizationApi.getOrganization(data.id)
    currentOrg.value = org
    loadChildren(data.id)
    loadMembers(data.id)
  } catch (error) {
    ElMessage.error('加载机构详情失败')
  }
}

async function loadChildren(parentId: number) {
  childrenLoading.value = true
  try {
    const { data } = await organizationApi.getOrganizationChildren(parentId)
    childrenList.value = data
  } catch (error) {
    childrenList.value = []
  } finally {
    childrenLoading.value = false
  }
}

async function loadMembers(orgId: number) {
  membersLoading.value = true
  try {
    const { data } = await organizationApi.getMembers(orgId)
    members.value = data
  } catch (error) {
    members.value = []
  } finally {
    membersLoading.value = false
  }
}

function expandAll() {
  if (treeRef.value) {
    const nodes = treeRef.value.store.root
    expandNode(nodes)
  }
}

function expandNode(node: any) {
  if (node) {
    node.expanded = true
    node.childNodes?.forEach((child: any) => expandNode(child))
  }
}

function showCreateDialog(parentId?: number) {
  editingOrg.value = null
  form.value = {
    name: '',
    type: 'family',
    parentId: parentId || undefined,
    contactName: '',
    contactPhone: '',
    contactEmail: '',
    address: '',
    sortOrder: 0,
    description: '',
    status: 1
  }
  dialogVisible.value = true
}

function showEditDialog(org: Organization) {
  editingOrg.value = org
  form.value = {
    name: org.name,
    type: org.type || 'family',
    parentId: org.parentId || undefined,
    contactName: org.contactName || '',
    contactPhone: org.contactPhone || '',
    contactEmail: org.contactEmail || '',
    address: org.address || '',
    sortOrder: org.sortOrder || 0,
    description: org.description || '',
    status: org.status
  }
  dialogVisible.value = true
}

async function saveOrganization() {
  if (!form.value.name?.trim()) {
    ElMessage.warning('请输入机构名称')
    return
  }
  
  saving.value = true
  try {
    if (editingOrg.value) {
      await organizationApi.updateOrganization(editingOrg.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await organizationApi.createOrganization(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadTree()
  } catch (error) {
    ElMessage.error(editingOrg.value ? '更新失败' : '创建失败')
  } finally {
    saving.value = false
  }
}

async function deleteOrganization(org: Organization) {
  try {
    await ElMessageBox.confirm(
      `确定要删除机构"${org.name}"吗？此操作将同时删除所有子机构，且不可恢复。`,
      '删除确认',
      { type: 'warning' }
    )
    await organizationApi.deleteOrganization(org.id)
    ElMessage.success('删除成功')
    if (currentOrg.value?.id === org.id) {
      currentOrg.value = null
      childrenList.value = []
      members.value = []
    }
    loadTree()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function showAddMemberDialog() {
  userSearchKeyword.value = ''
  searchResults.value = []
  selectedUser.value = null
  addMemberRole.value = 'member'
  addMemberDialogVisible.value = true
}

function searchUsers() {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(async () => {
    if (!currentOrg.value || !currentOrg.value.id || !userSearchKeyword.value.trim()) {
      searchResults.value = []
      return
    }
    try {
      const { data } = await organizationApi.searchUsers(currentOrg.value.id, userSearchKeyword.value)
      searchResults.value = data
    } catch (error) {
      searchResults.value = []
    }
  }, 300)
}

function selectUser(user: UserSearchResult) {
  selectedUser.value = user
  searchResults.value = []
  userSearchKeyword.value = ''
}

async function addMember() {
  if (!currentOrg.value || !selectedUser.value) return
  
  addingMember.value = true
  try {
    await organizationApi.addMember(currentOrg.value.id, {
      userId: selectedUser.value.id,
      role: addMemberRole.value
    })
    ElMessage.success('添加成功')
    addMemberDialogVisible.value = false
    loadMembers(currentOrg.value.id)
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '添加失败')
  } finally {
    addingMember.value = false
  }
}

function showChangeRoleDialog(member: OrganizationMember) {
  // TODO: 实现角色变更对话框
  ElMessage.info('角色变更功能开发中')
}

async function removeMember(member: OrganizationMember) {
  if (!currentOrg.value) return
  
  try {
    await ElMessageBox.confirm(
      `确定要移除成员"${member.nickname || member.username}"吗？`,
      '移除确认',
      { type: 'warning' }
    )
    await organizationApi.removeMember(currentOrg.value.id, member.userId)
    ElMessage.success('移除成功')
    loadMembers(currentOrg.value.id)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

function formatDate(dateStr: string | null) {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

function getTypeTagType(type: string) {
  const map: Record<string, string> = {
    family: 'success',
    restaurant: 'warning',
    hotel: 'primary',
    company: 'info',
    other: 'info'
  }
  return map[type] || 'info'
}

function getTypeLabel(type: string) {
  const map: Record<string, string> = {
    family: '家庭',
    restaurant: '餐厅',
    hotel: '酒店',
    company: '企业',
    other: '其他'
  }
  return map[type] || type
}

function getRoleTagType(role: string) {
  const map: Record<string, string> = { owner: 'danger', admin: 'warning', member: 'info' }
  return map[role] || 'info'
}

function getRoleLabel(role: string) {
  const map: Record<string, string> = { owner: '所有者', admin: '管理员', member: '成员' }
  return map[role] || role
}
</script>

<style scoped>
.organization-management {
  padding: 20px;
  height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.content-layout {
  display: flex;
  gap: 20px;
  height: calc(100% - 60px);
}

/* 左侧机构树 */
.org-tree-panel {
  width: 300px;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  border: 1px solid #ebeef5;
}

.panel-header {
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  font-weight: 600;
  color: #303133;
}

.tree-container {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  padding: 4px 0;
}

.node-icon {
  color: #409eff;
}

.node-label {
  flex: 1;
  font-size: 14px;
}

.node-tag {
  margin-left: auto;
}

/* 右侧详情 */
.org-detail-panel {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  min-width: 0;
  border: 1px solid #ebeef5;
  overflow-y: auto;
}

.detail-header {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid #ebeef5;
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 10;
}

.header-left {
  display: flex;
  gap: 16px;
}

.org-avatar {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  background: linear-gradient(135deg, #409eff, #53a8ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 24px;
  flex-shrink: 0;
}

.org-info h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  color: #303133;
}

.org-info p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
}

.org-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-text {
  font-size: 12px;
  color: #909399;
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* 信息区域 */
.info-section,
.children-section,
.member-section {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h4 {
  margin: 0;
  font-size: 15px;
  color: #303133;
}

.org-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #409eff;
}

.org-name-cell:hover {
  text-decoration: underline;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  line-height: 1.3;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.user-account {
  font-size: 12px;
  color: #909399;
}

.no-org-selected {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 添加成员对话框 */
.search-results {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.search-result-item:hover {
  background: #f5f7fa;
}

.result-info {
  flex: 1;
}

.result-name {
  font-size: 14px;
  color: #303133;
}

.result-account {
  font-size: 12px;
  color: #909399;
}

.selected-user {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
