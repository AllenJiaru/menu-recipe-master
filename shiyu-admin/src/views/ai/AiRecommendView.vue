<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon orange"><el-icon :size="22"><Sunny /></el-icon></div>
        <div class="page-header-text">
          <h2>智能推荐</h2>
          <p>AI 根据当前时间和季节为你推荐菜谱</p>
        </div>
      </div>
      <el-button type="primary" @click="loadRecommend" :loading="loading">
        <el-icon><Refresh /></el-icon> 换一批
      </el-button>
    </div>

    <div v-loading="loading" class="recommend-grid">
      <div v-if="!rawResult && !loading" class="empty-card">
        <div style="font-size: 48px">🤖</div>
        <p>点击「换一批」获取 AI 推荐</p>
      </div>
      <div v-for="(item, i) in recipes" :key="i" class="recipe-card">
        <div class="recipe-header">
          <span class="recipe-name">{{ item.name }}</span>
          <el-tag size="small" :type="difficultyType(item.difficulty)">{{ item.difficulty }}</el-tag>
        </div>
        <div class="recipe-meta">
          <span><el-icon><Clock /></el-icon> {{ item.cookingTime }}分钟</span>
          <span>🍽️ {{ item.servings || 2 }}人份</span>
        </div>
        <div class="recipe-ingredients">
          <el-tag v-for="ing in (item.ingredients || []).slice(0, 6)" :key="ing" size="small" effect="plain" round>{{ ing }}</el-tag>
        </div>
        <div class="recipe-desc">{{ item.description }}</div>
      </div>
      <div v-if="rawResult && !recipes.length" class="raw-result markdown-body" v-html="renderMarkdown(rawResult)"></div>
      <el-button v-if="rawResult" size="small" text @click="copyResult" style="margin-top: 8px;">
        <el-icon><DocumentCopy /></el-icon> 复制结果
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { aiRecommend } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'

const loading = ref(false)
const rawResult = ref('')
const recipes = ref<any[]>([])

const loadRecommend = async () => {
  loading.value = true
  try {
    const res: any = await aiRecommend()
    rawResult.value = res.data?.content || ''
    try {
      const jsonMatch = rawResult.value.match(/\[[\s\S]*?\]/g)
      if (jsonMatch) {
        const longest = jsonMatch.sort((a, b) => b.length - a.length)[0]
        recipes.value = JSON.parse(longest)
      }
    } catch {}
  } catch (e: any) { ElMessage.error(e?.message || '获取推荐失败，请检查网络和AI配置') } finally { loading.value = false }
}

const difficultyType = (d: string) => d === '简单' ? 'success' : d === '中等' ? 'warning' : 'danger'

const copyResult = async () => {
  try {
    await navigator.clipboard.writeText(rawResult.value)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

onMounted(loadRecommend)
</script>

<style scoped lang="scss">
.recommend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}
.recipe-card {
  background: var(--apple-card-bg, #fff);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid var(--apple-border, #e8e8e8);
  transition: all 0.3s;
  &:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
}
.recipe-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.recipe-name { font-size: 18px; font-weight: 600; color: var(--apple-text-primary, #1d1d1f); }
.recipe-meta { display: flex; gap: 16px; margin-bottom: 12px; color: var(--apple-text-secondary, #8e8e93); font-size: 13px; span { display: flex; align-items: center; gap: 4px; } }
.recipe-ingredients { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 12px; }
.recipe-desc { font-size: 14px; color: var(--apple-text-secondary, #8e8e93); line-height: 1.5; }
.empty-card { grid-column: 1 / -1; text-align: center; padding: 80px 0; color: var(--apple-text-secondary); }
.raw-result { grid-column: 1 / -1; background: var(--apple-card-bg, #fff); border-radius: 12px; padding: 24px; border: 1px solid var(--apple-border); }
.page-header-icon.orange { background: rgba(255, 149, 0, 0.1); color: #ff9500; }
</style>
