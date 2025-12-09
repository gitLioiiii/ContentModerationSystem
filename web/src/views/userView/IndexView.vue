<template>
  <div class="topic-list-component">
    <!-- 顶部容器 -->
    <div class="above-main-container">
      <h2 class="community-slogan">
        真诚、友善、团结、专业,共建你我引以为荣之社区。
      </h2>
    </div>

    <!-- 列表控制区域 -->
    <div class="list-controls">
      <div class="container">

        <!-- 导航容器 -->
        <section class="navigation-container">
          <!-- 顶部操作栏：筛选器和新建按钮 -->
          <div class="top-action-bar">
            <!-- 筛选器组 -->
            <!-- 类别选择器 -->
            <ElDropdown trigger="click" @command="handleCategoryChange">
              <ElButton>
                {{ selectedCategory || '类别' }}
                <i class="bi bi-chevron-down ms-2"></i>
              </ElButton>
              <template #dropdown>
                <ElDropdownMenu>
                  <ElDropdownItem command="">全部类别</ElDropdownItem>
                  <ElDropdownItem command="技术">技术</ElDropdownItem>
                  <ElDropdownItem command="生活">生活</ElDropdownItem>
                  <ElDropdownItem command="娱乐">娱乐</ElDropdownItem>
                  <ElDropdownItem command="新闻">新闻</ElDropdownItem>
                </ElDropdownMenu>
              </template>
            </ElDropdown>

            <!-- 新建话题按钮 -->
            <ElButton type="primary" @click="handleCreateTopic">
              <i class="bi bi-plus-lg me-1"></i>
              新建话题
            </ElButton>
          </div>

          <!-- 主导航栏 -->
          <ElTabs v-model="currentNav" @tabChange="handleNavClick">
            <ElTabPane
              v-for="nav in navItems"
              :key="nav.key"
              :label="nav.label"
              :name="nav.key"
            />
          </ElTabs>
        </section>

        <!-- 时间筛选器 -->
        <ElDropdown trigger="click" @command="handlePeriodChange">
          <ElButton>
            <span class="date-section">{{ selectedPeriod.label }}</span>
            <span v-if="selectedPeriod.dateRange" class="top-date-string">{{ selectedPeriod.dateRange }}</span>
            <i class="bi bi-chevron-down ms-2"></i>
          </ElButton>
          <template #dropdown>
            <ElDropdownMenu>
              <ElDropdownItem
                v-for="period in periodOptions"
                :key="period.value"
                :command="period.value"
              >
                <div class="period-item">
                  <span class="date-section">{{ period.label }}</span>
                  <span v-if="period.dateRange" class="top-date-string">{{ period.dateRange }}</span>
                </div>
              </ElDropdownItem>
            </ElDropdownMenu>
          </template>
        </ElDropdown>

        <!-- 内容列表 -->
        <section class="topic-list-section">
          <ElTable
            v-loading="loading"
            :data="topicList"
            class="topic-table"
            stripe
            @rowClick="handleRowClick"
          >
            <ElTableColumn label="内容预览" :minWidth="400">
              <template #default="{ row }">
                <div class="topic-title-cell">
                  <div class="topic-main-info">
                    <div class="content-preview">{{ getContentPreview(row) }}</div>
                  </div>
                </div>
              </template>
            </ElTableColumn>

            <ElTableColumn label="发布者" :width="120" align="center">
              <template #default="{ row }">
                <div class="author-cell">
                  <span class="author-name">{{ row.username || '未知用户' }}</span>
                </div>
              </template>
            </ElTableColumn>

            <ElTableColumn label="状态" width="120" align="center">
              <template #default="{ row }">
                <div class="stat-cell">
                  <ElTag :type="getStatusTagType(row.status)" size="small">
                    {{ getStatusText(row.status) }}
                  </ElTag>
                </div>
              </template>
            </ElTableColumn>

            <ElTableColumn label="创建时间" width="150">
              <template #default="{ row }">
                <div class="activity-cell">
                  <span class="activity-time">{{ formatTime(row.createdAt) }}</span>
                </div>
              </template>
            </ElTableColumn>
          </ElTable>

          <!-- 分页 -->
          <ElPagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            class="topic-pagination"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </section>
      </div>
    </div>

    <!-- 新建话题对话框 -->
    <ElDialog
      v-model="createDialogVisible"
      :width="aiPanelVisible ? '1200px' : '750px'"
      :close-on-click-modal="false"
      class="create-topic-dialog"
    >
      <!-- 自定义标题 -->
      <template #header>
        <div class="dialog-header">
          <span class="dialog-title">新建话题</span>
          <ElButton
            circle
            size="small"
            @click="aiPanelVisible = !aiPanelVisible"
            class="toggle-ai-button"
            :title="aiPanelVisible ? '隐藏' : '显示'"
          >
            <i :class="aiPanelVisible ? 'bi bi-chevron-right' : 'bi bi-chevron-left'"></i>
          </ElButton>
        </div>
      </template>

      <!-- 模态框 -->
      <div class="dialog-content-wrapper">
        <!-- 发布内容 -->
        <div class="publish-section">
          <h3 class="section-title">发布内容</h3>
          <ElForm ref="topicForm" :model="topicModel" :rules="topicRules" label-width="80px">
            <ElFormItem label="文本内容">
              <ElInput
                v-model="topicModel.content"
                type="textarea"
                :rows="8"
                placeholder="请输入内容（可选）"
                maxlength="5000"
                show-word-limit
                @input="handleContentChange"
              />
            </ElFormItem>

            <ElFormItem label="类别" required>
              <ElCheckboxGroup v-model="topicModel.categories">
                <ElCheckbox label="技术">技术</ElCheckbox>
                <ElCheckbox label="生活">生活</ElCheckbox>
                <ElCheckbox label="娱乐">娱乐</ElCheckbox>
                <ElCheckbox label="新闻">新闻</ElCheckbox>
              </ElCheckboxGroup>
            </ElFormItem>

            <ElFormItem label="媒体类型">
              <ElRadioGroup v-model="mediaType" @change="handleMediaTypeChange">
                <ElRadio label="image">图片</ElRadio>
                <ElRadio label="video">视频</ElRadio>
              </ElRadioGroup>
            </ElFormItem>

            <ElFormItem v-if="mediaType === 'image'" label="图片">
              <ElUpload
                v-model:file-list="topicModel.images"
                action="#"
                list-type="picture-card"
                :auto-upload="false"
                :limit="6"
                accept="image/*"
                @exceed="handleExceedImages"
                @change="handleContentChange"
              >
                <i class="bi bi-plus-lg"></i>
              </ElUpload>
              <div class="upload-tip">最多上传6张图片，支持 jpg、png、gif 格式</div>
            </ElFormItem>

            <ElFormItem v-if="mediaType === 'video'" label="视频">
              <ElUpload
                v-model:file-list="topicModel.videos"
                action="#"
                :auto-upload="false"
                :limit="1"
                accept="video/*"
                @exceed="handleExceedVideos"
                @change="handleContentChange"
              >
                <ElButton type="primary" :disabled="topicModel.videos.length >= 1">
                  <i class="bi bi-camera-video me-1"></i>
                  选择视频
                </ElButton>
              </ElUpload>
              <div class="upload-tip">最多上传1个视频，支持 mp4、avi、mov 格式</div>
            </ElFormItem>
          </ElForm>
        </div>

    <!-- AI审核 -->
        <transition name="slide-fade">
          <div v-if="aiPanelVisible" class="review-section">
            <h3 class="section-title">AI 智能审核</h3>

          <!-- 审核状态 -->
          <div class="review-status">
            <div v-if="aiReview.loading" class="status-loading">
              <i class="bi bi-arrow-clockwise spinner"></i>
              <span>AI 审核中...</span>
            </div>
            <div v-else-if="!topicModel.content" class="status-empty">
              <i class="bi bi-file-text"></i>
              <span>请输入内容后开始审核</span>
            </div>
            <div v-else class="status-result">
              <!-- 内容安全检测折叠面板 -->
              <ElCollapse v-model="activeCollapse" class="security-collapse">
                <ElCollapseItem name="security">
                  <template #title>
                    <div class="collapse-title">
                      <div class="collapse-header">
                        <i
                          :class="[
                            'bi',
                            aiReview.overallRiskLevel === 'risky' ? 'bi-exclamation-triangle-fill header-icon-danger' :
                            aiReview.overallRiskLevel === 'suspected' ? 'bi-exclamation-triangle header-icon-warning' :
                            'bi-check-circle-fill header-icon-success'
                          ]"
                        ></i>
                        <h4>内容安全检测</h4>
                      </div>
                      <div class="collapse-extra">
                        <ElTag
                          :type="getRiskTagType(aiReview.overallRiskLevel)"
                          size="small"
                        >
                          {{ getRiskLevelText(aiReview.overallRiskLevel) }}
                          <span v-if="getRiskCount() > 0" class="risk-count">{{ getRiskCount() }}</span>
                        </ElTag>
                      </div>
                    </div>
                  </template>

                  <div class="collapse-desc">
                    适用场景：检测文本内容色情、暴恐、违禁、涉政、涉价值观、广告法违规等安全合规检测
                  </div>

                  <!-- 检测列表 -->
                  <div class="detection-list">
                    <!-- 敏感词检测 -->
                    <div
                      class="detection-item"
                      :class="getDetectionItemClass('sensitive_words')"
                      @click="toggleDetectionItem('sensitive_words')"
                    >
                      <div class="detection-item-main">
                        <div class="detection-label">
                          <span>敏感词检测</span>
                          <span v-if="getViolationCount('sensitive_words') > 0" class="detection-count">
                            ({{ getViolationCount('sensitive_words') }})
                          </span>
                        </div>
                        <div class="detection-status">
                          <span
                            v-if="getViolationCount('sensitive_words') > 0"
                            class="status-badge status-badge-danger"
                          >
                            {{ getViolationCount('sensitive_words') }}
                          </span>
                          <i v-else class="bi bi-check-circle status-icon status-icon-pass"></i>
                          <i
                            v-if="getViolationCount('sensitive_words') > 0"
                            class="bi bi-chevron-right arrow-icon"
                            :class="{ 'arrow-icon-down': expandedItems.includes('sensitive_words') }"
                          ></i>
                        </div>
                      </div>
                      <!-- 敏感词列表 -->
                      <div
                        v-if="expandedItems.includes('sensitive_words') && getKeywords('sensitive_words').length > 0"
                        class="detection-keywords"
                      >
                        <ElTag
                          v-for="(word, index) in getKeywords('sensitive_words')"
                          :key="index"
                          type="danger"
                          size="small"
                        >
                          {{ word }}
                        </ElTag>
                      </div>
                    </div>

                    <!-- 色情检测 -->
                    <div class="detection-item" :class="getDetectionItemClass('porn')">
                      <div class="detection-item-main">
                        <div class="detection-label">
                          <span>色情检测</span>
                        </div>
                        <div class="detection-status">
                          <span
                            v-if="getViolationCount('porn') > 0"
                            class="status-badge status-badge-danger"
                          >
                            {{ getViolationCount('porn') }}
                          </span>
                          <i v-else class="bi bi-check-circle status-icon status-icon-pass"></i>
                        </div>
                      </div>
                    </div>

                    <!-- 暴恐检测 -->
                    <div class="detection-item" :class="getDetectionItemClass('violence')">
                      <div class="detection-item-main">
                        <div class="detection-label">
                          <span>暴恐检测</span>
                        </div>
                        <div class="detection-status">
                          <span
                            v-if="getViolationCount('violence') > 0"
                            class="status-badge status-badge-danger"
                          >
                            {{ getViolationCount('violence') }}
                          </span>
                          <i v-else class="bi bi-check-circle status-icon status-icon-pass"></i>
                        </div>
                      </div>
                    </div>

                    <!-- 涉政检测 -->
                    <div class="detection-item" :class="getDetectionItemClass('political')">
                      <div class="detection-item-main">
                        <div class="detection-label">
                          <span>涉政检测</span>
                        </div>
                        <div class="detection-status">
                          <span
                            v-if="getViolationCount('political') > 0"
                            class="status-badge status-badge-danger"
                          >
                            {{ getViolationCount('political') }}
                          </span>
                          <i v-else class="bi bi-check-circle status-icon status-icon-pass"></i>
                        </div>
                      </div>
                    </div>

                    <!-- 广告检测 -->
                    <div class="detection-item" :class="getDetectionItemClass('spam_mail')">
                      <div class="detection-item-main">
                        <div class="detection-label">
                          <span>广告检测</span>
                        </div>
                        <div class="detection-status">
                          <span
                            v-if="getViolationCount('spam_mail') > 0"
                            class="status-badge status-badge-danger"
                          >
                            {{ getViolationCount('spam_mail') }}
                          </span>
                          <i v-else class="bi bi-check-circle status-icon status-icon-pass"></i>
                        </div>
                      </div>
                    </div>
                  </div>
                </ElCollapseItem>
              </ElCollapse>

              <!-- 综合评分 -->
              <div class="score-summary">
                <span class="score-label">综合评分：</span>
                <span class="score-value" :class="getScoreClass(aiReview.overallScore)">
                  {{ aiReview.overallScore }}
                </span>
                <span class="score-max">/100</span>
              </div>
            </div>
          </div>
        </div>
        </transition>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="createDialogVisible = false">取消</ElButton>
          <ElButton
            type="primary"
            @click="handleSubmitTopic"
            :disabled="aiReview.loading || aiReview.overallRiskLevel === 'risky'"
            :loading="aiReview.loading"
          >
            {{ aiReview.loading ? '审核中...' : '发布' }}
          </ElButton>
        </div>
      </template>
    </ElDialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  ElDropdown,
  ElDropdownMenu,
  ElDropdownItem,
  ElButton,
  ElTable,
  ElTableColumn,
  ElPagination,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElUpload,
  ElRadioGroup,
  ElRadio,
  ElTabs,
  ElTabPane,
  ElCollapse,
  ElCollapseItem,
  ElTag,
  ElCheckboxGroup,
  ElCheckbox
} from 'element-plus'


