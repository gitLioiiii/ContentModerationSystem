<template>
  <div class="video-review-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">视频自动审核</h1>
      <p class="page-description">基于关键帧抽取和AI分析的视频内容自动审核系统</p>
    </div>

    <!-- 审核统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(64, 158, 255, 0.1)">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="#409eff"
            stroke-width="2"
          >
            <polygon points="23 7 16 12 23 17 23 7"></polygon>
            <rect x="1" y="5" width="15" height="14" rx="2" ry="2"></rect>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-label">今日审核总量</div>
          <div class="stat-value">{{ stats.totalToday }}</div>
          <div class="stat-trend">
            <span class="trend-up">↑ 15.7%</span> 较昨日
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(103, 194, 58, 0.1)">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="#67c23a"
            stroke-width="2"
          >
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22 4 12 14.01 9 11.01"></polyline>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-label">自动通过</div>
          <div class="stat-value">{{ stats.autoApproved }}</div>
          <div class="stat-trend">
            <span class="trend-rate">{{ stats.autoApprovedRate }}%</span> 通过率
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(230, 162, 60, 0.1)">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="#e6a23c"
            stroke-width="2"
          >
            <path
              d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"
            ></path>
            <line x1="12" y1="9" x2="12" y2="13"></line>
            <line x1="12" y1="17" x2="12.01" y2="17"></line>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-label">待人工审核</div>
          <div class="stat-value">{{ stats.pendingReview }}</div>
          <div class="stat-trend">
            <span class="trend-rate">{{ stats.pendingReviewRate }}%</span> 疑似违规
          </div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(245, 108, 108, 0.1)">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="#f56c6c"
            stroke-width="2"
          >
            <circle cx="12" cy="12" r="10"></circle>
            <line x1="15" y1="9" x2="9" y2="15"></line>
            <line x1="9" y1="9" x2="15" y2="15"></line>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-label">自动拒绝</div>
          <div class="stat-value">{{ stats.autoRejected }}</div>
          <div class="stat-trend">
            <span class="trend-rate">{{ stats.autoRejectedRate }}%</span> 违规率
          </div>
        </div>
      </div>
    </div>

    <!-- 审核操作区域 -->
    <div class="review-section">
      <ElCard class="review-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">视频内容审核</span>
            <div class="header-actions">
              <ElButton type="primary" @click="showBatchReview = true">批量审核</ElButton>
            </div>
          </div>
        </template>

        <!-- 视频上传区域 -->
        <div class="review-input-section">
          <ElTabs v-model="activeTab" class="review-tabs">
            <!-- 上传视频 -->
            <ElTabPane label="上传视频" name="upload">
              <ElUpload
                v-model:file-list="uploadedVideos"
                class="upload-area"
                drag
                :auto-upload="false"
                :on-change="handleVideoChange"
                :on-remove="handleVideoRemove"
                accept="video/*"
                multiple
                :limit="5"
              >
                <div class="upload-placeholder">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="48"
                    height="48"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="#409eff"
                    stroke-width="2"
                  >
                    <polygon points="23 7 16 12 23 17 23 7"></polygon>
                    <rect x="1" y="5" width="15" height="14" rx="2" ry="2"></rect>
                  </svg>
                  <div class="upload-text">
                    点击或拖拽上传视频
                    <div class="upload-tip">
                      支持 MP4、AVI、MOV 格式，单个视频不超过 500MB，最多上传 5 个
                    </div>
                  </div>
                </div>
              </ElUpload>
            </ElTabPane>

            <!-- 视频URL -->
            <ElTabPane label="视频URL" name="url">
              <div class="url-input-section">
                <ElInput
                  v-model="videoUrl"
                  placeholder="请输入视频URL地址"
                  clearable
                  size="large"
                >
                  <template #prepend>
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <path
                        d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"
                      ></path>
                      <path
                        d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"
                      ></path>
                    </svg>
                  </template>
                </ElInput>
                <ElButton type="primary" size="large" @click="handleAddUrl">添加URL</ElButton>
              </div>
            </ElTabPane>
          </ElTabs>

          <!-- 审核配置 -->
          <div class="review-config">
            <div class="config-title">审核配置</div>
            <div class="config-options">
              <div class="config-item">
                <span class="config-label">抽帧间隔：</span>
                <ElSelect v-model="reviewConfig.frameInterval" size="small" style="width: 120px">
                  <ElOption label="1秒" :value="1"></ElOption>
                  <ElOption label="2秒" :value="2"></ElOption>
                  <ElOption label="5秒" :value="5"></ElOption>
                  <ElOption label="10秒" :value="10"></ElOption>
                </ElSelect>
              </div>
              <div class="config-item">
                <span class="config-label">审核维度：</span>
                <ElCheckboxGroup v-model="reviewConfig.dimensions" size="small">
                  <ElCheckbox label="色情检测"></ElCheckbox>
                  <ElCheckbox label="暴力血腥"></ElCheckbox>
                  <ElCheckbox label="政治敏感"></ElCheckbox>
                  <ElCheckbox label="违禁物品"></ElCheckbox>
                </ElCheckboxGroup>
              </div>
            </div>
          </div>

          <div class="review-actions">
            <ElButton
              type="primary"
              size="large"
              @click="handleReview"
              :loading="reviewing"
              :disabled="uploadedVideos.length === 0 && !videoUrl"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                style="margin-right: 8px"
              >
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
              开始审核
            </ElButton>
            <ElButton size="large" @click="handleClear">清空</ElButton>
          </div>
        </div>

        <!-- 审核进度 -->
        <div v-if="reviewing" class="review-progress">
          <div class="progress-header">
            <span>正在处理视频...</span>
            <span>{{ reviewProgress }}%</span>
          </div>
          <ElProgress :percentage="reviewProgress" :stroke-width="8" status="success" />
          <div class="progress-info">
            <span>已抽取 {{ extractedFrames }} 个关键帧</span>
            <span>{{ reviewingVideo }}</span>
          </div>
        </div>

        <!-- 审核结果 -->
        <div v-if="reviewResults.length > 0" class="review-results">
          <div class="results-header">
            <h3>审核结果</h3>
            <span class="results-summary"
              >已审核 {{ reviewResults.length }} 个视频，通过 {{ passedCount }}，待复核
              {{ pendingCount }}，拒绝 {{ rejectedCount }}</span
            >
          </div>

          <div class="results-list">
            <div
              v-for="(result, index) in reviewResults"
              :key="index"
              class="result-item"
              :class="result.status"
            >
              <!-- 视频预览 -->
              <div class="result-video-section">
                <div class="video-thumbnail">
                  <img :src="result.thumbnail" :alt="result.fileName" />
                  <div class="video-duration">{{ result.duration }}</div>
                  <div class="play-icon">
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="48"
                      height="48"
                      viewBox="0 0 24 24"
                      fill="white"
                    >
                      <polygon points="5 3 19 12 5 21 5 3"></polygon>
                    </svg>
                  </div>
                </div>
              </div>

              <!-- 审核信息 -->
              <div class="result-info-section">
                <div class="result-header-row">
                  <div class="result-filename">{{ result.fileName }}</div>
                  <div class="result-status-badge" :class="result.status">
                    <svg
                      v-if="result.status === 'approved'"
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                      <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                    <svg
                      v-else-if="result.status === 'pending'"
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <path
                        d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"
                      ></path>
                    </svg>
                    <svg
                      v-else
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="15" y1="9" x2="9" y2="15"></line>
                      <line x1="9" y1="9" x2="15" y2="15"></line>
                    </svg>
                    <span>{{ result.statusText }}</span>
                  </div>
                </div>

                <div class="result-details-grid">
                  <div class="detail-item">
                    <span class="detail-label">违规评分：</span>
                    <span class="detail-value" :class="getScoreClass(result.score)">{{
                      result.score
                    }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">视频时长：</span>
                    <span class="detail-value">{{ result.duration }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">抽帧数量：</span>
                    <span class="detail-value">{{ result.frameCount }} 帧</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">审核时间：</span>
                    <span class="detail-value">{{ result.reviewTime }}</span>
                  </div>
                </div>

                <!-- 关键帧预览 -->
                <div class="keyframes-section">
                  <div class="keyframes-header">
                    <span>关键帧预览 ({{ result.keyframes.length }})</span>
                    <ElButton text size="small" @click="handleViewAllFrames(result)"
                      >查看全部</ElButton
                    >
                  </div>
                  <div class="keyframes-grid">
                    <div
                      v-for="(frame, idx) in result.keyframes.slice(0, 6)"
                      :key="idx"
                      class="keyframe-item"
                      :class="{ 'has-violation': frame.hasViolation }"
                    >
                      <img :src="frame.url" :alt="`帧 ${frame.time}`" />
                      <div class="keyframe-info">
                        <span class="keyframe-time">{{ frame.time }}</span>
                        <span v-if="frame.hasViolation" class="keyframe-violation">⚠</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 违规信息 -->
                <div v-if="result.violations.length > 0" class="violations-section">
                  <div class="violations-header">
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="14"
                      height="14"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <path
                        d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"
                      ></path>
                    </svg>
                    <span>检测到 {{ result.violations.length }} 处违规内容</span>
                  </div>
                  <div class="violations-list">
                    <ElTag
                      v-for="(violation, idx) in result.violations"
                      :key="idx"
                      type="danger"
                      size="small"
                      effect="dark"
                    >
                      {{ violation.type }} ({{ violation.time }})
                    </ElTag>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </ElCard>
    </div>

    <!-- 最近审核记录 -->
    <div class="recent-reviews-section">
      <ElCard>
        <template #header>
          <div class="card-header">
            <span class="card-title">最近审核记录</span>
            <ElButton text @click="handleViewAllRecords">查看全部</ElButton>
          </div>
        </template>

        <ElTable :data="recentReviews" style="width: 100%">
          <ElTableColumn prop="id" label="审核ID" width="100" />
          <ElTableColumn label="视频预览" width="120">
            <template #default="{ row }">
              <div class="table-video-preview">
                <img :src="row.thumbnail" class="table-thumbnail" />
                <div class="table-duration">{{ row.duration }}</div>
              </div>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
          <ElTableColumn prop="status" label="审核结果" width="120">
            <template #default="{ row }">
              <ElTag :type="getStatusTagType(row.status)">{{ row.statusText }}</ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="score" label="违规评分" width="100">
            <template #default="{ row }">
              <span :class="getScoreClass(row.score)">{{ row.score }}</span>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="frameCount" label="抽帧数" width="100" />
          <ElTableColumn prop="violationCount" label="违规项" width="100" />
          <ElTableColumn prop="reviewTime" label="审核时间" width="180" />
          <ElTableColumn label="操作" width="150">
            <template #default="{ row }">
              <ElButton text type="primary" size="small" @click="handleViewDetail(row)"
                >详情</ElButton
              >
              <ElButton text type="danger" size="small" @click="handleDelete(row)">删除</ElButton>
            </template>
          </ElTableColumn>
        </ElTable>

        <div class="pagination-wrapper">
          <ElPagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </ElCard>
    </div>

    <!-- 批量审核对话框 -->
    <ElDialog v-model="showBatchReview" title="批量视频审核" width="70%">
      <div class="batch-review-content">
        <ElUpload
          class="upload-area"
          drag
          :auto-upload="false"
          :on-change="handleBatchFileChange"
          accept=".zip,.rar"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="48"
            height="48"
            viewBox="0 0 24 24"
            fill="none"
            stroke="#409eff"
            stroke-width="2"
          >
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="17 8 12 3 7 8"></polyline>
            <line x1="12" y1="3" x2="12" y2="15"></line>
          </svg>
          <div class="el-upload__text">
            将压缩包拖到此处，或<em>点击上传</em>
            <div class="upload-tip">支持 .zip, .rar 格式，单个文件不超过 2GB</div>
          </div>
        </ElUpload>
      </div>
      <template #footer>
        <ElButton @click="showBatchReview = false">取消</ElButton>
        <ElButton type="primary" @click="handleBatchReview">开始批量审核</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ElCard,
  ElButton,
  ElInput,
  ElTable,
  ElTableColumn,
  ElTag,
  ElPagination,
  ElDialog,
  ElUpload,
  ElTabs,
  ElTabPane,
  ElSelect,
  ElOption,
  ElCheckbox,
  ElCheckboxGroup,
  ElProgress,
} from 'element-plus'

// 统计数据
const stats = reactive({
  totalToday: 3654,
  autoApproved: 3189,
  autoApprovedRate: 87.3,
  pendingReview: 412,
  pendingReviewRate: 11.3,
  autoRejected: 53,
  autoRejectedRate: 1.4,
})

// 上传的视频
const activeTab = ref('upload')
const uploadedVideos = ref([])
const videoUrl = ref('')
const reviewing = ref(false)
const reviewProgress = ref(0)
const extractedFrames = ref(0)
const reviewingVideo = ref('')
const reviewResults = ref([])

// 审核配置
const reviewConfig = reactive({
  frameInterval: 2,
  dimensions: ['色情检测', '暴力血腥', '政治敏感'],
})

// 批量审核
const showBatchReview = ref(false)

// 计算通过、待审、拒绝数量
const passedCount = computed(() => {
  return reviewResults.value.filter((r) => r.status === 'approved').length
})

const pendingCount = computed(() => {
  return reviewResults.value.filter((r) => r.status === 'pending').length
})

const rejectedCount = computed(() => {
  return reviewResults.value.filter((r) => r.status === 'rejected').length
})

// 最近审核记录
const recentReviews = ref([
  {
    id: 'VR20250001',
    thumbnail: 'https://picsum.photos/200/120?random=11',
    fileName: 'user_video_123.mp4',
    duration: '02:35',
    status: 'approved',
    statusText: '自动通过',
    score: 18,
    frameCount: 78,
    violationCount: 0,
    reviewTime: '2025-01-16 14:30:25',
  },
  {
    id: 'VR20250002',
    thumbnail: 'https://picsum.photos/200/120?random=12',
    fileName: 'post_video_456.avi',
    duration: '01:45',
    status: 'pending',
    statusText: '待人工审核',
    score: 72,
    frameCount: 53,
    violationCount: 3,
    reviewTime: '2025-01-16 14:28:12',
  },
  {
    id: 'VR20250003',
    thumbnail: 'https://picsum.photos/200/120?random=13',
    fileName: 'comment_video_789.mov',
    duration: '00:58',
    status: 'rejected',
    statusText: '自动拒绝',
    score: 94,
    frameCount: 29,
    violationCount: 8,
    reviewTime: '2025-01-16 14:25:45',
  },
])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 156,
})

