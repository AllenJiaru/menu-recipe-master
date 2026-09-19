<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon pink"><el-icon :size="22"><Star /></el-icon></div>
        <div class="page-header-text"><h2>我的收藏</h2><p>管理您收藏的所有菜谱</p></div>
      </div>
      <div class="page-toolbar-right">
        <span class="page-count">共 <b>{{ total }}</b> 道收藏</span>
      </div>
    </div>

    <div class="page-card" v-loading="loading">
      <div class="recipe-grid" v-if="favorites.length">
        <div v-for="item in favorites" :key="item.id" class="recipe-card" @click="$router.push(`/recipes/${item.recipeId}`)">
          <div class="card-cover">
            <el-image v-if="item.recipeCover" :src="imgUrl(item.recipeCover)" fit="cover" class="cover-img" />
            <div v-else class="cover-placeholder"><el-icon :size="32"><Picture /></el-icon></div>
            <el-button class="fav-btn" type="danger" circle size="small" @click.stop="handleRemove(item)">
              <el-icon><StarFilled /></el-icon>
            </el-button>
            <span v-if="item.categoryName" class="card-category">{{ item.categoryName }}</span>
          </div>
          <div class="card-info">
            <h3 class="card-name">{{ item.recipeName }}</h3>
            <div class="card-meta">
              <div class="meta-left">
                <span v-if="item.difficulty" class="meta-item">
                  <el-icon :size="12"><Star /></el-icon>
                  {{ item.difficulty }}星
                </span>
                <span v-if="item.cookingTime" class="meta-item">
                  <el-icon :size="12"><Clock /></el-icon>
                  {{ item.cookingTime }}min
                </span>
              </div>
              <span class="fav-time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!favorites.length && !loading" description="暂无收藏的菜谱" />
      <div class="page-pagination" v-if="total > pageSize">
        <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[12,24,48]" layout="total, sizes, prev, pager, next" @current-change="loadData" @size-change="handleSizeChange" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFavorites, toggleFavorite } from '@/api/favorite'
import { imgUrl } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'

const favorites = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)

const formatTime = (t: string) => {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getFavorites({ page: page.value, size: pageSize.value })
    favorites.value = res.data.records || []
    total.value = res.data.total
  } catch { ElMessage.error('加载收藏失败') } finally { loading.value = false }
}

const handleSizeChange = () => { page.value = 1; loadData() }

const handleRemove = async (item: any) => {
  try {
    await ElMessageBox.confirm(`确定取消收藏「${item.recipeName}」？`, '取消收藏', { type: 'warning' })
    await toggleFavorite(item.recipeId, item.recipeName, item.recipeCover)
    ElMessage.success('已取消收藏')
    loadData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.recipe-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  padding: 4px;
}

.recipe-card {
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fff;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    transform: translateY(-4px);

    .cover-img { transform: scale(1.05); }
  }
}

.card-cover {
  position: relative;
  height: 160px;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #F5F5F7, #E8E8ED);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #C7C7CC;
}

.fav-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.card-category {
  position: absolute;
  bottom: 10px;
  left: 10px;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  color: #007AFF;
}

.card-info { padding: 14px; }

.card-name {
  font-size: 14px;
  font-weight: 600;
  color: #1D1D1F;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.meta-left {
  display: flex;
  gap: 10px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: #86868B;
}

.fav-time {
  font-size: 11px;
  color: #AEAEB2;
}
</style>
