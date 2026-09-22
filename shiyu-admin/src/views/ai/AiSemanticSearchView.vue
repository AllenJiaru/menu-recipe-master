<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon blue"><el-icon :size="22"><Search /></el-icon></div>
        <div class="page-header-text">
          <h2>AI 语义搜索</h2>
          <p>用自然语言描述，智能推荐菜谱</p>
        </div>
      </div>
    </div>
    <div class="search-layout">
      <div class="input-card">
        <h3>🔍 语义搜索</h3>
        <el-input v-model="input" type="textarea" :rows="6" placeholder="例如：适合夏天的清淡菜、15分钟快手菜、给3岁宝宝吃的..." />
        <el-button type="primary" @click="submit" :loading="loading" :disabled="!input.trim()" style="width:100%;margin-top:16px" size="large">
          <el-icon><MagicStick /></el-icon> 搜索菜谱
        </el-button>
      </div>
      <div class="result-card" v-loading="loading">
        <h3>🎯 搜索结果</h3>
        <div v-if="!rawResult && !loading" class="empty-result">
          <div style="font-size: 48px">🔍</div>
          <p>描述你想吃的，AI 帮你找</p>
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
import { aiSemanticSearch } from '@/api/ai'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'
const loading = ref(false)
const input = ref('')
const rawResult = ref('')
const submit = async () => { loading.value = true; try { const res: any = await aiSemanticSearch({ message: input.value }); rawResult.value = res.data?.content || '' } catch (e: any) { ElMessage.error(e?.message || '操作失败，请检查网络和AI配置') } finally { loading.value = false } }
const copyResult = async () => { try { await navigator.clipboard.writeText(rawResult.value); ElMessage.success('已复制到剪贴板') } catch { ElMessage.error('复制失败') } }
</script>
<style scoped lang="scss">
.search-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.input-card, .result-card { background: var(--apple-card-bg, #fff); border-radius: 16px; padding: 24px; border: 1px solid var(--apple-border, #e8e8e8); }
.input-card h3, .result-card h3 { margin: 0 0 16px; font-size: 16px; }
.empty-result { text-align: center; padding: 60px 0; color: var(--apple-text-secondary); }
.result-content { font-size: 14px; line-height: 1.8; }
.page-header-icon.blue { background: rgba(59, 130, 246, 0.1); color: #3b82f6; }
@media (max-width: 900px) { .search-layout { grid-template-columns: 1fr; } }
</style>