// 视频上传处理
const handleVideoChange = (file) => {
  console.log('上传视频:', file)
}

const handleVideoRemove = (file) => {
  console.log('移除视频:', file)
}

const handleAddUrl = () => {
  if (!videoUrl.value.trim()) {
    ElMessage.warning('请输入视频URL')
    return
  }
  ElMessage.success('视频URL已添加')
  videoUrl.value = ''
}

const handleClear = () => {
  uploadedVideos.value = []
  videoUrl.value = ''
}

// 审核处理
const handleReview = async () => {
  if (uploadedVideos.value.length === 0 && !videoUrl.value) {
    ElMessage.warning('请先上传视频或输入视频URL')
    return
  }

  reviewing.value = true
  reviewProgress.value = 0
  extractedFrames.value = 0

  // 模拟审核进度
  const progressInterval = setInterval(() => {
    reviewProgress.value += 5
    extractedFrames.value += Math.floor(Math.random() * 3) + 1

    if (reviewProgress.value >= 100) {
      clearInterval(progressInterval)
      reviewing.value = false

      // 生成审核结果
      reviewResults.value = uploadedVideos.value.map((file, index) => {
        const score = Math.floor(Math.random() * 100)
        let status, statusText, violations

        const keyframes = Array.from({ length: 12 }, (_, i) => ({
          url: `https://picsum.photos/150/100?random=${index * 12 + i}`,
          time: `${String(Math.floor((i * 10) / 60)).padStart(2, '0')}:${String((i * 10) % 60).padStart(2, '0')}`,
          hasViolation: score > 60 && Math.random() > 0.7,
        }))

        if (score < 60) {
          status = 'approved'
          statusText = '自动通过'
          violations = []
        } else if (score < 80) {
          status = 'pending'
          statusText = '待人工审核'
          violations = [
            { type: '疑似暴露镜头', time: '00:25' },
            { type: '不当画面', time: '01:15' },
          ]
        } else {
          status = 'rejected'
          statusText = '自动拒绝'
          violations = [
            { type: '色情内容', time: '00:15' },
            { type: '暴力血腥', time: '00:45' },
            { type: '违禁物品', time: '01:20' },
          ]
        }

        return {
          fileName: file.name,
          thumbnail: `https://picsum.photos/320/180?random=${index + 20}`,
          duration: `0${Math.floor(Math.random() * 5)}:${String(Math.floor(Math.random() * 60)).padStart(2, '0')}`,
          status,
          statusText,
          score,
          frameCount: keyframes.length,
          keyframes,
          violations,
          reviewTime: new Date().toLocaleString('zh-CN'),
        }
      })

      ElMessage.success(`审核完成，共 ${uploadedVideos.value.length} 个视频`)
    }
  }, 200)

  reviewingVideo.value = uploadedVideos.value[0]?.name || '视频审核中...'
}

