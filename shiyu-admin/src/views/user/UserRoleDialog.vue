<template>
  <el-dialog
    :model-value="visible"
    :title="`分配角色 - ${username}`"
    width="450px"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
  >
    <div class="user-role-wrap" v-loading="loading">
      <div class="user-info" v-if="username">
        <span class="info-label">用户</span>
        <span class="info-value">{{ username }}</span>
      </div>
      <el-divider />
      <div class="role-list">
        <el-checkbox-group v-model="selectedRoleIds">
          <div v-for="role in allRoles" :key="role.id" class="role-item">
            <el-checkbox :label="role.id" :value="role.id">
              <span class="role-name">{{ role.name }}</span>
              <span class="role-code">{{ role.code }}</span>
            </el-checkbox>
          </div>
        </el-checkbox-group>
        <el-empty v-if="!loading && allRoles.length === 0" description="暂无角色数据" />
      </div>
    </div>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getAllRoles } from '@/api/role'
import { getUserRoles, assignRoles } from '@/api/userManagement'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  visible: boolean
  userId: number | null
  username: string
}>()

const emit = defineEmits<{
  'update:visible': [val: boolean]
  saved: []
}>()

const allRoles = ref<any[]>([])
const selectedRoleIds = ref<number[]>([])
const loading = ref(false)
const saving = ref(false)

const loadData = async () => {
  if (!props.userId) return
  loading.value = true
  try {
    const [rolesRes, userRolesRes]: any[] = await Promise.all([
      getAllRoles(),
      getUserRoles(props.userId)
    ])
    allRoles.value = rolesRes.data || []
    selectedRoleIds.value = (userRolesRes.data || []).map((r: any) => r.id || r.roleId)
  } catch {
    ElMessage.error('加载角色数据失败')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!props.userId) return
  saving.value = true
  try {
    await assignRoles(props.userId, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    emit('update:visible', false)
    emit('saved')
  } catch (e: any) {
    ElMessage.error(e.message || '分配失败')
  } finally {
    saving.value = false
  }
}

watch(() => props.visible, (val) => {
  if (val && props.userId) loadData()
})
</script>

<style scoped lang="scss">
.user-role-wrap {
  min-height: 200px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.info-label {
  font-size: 12px;
  color: var(--text-tertiary);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.role-list {
  .role-item {
    padding: 10px 0;
    border-bottom: 1px solid var(--border-light);
    &:last-child { border-bottom: none; }
  }
  .role-name {
    font-weight: 600;
    margin-right: 8px;
    color: var(--text-primary);
  }
  .role-code {
    color: var(--text-tertiary);
    font-size: 13px;
    font-family: 'SF Mono', 'Menlo', monospace;
  }
}
</style>
