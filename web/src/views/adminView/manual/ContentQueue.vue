<template>
  <div class="content-queue">
    <!-- 筛选表单 -->
    <ElForm :model="filterModel" inline>
      <ElFormItem label="审核类型" style="width: 15rem">
        <ElSelect v-model="filterModel.contentType" placeholder="全部" clearable>
          <ElOption label="文本" value="text" />
          <ElOption label="图片" value="image" />
          <ElOption label="视频" value="video" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="时间周期" style="width: 18rem">
        <ElDatePicker
          v-model="filterModel.date"
          type="date"
          placeholder="选择日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          clearable
        />
      </ElFormItem>
    </ElForm>

    <!-- 页面标题 -->
    <h1 style="margin: 2rem 0 1rem 0; font-size: 1.5rem; font-weight: bold;">待审核队列</h1>

    <!-- 状态单选 -->
    <ElRadioGroup v-model="filterModel.status" @change="fetch" style="margin-bottom: 1rem;">
      <ElRadioButton value="">全部</ElRadioButton>
      <ElRadioButton value="pending">待人工审核</ElRadioButton>
      <ElRadioButton value="approved">审核通过</ElRadioButton>
      <ElRadioButton value="appealed">有用户申诉</ElRadioButton>
      <ElRadioButton value="appeal_approved">申诉通过</ElRadioButton>
    </ElRadioGroup>

    <!-- 审核队列表格 -->
    <ElTable :data="queueList" style="width: 100%" stripe border :show-header="true">
      <ElTableColumn prop="id" label="任务ID" width="80" />
      <ElTableColumn prop="contentType" label="审核类型" width="100">
        <template #default="{ row }">
          <ElTag v-if="row.contentType === 'text'" type="info">文本</ElTag>
          <ElTag v-else-if="row.contentType === 'image'" type="success">图片</ElTag>
          <ElTag v-else-if="row.contentType === 'video'" type="warning">视频</ElTag>
          <ElTag v-else>未知</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="manualReviewTriggeredAt" label="触发人工审核时间" width="180" />
      <ElTableColumn prop="reviewCompletedAt" label="审核完成时间" width="180">
        <template #default="{ row }">
          <span v-if="row.reviewCompletedAt">{{ row.reviewCompletedAt }}</span>
          <span v-else style="color: #999;">未完成</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="appealedAt" label="申诉时间" width="180">
        <template #default="{ row }">
          <span v-if="row.appealedAt">{{ row.appealedAt }}</span>
          <span v-else style="color: #999;">无</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="status" label="审核状态" width="120">
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
      <ElTableColumn label="操作" width="200" header-align="center">
        <template #default="{ row }">
          <ElButton
            type="primary"
            size="small"
            @click="viewDetail(row)"
          >查看详情</ElButton>
          <ElButton
            v-if="row.status === 'pending' || row.status === 'appealed'"
            type="success"
            size="small"
            @click="reviewContent(row)"
          >审核</ElButton>
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
      style="margin-top: 1rem;"
    />

    <!-- 审核详情对话框 -->
    <ElDialog
      v-model="detailDialogVisible"
      title="审核任务详情"
      width="60%"
      align-center
    >
      <ElDescriptions :column="2" border v-if="currentTask">
        <ElDescriptionsItem label="任务ID">{{ currentTask.id }}</ElDescriptionsItem>
        <ElDescriptionsItem label="审核类型">
          <ElTag v-if="currentTask.contentType === 'text'" type="info">文本</ElTag>
          <ElTag v-else-if="currentTask.contentType === 'image'" type="success">图片</ElTag>
          <ElTag v-else-if="currentTask.contentType === 'video'" type="warning">视频</ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="触发人工审核时间">{{ currentTask.manualReviewTriggeredAt }}</ElDescriptionsItem>
        <ElDescriptionsItem label="审核完成时间">{{ currentTask.reviewCompletedAt || '未完成' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="申诉时间">{{ currentTask.appealedAt }}</ElDescriptionsItem>
        <ElDescriptionsItem label="审核状态" :span="1">
          <ElTag v-if="currentTask.status === 'pending'" type="warning">待人工审核</ElTag>
          <ElTag v-else-if="currentTask.status === 'approved'" type="success">审核通过</ElTag>
          <ElTag v-else-if="currentTask.status === 'rejected'" type="danger">审核拒绝</ElTag>
          <ElTag v-else-if="currentTask.status === 'appealed'" type="info">有用户申诉</ElTag>
          <ElTag v-else-if="currentTask.status === 'appeal_approved'" type="success">申诉通过</ElTag>
          <ElTag v-else-if="currentTask.status === 'appeal_rejected'" type="danger">申诉拒绝</ElTag>
        </ElDescriptionsItem>
      </ElDescriptions>
      <template #footer>
        <ElButton @click="detailDialogVisible = false">关闭</ElButton>
      </template>
    </ElDialog>

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
            <ElRadio value="approved">通过</ElRadio>
            <ElRadio value="rejected">拒绝</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="审核意见">
          <ElInput
            v-model="reviewModel.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见（可选）"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="reviewDialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="submitReview">提交审核</ElButton>
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
  ElRadioButton,
  ElRadio,
  ElTable,
  ElTableColumn,
  ElTag,
  ElPagination,
  ElDialog,
  ElDescriptions,
  ElDescriptionsItem,
  ElInput,
  ElMessage
} from 'element-plus'
import request from '@/utils/request'

// 审核队列列表
const queueList = ref([])

// 筛选表单
const filterModel = reactive({
  contentType: '',
  date: null,
  status: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 5,
  total: 0
})

// 获取审核队列数据
const fetch = () => {
  let params = new URLSearchParams()
  params.append('page', pagination.currentPage)
  params.append('pageSize', pagination.pageSize)

  if (filterModel.contentType !== '' && filterModel.contentType !== undefined && filterModel.contentType !== null) {
    params.append('contentType', filterModel.contentType)
  }

  if (filterModel.status !== '' && filterModel.status !== undefined && filterModel.status !== null) {
    params.append('status', filterModel.status)
  }

  if (filterModel.date) {
    params.append('date', filterModel.date)
  }

  request.get('/queue', { params }).then((response) => {
    if (response.data.status === true) {
      queueList.value = response.data.payload.queueList
      Object.assign(pagination, response.data.payload.pagination)
    }
  }).catch(() => {
    ElMessage.error('获取审核队列失败！')
  })
}

// 监听分页变化
watch(
  () => [pagination.currentPage, pagination.pageSize],
  () => {
    fetch()
  },
  { immediate: true }
)

// 监听筛选条件变化
watch(
  () => [filterModel.contentType, filterModel.date],
  () => {
    // 重置到第一页并触发筛选
    pagination.currentPage = 1
    fetch()
  }
)

// 查看详情
const detailDialogVisible = ref(false)
const currentTask = ref(null)

const viewDetail = (row) => {
  currentTask.value = row
  detailDialogVisible.value = true
}

// 审核
const reviewDialogVisible = ref(false)
const reviewModel = reactive({
  taskId: null,
  decision: 'approved',
  comment: ''
})

const reviewContent = (row) => {
  reviewModel.taskId = row.id
  reviewModel.decision = 'approved'
  reviewModel.comment = ''
  currentTask.value = row
  reviewDialogVisible.value = true
}

const submitReview = () => {
  const reviewData = {
    taskId: reviewModel.taskId,
    decision: reviewModel.decision,
    comment: reviewModel.comment
  }

  request.post('/queue/review', reviewData).then((response) => {
    if (response.data.status === true) {
      ElMessage.success('审核提交成功！')
      reviewDialogVisible.value = false
      fetch()
    } else {
      ElMessage.error('审核提交失败！')
    }
  }).catch(() => {
    ElMessage.error('审核提交失败！')
  })
}
</script>

<style scoped>
.content-queue {
  padding: 1rem;
}
</style>
