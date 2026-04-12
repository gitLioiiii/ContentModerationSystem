<template>
  <div class="content-queue">
    <!-- 页面标题 -->
    <h1 style="margin: 2rem 0 1rem 0; font-size: 1.5rem; font-weight: bold">待审核队列</h1>

    <!-- 筛选表单 -->
    <ElForm :model="filterModel" @submit.prevent="handleSearch" inline class="filter-form">
      <ElFormItem label="状态">
        <ElSelect v-model="filterModel.status" placeholder="全部" clearable style="width: 150px">
          <ElOption label="待人工审核" value="pending" />
          <ElOption label="审核通过" value="approved" />
          <ElOption label="有用户申诉" value="appealed" />
          <ElOption label="申诉通过" value="appeal_approved" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="时间周期">
        <ElDatePicker
          v-model="filterModel.date"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          clearable
          style="width: 180px"
        />
      </ElFormItem>
      <ElFormItem>
        <ElButton native-type="submit" type="primary">筛选</ElButton>
      </ElFormItem>
    </ElForm>

    <!-- 审核队列表格 -->
    <ElTable :data="queueList" style="width: 100%" stripe border :show-header="true">
      <!-- <ElTableColumn prop="contentId" label="作品ID" width="200" /> -->
      <ElTableColumn prop="workTitle" label="作品名" width="150">
        <template #default="{ row }">
          <span v-if="row.workTitle">{{ row.workTitle }}</span>
          <span v-else style="color: #999">无</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="username" label="用户名" width="100">
        <template #default="{ row }">
          <span v-if="row.username">{{ row.username }}</span>
          <span v-else style="color: #999">无</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="status" label="审核状态" width="130">
        <template #default="{ row }">
          <ElTag v-if="row.status === 'pending'" type="warning">待人工审核</ElTag>
          <ElTag v-else-if="row.status === 'approved'" type="success">审核通过</ElTag>
          <ElTag v-else-if="row.status === 'rejected'" type="danger">审核拒绝</ElTag>
          <ElTag v-else-if="row.status === 'appealed'" type="info">有用户申诉</ElTag>
          <ElTag v-else-if="row.status === 'appeal_approved'" type="success">申诉通过</ElTag>
          <ElTag v-else-if="row.status === 'appeal_rejected'" type="danger">申诉拒绝</ElTag>
          <ElTag v-else>未知</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="reviewerName" label="审核员" width="100">
        <template #default="{ row }">
          <span v-if="row.reviewerName">{{ row.reviewerName }}</span>
          <span v-else style="color: #999">未审核</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="reviewReason" label="审核理由" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="row.reviewReason">{{ row.reviewReason }}</span>
          <span v-else style="color: #999">无</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="manualReviewTriggeredAt" label="触发时间" width="160" />
      <ElTableColumn prop="reviewCompletedAt" label="完成时间" width="160">
        <template #default="{ row }">
          <span v-if="row.reviewCompletedAt">{{ row.reviewCompletedAt }}</span>
          <span v-else style="color: #999">未完成</span>
        </template>
      </ElTableColumn>
      <ElTableColumn label="操作" width="260" fixed="right" header-align="center">
        <template #default="{ row }">
          <ElButton type="primary" size="small" @click="viewWork(row)">查看作品</ElButton>
          <ElButton type="info" size="small" @click="viewAIReview(row)">查看AI审核</ElButton>
          <ElButton
            v-if="row.status === 'pending' || row.status === 'appealed'"
            type="success"
            size="small"
            @click="reviewContent(row)"
            >审核</ElButton
          >
        </template>
      </ElTableColumn>
    </ElTable>

    <!-- 分页 -->
    <ElPagination
      layout="prev, pager, next, jumper, sizes, ->, total"
      :page-sizes="[5, 10, 20, 50, 100]"
      v-model:current-page="pagination.currentPage"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      background
      style="margin-top: 1rem"
    />

    <!-- AI审核结果抽屉 -->
    <AIreviewCard
      v-model:drawerVisible="reviewDrawerVisible"
      :review-data="currentReview"
      :work-title="currentWorkTitle"
    />

    <!-- 视频播放器 -->
    <PlayVideo
      v-model:visible="videoPlayerVisible"
      :video-url="currentVideoUrl"
      :video-title="currentVideoTitle"
    />

    <!-- 审核对话框 -->
    <ElDialog
      v-model="reviewDialogVisible"
      title="人工审核"
      width="50%"
      align-center
    >
      <ElForm :model="reviewModel" label-width="100">
        <ElFormItem label="审核决定">
          <ElRadioGroup v-model="reviewModel.decision">
            <ElRadio value="pass">通过</ElRadio>
            <ElRadio value="reject">拒绝</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="审核意见">
          <ElInput
            v-model="reviewModel.comment"
            type="textarea"
            :rows="4"
            placeholder="


                请输入审核意见（可选）
            "
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="reviewDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitReview">提交</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import {
  ElForm,
  ElFormItem,
  ElSelect,
  ElOption,
  ElDatePicker,
  ElButton,
  ElRadioGroup,
  ElRadio,
  ElTable,
  ElTableColumn,
  ElTag,
  ElPagination,
  ElDialog,
  ElInput,
  ElMessage,
} from 'element-plus'
import AIreviewCard from '@/components/AIreviewCard.vue'
import PlayVideo from '@/components/PlayVideo.vue'
import request from '@/utils/request'
import { buildVideoURL } from '@/utils/helper'

