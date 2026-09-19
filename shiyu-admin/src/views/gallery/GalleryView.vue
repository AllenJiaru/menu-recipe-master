<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-header-left">
        <div class="page-header-icon pink">
          <el-icon :size="22"><Picture /></el-icon>
        </div>
        <div class="page-header-text">
          <h2>美食相册</h2>
          <p>管理所有美食照片</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button v-if="selectedIds.length" type="danger" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon> 批量删除 ({{ selectedIds.length }})
        </el-button>
        <el-upload :http-request="handleUpload" :show-file-list="false" :before-upload="beforeUpload" accept="image/*">
          <el-button type="primary"><el-icon><Upload /></el-icon> 上传图片</el-button>
        </el-upload>
      </div>
    </div>

    <div class="gallery-grid" v-loading="loading">
      <div v-for="img in images" :key="img.id" class="gallery-item" :class="{ selected: selectedIds.includes(img.id) }" @click="toggleSelect(img.id)">
        <div class="gallery-img-wrap">
          <el-image :src="imgUrl(img.imageUrl)" fit="cover" class="gallery-img" :preview-src-list="[imgUrl(img.imageUrl)]" />
          <div class="gallery-actions">
            <el-popconfirm title="确定删除?" @confirm="handleDelete(img.id)">
              <template #reference>
                <el-button circle type="danger" size="small" @click.stop><el-icon><Delete /></el-icon></el-button>
              </template>
            </el-popconfirm>
          </div>
          <div class="gallery-check" v-if="selectedIds.includes(img.id)">
            <el-icon :size="20"><Check /></el-icon>
          </div>
        </div>
        <div class="gallery-info">
          <span class="gallery-desc">{{ img.description || '暂无描述' }}</span>
          <span class="gallery-time">{{ img.createTime?.slice(0, 10) }}</span>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && images.length === 0" description="暂无图片">
      <el-upload :http-request="handleUpload" :show-file-list="false" :before-upload="beforeUpload" accept="image/*">
        <el-button type="primary"><el-icon><Upload /></el-icon> 上传第一张图片</el-button>
      </el-upload>
    </el-empty>

    <div class="page-pagination" v-if="total > 0">
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" :page-sizes="[12,24,48]" layout="total, sizes, prev, pager, next" @current-change="loadData" @size-change="handleSizeChange" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getGalleryImages, deleteGalleryImage, batchDeleteGallery, uploadGalleryImage } from '@/api/gallery'
import { imgUrl } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'

const images = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const selectedIds = ref<number[]>([])

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getGalleryImages({ page: page.value, size: pageSize.value })
    images.value = res.data.records || []
    total.value = res.data.total || 0
  } catch { ElMessage.error('加载相册失败') } finally { loading.value = false }
}
const handleSizeChange = () => { page.value = 1; loadData() }
const toggleSelect = (id: number) => { const idx = selectedIds.value.indexOf(id); if (idx > -1) selectedIds.value.splice(idx, 1); else selectedIds.value.push(id) }

const beforeUpload = (file: File) => {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片文件'); return false }
  if (file.size > 10 * 1024 * 1024) { ElMessage.error('图片大小不能超过10MB'); return false }
  return true
}

const handleUpload = async (options: any) => {
  const formData = new FormData(); formData.append('file', options.file)
  try {
    const res: any = await uploadGalleryImage(formData)
    if (res.code === 200) { ElMessage.success('上传成功'); loadData() } else ElMessage.error(res.message || '上传失败')
  } catch { ElMessage.error('上传失败') }
}

const handleDelete = async (id: number) => {
  try { await deleteGalleryImage(id); ElMessage.success('删除成功'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '删除失败') }
}
const handleBatchDelete = async () => {
  try { await ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 张图片？`, '批量删除', { type: 'warning' }) } catch { return }
  try { await batchDeleteGallery(selectedIds.value); selectedIds.value = []; ElMessage.success('批量删除成功'); loadData() }
  catch (e: any) { ElMessage.error(e.message || '批量删除失败') }
}

onMounted(loadData)

let refreshTimer: ReturnType<typeof setInterval> | null = null
onMounted(() => { refreshTimer = setInterval(loadData, 15000) })
onUnmounted(() => { if (refreshTimer) clearInterval(refreshTimer) })
</script>

<style scoped lang="scss">
.header-actions { display: flex; gap: 12px; align-items: center; }

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.gallery-item {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: var(--shadow-xs);

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-md);
    border-color: transparent;
  }

  &.selected {
    border-color: var(--apple-blue);
    box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.2);
  }
}

.gallery-img-wrap {
  position: relative;
  width: 100%;
  height: 220px;
  overflow: hidden;
  background: var(--bg-page);
}

.gallery-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
  .gallery-item:hover & { transform: scale(1.05); }
}

.gallery-actions {
  position: absolute;
  top: 10px;
  right: 10px;
  opacity: 0;
  transition: opacity 0.2s ease;
  .gallery-item:hover & { opacity: 1; }
}

.gallery-check {
  position: absolute;
  top: 10px;
  left: 10px;
  width: 30px;
  height: 30px;
  background: var(--apple-blue);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.3);
}

.gallery-info {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.gallery-desc {
  font-size: 13px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.gallery-time {
  font-size: 11px;
  color: var(--text-tertiary);
}
</style>
