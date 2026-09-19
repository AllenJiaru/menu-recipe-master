<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon pink">
          <el-icon :size="22"><Connection /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>情侣空间</h2>
          <p>管理所有情侣空间</p>
        </div>
      </div>
      <el-button type="primary" @click="showDialog()" v-permission="'business:couple:create'"><el-icon><Plus /></el-icon> 新建空间</el-button>
    </div>

    <div class="page-table-card" v-loading="loading">
      <el-table :data="couples" style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="spaceName" label="空间名称" min-width="150">
          <template #default="{ row }"><span class="name-cell">{{ row.spaceName }}</span></template>
        </el-table-column>
        <el-table-column prop="chefName" label="主厨" width="120" align="center" />
        <el-table-column prop="dinerName" label="食客" width="120" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <span class="badge" :class="row.status === 1 ? 'badge-green' : 'badge-gray'">{{ row.status === 1 ? '正常' : '已解散' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center">
          <template #default="{ row }"><span class="time-cell">{{ formatDate(row.createTime) }}</span></template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button class="action-btn" text type="primary" size="small" @click="showDialog(row)" v-permission="'business:couple:edit'">编辑</el-button>
            <el-popconfirm title="确定解散此空间?" @confirm="handleDelete(row.id)">
              <template #reference><el-button class="action-btn" text type="danger" size="small" v-permission="'business:couple:delete'">解散</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @current-change="loadData" @size-change="handleSizeChange" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editItem ? '编辑空间' : '新建空间'" width="480px" destroy-on-close>
      <el-form :model="form" label-width="90px" ref="formRef" :rules="rules" size="large">
        <el-form-item label="空间名称" prop="spaceName"><el-input v-model="form.spaceName" placeholder="请输入空间名称" /></el-form-item>
        <el-form-item label="主厨昵称" prop="chefName"><el-input v-model="form.chefName" placeholder="请输入主厨昵称" /></el-form-item>
        <el-form-item label="食客昵称" prop="dinerName"><el-input v-model="form.dinerName" placeholder="请输入食客昵称" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getCouples, createCouple, updateCouple, deleteCouple } from '@/api/couple'
import { formatDate } from '@/utils/format'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const couples = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editItem = ref<any>(null)
const formRef = ref<FormInstance>()
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const form = reactive({ spaceName: '', chefName: '', dinerName: '' })
const rules: FormRules = {
  spaceName: [{ required: true, message: '请输入空间名称', trigger: 'blur' }],
  chefName: [{ required: true, message: '请输入主厨昵称', trigger: 'blur' }],
  dinerName: [{ required: true, message: '请输入食客昵称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getCouples({ page: page.value, size: pageSize.value })
    couples.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch { ElMessage.error('加载情侣空间失败') } finally { loading.value = false }
}
const handleSizeChange = () => { page.value = 1; loadData() }

const showDialog = (item?: any) => {
  editItem.value = item || null
  if (item) Object.assign(form, { spaceName: item.spaceName, chefName: item.chefName, dinerName: item.dinerName })
  else Object.assign(form, { spaceName: '', chefName: '', dinerName: '' })
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editItem.value) await updateCouple(editItem.value.id, form)
    else await createCouple(form)
    ElMessage.success(editItem.value ? '更新成功' : '创建成功')
    dialogVisible.value = false; loadData()
  } catch (e: any) { ElMessage.error(e.message || '保存失败') } finally { saving.value = false }
}

const handleDelete = async (id: number) => {
  try { await deleteCouple(id); ElMessage.success('已解散'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '解散失败') }
}

onMounted(loadData)
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