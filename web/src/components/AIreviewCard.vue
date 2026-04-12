<template>
  <!-- AI审核结果抽屉 -->
  <ElDrawer
    v-model="drawerVisible"
    :with-header="true"
    :z-index="100"
    direction="rtl"
    size="40%"
    @close="handleClose"
  >
    <template #header>
      <h3>AI审核结果</h3>
    </template>

    <div v-if="reviewData" class="review-content">
      <ElSpace direction="vertical" alignment="normal" :fill="true" style="width: 100%">
        <!-- 总体审核结果 -->
        <ElCard class="review-card review-card-summary" shadow="hover" :body-style="{ backgroundColor: '#e3f2fd' }">
          <h3 class="section-title">作品《{{ workTitle }}》的AI审核结果为
            <ElTag :type="getStatusType(reviewData.status)" size="large">
              {{ getStatusText(reviewData.status) }}
            </ElTag>
          </h3>
          <div class="processing-time">
            <img src="@/assets/img/时间.png" alt="时间" class="time-icon" />
            AI审核时长: {{ reviewData.finalProcessingTime }}秒
          </div>
        </ElCard>

        <!-- 文本审核 -->
        <ElCard v-if="reviewData.reviewResults?.textReview" class="review-card review-card-text" shadow="hover" :body-style="{ backgroundColor: '#f3e5f5' }">
          <h3 class="section-title">
            <i class="bi bi-file-text"></i>
            文本审核（标题 + 描述）
          </h3>
          <div class="review-item">
            <span class="label">审核结果:</span>
            <ElTag :type="getResultType(reviewData.reviewResults.textReview.result)">
              {{ reviewData.reviewResults.textReview.result }}
            </ElTag>
          </div>
          <div class="review-item">
            <span class="label">审核理由:</span>
            <span>{{ reviewData.reviewResults.textReview.reason }}</span>
          </div>
          <div class="review-item">
            <span class="label">风险等级:</span>
            <ElTag :type="getRiskType(reviewData.reviewResults.textReview.riskLevel)">
              {{ reviewData.reviewResults.textReview.riskLevel }}
            </ElTag>
          </div>
          <div v-if="reviewData.reviewResults.textReview.sensitiveWords?.length > 0" class="review-item">
            <span class="label">敏感词:</span>
            <div class="sensitive-words">
              <ElTag
                v-for="(word, index) in reviewData.reviewResults.textReview.sensitiveWords"
                :key="index"
                type="danger"
                size="small"
              >
                {{ word }}
              </ElTag>
            </div>
          </div>
          <div class="review-item">
            <span class="label">处理耗时:</span>
            <span>{{ reviewData.reviewResults.textReview.processingTime }}秒</span>
          </div>
        </ElCard>

        <!-- 封面审核 -->
        <ElCard v-if="reviewData.reviewResults?.imageReview" class="review-card review-card-image" shadow="hover" :body-style="{ backgroundColor: '#e8f5e9' }">
          <h3 class="section-title">
            <i class="bi bi-image"></i>
            封面审核
          </h3>
          <div class="review-item">
            <span class="label">审核结果:</span>
            <ElTag :type="getResultType(reviewData.reviewResults.imageReview.result)">
              {{ reviewData.reviewResults.imageReview.result }}
            </ElTag>
          </div>
          <div class="review-item">
            <span class="label">审核理由:</span>
            <span>{{ reviewData.reviewResults.imageReview.reason }}</span>
          </div>
          <div class="review-item">
            <span class="label">违规匹配分数:</span>
            <span>{{ reviewData.reviewResults.imageReview.matchScore.toFixed(1) }}/100</span>
          </div>
          <div class="review-item">
            <span class="label">处理耗时:</span>
            <span>{{ reviewData.reviewResults.imageReview.processingTime }}秒</span>
          </div>
        </ElCard>

        <!-- 视频审核 -->
        <ElCard v-if="reviewData.reviewResults?.videoReview" class="review-card review-card-video" shadow="hover" :body-style="{ backgroundColor: '#fff3e0' }">
          <h3 class="section-title">
            <i class="bi bi-camera-video"></i>
            视频审核
          </h3>
          <div class="review-item">
            <span class="label">审核结果:</span>
            <ElTag :type="getResultType(reviewData.reviewResults.videoReview.result)">
              {{ reviewData.reviewResults.videoReview.result }}
            </ElTag>
          </div>
          <div class="review-item">
            <span class="label">审核理由:</span>
            <span>{{ reviewData.reviewResults.videoReview.reason }}</span>
          </div>
          <div class="review-item">
            <span>
              风险帧数为 {{ reviewData.reviewResults.videoReview.riskyCount }}
            </span>
          </div>
          <div class="review-item">
            <span class="label">最高风险分数:</span>
            <span>{{ reviewData.reviewResults.videoReview.maxScore.toFixed(1) }}/100</span>
          </div>
          <div v-if="reviewData.reviewResults.videoReview.riskyList?.length > 0" class="review-item">
            <span class="label">风险帧详情:</span>
            <div class="risky-frames">
              <div
                v-for="(frame, index) in reviewData.reviewResults.videoReview.riskyList"
                :key="index"
                class="risky-frame-item"
              >
                <div class="frame-info">
                  <span class="frame-index">风险帧{{ index + 1 }}</span>
                  <span class="frame-time">时间: {{ frame.timestamp.toFixed(2) }}秒</span>
                  <ElTag type="danger" size="small">分数: {{ frame.score.toFixed(1) }}/100</ElTag>
                </div>
                <div class="frame-reason">{{ frame.reason }}</div>
              </div>
            </div>
          </div>
          <div class="review-item">
            <span class="label">处理耗时:</span>
            <span>{{ reviewData.reviewResults.videoReview.processingTime }}秒</span>
          </div>
        </ElCard>

        <!-- 审核时间 -->
        <ElCard class="review-card review-card-time" shadow="hover" :body-style="{ backgroundColor: '#f5f5f5' }">
          <h3 class="section-title">
            <i class="bi bi-calendar"></i>
            审核时间
          </h3>
          <div class="review-item">
            <span>{{ formatDateTime(reviewData.reviewedAt) }}</span>
          </div>
        </ElCard>
      </ElSpace>
    </div>

    <!-- 如果没有审核过 -->
    <div v-else class="no-review">
      <i class="bi bi-exclamation-circle"></i>
      <p>暂无审核结果</p>
    </div>
  </ElDrawer>
