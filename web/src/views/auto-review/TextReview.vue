<template>
  <div class="text-review-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">文本自动审核</h1>
      <p class="page-description">基于敏感词过滤和违规语义识别的文本内容自动审核系统</p>
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
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
            <polyline points="14 2 14 8 20 8"></polyline>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-label">今日审核总量</div>
          <div class="stat-value">{{ stats.totalToday }}</div>
          <div class="stat-trend">
            <span class="trend-up">↑ 12.5%</span> 较昨日
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
            <span class="card-title">文本内容审核</span>
            <ElButton type="primary" @click="showBatchReview = true">批量审核</ElButton>
          </div>
        </template>

        <!-- 文本输入区域 -->
        <div class="review-input-section">
          <ElInput
            v-model="reviewText"
            type="textarea"
            :rows="6"
            placeholder="请输入需要审核的文本内容，支持多行文本..."
            maxlength="5000"
            show-word-limit
          />

          <div class="review-actions">
            <ElButton type="primary" size="large" @click="handleReview" :loading="reviewing">
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
            <ElButton size="large" @click="reviewText = ''">清空</ElButton>
          </div>
        </div>

        <!-- 审核结果 -->
        <div v-if="reviewResult" class="review-result">
          <div class="result-header">
            <div class="result-status" :class="reviewResult.status">
              <svg
                v-if="reviewResult.status === 'approved'"
                xmlns="http://www.w3.org/2000/svg"
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
              <svg
                v-else-if="reviewResult.status === 'pending'"
                xmlns="http://www.w3.org/2000/svg"
                width="24"
                height="24"
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
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="15" y1="9" x2="9" y2="15"></line>
                <line x1="9" y1="9" x2="15" y2="15"></line>
              </svg>
              <span class="status-text">{{ reviewResult.statusText }}</span>
            </div>
            <div class="result-score">
              <span class="score-label">违规评分</span>
              <span class="score-value" :class="getScoreClass(reviewResult.score)">{{
                reviewResult.score
              }}</span>
            </div>
          </div>

          <div class="result-details">
            <div class="detail-item">
              <span class="detail-label">审核时间：</span>
              <span class="detail-value">{{ reviewResult.reviewTime }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">文本长度：</span>
              <span class="detail-value">{{ reviewResult.textLength }} 字符</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">检测维度：</span>
              <span class="detail-value">{{ reviewResult.dimensions }}</span>
            </div>
          </div>

          <!-- 违规词汇列表 -->
          <div v-if="reviewResult.violations.length > 0" class="violations-section">
            <div class="violations-header">
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
                  d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"
                ></path>
              </svg>
              <span>检测到 {{ reviewResult.violations.length }} 处违规内容</span>
            </div>
            <div class="violations-list">
              <div
                v-for="(violation, index) in reviewResult.violations"
                :key="index"
                class="violation-item"
              >
                <ElTag :type="getViolationTagType(violation.level)" effect="dark">{{
                  violation.level
                }}</ElTag>
                <span class="violation-word">{{ violation.word }}</span>
                <span class="violation-type">{{ violation.type }}</span>
                <span class="violation-position">位置: {{ violation.position }}</span>
              </div>
            </div>
          </div>

          <!-- 语义分析 -->
          <div class="semantic-analysis">
            <div class="analysis-title">语义分析结果</div>
            <div class="analysis-tags">
              <ElTag
                v-for="tag in reviewResult.semanticTags"
                :key="tag"
                type="info"
                effect="plain"
                >{{ tag }}</ElTag
              >
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
          <ElTableColumn prop="content" label="文本内容" min-width="200" show-overflow-tooltip />
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
    <ElDialog v-model="showBatchReview" title="批量文本审核" width="70%">
      <div class="batch-review-content">
        <ElUpload
          class="upload-area"
          drag
          :auto-upload="false"
          :on-change="handleFileChange"
          accept=".txt,.csv,.json"
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
            将文件拖到此处，或<em>点击上传</em>
            <div class="upload-tip">支持 .txt, .csv, .json 格式，单个文件不超过 10MB</div>
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
import { ref, reactive } from 'vue'
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
} from 'element-plus'

// 统计数据
const stats = reactive({
  totalToday: 12456,
  autoApproved: 11210,
  autoApprovedRate: 90,
  pendingReview: 1043,
  pendingReviewRate: 8.4,
  autoRejected: 203,
  autoRejectedRate: 1.6,
})

// 审核文本
const reviewText = ref('')
const reviewing = ref(false)
const reviewResult = ref(null)

// 批量审核
const showBatchReview = ref(false)

