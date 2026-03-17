<template>
  <div class="discover-view" :class="{ 'dark-theme': isDark }">
    <div class="header-section">
      <h2 class="page-title">发现</h2>
    </div>

    <ElForm :model="filterModel" @submit.prevent="fetchWorks" inline class="filter-form">
      <ElFormItem label="关键字">
        <ElInput
          v-model="filterModel.keywords"
          placeholder="搜索作品标题"
          clearable
          style="width: 200px"
        />
      </ElFormItem>
      <ElFormItem>
        <ElButton native-type="submit" type="primary">筛选</ElButton>
      </ElFormItem>
    </ElForm>

    <!-- 作品卡片列表 -->
    <div v-if="works.length > 0" class="works-grid">
      <WorkCard
        v-for="work in works"
        :key="work.id"
        :cover-url="work.coverUrl"
        :title="work.title"
        :description="work.description"
        :author="work.author"
        :author-avatar="work.authorAvatar"
        :publish-date="work.publishDate"
        :show-delete="false"
        @click="handlePlayVideo(work)"
      />
    </div>

    <!-- 当页面没有显示作品 -->
    <div v-else class="empty-state">
      <i class="bi bi-camera-video"></i>
      <p>暂无作品</p>
    </div>

    <!-- 分页 -->
    <ElPagination
      v-if="works.length > 0"
      layout="prev, pager, next, jumper, sizes, ->, total"
      :page-sizes="[8, 16, 24]"
      v-model:current-page="pagination.currentPage"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      background
      class="pagination"
    />

    <!-- 视频播放器 -->
    <PlayVideo
      v-model:visible="videoPlayerVisible"
      :video-url="currentVideoUrl"
      :video-title="currentVideoTitle"
      :description="currentVideoDescription"
    />
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElButton,ElForm,ElFormItem,ElInput,ElPagination,ElMessage, } from 'element-plus'
import WorkCard from '@/components/WorkCard.vue'
import PlayVideo from '@/components/PlayVideo.vue'
import request from '@/utils/request'
import { buildCoverURL, buildVideoURL, buildAvatarURL } from '@/utils/helper'
import { useTheme } from '@/utils/useTheme'
import 'bootstrap-icons/font/bootstrap-icons.css'

const { isDark } = useTheme()

// 作品列表
const works = ref([])

// 视频播放器状态
const videoPlayerVisible = ref(false)
const currentVideoUrl = ref('')
const currentVideoTitle = ref('')
const currentVideoDescription = ref('')

// 筛选表单
const filterModel = reactive({
  keywords: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 8,
  total: 0
})

// 获取所有用户审核通过的作品列表
const fetchWorks = () => {
  const params = new URLSearchParams()
  params.append('page', pagination.currentPage)
  params.append('pageSize', pagination.pageSize)

  if (filterModel.keywords) {
    params.append('keywords', filterModel.keywords)
  }

  request.get('/works/discover', { params }).then((response) => {
    if (response.data.status === true) {
      // 处理返回的作品数据，转换URL
      works.value = response.data.payload.works.map(work => ({
        ...work,
        coverUrl: work.coverUrl ? buildCoverURL(work.coverUrl) : '',
        videoUrl: work.videoUrl ? buildVideoURL(work.videoUrl) : '',
        authorAvatar: work.authorAvatar ? buildAvatarURL(work.authorAvatar) : '',
        publishDate: work.createdAt // 使用 createdAt 作为发布日期
      }))
      Object.assign(pagination, response.data.payload.pagination)
    }
  }).catch(() => {
    ElMessage.error('获取作品列表失败')
  })
}

// 监听分页变化
watch(
  () => [pagination.currentPage, pagination.pageSize],
  () => {
    fetchWorks()
  },
  { immediate: true }
)

// 点击视频卡片播放视频
const handlePlayVideo = (work) => {
  currentVideoUrl.value = work.videoUrl
  currentVideoTitle.value = work.title
  currentVideoDescription.value = work.description || ''
  videoPlayerVisible.value = true
}
</script>

<style scoped lang="scss">
.discover-view {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

// 顶部区域
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }
}

// 筛选表单
.filter-form {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
}

// 作品网格
.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 24px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 16px;
  }

  @media (max-width: 480px) {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #909399;

  i {
    font-size: 80px;
    margin-bottom: 16px;
    opacity: 0.5;
  }

  p {
    font-size: 16px;
    margin-bottom: 24px;
  }
}

// 分页
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

// 暗黑主题
.discover-view.dark-theme {
  .page-title {
    color: #e5e5e5;
  }

  .filter-form {
    background-color: #1e1e1e;
  }

  .empty-state {
    color: #6a6a6a;
  }
}
</style>
