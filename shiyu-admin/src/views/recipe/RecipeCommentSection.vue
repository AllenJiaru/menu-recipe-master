<template>
  <div class="comment-section">
    <div class="page-card">
      <div class="page-card-header">
        <h3>评论区 <span class="count">({{ total }})</span></h3>
      </div>

      <div class="add-comment">
        <div class="comment-form-header">
          <span class="form-label">发表评论</span>
          <div class="rating-select">
            <span class="rating-text">评分：</span>
            <el-rate v-model="newComment.rating" :max="5" />
          </div>
        </div>
        <el-input v-model="newComment.content" type="textarea" :rows="3" placeholder="分享您对这道菜的看法..." maxlength="500" show-word-limit />
        <div class="form-actions">
          <el-button type="primary" :loading="submitting" @click="handleSubmitComment">发表评论</el-button>
        </div>
      </div>

      <div class="comment-list" v-loading="loading">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-avatar">
            <el-avatar :size="40" :src="comment.avatarUrl">{{ comment.userName?.charAt(0) }}</el-avatar>
          </div>
          <div class="comment-body">
            <div class="comment-header">
              <span class="comment-user">{{ comment.userName || '匿名用户' }}</span>
              <el-rate v-if="comment.rating" v-model="comment.rating" disabled size="small" />
            </div>
            <p class="comment-content">{{ comment.content }}</p>
            <div class="comment-footer">
              <span class="time-cell">{{ comment.createTime }}</span>
              <el-popconfirm v-if="comment.isOwner" title="确定删除此评论?" @confirm="handleDelete(comment.id)">
                <template #reference>
                  <el-button text type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </div>
        <el-empty v-if="!comments.length && !loading" description="暂无评论，快来抢沙发吧" :image-size="80" />
        <div class="page-pagination" v-if="comments.length">
          <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next" @current-change="loadComments" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getComments, addComment, deleteComment } from '@/api/comment'
import { ElMessage } from 'element-plus'

const props = defineProps<{ recipeId: number }>()

const comments = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const submitting = ref(false)
const newComment = reactive({ content: '', rating: 5 })

const loadComments = async () => {
  loading.value = true
  try {
    const res: any = await getComments(props.recipeId, { page: page.value, size: 10 })
    comments.value = res.data.records || []
    total.value = res.data.total
  } catch { ElMessage.error('加载评论失败') } finally { loading.value = false }
}

const handleSubmitComment = async () => {
  if (!newComment.content.trim()) { ElMessage.warning('请输入评论内容'); return }
  submitting.value = true
  try {
    await addComment({ recipeId: props.recipeId, content: newComment.content, rating: newComment.rating })
    ElMessage.success('评论发表成功')
    newComment.content = ''
    newComment.rating = 5
    page.value = 1
    loadComments()
  } catch (e: any) { ElMessage.error(e.message || '发表失败') } finally { submitting.value = false }
}

const handleDelete = async (id: number) => {
  try { await deleteComment(id); ElMessage.success('已删除'); loadComments() }
  catch (e: any) { ElMessage.error(e.message || '删除失败') }
}

onMounted(() => loadComments())
</script>

<style scoped lang="scss">
.comment-section { margin-top: 24px; }

.count { font-size: 14px; color: var(--text-muted); font-weight: 400; }

.add-comment {
  padding: 0 0 20px 0;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 20px;
}

.comment-form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.form-label { font-weight: 700; font-size: 14px; color: var(--text-primary); }

.rating-select {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-text { font-size: 13px; color: var(--text-secondary); }

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.comment-item {
  display: flex;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border-color);
  &:last-child { border-bottom: none; }
}

.comment-body { flex: 1; }

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.comment-user { font-weight: 700; font-size: 14px; color: var(--text-primary); }

.comment-content {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0 0 8px 0;
}

.comment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