// 最近审核记录
const recentReviews = ref([
  {
    id: 'TR20250001',
    content: '这是一条测试文本内容，包含正常的社交媒体发言...',
    status: 'approved',
    statusText: '自动通过',
    score: 15,
    violationCount: 0,
    reviewTime: '2025-01-16 14:30:25',
  },
  {
    id: 'TR20250002',
    content: '疑似包含敏感词汇的文本内容需要人工审核...',
    status: 'pending',
    statusText: '待人工审核',
    score: 75,
    violationCount: 2,
    reviewTime: '2025-01-16 14:28:12',
  },
  {
    id: 'TR20250003',
    content: '严重违规的文本内容已被系统自动拒绝...',
    status: 'rejected',
    statusText: '自动拒绝',
    score: 95,
    violationCount: 5,
    reviewTime: '2025-01-16 14:25:45',
  },
])

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 245,
})

// 审核处理
const handleReview = async () => {
  if (!reviewText.value.trim()) {
    ElMessage.warning('请输入需要审核的文本内容')
    return
  }

  reviewing.value = true

  // 模拟审核过程
  setTimeout(() => {
    // 模拟审核结果
    const score = Math.floor(Math.random() * 100)
    let status, statusText, violations

    if (score < 60) {
      status = 'approved'
      statusText = '自动通过'
      violations = []
    } else if (score < 80) {
      status = 'pending'
      statusText = '待人工审核'
      violations = [
        { level: '中度', word: '敏感词1', type: '政治敏感', position: '第15-20字符' },
        { level: '轻度', word: '敏感词2', type: '不当言论', position: '第45-50字符' },
      ]
    } else {
      status = 'rejected'
      statusText = '自动拒绝'
      violations = [
        { level: '严重', word: '违禁词1', type: '暴力血腥', position: '第10-15字符' },
        { level: '严重', word: '违禁词2', type: '色情低俗', position: '第30-35字符' },
        { level: '中度', word: '敏感词3', type: '政治敏感', position: '第60-65字符' },
      ]
    }

    reviewResult.value = {
      status,
      statusText,
      score,
      reviewTime: new Date().toLocaleString('zh-CN'),
      textLength: reviewText.value.length,
      dimensions: '敏感词、语义分析、情感倾向',
      violations,
      semanticTags: ['社交媒体', '用户评论', '中性情感'],
    }

    reviewing.value = false

    if (status === 'approved') {
      ElMessage.success('审核通过')
    } else if (status === 'pending') {
      ElMessage.warning('检测到疑似违规内容，建议人工审核')
    } else {
      ElMessage.error('检测到严重违规内容，已自动拒绝')
    }
  }, 1500)
}

// 获取违规标签类型
const getViolationTagType = (level) => {
  const types = {
    轻度: 'warning',
    中度: 'danger',
    严重: 'danger',
  }
  return types[level] || 'info'
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
  console.log('每页显示', val, '条')
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  console.log('当前页', val)
}

// 批量审核
const handleFileChange = (file) => {
  console.log('选择的文件:', file)
}

const handleBatchReview = () => {
  ElMessage.success('批量审核任务已提交，请稍后查看结果')
  showBatchReview.value = false
}
</script>

<style scoped>
.text-review-container {
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

.review-input-section {
  margin-bottom: 24px;
}

.review-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

/* 审核结果 */
.review-result {
  margin-top: 24px;
  padding: 20px;
  background: #f9fafc;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.result-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-status.approved {
  color: #67c23a;
}

.result-status.pending {
  color: #e6a23c;
}

.result-status.rejected {
  color: #f56c6c;
}

.status-text {
  font-size: 18px;
  font-weight: 600;
}

.result-score {
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-label {
  font-size: 14px;
  color: #909399;
}

.score-value {
  font-size: 24px;
  font-weight: 600;
}

.score-safe {
  color: #67c23a;
}

.score-warning {
  color: #e6a23c;
}

.score-danger {
  color: #f56c6c;
}

.result-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.detail-item {
  font-size: 14px;
}

.detail-label {
  color: #909399;
}

.detail-value {
  color: #303133;
  font-weight: 500;
  margin-left: 4px;
}

/* 违规内容 */
.violations-section {
  margin-top: 16px;
  padding: 16px;
  background: white;
  border-radius: 6px;
}

.violations-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #e6a23c;
  margin-bottom: 12px;
}

.violations-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.violation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #fef0f0;
  border-radius: 4px;
  font-size: 13px;
}

.violation-word {
  font-weight: 600;
  color: #f56c6c;
}

.violation-type {
  color: #909399;
}

.violation-position {
  margin-left: auto;
  color: #606266;
  font-size: 12px;
}

/* 语义分析 */
.semantic-analysis {
  margin-top: 16px;
  padding: 16px;
  background: white;
  border-radius: 6px;
}

.analysis-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.analysis-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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

.upload-area {
  width: 100%;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

/* 响应式 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .result-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
