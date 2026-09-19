<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon pink"><el-icon :size="22"><ShoppingBag /></el-icon></div>
        <div class="page-header-text">
          <h2>智能购物清单</h2>
          <p>根据菜谱计划自动生成购物清单并估算价格</p>
        </div>
      </div>
    </div>

    <div class="shopping-layout">
      <div class="input-card">
        <h3>📝 菜谱计划</h3>
        <el-input v-model="mealPlanText" type="textarea" :rows="10" placeholder="输入你的菜谱计划，例如：&#10;&#10;周一午餐：番茄炒蛋、清炒时蔬&#10;周一晚餐：红烧排骨、凉拌黄瓜&#10;周二午餐：宫保鸡丁..." />
        <el-button type="primary" @click="generate" :loading="loading" :disabled="!mealPlanText.trim()" style="width:100%;margin-top:16px" size="large">
          <el-icon><ShoppingBag /></el-icon> 生成购物清单
        </el-button>
      </div>

      <div class="result-card" v-loading="loading">
        <h3>🛒 购物清单</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">🛍️</div>
          <p>输入菜谱计划后生成购物清单</p>
        </div>
        <div v-if="rawResult" class="result-content markdown-body" v-html="renderMarkdown(rawResult)"></div>
        <el-button v-if="rawResult" size="small" text @click="copyResult" style="margin-top: 8px;">
          <el-icon><DocumentCopy /></el-icon> 复制结果
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { aiShoppingList } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const loading = ref(false)
const mealPlanText = ref('')
const rawResult = ref('')

const generate = async () => {
  loading.value = true
  try {
    const res: any = await aiShoppingList({ mealPlan: mealPlanText.value })
    rawResult.value = res.data?.content || ''
  } catch (e: any) { ElMessage.error(e?.message || '生成失败，请检查网络和AI配置') } finally { loading.value = false }
}

const copyResult = async () => {
  try {
    await navigator.clipboard.writeText(rawResult.value)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped lang="scss">
.shopping-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.input-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.input-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.result-content { font-size: 14px; line-height: 1.8; }
.page-header-icon.pink { background: rgba(255, 45, 85, 0.1); color: #ff2d55; }
@media (max-width: 900px) { .shopping-layout { grid-template-columns: 1fr; } }
</style>