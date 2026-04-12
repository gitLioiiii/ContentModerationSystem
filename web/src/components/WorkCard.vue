<template>
  <!-- 作品卡片 -->
  <ElCard
    class="work-card"
    :body-style="{ padding: '0px' }"
    shadow="hover"
    @click="handleClick"
  >
    <!-- 左上角审核状态标签 -->
    <!-- 待AI审核 -->
    <div v-if="reviewStatus === 'pending'" class="status-badge status-pending">
      <span>AI审核</span>
    </div>
    <!-- 审核不通过 -->
    <div v-else-if="reviewStatus === 'rejected'" class="status-badge status-rejected">
      <span>审核不通过</span>
    </div>
    <!-- 人工审核中 -->
    <div v-else-if="reviewStatus === 'reviewing'" class="status-badge status-reviewing">
      <span>人工审核中</span>
    </div>
    <!-- 人工审核不通过 -->
    <div v-else-if="reviewStatus === 'manual_rejected'" class="status-badge status-manual-rejected">
      <span>人工审核不通过</span>
    </div>
    <!-- 申诉中 -->
    <div v-else-if="reviewStatus === 'appealing'" class="status-badge status-appealing">
      <span>申诉中</span>
    </div>
    <!-- 申诉驳回 -->
    <div v-else-if="reviewStatus === 'appeal_rejected'" class="status-badge status-appeal-rejected">
      <span>申诉已驳回</span>
    </div>

    <!-- 删除按钮显示 -->
    <div v-if="showDelete" class="delete-btn" @click.stop="handleDelete">
      <i class="bi bi-trash"></i>
    </div>

    <!-- 查看审核结果按钮 -->
    <div v-if="showReview" class="review-btn" @click.stop="handleReview">
      <i class="bi bi-search"></i>
    </div>

    <!-- 申诉按钮 -->
    <div v-if="showAppeal" class="appeal-btn" @click.stop="handleAppeal">
      <i class="bi bi-pen"></i>
    </div>

    <!-- 图片/视频封面区域 -->
    <div class="media-cover">
      <ElImage
        :src="coverUrl"
        :alt="title"
        fit="cover"
        class="cover-image"
        :lazy="lazy"
      >
        <template #placeholder>
          <div class="image-placeholder">
            <i class="bi bi-image"></i>
            <span>加载中...</span>
          </div>
        </template>
        <template #error>
          <div class="image-error">
            <i class="bi bi-image-fill"></i>
            <span>加载失败</span>
          </div>
        </template>
      </ElImage>
    </div>

    <!-- 卡片信息区域 -->
    <div class="card-info">
      <!-- 标题 -->
      <div class="card-title" :title="title">
        {{ title }}
      </div>

      <!-- 作品描述 -->
      <div v-if="description" class="card-description" :title="description">
        {{ description }}
      </div>

      <!-- 底部信息 -->
      <div class="card-bottom">
        <!-- 作者信息 -->
        <div v-if="author" class="author-info">
          <ElAvatar
            v-if="authorAvatar"
            :src="authorAvatar"
            :size="24"
            class="author-avatar"
          >
            <template #error>
              <i class="bi bi-person-circle"></i>
            </template>
          </ElAvatar>
          <i v-else class="bi bi-person-circle author-icon"></i>
          <span class="author-name" :title="author">{{ author }}</span>
          <span v-if="publishDate" class="publish-date">· {{ publishDate }}</span>
        </div>

      </div>
    </div>
  </ElCard>
</template>

<script setup>
import { ElCard, ElImage, ElAvatar } from 'element-plus'
import 'bootstrap-icons/font/bootstrap-icons.css'


defineProps({
  // 视频封面图片地址
  coverUrl: {
    type: String,
    required: true
  },
  // 视频标题
  title: {
    type: String,
    required: true
  },
  // 作品描述
  description: {
    type: String,
    default: ''
  },
  // 作者名称
  author: {
    type: String,
    default: ''
  },
  // 作者头像URL
  authorAvatar: {
    type: String,
    default: ''
  },
  // 发布日期
  publishDate: {
    type: String,
    default: ''
  },
  // 是否懒加载
  lazy: {
    type: Boolean,
    default: true
  },
  // 是否显示删除按钮
  showDelete: {
    type: Boolean,
    default: false
  },
  // 是否显示审核结果按钮
  showReview: {
    type: Boolean,
    default: false
  },
  // 是否显示申诉按钮
  showAppeal: {
    type: Boolean,
    default: false
  },
  // 审核状态
  reviewStatus: {
    type: String,
    default: ''
  }
})

// 定义事件
const clickCard = defineEmits(['delete', 'click', 'review', 'appeal'])

// 删除处理
const handleDelete = () => {
  clickCard('delete')
}

// 查看审核结果处理
const handleReview = () => {
  clickCard('review')
}

