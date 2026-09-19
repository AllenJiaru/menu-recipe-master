<template>
  <div class="profile-page">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon blue">
          <el-icon :size="22"><User /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>个人中心</h2>
          <p>管理您的账号信息与安全设置</p>
        </div>
      </div>
    </div>

    <div class="profile-grid">
      <div class="page-card profile-card">
        <div class="avatar-section">
          <div class="avatar-upload" @click="triggerUpload">
            <img v-if="avatarPreview || userInfo?.avatar" :src="avatarPreview || imgUrl(userInfo?.avatar)" class="avatar-img" />
            <div v-else class="avatar-placeholder">
              {{ userInfo?.nickname?.charAt(0) || userInfo?.username?.charAt(0) || '?' }}
            </div>
            <div class="avatar-overlay">
              <el-icon :size="24"><Camera /></el-icon>
              <span>更换头像</span>
            </div>
            <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />
          </div>

          <h3 class="display-name">{{ userInfo?.nickname || userInfo?.username }}</h3>
          <span class="role-badge" :class="userInfo?.role">{{ roleLabel }}</span>

          <div class="info-divider"></div>

          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">用户名</span>
              <span class="info-value">{{ userInfo?.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">用户ID</span>
              <span class="info-value">#{{ userInfo?.id }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">注册时间</span>
              <span class="info-value">{{ formatDate(userInfo?.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">上次登录</span>
              <span class="info-value">{{ formatDate(userInfo?.lastLoginTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="page-card profile-card">
        <div class="page-card-header">
          <div class="page-header-left">
            <div class="page-header-icon blue">
              <el-icon :size="18"><User /></el-icon>
            </div>
            <div>
              <h3>基本资料</h3>
              <p class="text-secondary" style="font-size:12px;margin-top:2px">更新您的个人信息</p>
            </div>
          </div>
        </div>
        <el-form :model="profileForm" label-position="top" size="large" class="profile-form">
          <el-form-item label="用户名">
            <el-input :value="userInfo?.username" disabled />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" placeholder="请输入新昵称" maxlength="20" show-word-limit />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="profileSaving" @click="saveProfile">
              <el-icon><Check /></el-icon> 保存修改
            </el-button>
          </el-form-item>
        </el-form>

        <el-divider />

        <div class="page-card-header">
          <div class="page-header-left">
            <div class="page-header-icon orange">
              <el-icon :size="18"><Lock /></el-icon>
            </div>
            <div>
              <h3>修改密码</h3>
              <p class="text-secondary" style="font-size:12px;margin-top:2px">定期修改密码以保障账号安全</p>
            </div>
          </div>
        </div>
        <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-position="top" size="large" class="profile-form">
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" placeholder="请设置新密码（至少6位）" show-password />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="warning" :loading="pwdSaving" @click="changePwd">
              <el-icon><Lock /></el-icon> 修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { updateProfile, changePassword } from '@/api/auth'
import { uploadFile } from '@/api/file'
import { formatDate, imgUrl } from '@/utils/format'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const userInfo = computed(() => authStore.userInfo)
const roleLabel = computed(() => ({ admin: '管理员', chef: '主厨', diner: '食客' }[userInfo.value?.role || ''] || '未知'))

const fileInput = ref<HTMLInputElement>()
const avatarPreview = ref('')
const uploading = ref(false)

const profileForm = reactive({ nickname: '' })
const profileSaving = ref(false)

const pwdFormRef = ref()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdSaving = ref(false)

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (value !== pwdForm.newPassword) callback(new Error('两次密码输入不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

function triggerUpload() {
  fileInput.value?.click()
}

async function handleAvatarChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  const reader = new FileReader()
  reader.onload = (ev) => { avatarPreview.value = ev.target?.result as string }
  reader.readAsDataURL(file)

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', 'avatar')
    const res: any = await uploadFile(formData)
    if (res.code === 200) {
      const filename = res.data
      const profileRes: any = await updateProfile({ avatar: filename })
      authStore.userInfo = profileRes.data
      ElMessage.success('头像已更新')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e: any) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

async function saveProfile() {
  if (!profileForm.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  profileSaving.value = true
  try {
    const res: any = await updateProfile({ nickname: profileForm.nickname })
    authStore.userInfo = res.data
    ElMessage.success('资料已更新')
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    profileSaving.value = false
  }
}

async function changePwd() {
  try { await pwdFormRef.value?.validate() } catch { return }
  pwdSaving.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (e: any) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    pwdSaving.value = false
  }
}

onMounted(() => {
  if (userInfo.value) {
    profileForm.nickname = userInfo.value.nickname || ''
  }
})

watch(() => userInfo.value, (val) => {
  if (val && !profileForm.nickname) {
    profileForm.nickname = val.nickname || ''
  }
})
</script>

<style scoped lang="scss">
.profile-page {
  max-width: 960px;
  margin: 0 auto;
}

.profile-grid {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
}

.profile-card {
  padding: 32px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-upload {
  width: 110px;
  height: 110px;
  border-radius: 50%;
  position: relative;
  cursor: pointer;
  margin-bottom: 20px;
  overflow: hidden;
  border: 3px solid var(--border-color);
  transition: all 0.2s ease;
  box-shadow: var(--shadow-sm);

  &:hover {
    transform: scale(1.05);
    box-shadow: var(--shadow-md);
  }

  &:hover .avatar-overlay { opacity: 1; }
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: 600;
  color: var(--apple-blue);
  background: var(--bg-page);
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s ease;
  border-radius: 50%;
  color: #fff;
  span { font-size: 11px; font-weight: 500; }
}

.display-name {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.role-badge {
  display: inline-block;
  padding: 4px 16px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 24px;

  &.admin { background: rgba(0, 122, 255, 0.1); color: var(--apple-blue); }
  &.chef { background: rgba(255, 149, 0, 0.1); color: var(--apple-orange); }
  &.diner { background: rgba(52, 199, 89, 0.1); color: var(--apple-green); }
}

.info-divider {
  width: 100%;
  height: 1px;
  background: var(--border-color);
  margin-bottom: 20px;
}

.info-grid {
  width: 100%;
  grid-template-columns: 1fr;
  gap: 14px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-form {
  max-width: 420px;

  :deep(.el-form-item__label) {
    font-weight: 600;
    color: var(--text-secondary);
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .profile-grid { grid-template-columns: 1fr; }
}
</style>