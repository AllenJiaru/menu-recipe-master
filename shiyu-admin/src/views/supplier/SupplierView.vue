<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon teal">
          <el-icon :size="22"><Shop /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>供应商管理</h2>
          <p>管理食材供应商信息，包括联系方式、评级等</p>
        </div>
      </div>
      <el-button type="primary" @click="openDialog()" v-permission="'business:supplier:create'">
        <el-icon><Plus /></el-icon> 新增供应商
      </el-button>
    </div>

    <div class="page-toolbar">
      <div class="page-search">
        <el-icon class="search-icon"><Search /></el-icon>
        <input v-model="filters.keyword" placeholder="搜索供应商名称..." @keyup.enter="loadData" />
      </div>
      <el-select v-model="filters.rating" placeholder="全部评级" clearable style="width: 140px" @change="loadData">
        <el-option v-for="r in 5" :key="r" :label="`${r}星`" :value="r" />
      </el-select>
      <div class="page-toolbar-right">
        <span class="page-count">共 <b>{{ total }}</b> 条</span>
      </div>
    </div>

    <div class="page-table-card" v-loading="loading">
      <el-table :data="suppliers" stripe style="width: 100%">
        <el-table-column prop="name" label="供应商名称" min-width="150">
          <template #default="{ row }"><span class="name-cell">{{ row.name }}</span></template>
        </el-table-column>
        <el-table-column prop="contact" label="联系人" width="110" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column label="评级" width="150">
          <template #default="{ row }">
            <div class="rating-stars">
              <span v-for="s in 5" :key="s" class="star" :class="{ active: s <= row.rating }">★</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="供应类别" width="120">
          <template #default="{ row }"><span class="badge badge-purple">{{ row.category || '未分类' }}</span></template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button class="action-btn" size="small" @click="openDialog(row)" v-permission="'business:supplier:edit'">编辑</el-button>
            <el-popconfirm title="确定删除该供应商?" @confirm="handleDelete(row.id)">
              <template #reference><el-button class="action-btn danger" size="small" v-permission="'business:supplier:delete'">删除</el-button></template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="page-pagination">
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @current-change="loadData" @size-change="handleSizeChange" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑供应商' : '新增供应商'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" placeholder="请输入供应商名称" /></el-form-item>
        <el-form-item label="联系人" prop="contact"><el-input v-model="form.contact" placeholder="请输入联系人" /></el-form-item>
        <el-form-item label="电话" prop="phone"><el-input v-model="form.phone" placeholder="请输入联系电话" /></el-form-item>
        <el-form-item label="评级" prop="rating">
          <el-rate v-model="form.rating" :max="5" show-score />
        </el-form-item>
        <el-form-item label="类别" prop="category"><el-input v-model="form.category" placeholder="如：蔬菜、肉类、调料" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入地址" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注信息" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getSuppliers, createSupplier, updateSupplier, deleteSupplier } from '@/api/supplier'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

const suppliers = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const filters = reactive({ keyword: '', rating: null as number | null })

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ name: '', contact: '', phone: '', rating: 3, category: '', address: '', remark: '' })
const rules: FormRules = {
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getSuppliers({ page: page.value, size: pageSize.value, ...filters })
    suppliers.value = res.data.records
    total.value = res.data.total
  } catch { ElMessage.error('加载供应商失败') } finally { loading.value = false }
}

const handleSizeChange = () => { page.value = 1; loadData() }

const openDialog = (row?: any) => {
  editingId.value = row?.id || null
  Object.assign(form, row ? { name: row.name, contact: row.contact, phone: row.phone, rating: row.rating, category: row.category, address: row.address, remark: row.remark } : { name: '', contact: '', phone: '', rating: 3, category: '', address: '', remark: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await updateSupplier(editingId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createSupplier({ ...form })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e: any) { ElMessage.error(e.message || '操作失败') } finally { submitting.value = false }
}

const handleDelete = async (id: number) => {
  try { await deleteSupplier(id); ElMessage.success('删除成功'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '删除失败') }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.rating-stars {
  display: flex;
  gap: 2px;
}

.star {
  font-size: 16px;
  color: var(--text-quaternary);
  &.active { color: var(--apple-orange); }
}
</style>