// 导航项配置
const navItems = [
  { key: 'latest', label: '最新', title: '有新帖子的话题' },
  { key: 'hot', label: '热门', title: '最近热门话题' },
  { key: 'categories', label: '类别', title: '按类别分组的所有话题' }
]

// 当前导航
const currentNav = ref('latest')

// 筛选条件
const selectedCategory = ref('')

// 时间筛选
const selectedPeriod = ref({
  value: 'all',
  label: '所有时间',
  dateRange: ''
})

const periodOptions = [
  {
    value: 'all',
    label: '所有时间',
    dateRange: ''
  },
  {
    value: 'yearly',
    label: '年',
    dateRange: '2024 年 12月 1 日 – 2025 年 12月 1 日'
  },
  {
    value: 'monthly',
    label: '月',
    dateRange: '11月 1 日 – 12月 1 日'
  },
  {
    value: 'weekly',
    label: '周',
    dateRange: '11月 25 日 – 12月 1 日'
  },
  {
    value: 'daily',
    label: '今天',
    dateRange: '十二月 1 日'
  }
]

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载状态
const loading = ref(false)

// 话题列表
const topicList = ref([])

// 新建话题对话框
const createDialogVisible = ref(false)
const topicForm = ref(null)
// AI窗口显示状态（默认隐藏）
const aiPanelVisible = ref(false)

