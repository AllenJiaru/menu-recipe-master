<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon orange">
          <el-icon :size="22"><Bell /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>公告管理</h2>
          <p>管理系统公告与通知信息</p>
        </div>
      </div>
      <el-button type="primary" @click="showDialog()" v-permission="'business:notice:create'"><el-icon><Plus /></el-icon> 新建公告</el-button>
    </div>

    <div class="page-toolbar">
      <el-select v-model="filters.type" placeholder="全部类型" clearable style="width: 130px" @change="loadData">
        <el-option label="系统公告" value="announcement" />
        <el-option label="活动通知" value="activity" />
        <el-option label="维护通知" value="maintenance" />
      </el-select>
      <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 130px" @change="loadData">
        <el-option label="草稿" :value="0" />
        <el-option label="已发布" :value="1" />
      </el-select>
      <div class="page-toolbar-right">
        <span class="page-count">共 <b>{{ total }}</b> 条公告</span>
      </div>
    </div>

    <div class="page-table-card" v-loading="loading">
      <el-table :data="notices" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="标题" min-width="150">
          <template #default="{ row }"><span class="name-cell">{{ row.title }}</span></template>
        </el-table-column>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <span class="badge badge-gray">{{ noticeTypeMap[row.type] || row.type }}</span>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="90">
          <template #default="{ row }">
            <span class="badge" :class="row.priority >= 3 ? 'badge-red' : row.priority === 2 ? 'badge-orange' : 'badge-gray'">
              {{ row.priority >= 3 ? '高' : row.priority === 2 ? '中' : '低' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <span class="badge" :class="row.status === 1 ? 'badge-green' : 'badge-gray'">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="100" />
        <el-table-column label="发布时间" width="170">
          <template #default="{ row }">
            <span class="time-cell">{{ row.publishTime ? formatDate(row.publishTime) : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button class="action-btn" text type="primary" size="small" @click="showDialog(row)" v-permission="'business:notice:edit'">编辑</el-button>
            <el-button v-if="row.status === 0" class="action-btn" text type="success" size="small" @click="handlePublish(row)" v-permission="'business:notice:publish'">发布</el-button>
            <el-popconfirm title="确定删除?" @confirm="handleDelete(row.id)">
              <template #reference><el-button class="action-btn" text type="danger" size="small" v-permission="'business:notice:delete'">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @current-change="loadData" @size-change="handleSizeChange" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editItem ? '编辑公告' : '新建公告'" width="550px" destroy-on-close>
      <el-form :model="form" label-width="80px" size="large">
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="请输入公告标题" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入公告内容" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="系统公告" value="announcement" />
            <el-option label="活动通知" value="activity" />
            <el-option label="维护通知" value="maintenance" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority" style="width: 100%">
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { getNotices, createNotice, updateNotice, deleteNotice, publishNotice } from '@/api/notice'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'

const noticeTypeMap: Record<string, string> = { announcement: '系统公告', activity: '活动通知', maintenance: '维护通知' }

const notices = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editItem = ref<any>(null)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const filters = reactive({ type: '', status: '' as number | string })
const form = reactive({ title: '', content: '', type: 'announcement', priority: 1, status: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const params: any = { page: page.value, size: pageSize.value }
    if (filters.type) params.type = filters.type
    if (filters.status !== '' && filters.status !== null) params.status = filters.status
    const res: any = await getNotices(params)
    notices.value = res.data.records
    total.value = res.data.total
  } catch { ElMessage.error('加载公告失败') } finally { loading.value = false }
}
const handleSizeChange = () => { page.value = 1; loadData() }
const handleDelete = async (id: number) => {
  try { await deleteNotice(id); ElMessage.success('删除成功'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '删除失败') }
}
const handlePublish = async (row: any) => {
  try { await publishNotice(row.id); ElMessage.success('发布成功'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '发布失败') }
}

const showDialog = (item?: any) => {
  editItem.value = item || null
  if (item) { Object.assign(form, { title: item.title, content: item.content, type: item.type, priority: item.priority, status: item.status }) }
  else { Object.assign(form, { title: '', content: '', type: 'announcement', priority: 1, status: 0 }) }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.title.trim()) { ElMessage.warning('请输入公告标题'); return }
  if (!form.content.trim()) { ElMessage.warning('请输入公告内容'); return }
  saving.value = true
  try {
    if (editItem.value) { await updateNotice(editItem.value.id, form) }
    else { await createNotice(form) }
    ElMessage.success(editItem.value ? '更新成功' : '创建成功')
    dialogVisible.value = false; loadData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') }
  finally { saving.value = false }
}

onMounted(loadData)

let refreshTimer: ReturnType<typeof setInterval> | null = null
onMounted(() => { refreshTimer = setInterval(loadData, 30000) })
onUnmounted(() => { if (refreshTimer) clearInterval(refreshTimer) })
</script>

<style scoped lang="scss">
.name-cell { font-weight: 500; color: var(--text-primary); font-size: 14px; }
.time-cell { color: var(--text-tertiary); font-size: 13px; font-family: 'SF Mono', 'Menlo', monospace; }
.action-btn {
  font-size: 13px !important; color: var(--text-secondary) !important;
  height: 30px !important; padding: 0 8px !important;
  border-radius: var(--radius-xs) !important;
  &:hover { color: var(--apple-blue) !important; background: rgba(0, 122, 255, 0.06) !important; }
}
</style>