</template>

<script setup>
import { computed } from 'vue'
import { ElDrawer, ElTag, ElSpace, ElCard, } from 'element-plus'
import 'bootstrap-icons/font/bootstrap-icons.css'

// Props
const props = defineProps({
  drawerVisible: {
    type: Boolean,
    default: false
  },
  reviewData: {
    type: Object,
    default: null
  },
  workTitle: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:drawerVisible'])

const drawerVisible = computed({
  get: () => props.drawerVisible, // 从父组件获取 drawerVisible 的值
  set: (value) => emit('update:drawerVisible', value) // 当值改变时，通知父组件更新
})

const handleClose = () => {
  emit('update:drawerVisible', false)
}

// 获取状态类型（包含所有状态）
const getStatusType = (status) => {
  switch (status) {
    case 'approved':
      return 'success'
    case 'rejected':
      return 'danger'
    case 'reviewing':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'approved':
      return '已通过'
    case 'rejected':
      return '已拒绝'
    case 'reviewing':
      return '待人工审核'
    default:
      return '未知状态'
  }
}

// 获取审核结果类型
const getResultType = (result) => {
  if (result === '通过') return 'success'
  if (result === '拒绝' || result === '不通过') return 'danger'
  if (result === '人工审核' || result === '需人工审核') return 'warning'
  return 'info'
}

// 获取风险等级类型
const getRiskType = (riskLevel) => {
  if (riskLevel === '低') return 'success'
  if (riskLevel === '中') return 'warning'
  if (riskLevel === '高') return 'danger'
  return 'info'
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '未知'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}
</script>

<style scoped lang="scss">
.review-content {
  padding: 0 8px;
}

.review-card {
  margin-bottom: 16px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    gap: 8px;

    i {
      font-size: 18px;
    }
  }

  .review-item {
    margin-bottom: 12px;
    line-height: 1.6;

    &:last-child {
      margin-bottom: 0;
    }

    .sensitive-words {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 8px;
    }

    .risky-frames {
      margin-top: 12px;

      .risky-frame-item {
        margin-bottom: 12px;
        padding-bottom: 12px;
        border-bottom: 1px solid #e4e7ed;

        &:last-child {
          margin-bottom: 0;
          padding-bottom: 0;
          border-bottom: none;
        }

        .frame-info {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 8px;
          flex-wrap: wrap;

          .frame-index {
            font-weight: 600;
            color: #303133;
          }

          .frame-time {
            color: #606266;
          }
        }

        .frame-reason {
          color: #606266;
          font-size: 14px;
        }
      }
    }
  }
}

.no-review {
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
    margin: 0;
  }
}

.processing-time {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  margin-top: 12px;
}

.time-icon {
  width: 1.5rem;
  height: 1.5rem;
}
</style>