// 媒体类型选择
const mediaType = ref('image')

// 内容表单数据(对应数据库 content 集合)
const topicModel = reactive({
  content: '',
  categories: [], // 选中的类别
  images: [],
  videos: []
})

// 表单验证规则
const topicRules = {
  // 内容验证会在提交时进行
}

// AI审核状态（适配数据库结构）
const aiReview = reactive({
  loading: false,
  itemReviews: [], // 每个内容项的审核结果
  overallScore: 0, // 综合评分
  overallRiskLevel: 'safe' // 整体风险等级: safe/suspected/risky
})

// 折叠面板状态
const activeCollapse = ref(['security'])

// 展开的检测项
const expandedItems = ref([])

// 防抖定时器
let reviewTimer = null

// 处理内容变化 - 触发AI审核
const handleContentChange = () => {
  // 清除之前的定时器
  if (reviewTimer) {
    clearTimeout(reviewTimer)
  }

  // 如果内容为空，重置审核状态
  if (!topicModel.content) {
    aiReview.itemReviews = []
    aiReview.overallScore = 0
    aiReview.overallRiskLevel = 'safe'
    return
  }

  // 防抖：500ms后执行审核
  reviewTimer = setTimeout(() => {
    performAIReview()
  }, 500)
}

// 执行AI审核（模拟）
const performAIReview = () => {
  aiReview.loading = true

  // 模拟审核过程
  setTimeout(() => {
    const content = topicModel.content

    // 简单的敏感词检测
    const sensitiveWordList = ['广告', '违规', '敏感', '测试敏感词', '迷信药']
    const foundWords = sensitiveWordList.filter(word => content.includes(word))

    // 构建审核结果
    const itemReview = {
      itemOrder: 0,
      itemType: 'text',
      score: 100,
      riskLevel: 'safe',
      keywords: [],
      violationTypes: [],
      details: null,
      processingTime: 150
    }

    // 检测各种违规类型
    if (foundWords.length > 0) {
      itemReview.keywords = foundWords
      itemReview.violationTypes.push('sensitive_words')
      itemReview.score -= foundWords.length * 15
      itemReview.riskLevel = 'risky'
    }

    if (content.includes('色情') || content.includes('成人')) {
      itemReview.violationTypes.push('porn')
      itemReview.score -= 30
      itemReview.riskLevel = 'risky'
    }

    if (content.includes('暴力') || content.includes('血腥')) {
      itemReview.violationTypes.push('violence')
      itemReview.score -= 25
      itemReview.riskLevel = 'risky'
    }

    if (content.includes('政治') || content.includes('敏感政治')) {
      itemReview.violationTypes.push('political')
      itemReview.score -= 40
      itemReview.riskLevel = 'risky'
    }

    if (content.includes('广告') || content.includes('推广')) {
      itemReview.violationTypes.push('spam_mail')
      itemReview.score -= 10
    }

    itemReview.score = Math.max(0, Math.min(100, itemReview.score))

    // 更新审核结果
    aiReview.itemReviews = [itemReview]
    aiReview.overallScore = itemReview.score
    aiReview.overallRiskLevel = itemReview.riskLevel
    aiReview.loading = false
  }, 800)
}

