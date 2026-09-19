<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon teal"><el-icon :size="22"><Calendar /></el-icon></div>
        <div class="page-header-text">
          <h2>AI 周菜谱</h2>
          <p>AI 帮你规划一周的菜谱，营养均衡又省心</p>
        </div>
      </div>
    </div>

    <div class="mealplan-layout">
      <div class="input-card">
        <h3>📅 设置需求</h3>
        <el-form label-position="top">
          <el-form-item label="规划天数"><el-slider v-model="form.days" :min="3" :max="14" :marks="{3:'3天',7:'7天',14:'14天'}" show-stops /></el-form-item>
          <el-form-item label="每周预算(元)"><el-input-number v-model="form.budget" :min="50" :max="5000" :step="50" style="width:100%" /></el-form-item>
          <el-form-item label="口味偏好">
            <el-checkbox-group v-model="form.preferences">
              <el-checkbox v-for="p in prefOptions" :key="p" :label="p">{{ p }}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="过敏食物">
            <el-checkbox-group v-model="form.allergies">
              <el-checkbox v-for="a in allergyOptions" :key="a" :label="a">{{ a }}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item label="饮食风格">
            <el-radio-group v-model="form.style">
              <el-radio label="">不限</el-radio>
              <el-radio label="家常菜">家常菜</el-radio>
              <el-radio label="轻食">轻食</el-radio>
              <el-radio label="素食">素食</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="generate" :loading="loading" style="width:100%" size="large">
          <el-icon><MagicStick /></el-icon> 生成菜谱计划
        </el-button>
      </div>

      <div class="result-card" v-loading="loading">
        <h3>📋 菜谱计划</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">📆</div>
          <p>设置参数后点击生成</p>
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
import { ref, reactive } from 'vue'
import { aiMealPlan } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const loading = ref(false)
const rawResult = ref('')
const prefOptions = ['清淡', '麻辣', '酸甜', '咸鲜', '微辣', '家常']
const allergyOptions = ['海鲜', '花生', '牛奶', '鸡蛋', '大豆', '坚果']
const form = reactive({ days: 7, budget: 500, preferences: [] as string[], allergies: [] as string[], style: '' })

const generate = async () => {
  loading.value = true
  try {
    const res: any = await aiMealPlan(form)
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
.mealplan-layout { display: grid; grid-template-columns: 380px 1fr; gap: 24px; }
.input-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.input-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.result-content { font-size: 14px; line-height: 1.8; }
.page-header-icon.teal { background: rgba(90, 200, 250, 0.1); color: #5ac8fa; }
@media (max-width: 900px) { .mealplan-layout { grid-template-columns: 1fr; } }
</style>