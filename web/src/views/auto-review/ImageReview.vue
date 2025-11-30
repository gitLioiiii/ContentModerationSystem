<template>
  <div class="image-review-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">图像自动审核</h1>
      <p class="page-description">基于AI识别的色情、暴力、政治敏感图像内容审核系统</p>
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
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
            <circle cx="8.5" cy="8.5" r="1.5"></circle>
            <polyline points="21 15 16 10 5 21"></polyline>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-label">今日审核总量</div>
          <div class="stat-value">{{ stats.totalToday }}</div>
          <div class="stat-trend">
            <span class="trend-up">↑ 18.3%</span> 较昨日
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
            <span class="card-title">图像内容审核</span>
            <div class="header-actions">
              <ElButton type="primary" @click="showBatchReview = true">批量审核</ElButton>
            </div>
          </div>
        </template>

        <!-- 图像上传区域 -->
        <div class="review-input-section">
          <ElUpload
            v-model:file-list="uploadedImages"
            class="upload-area"
            drag
            :auto-upload="false"
            :on-change="handleImageChange"
            :on-remove="handleImageRemove"
            accept="image/*"
            multiple
            list-type="picture-card"
            :limit="10"
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
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                <circle cx="8.5" cy="8.5" r="1.5"></circle>
                <polyline points="21 15 16 10 5 21"></polyline>
              </svg>
              <div class="upload-text">
                点击或拖拽上传图片
                <div class="upload-tip">
                  支持 JPG、PNG、GIF 格式，单张图片不超过 5MB，最多上传 10 张
                </div>
              </div>
            </div>
          </ElUpload>

          <div class="review-actions">
            <ElButton
              type="primary"
              size="large"
              @click="handleReview"
              :loading="reviewing"
              :disabled="uploadedImages.length === 0"
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
              开始审核 ({{ uploadedImages.length }})
            </ElButton>
            <ElButton size="large" @click="uploadedImages = []">清空</ElButton>
          </div>
        </div>

        <!-- 审核结果 -->
        <div v-if="reviewResults.length > 0" class="review-results">
          <div class="results-header">
            <h3>审核结果</h3>
            <span class="results-summary"
              >已审核 {{ reviewResults.length }} 张图片，通过 {{ passedCount }}，待复核
              {{ pendingCount }}，拒绝 {{ rejectedCount }}</span
            >
          </div>

          <div class="results-grid">
            <div
              v-for="(result, index) in reviewResults"
              :key="index"
              class="result-item"
              :class="result.status"
            >
              <div class="result-image-wrapper">
                <img :src="result.imageUrl" :alt="result.fileName" class="result-image" />
                <div class="result-overlay">
                  <div class="result-status-badge" :class="result.status">
                    <svg
                      v-if="result.status === 'approved'"
                      xmlns="http://www.w3.org/2000/svg"
                      width="20"
                      height="20"
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
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <path
                        d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"
                      ></path>
                      <line x1="12" y1="9" x2="12" y2="13"></line>
                      <line x1="12" y1="17" x2="12.01" y2="17"></line>
                    </svg>
                    <svg
                      v-else
                      xmlns="http://www.w3.org/2000/svg"
                      width="20"
                      height="20"
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
              </div>

              <div class="result-details">
                <div class="result-filename">{{ result.fileName }}</div>
                <div class="result-info">
                  <span class="info-item">评分: <strong :class="getScoreClass(result.score)">{{ result.score }}</strong></span>
                  <span class="info-item">{{ result.reviewTime }}</span>
                </div>

                <!-- 识别标签 -->
                <div class="result-tags">
                  <ElTag
                    v-for="(tag, idx) in result.detectionTags"
                    :key="idx"
                    :type="tag.type"
                    size="small"
                    effect="dark"
                  >
                    {{ tag.label }}: {{ tag.confidence }}%
                  </ElTag>
                </div>

                <!-- 违规区域标注 -->
                <div v-if="result.violations.length > 0" class="violations-info">
                  <div class="violations-title">检测到 {{ result.violations.length }} 处违规</div>
                  <div class="violations-tags">
                    <ElTag
                      v-for="(violation, idx) in result.violations"
                      :key="idx"
                      type="danger"
                      size="small"
                    >
                      {{ violation }}
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
          <ElTableColumn label="图片预览" width="100">
            <template #default="{ row }">
              <img :src="row.imageUrl" class="table-image" @click="handlePreview(row)" />
            </template>
          </ElTableColumn>
          <ElTableColumn prop="fileName" label="文件名" min-width="150" show-overflow-tooltip />
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
          <ElTableColumn label="识别标签" min-width="200">
            <template #default="{ row }">
              <ElTag
                v-for="(tag, index) in row.tags"
                :key="index"
                size="small"
                style="margin-right: 4px"
                >{{ tag }}</ElTag
              >
            </template>
          </ElTableColumn>
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
    <ElDialog v-model="showBatchReview" title="批量图像审核" width="70%">
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
            <div class="upload-tip">支持 .zip, .rar 格式，单个文件不超过 100MB</div>
          </div>
        </ElUpload>
      </div>
      <template #footer>
        <ElButton @click="showBatchReview = false">取消</ElButton>
        <ElButton type="primary" @click="handleBatchReview">开始批量审核</ElButton>
      </template>
    </ElDialog>

    <!-- 图片预览对话框 -->
    <ElDialog v-model="showPreview" title="图片预览" width="60%">
      <div v-if="previewImage" class="preview-content">
        <img :src="previewImage.imageUrl" class="preview-image" />
      </div>
    </ElDialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ElCard,
  ElButton,
  ElTable,
  ElTableColumn,
  ElTag,
  ElPagination,
  ElDialog,
  ElUpload,
} from 'element-plus'