// 获取风险标签类型
const getRiskTagType = (riskLevel) => {
  if (riskLevel === 'risky') return 'danger'
  if (riskLevel === 'suspected') return 'warning'
  return 'success'
}

// 获取风险等级文本
const getRiskLevelText = (riskLevel) => {
  if (riskLevel === 'risky') return '高风险'
  if (riskLevel === 'suspected') return '疑似风险'
  return '安全'
}

// 获取风险数量
const getRiskCount = () => {
  let count = 0
  aiReview.itemReviews.forEach(item => {
    if (item.violationTypes && item.violationTypes.length > 0) {
      count += item.violationTypes.length
    }
  })
  return count
}

// 获取检测类名
const getDetectionItemClass = (violationType) => {
  const hasViolation = getViolationCount(violationType) > 0
  return {
    'detection-item-active': hasViolation,
    'detection-item-disabled': !hasViolation
  }
}

// 切换检测项展开
const toggleDetectionItem = (violationType) => {
  if (getViolationCount(violationType) > 0) {
    const index = expandedItems.value.indexOf(violationType)
    if (index > -1) {
      expandedItems.value.splice(index, 1)
    } else {
      expandedItems.value.push(violationType)
    }
  }
}

// 获取指定违规类型的数量
const getViolationCount = (violationType) => {
  let count = 0
  aiReview.itemReviews.forEach(item => {
    if (item.violationTypes && item.violationTypes.includes(violationType)) {
      count++
    }
  })
  return count
}