// 查看所有关键帧
const handleViewAllFrames = (result) => {
  ElMessage.info(`查看视频 ${result.fileName} 的所有关键帧`)
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const types = {
    approved: 'success',
    pending: 'warning',
    rejected: 'danger',
  }
  return types[status] || 'info'
}

// 获取评分样式类
const getScoreClass = (score) => {
  if (score < 60) return 'score-safe'
  if (score < 80) return 'score-warning'
  return 'score-danger'
}

// 查看详情
const handleViewDetail = (row) => {
  ElMessage.info(`查看审核记录 ${row.id} 的详情`)
}

// 删除记录
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除审核记录 ${row.id} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    ElMessage.success('删除成功')
  })
}

// 查看全部记录
const handleViewAllRecords = () => {
  ElMessage.info('跳转到全部审核记录页面')
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
}

// 批量审核
const handleBatchFileChange = (file) => {
  console.log('选择的压缩包:', file)
}

const handleBatchReview = () => {
  ElMessage.success('批量审核任务已提交，请稍后查看结果')
  showBatchReview.value = false
}
</script>

<style scoped>
.video-review-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 50px);
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-description {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  color: #606266;
}

.trend-up {
  color: #67c23a;
  font-weight: 500;
}

.trend-rate {
  color: #409eff;
  font-weight: 500;
}