// 统计数据
const stats = reactive({
  totalToday: 8934,
  autoApproved: 7845,
  autoApprovedRate: 87.8,
  pendingReview: 967,
  pendingReviewRate: 10.8,
  autoRejected: 122,
  autoRejectedRate: 1.4,
})

// 上传的图片
const uploadedImages = ref([])
const reviewing = ref(false)
const reviewResults = ref([])

// 批量审核
const showBatchReview = ref(false)

// 图片预览
const showPreview = ref(false)
const previewImage = ref(null)

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
    id: 'IR20250001',
    imageUrl: 'https://picsum.photos/200/200?random=1',
    fileName: 'user_avatar_123.jpg',
    status: 'approved',
    statusText: '自动通过',
    score: 12,
    tags: ['正常', '人物', '头像'],
    reviewTime: '2025-01-16 14:30:25',
  },
  {
    id: 'IR20250002',
    imageUrl: 'https://picsum.photos/200/200?random=2',
    fileName: 'post_image_456.png',
    status: 'pending',
    statusText: '待人工审核',
    score: 68,
    tags: ['疑似色情', '暴露'],
    reviewTime: '2025-01-16 14:28:12',
  },
  {
    id: 'IR20250003',
    imageUrl: 'https://picsum.photos/200/200?random=3',
    fileName: 'comment_pic_789.jpg',
    status: 'rejected',
    statusText: '自动拒绝',
    score: 92,
    tags: ['色情', '暴力'],
    reviewTime: '2025-01-16 14:25:45',
  },
])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 189,
})

// 图片上传处理
const handleImageChange = (file) => {
  console.log('上传图片:', file)
}

const handleImageRemove = (file) => {
  console.log('移除图片:', file)
}

// 审核处理
const handleReview = async () => {
  if (uploadedImages.value.length === 0) {
    ElMessage.warning('请先上传图片')
    return
  }

  reviewing.value = true

  // 模拟审核过程
  setTimeout(() => {
    reviewResults.value = uploadedImages.value.map((file, index) => {
      const score = Math.floor(Math.random() * 100)
      let status, statusText, violations, detectionTags

      if (score < 60) {
        status = 'approved'
        statusText = '自动通过'
        violations = []
        detectionTags = [
          { label: '正常内容', confidence: 98, type: 'success' },
          { label: '风景', confidence: 85, type: 'info' },
        ]
      } else if (score < 80) {
        status = 'pending'
        statusText = '待人工审核'
        violations = ['疑似暴露', '不当画面']
        detectionTags = [
          { label: '疑似色情', confidence: 72, type: 'warning' },
          { label: '人物', confidence: 88, type: 'info' },
        ]
      } else {
        status = 'rejected'
        statusText = '自动拒绝'
        violations = ['色情内容', '暴力血腥', '违禁物品']
        detectionTags = [
          { label: '色情', confidence: 95, type: 'danger' },
          { label: '暴力', confidence: 89, type: 'danger' },
        ]
      }

      return {
        fileName: file.name,
        imageUrl: URL.createObjectURL(file.raw),
        status,
        statusText,
        score,
        reviewTime: new Date().toLocaleString('zh-CN'),
        detectionTags,
        violations,
      }
    })

    reviewing.value = false
    ElMessage.success(`审核完成，共 ${uploadedImages.value.length} 张图片`)
  }, 2000)
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

// 预览图片
const handlePreview = (row) => {
  previewImage.value = row
  showPreview.value = true
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
.image-review-container {
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

.review-actions {
  display: flex;
  gap: 12px;
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

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.result-item {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.result-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.result-image-wrapper {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f5f7fa;
}

.result-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.result-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.3), transparent);
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 12px;
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
  background: rgba(103, 194, 58, 0.9);
}

.result-status-badge.pending {
  background: rgba(230, 162, 60, 0.9);
}

.result-status-badge.rejected {
  background: rgba(245, 108, 108, 0.9);
}

.result-details {
  padding: 16px;
}

.result-filename {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
}

.info-item strong {
  font-weight: 600;
}

.result-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.violations-info {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.violations-title {
  font-size: 12px;
  font-weight: 500;
  color: #f56c6c;
  margin-bottom: 8px;
}

.violations-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
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

/* 表格图片 */
.table-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.table-image:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
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

/* 预览对话框 */
.preview-content {
  display: flex;
  justify-content: center;
  align-items: center;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
}

/* 批量审核 */
.batch-review-content {
  padding: 20px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .results-grid {
    grid-template-columns: 1fr;
  }
}
</style>