// 获取指定违规类型的关键词
const getKeywords = (violationType) => {
  const keywords = []
  aiReview.itemReviews.forEach(item => {
    if (item.violationTypes && item.violationTypes.includes(violationType)) {
      if (item.keywords && item.keywords.length > 0) {
        keywords.push(...item.keywords)
      }
    }
  })
  return [...new Set(keywords)] // 去重
}

// 获取评分样式类名
const getScoreClass = (score) => {
  if (score >= 90) return 'excellent'
  if (score >= 75) return 'good'
  if (score >= 60) return 'fair'
  return 'poor'
}

// 处理图片超出限制
const handleExceedImages = () => {
  ElMessage.warning('最多只能上传6张图片')
}

// 处理视频超出限制
const handleExceedVideos = () => {
  ElMessage.warning('最多只能上传1个视频')
}

// 获取内容预览文本
const getContentPreview = (row) => {
  if (!row.contentItems || row.contentItems.length === 0) {
    return '无内容'
  }

  // 查找第一个文本项
  const textItem = row.contentItems.find(item => item.type === 'text' && item.textContent)
  if (textItem) {
    // 截取前100个字符
    return textItem.textContent.length > 100
      ? textItem.textContent.substring(0, 100) + '...'
      : textItem.textContent
  }

  // 如果没有文本,显示媒体类型信息
  const imageCount = row.contentItems.filter(item => item.type === 'image').length
  const videoCount = row.contentItems.filter(item => item.type === 'video').length

  const parts = []
  if (imageCount > 0) parts.push(`${imageCount}张图片`)
  if (videoCount > 0) parts.push(`${videoCount}个视频`)

  return parts.join(' + ') || '无内容'
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 'passed': return 'success'
    case 'pending': return 'info'
    case 'reviewing': return 'warning'
    case 'rejected': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'passed': return '已通过'
    case 'pending': return '待审核'
    case 'reviewing': return '人工审核中'
    case 'rejected': return '已驳回'
    default: return status
  }
}