/* 审核区域 */
.review-section {
  margin-bottom: 24px;
}

.review-card {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.review-input-section {
  margin-bottom: 24px;
}

.review-tabs {
  margin-bottom: 20px;
}

.upload-area {
  width: 100%;
  margin-bottom: 16px;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.upload-text {
  margin-top: 16px;
  font-size: 16px;
  color: #606266;
  text-align: center;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.url-input-section {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 审核配置 */
.review-config {
  margin: 20px 0;
  padding: 16px;
  background: #f9fafc;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.config-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.config-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.config-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.config-label {
  font-size: 14px;
  color: #606266;
  min-width: 80px;
}

.review-actions {
  display: flex;
  gap: 12px;
}

/* 审核进度 */
.review-progress {
  margin-top: 24px;
  padding: 20px;
  background: #f9fafc;
  border-radius: 8px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
}

/* 审核结果 */
.review-results {
  margin-top: 24px;
  padding: 20px;
  background: #f9fafc;
  border-radius: 8px;
}

.results-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.results-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.results-summary {
  font-size: 14px;
  color: #909399;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-item {
  display: flex;
  gap: 20px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.result-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.result-video-section {
  flex-shrink: 0;
  width: 320px;
}

.video-thumbnail {
  position: relative;
  width: 100%;
  height: 180px;
  background: #f5f7fa;
  overflow: hidden;
}

.video-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.video-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  padding: 4px 8px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 12px;
  border-radius: 4px;
}

.play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
}

.play-icon:hover {
  background: rgba(0, 0, 0, 0.7);
  transform: translate(-50%, -50%) scale(1.1);
}

.result-info-section {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-filename {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.result-status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
  color: white;
}

.result-status-badge.approved {
  background: #67c23a;
}

.result-status-badge.pending {
  background: #e6a23c;
}

.result-status-badge.rejected {
  background: #f56c6c;
}

.result-details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
}

.detail-item {
  font-size: 13px;
}

.detail-label {
  color: #909399;
}

.detail-value {
  color: #303133;
  font-weight: 500;
  margin-left: 4px;
}

.score-safe {
  color: #67c23a;
  font-weight: 600;
}

.score-warning {
  color: #e6a23c;
  font-weight: 600;
}

.score-danger {
  color: #f56c6c;
  font-weight: 600;
}

/* 关键帧 */
.keyframes-section {
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.keyframes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.keyframes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 8px;
}

.keyframe-item {
  position: relative;
  border-radius: 4px;
  overflow: hidden;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.keyframe-item:hover {
  border-color: #409eff;
  transform: scale(1.05);
}

.keyframe-item.has-violation {
  border-color: #f56c6c;
}

.keyframe-item img {
  width: 100%;
  height: 60px;
  object-fit: cover;
}

.keyframe-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 6px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  color: white;
  font-size: 11px;
}

.keyframe-violation {
  color: #f56c6c;
  font-size: 14px;
}

/* 违规信息 */
.violations-section {
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.violations-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #f56c6c;
  margin-bottom: 8px;
}

.violations-list {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

/* 表格预览 */
.table-video-preview {
  position: relative;
  width: 100px;
  height: 60px;
}

.table-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.table-duration {
  position: absolute;
  bottom: 4px;
  right: 4px;
  padding: 2px 4px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  font-size: 10px;
  border-radius: 2px;
}

/* 最近记录 */
.recent-reviews-section {
  margin-bottom: 24px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 批量审核 */
.batch-review-content {
  padding: 20px 0;
}

/* 响应式 */
@media (max-width: 1024px) {
  .result-item {
    flex-direction: column;
  }

  .result-video-section {
    width: 100%;
  }

  .keyframes-grid {
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .result-details-grid {
    grid-template-columns: 1fr;
  }
}
</style>
