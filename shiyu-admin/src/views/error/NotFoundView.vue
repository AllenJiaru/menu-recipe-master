<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1>页面未找到</h1>
      <p>抱歉，您访问的页面不存在或已被移除。</p>
      <div class="action-buttons">
        <el-button type="primary" size="large" @click="$router.push('/')">
          <el-icon><HomeFilled /></el-icon> 返回首页
        </el-button>
        <el-button size="large" @click="$router.back()">
          <el-icon><ArrowLeft /></el-icon> 返回上一页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
:root {
  --apple-blue: #007AFF;
  --apple-green: #34C759;
  --apple-orange: #FF9500;
  --apple-red: #FF3B30;
  --apple-text-primary: #1D1D1F;
  --apple-text-secondary: #6E6E73;
  --apple-bg-page: #F5F5F7;
  --apple-bg-card: #FFFFFF;
  --apple-border: #E5E5EA;
  --apple-radius-card: 12px;
  --apple-radius-btn: 8px;
  --apple-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.not-found-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
}

.not-found-content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  color: var(--apple-text-primary);
  line-height: 1;
  margin-bottom: 16px;
}

h1 {
  font-size: 28px;
  font-weight: 600;
  color: var(--apple-text-primary);
  margin-bottom: 12px;
}

p {
  font-size: 16px;
  color: var(--apple-text-secondary);
  margin-bottom: 32px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
