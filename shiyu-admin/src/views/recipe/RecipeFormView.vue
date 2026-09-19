<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon green"><el-icon><Edit /></el-icon></div>
        <div class="page-header-text">
          <h2>{{ isEdit ? '编辑菜谱' : '新建菜谱' }}</h2>
          <p>{{ isEdit ? '修改菜谱信息' : '创建一个新的菜谱' }}</p>
        </div>
      </div>
    </div>

    <div class="form-grid">
      <div class="page-card">
        <div class="page-card-header"><h3>基本信息</h3></div>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
          <el-form-item label="菜名" prop="name"><el-input v-model="form.name" placeholder="请输入菜名" /></el-form-item>
          <el-form-item label="分类" prop="type">
            <el-select v-model="form.type" style="width: 100%">
              <el-option v-for="(name, key) in recipeTypeMap" :key="key" :label="name" :value="Number(key)" />
            </el-select>
          </el-form-item>
          <el-form-item label="封面图"><ImageUpload v-model="form.coverImage" /></el-form-item>
          <el-form-item label="烹饪时间"><el-input-number v-model="form.cookingTime" :min="1" :max="480" /> 分钟</el-form-item>
          <el-form-item label="难度"><el-rate v-model="form.difficulty" :max="5" /></el-form-item>
          <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" placeholder="菜谱描述" /></el-form-item>
        </el-form>
      </div>

      <div class="page-card">
        <div class="page-card-header"><h3>食材清单</h3></div>
        <div v-for="(m, i) in form.materials" :key="i" class="material-row">
          <el-input v-model="m.name" placeholder="食材名" style="flex:2" />
          <el-input v-model="m.amount" placeholder="用量" style="flex:1" />
          <el-input v-model="m.unit" placeholder="单位" style="flex:1" />
          <el-button text type="danger" @click="form.materials.splice(i, 1)"><el-icon><Delete /></el-icon></el-button>
        </div>
        <el-button type="primary" text @click="form.materials.push({ name: '', amount: '', unit: '', sortOrder: form.materials.length })">
          <el-icon><Plus /></el-icon> 添加食材
        </el-button>

        <div class="section-divider"></div>

        <div class="page-card-header"><h3>烹饪步骤</h3></div>
        <div v-for="(s, i) in form.steps" :key="i" class="step-item">
          <div class="step-header">
            <span class="badge badge-orange">步骤 {{ i + 1 }}</span>
            <el-button text type="danger" size="small" @click="removeStep(i)"><el-icon><Delete /></el-icon></el-button>
          </div>
          <el-input v-model="s.description" type="textarea" :rows="2" placeholder="描述这个步骤..." />
        </div>
        <el-button type="primary" text @click="addStep">
          <el-icon><Plus /></el-icon> 添加步骤
        </el-button>
      </div>
    </div>

    <div class="form-grid">
      <div class="page-card">
        <div class="page-card-header"><h3>营养信息</h3></div>
        <el-form label-position="top" size="large">
          <div class="form-row-3">
            <el-form-item label="热量 (千卡)"><el-input-number v-model="form.calories" :min="0" :precision="1" style="width:100%" /></el-form-item>
            <el-form-item label="蛋白质 (g)"><el-input-number v-model="form.protein" :min="0" :precision="1" style="width:100%" /></el-form-item>
            <el-form-item label="脂肪 (g)"><el-input-number v-model="form.fat" :min="0" :precision="1" style="width:100%" /></el-form-item>
          </div>
          <div class="form-row-3">
            <el-form-item label="碳水 (g)"><el-input-number v-model="form.carbs" :min="0" :precision="1" style="width:100%" /></el-form-item>
            <el-form-item label="膳食纤维 (g)"><el-input-number v-model="form.fiber" :min="0" :precision="1" style="width:100%" /></el-form-item>
            <el-form-item></el-form-item>
          </div>
        </el-form>
      </div>
      <div class="page-card">
        <div class="page-card-header"><h3>成本定价</h3></div>
        <el-form label-position="top" size="large">
          <div class="form-row-2">
            <el-form-item label="食材成本 (元)"><el-input-number v-model="form.cost" :min="0" :precision="2" style="width:100%" /></el-form-item>
            <el-form-item label="售价 (元)"><el-input-number v-model="form.sellingPrice" :min="0" :precision="2" style="width:100%" /></el-form-item>
          </div>
        </el-form>
      </div>
    </div>

    <div class="action-bar">
      <el-button @click="$router.back()">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave"><el-icon><Check /></el-icon> 保存</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createRecipe, updateRecipe, getRecipeById } from '@/api/recipe'
import { recipeTypeMap } from '@/utils/format'
import { ElMessage } from 'element-plus'
import ImageUpload from '@/components/common/ImageUpload.vue'

const route = useRoute(); const router = useRouter()
const formRef = ref(); const saving = ref(false)
const isEdit = computed(() => !!route.params.id)

const form = reactive<any>({ name: '', type: 1, coverImage: '', cookingTime: 30, difficulty: 1, description: '', materials: [], steps: [], calories: null, protein: null, fat: null, carbs: null, fiber: null, cost: null, sellingPrice: null })
const rules = {
  name: [{ required: true, message: '请输入菜名', trigger: 'blur' }],
  type: [{ required: true, message: '请选择分类', trigger: 'change' }],
  cookingTime: [{ required: true, message: '请设置烹饪时间', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
}

function addStep() {
  form.steps.push({ stepNumber: form.steps.length + 1, description: '' })
}

function removeStep(index: number) {
  form.steps.splice(index, 1)
  form.steps.forEach((s: any, i: number) => { s.stepNumber = i + 1 })
}

onMounted(async () => {
  if (route.params.id) {
    try {
      const res: any = await getRecipeById(Number(route.params.id))
      Object.assign(form, res.data)
    } catch { ElMessage.error('加载菜谱失败') }
  }
})

const handleSave = async () => {
  try { await formRef.value?.validate() } catch { return }
  if (!form.steps.length) { ElMessage.warning('请至少添加一个烹饪步骤'); return }
  const emptyMaterial = form.materials.find((m: any) => !m.name.trim())
  if (emptyMaterial) { ElMessage.warning('食材名称不能为空'); return }
  const emptyStep = form.steps.find((s: any) => !s.description.trim())
  if (emptyStep) { ElMessage.warning('步骤描述不能为空'); return }
  saving.value = true
  try {
    if (isEdit.value) await updateRecipe(Number(route.params.id), form); else await createRecipe(form)
    ElMessage.success('保存成功'); router.push('/recipes')
  } catch (e: any) { ElMessage.error(e.message || '保存失败') } finally { saving.value = false }
}
</script>

<style scoped lang="scss">
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.material-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  align-items: center;
}

.section-divider {
  height: 1px;
  background: var(--border-color);
  margin: 24px 0;
}

.step-item { margin-bottom: 18px; }
.step-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0 0;
}

.form-row-3 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
}

.form-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 900px) {
  .form-grid { grid-template-columns: 1fr; }
  .form-row-3, .form-row-2 { grid-template-columns: 1fr; }
}
</style>