// 申诉处理
const handleAppeal = () => {
  clickCard('appeal')
}

// 点击卡片处理
const handleClick = () => {
  clickCard('click')
}
</script>

<style scoped lang="scss">
.work-card {
  width: 100%;
  border-radius: 0.5rem;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  :deep(.el-card__body) {
    padding: 0;
  }
}

// 审核状态标签（左上角）
.status-badge {
  position: absolute;
  top: 0.5rem;
  left: 0.5rem;
  padding: 0.4rem 0.8rem;
  border-radius: 1rem;
  display: flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.75rem;
  font-weight: 600;
  z-index: 10;
  box-shadow: 0 0.125rem 0.5rem rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(4px);
  transition: all 0.3s ease;

  i {
    font-size: 0.875rem;
  }

  span {
    white-space: nowrap;
  }

  &:hover {
    transform: scale(1.05);
  }
}

// 待AI审核标签 - 浅蓝色
.status-pending {
  background: #3491FA;
  color: white;

  &:hover {
    background: #3491FA;
  }
}

// AI审核不通过标签 - 红色
.status-rejected {
  background: #F53F3F;
  color: white;

  &:hover {
    background: #F53F3F;
  }
}

// 人工审核中标签 - 橙色
.status-reviewing {
  background: #FF7D00;
  color: white;

  &:hover {
    background: #FF7D00;
  }
}

// 人工审核不通过标签 - 深红色
.status-manual-rejected {
  background: #F76560;
  color: white;

  &:hover {
    background: #F76560;
  }
}

// 申诉中标签 - 紫色
.status-appealing {
  background: #722ED1;
  color: white;

  &:hover {
    background: #722ED1;
  }
}

// 申诉驳回标签 - 灰色
.status-appeal-rejected {
  background: #86909c;
  color: white;

  &:hover {
    background: #86909c;
  }
}

// 删除按钮
.delete-btn {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  width: 2rem;
  height: 2rem;
  background-color: rgba(245, 108, 108, 0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s ease;
  box-shadow: 0 0.125rem 0.5rem rgba(0, 0, 0, 0.2);

  &:hover {
    background-color: rgba(245, 108, 108, 1);
    transform: scale(1.1);
  }

  i {
    color: white;
    font-size: 1rem;
  }
}

// 查看审核结果按钮
.review-btn {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  width: 2rem;
  height: 2rem;
  background-color: #C396ED;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s ease;
  box-shadow: 0 0.125rem 0.5rem rgba(0, 0, 0, 0.2);

  &:hover {
    background-color: #7BC0FC;
    transform: scale(1.1);
  }

  i {
    color: white;
    font-size: 1rem;
  }
}

// 申诉按钮
.appeal-btn {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  width: 2rem;
  height: 2rem;
  background-color: #FADC19;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s ease;
  box-shadow: 0 0.125rem 0.5rem rgba(0, 0, 0, 0.2);

  &:hover {
    background-color: #FADC19;
    transform: scale(1.1);
  }

  i {
    color: white;
    font-size: 1rem;
  }
}

// 封面区域
.media-cover {
  position: relative;
  width: 100%;
  padding-top: 56.25%; // 16:9 比例
  overflow: hidden;
  background-color: #f5f5f5;

  .cover-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;

    :deep(img) {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
  }

  &:hover {
    .cover-image :deep(img) {
      transform: scale(1.05);
    }
  }

  // 占位符
  .image-placeholder,
  .image-error {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    color: #909399;
    background-color: #f5f7fa;

    i {
      font-size: 2rem;
    }

    span {
      font-size: 0.8rem;
    }
  }

  .image-error {
    color: #f56c6c;
    background-color: #fef0f0;
  }
}

// 卡片信息区域
.card-info {
  padding: 0.8rem;
  background-color: white;
}

// 标题
.card-title {
  font-size: 0.8rem;
  font-weight: 500;
  color: #303133;
  line-height: 1.4;
  margin-bottom: 0.5rem;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

// 作品描述
.card-description {
  font-size: 0.75rem;
  color: #909399;
  line-height: 1.4;
  margin-bottom: 0.8rem;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

// 底部信息
.card-bottom {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

// 作者信息
.author-info {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.8rem;
  color: #909399;

  .author-avatar {
    flex-shrink: 0;

    :deep(.el-avatar) {
      border: 1px solid #e4e7ed;
    }

    i {
      font-size: 1.5rem;
      color: #c0c4cc;
    }
  }

  .author-icon {
    font-size: 1rem;
    flex-shrink: 0;
  }

  .author-name {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #606266;
  }

  .publish-date {
    color: #c0c4cc;
    flex-shrink: 0;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .card-title {
    font-size: 0.8rem;
  }

  .author-info {
    font-size: 0.7rem;
  }
}
</style>