// 处理类别切换
const handleCategoryChange = (command) => {
  selectedCategory.value = command
  fetchTopicList()
}

// 处理时间筛选切换
const handlePeriodChange = (command) => {
  const period = periodOptions.find(p => p.value === command)
  if (period) {
    selectedPeriod.value = period
    fetchTopicList()
  }
}

// 处理导航点击
const handleNavClick = (key) => {
  currentNav.value = key
  fetchTopicList()
}

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchTopicList()
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchTopicList()
}

// 处理行点击
const handleRowClick = (row) => {
  console.log('打开内容详情:', row)
  ElMessage.info(`点击了内容: ${getContentPreview(row).substring(0, 20)}...`)
}

// 处理新建话题
const handleCreateTopic = () => {
  createDialogVisible.value = true
}

// 处理媒体类型切换
const handleMediaTypeChange = (value) => {
  // 切换媒体类型时清空之前的上传
  if (value !== 'image') {
    topicModel.images = []
  }
  if (value !== 'video') {
    topicModel.videos = []
  }
  // 触发审核
  handleContentChange()
}

// 提交话题
const handleSubmitTopic = () => {
  if (!topicForm.value) return

  topicForm.value.validate((valid) => {
    if (valid) {
      // 验证类别：必须至少选择一个
      if (!topicModel.categories || topicModel.categories.length === 0) {
        ElMessage.error('请至少选择一个类别')
        return
      }

      // 验证内容：必须有文本、图片或视频中的至少一项
      const hasContent = topicModel.content.trim().length > 0
      const hasImages = topicModel.images.length > 0
      const hasVideos = topicModel.videos.length > 0

      if (!hasContent && !hasImages && !hasVideos) {
        ElMessage.error('请至少填写文本内容、上传图片或视频中的一项')
        return
      }

      // 构建内容数组（支持5种场景）
      const contentItems = []
      let order = 0

      if (hasContent && !hasImages && !hasVideos) {
        // 场景1: 纯文本
        contentItems.push({
          type: 'text',
          order: order++,
          textContent: topicModel.content,
          mediaUrl: null
        })
      } else if (!hasContent && hasImages && !hasVideos) {
        // 场景2: 纯图片
        topicModel.images.forEach(img => {
          contentItems.push({
            type: 'image',
            order: order++,
            textContent: null,
            mediaUrl: img.url || URL.createObjectURL(img.raw),
            thumbnailUrl: null,
            width: null,
            height: null
          })
        })
      } else if (!hasContent && !hasImages && hasVideos) {
        // 场景3: 纯视频
        contentItems.push({
          type: 'video',
          order: order++,
          textContent: null,
          mediaUrl: topicModel.videos[0].url || URL.createObjectURL(topicModel.videos[0].raw),
          thumbnailUrl: null,
          duration: null,
          width: null,
          height: null
        })
      } else if (hasContent && hasImages && !hasVideos) {
        // 场景4: 文本+图片
        contentItems.push({
          type: 'text',
          order: order++,
          textContent: topicModel.content,
          mediaUrl: null
        })
        topicModel.images.forEach(img => {
          contentItems.push({
            type: 'image',
            order: order++,
            textContent: null,
            mediaUrl: img.url || URL.createObjectURL(img.raw),
            thumbnailUrl: null,
            width: null,
            height: null
          })
        })
      } else if (hasContent && !hasImages && hasVideos) {
        // 场景5: 文本+视频
        contentItems.push({
          type: 'text',
          order: order++,
          textContent: topicModel.content,
          mediaUrl: null
        })
        contentItems.push({
          type: 'video',
          order: order++,
          textContent: null,
          mediaUrl: topicModel.videos[0].url || URL.createObjectURL(topicModel.videos[0].raw),
          thumbnailUrl: null,
          duration: null,
          width: null,
          height: null
        })
      }

      // 构建内容数据
      const contentData = {
        categories: topicModel.categories, // 类别数组
        contentItems: contentItems,
        status: 'pending',
        createdAt: new Date()
      }
      console.log('创建内容:', contentData)

      ElMessage.success('内容创建成功!')
      createDialogVisible.value = false

      // 重置表单
      topicModel.content = ''
      topicModel.categories = []
      topicModel.images = []
      topicModel.videos = []
      mediaType.value = 'image'

      // 重置AI审核状态
      aiReview.loading = false
      aiReview.itemReviews = []
      aiReview.overallScore = 0
      aiReview.overallRiskLevel = 'safe'
      expandedItems.value = []

      // 刷新列表
      fetchTopicList()
    }
  })
}