// 审核队列列表
const queueList = ref([])

// 筛选表单
const filterModel = reactive({
  status: '',
  date: null,
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
})

// 获取审核队列数据
const fetch = () => {
  let params = new URLSearchParams()
  params.append('page', pagination.currentPage)
  params.append('pageSize', pagination.pageSize)

  if (
    filterModel.status !== '' &&
    filterModel.status !== undefined &&
    filterModel.status !== null
  ) {
    params.append('status', filterModel.status)
  }

  if (filterModel.date) {
    params.append('date', filterModel.date)
  }

  request
    .get('/queue', { params })
    .then((response) => {
      if (response.data.status === true) {
        queueList.value = response.data.payload.queueList
        Object.assign(pagination, response.data.payload.pagination)
      }
    })
    .catch(() => {
      ElMessage.error('获取审核队列失败！')
    })
}

watch(
  () => [pagination.currentPage, pagination.pageSize],
  () => {
    fetch()
  },
  { immediate: true },
)

// 筛选搜索事件
const handleSearch = () => {
  pagination.currentPage = 1
  fetch()
}

// AI审核结果抽屉
const reviewDrawerVisible = ref(false)
const currentReview = ref(null)
const currentWorkTitle = ref('')

// 视频播放器
const videoPlayerVisible = ref(false)
const currentVideoUrl = ref('')
const currentVideoTitle = ref('')

// 查看AI审核结果
const viewAIReview = (row) => {
  fetchAIReviewResult(row.contentId, row.workTitle)
}

// 获取AI审核结果
const fetchAIReviewResult = (contentId, workTitle) => {
  const params = new URLSearchParams()
  params.append('contentId', contentId)

  request
    .get('/moderation/work/result', { params })
    .then((response) => {
      if (response.data.status === true) {
        currentReview.value = response.data.payload.review
        currentWorkTitle.value = workTitle
        reviewDrawerVisible.value = true
      } else {
        ElMessage.error(response.data.message || '该作品暂无AI审核结果')
      }
    })
    .catch(() => {
      ElMessage.error('获取AI审核结果失败')
    })
}

// 查看作品（播放视频）
const viewWork = (row) => {
  // 先获取作品详细信息
  const params = new URLSearchParams()
  params.append('contentId', row.contentId)

  request
    .get('/works/detail', { params })
    .then((response) => {
      if (response.data.status === true) {
        const work = response.data.payload.work
        if (work.videoUrl) {
          currentVideoUrl.value = buildVideoURL(work.videoUrl)
          currentVideoTitle.value = work.title
          videoPlayerVisible.value = true
        } else {
          ElMessage.warning('该作品没有视频文件')
        }
      } else {
        ElMessage.error('获取作品详情失败')
      }
    })
    .catch(() => {
      ElMessage.error('获取作品详情失败')
    })
}

// 审核
const reviewDialogVisible = ref(false)
const reviewModel = reactive({
  contentId: null,
  decision: 'pass',
  comment: '',
})

const currentTask = ref(null)

const reviewContent = (row) => {
  reviewModel.contentId = row.contentId
  reviewModel.decision = 'pass'
  reviewModel.comment = ''
  currentTask.value = row
  reviewDialogVisible.value = true
}

const submitReview = () => {
  const reviewData = {
    contentId: reviewModel.contentId,
    decision: reviewModel.decision,
    comment: reviewModel.comment,
  }

  request
    .post('/queue/review', reviewData)
    .then((response) => {
      if (response.data.status === true) {
        ElMessage.success('审核提交成功！')
        reviewDialogVisible.value = false
        fetch()
      } else {
        ElMessage.error('审核提交失败！')
      }
    })
    .catch(() => {
      ElMessage.error('审核提交失败！')
    })
}
</script>

<style scoped>
.content-queue {
  padding: 1rem;
}

.filter-form {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
}
</style>