// 格式化时间
const formatTime = (timestamp) => {
  const now = new Date()
  const time = new Date(timestamp)
  const diff = now - time

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)} 分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)} 小时前`
  } else if (diff < 30 * day) {
    return `${Math.floor(diff / day)} 天前`
  } else {
    return time.toLocaleDateString()
  }
}

// 获取话题列表
const fetchTopicList = () => {
  loading.value = true

  // 模拟数据加载
  setTimeout(() => {
    topicList.value = []
    total.value = 0
    loading.value = false
  }, 500)
}

// 初始化
onMounted(() => {
  fetchTopicList()
})
</script>

<style scoped lang="scss">
.topic-list-component {
  background-color: #f5f7fa;
  min-height: 100vh;
}

// Bootstrap Icons 样式
i.bi {
  font-size: 1em;
  vertical-align: middle;
}

// 旋转动画
.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.above-main-container {
  min-height: 40px;
}

.community-slogan {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: start;
  align-items: center;
  padding: 0.5rem 1rem;
  background-color: #e3f2fd;
  color: #1976d2;
  font-size: 14px;
}

.list-controls {
  padding: 0;

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 1rem;
  }
}

/* 导航容器 */
.navigation-container {
  margin-bottom: 1rem;
}

/* 顶部操作栏 */
.top-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

/* 时间筛选器样式 */
.date-section {
  margin-right: 0.5rem;
}

.top-date-string {
  color: #909399;
  font-size: 12px;
}

.period-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

/* 话题列表 */
.topic-list-section {
  padding: 0;
}

.topic-table {
  width: 100%;

  :deep(.el-table__row) {
    cursor: pointer;
  }
}

.topic-title-cell {
  padding: 0.5rem 0;
}

.topic-main-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.topic-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.5;
}

.topic-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.topic-category {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background-color: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.topic-tag {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background-color: #f4f4f5;
  color: #909399;
  border-radius: 4px;
  font-size: 12px;
}

.author-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.author-name {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.stat-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.stat-number {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.activity-cell {
  display: flex;
  align-items: center;
}

.activity-time {
  font-size: 13px;
  color: #909399;
}

/* 分页 */
.topic-pagination {
  margin-top: 1rem;
  display: flex;
  justify-content: center;
}

/* 对话框 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}

/* 上传提示 */
.upload-tip {
  margin-top: 0.5rem;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

/* 新建话题对话框样式 */
.create-topic-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__header) {
    padding: 1rem 1.5rem;
    border-bottom: 1px solid #e1e4e8;
  }
}

/* 对话框标题栏 */
.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;

  .dialog-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }

  .toggle-ai-button {
    transition: all 0.3s;

    &:hover {
      transform: scale(1.1);
    }
  }
}

.dialog-content-wrapper {
  display: flex;
  gap: 0;
  min-height: 600px;
  max-height: 70vh;
  transition: all 0.3s ease;
}

.section-title {
  margin: 0 0 1.5rem 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  padding-bottom: 0.75rem;
  border-bottom: 2px solid #409eff;
}

/* 左侧发布区域 */
.publish-section {
  flex: 1;
  padding: 1.5rem;
  border-right: 1px solid #e1e4e8;
  overflow-y: auto;
}

/* 右侧审核区域 */
.review-section {
  width: 400px;
  padding: 1.5rem;
  background-color: #f8f9fa;
  overflow-y: auto;
  flex-shrink: 0;
}


.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from {
  transform: translateX(20px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateX(20px);
  opacity: 0;
}

.review-status {
  .status-loading,
  .status-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 3rem 1rem;
    color: #909399;
    gap: 1rem;

    i.bi {
      font-size: 48px;
    }

    span {
      font-size: 14px;
    }
  }

  .status-loading {
    color: #409eff;
  }
}

/* 安全检测折叠面板 */
.security-collapse {
  margin-bottom: 1rem;
  border: none;

  :deep(.el-collapse-item__header) {
    border: none;
    background-color: white;
    border-radius: 8px 8px 0 0;
    padding: 1rem;
  }

  :deep(.el-collapse-item__wrap) {
    border: none;
    background-color: white;
  }

  :deep(.el-collapse-item__content) {
    padding: 0 1rem 1rem;
  }
}

.collapse-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 0.5rem;

  .collapse-header {
    display: flex;
    align-items: center;
    gap: 0.5rem;

    h4 {
      margin: 0;
      font-size: 15px;
      font-weight: 600;
      color: #303133;
    }

    i.bi {
      font-size: 18px;
    }

    .header-icon-danger {
      color: #f56c6c;
    }

    .header-icon-warning {
      color: #e6a23c;
    }

    .header-icon-success {
      color: #67c23a;
    }
  }

  .collapse-extra {
    display: flex;
    align-items: center;

    .risk-count {
      margin-left: 0.25rem;
      font-weight: 600;
    }
  }
}

.collapse-desc {
  padding: 0.75rem 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 0.75rem;
}

/* 检测列表 */
.detection-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.detection-item {
  padding: 0.75rem;
  border-radius: 6px;
  background-color: #f8f9fa;
  transition: all 0.3s;

  &.detection-item-active {
    background-color: #fff;
    border: 1px solid #f56c6c;
    cursor: pointer;

    &:hover {
      box-shadow: 0 2px 8px rgba(245, 108, 108, 0.15);
    }
  }

  &.detection-item-disabled {
    opacity: 0.7;
  }
}

.detection-item-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detection-label {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 14px;
  color: #303133;

  .detection-count {
    color: #f56c6c;
    font-weight: 600;
  }
}

.detection-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;

  .status-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 20px;
    height: 20px;
    padding: 0 6px;
    border-radius: 10px;
    font-size: 12px;
    font-weight: 600;
    color: white;

    &.status-badge-danger {
      background-color: #f56c6c;
    }
  }

  .status-icon {
    font-size: 18px;

    &.status-icon-pass {
      color: #67c23a;
    }
  }

  .arrow-icon {
    font-size: 14px;
    color: #909399;
    transition: transform 0.3s;

    &.arrow-icon-down {
      transform: rotate(90deg);
    }
  }
}

.detection-keywords {
  margin-top: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px dashed #e4e7ed;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

/* 综合评分 */
.score-summary {
  margin-top: 1rem;
  padding: 1rem;
  background-color: white;
  border-radius: 8px;
  text-align: center;
  font-size: 14px;

  .score-label {
    color: #606266;
    margin-right: 0.5rem;
  }

  .score-value {
    font-size: 24px;
    font-weight: bold;
    margin: 0 0.25rem;

    &.excellent {
      color: #67c23a;
    }

    &.good {
      color: #409eff;
    }

    &.fair {
      color: #e6a23c;
    }

    &.poor {
      color: #f56c6c;
    }
  }

  .score-max {
    color: #909399;
    font-size: 14px;
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .top-action-bar {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .topic-title {
    font-size: 14px;
  }

  :deep(.el-table__header-wrapper) {
    display: none;
  }

  :deep(.el-table__body-wrapper) {
    .el-table__row {
      display: flex;
      flex-direction: column;
      border-bottom: 1px solid #ebeef5;
      padding: 1rem 0;

      .el-table__cell {
        border: none;
        padding: 0.25rem 0;
      }
    }
  }

  .dialog-content-wrapper {
    flex-direction: column;
  }

  .publish-section,
  .review-section {
    width: 100% !important;
    border-right: none !important;
    border-bottom: 1px solid #e1e4e8;
    padding-bottom: 1.5rem;
  }
}
</style>
