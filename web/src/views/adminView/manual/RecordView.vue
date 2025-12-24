<template>
  <div class="review-record">
    <!-- 顶部区域 -->
    <div class="header-section">
      <h2 class="page-title">审核记录</h2>
    </div>

    <!-- 筛选表单 -->
    <ElForm :model="filterModel" @submit.prevent="handleSearch" inline class="filter-form">
      <ElFormItem label="审核结果">
        <ElSelect v-model="filterModel.decision" placeholder="请选择" clearable style="width: 150px">
          <ElOption label="已通过" value="pass" />
          <ElOption label="已拒绝" value="reject" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="审核人">
        <ElSelect
          v-model="filterModel.reviewerName"
          placeholder="请选择审核人"
          clearable
          style="width: 180px"
        >
          <ElOption
            v-for="admin in adminList"
            :key="admin.id"
            :label="admin.username"
            :value="admin.username"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="审核时间">
        <ElDatePicker
          v-model="filterModel.reviewDate"
          type="date"
          placeholder="请选择日期"
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

    <!-- 审核记录表格 -->
    <ElTable :data="recordList" style="width: 100%" stripe border :show-header="true">
      <ElTableColumn prop="workTitle" label="作品标题" width="150">
        <template #default="{ row }">
          <span v-if="row.workTitle">{{ row.workTitle }}</span>
          <span v-else style="color: #999;">无</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="username" label="用户" width="120">
        <template #default="{ row }">
          <span v-if="row.username">{{ row.username }}</span>
          <span v-else style="color: #999;">无</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="reviewerName" label="审核人" width="120">
        <template #default="{ row }">
          <span v-if="row.reviewerName">{{ row.reviewerName }}</span>
          <span v-else style="color: #999;">未知</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="decision" label="审核结果" width="100">
        <template #default="{ row }">
          <ElTag v-if="row.decision === 'pass'" type="success">通过</ElTag>
          <ElTag v-else-if="row.decision === 'reject'" type="danger">拒绝</ElTag>
          <ElTag v-else>未知</ElTag>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="reason" label="审核理由" min-width="200">
        <template #default="{ row }">
          <span v-if="row.reason">{{ row.reason }}</span>
          <span v-else style="color: #999;">无</span>
        </template>
      </ElTableColumn>
      <ElTableColumn prop="reviewedAt" label="审核时间" width="180" />
      <ElTableColumn label="操作" width="260" header-align="center">
        <template #default="{ row }">
          <ElButton
            type="primary"
            size="small"
            @click="viewWork(row)"
          >查看作品</ElButton>
          <ElButton
            type="info"
            size="small"
            @click="viewAIReview(row)"
          >查看AI审核</ElButton>
          <ElPopconfirm
            title="确认删除该审核记录吗？"
            confirm-button-text="确定"
            cancel-button-text="取消"
            @confirm="handleDelete(row)"
          >
            <template #reference>
              <ElButton type="danger" size="small">删除</ElButton>
            </template>
          </ElPopconfirm>
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
      class="pagination"
    />

    <!-- AI审核详情抽屉 -->
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
      :description="currentVideoDescription"
    />
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElForm, ElFormItem, ElSelect, ElOption, ElDatePicker,
  ElButton, ElTable, ElTableColumn, ElTag, ElPagination, ElMessage, ElPopconfirm
} from 'element-plus'
import AIreviewCard from '@/components/AIreviewCard.vue'
import PlayVideo from '@/components/PlayVideo.vue'
import request from '@/utils/request'
import { buildVideoURL } from '@/utils/helper'
import 'bootstrap-icons/font/bootstrap-icons.css'

// 审核记录列表
const recordList = ref([])

// 管理员列表（用于筛选）
const adminList = ref([])

// 筛选表单
const filterModel = reactive({
  decision: '',
  reviewerName: '',
  reviewDate: null
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 获取审核记录数据
const fetch = () => {
  let params = new URLSearchParams()
  params.append('page', pagination.currentPage)
  params.append('pageSize', pagination.pageSize)

  if (filterModel.decision) {
    params.append('decision', filterModel.decision)
  }

  if (filterModel.reviewerName) {
    params.append('reviewerName', filterModel.reviewerName)
  }

  if (filterModel.reviewDate) {
    params.append('reviewDate', filterModel.reviewDate)
  }

  request.get('/manual-review/records', { params }).then((response) => {
    if (response.data.status === true) {
      recordList.value = response.data.payload.records
      Object.assign(pagination, response.data.payload.pagination)
    }
  }).catch(() => {
    ElMessage.error('获取审核记录失败')
  })
}

// 获取管理员列表
const fetchAdminList = () => {
  request.get('/manual-review/admins').then((response) => {
    if (response.data.status === true) {
      adminList.value = response.data.payload.admins
    }
  }).catch(() => {
    ElMessage.error('获取管理员列表失败')
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

// 页面加载时获取管理员列表
fetchAdminList()

// 搜索事件
const handleSearch = () => {
  pagination.currentPage = 1
  fetch()
}

// AI审核详情抽屉
const reviewDrawerVisible = ref(false)
const currentReview = ref(null)
const currentWorkTitle = ref('')

// 视频播放器
const videoPlayerVisible = ref(false)
const currentVideoUrl = ref('')
const currentVideoTitle = ref('')
const currentVideoDescription = ref('')

// 查看AI审核详情
const viewAIReview = (row) => {
  fetchAIReviewRecord(row.contentId, row.workTitle)
}

// 获取AI审核详情
const fetchAIReviewRecord = (contentId, workTitle) => {
  const params = new URLSearchParams()
  params.append('contentId', contentId)

  request.get('/moderation/work/result', { params }).then((response) => {
    if (response.data.status === true) {
      currentReview.value = response.data.payload.review
      currentWorkTitle.value = workTitle
      reviewDrawerVisible.value = true
    } else {
      ElMessage.error(response.data.message || '查看详情失败AI审核详情')
    }
  }).catch(() => {
    ElMessage.error('获取AI审核详情失败')
  })
}

// 查看作品视频事件
const viewWork = (row) => {
  // 先获取查看作品的详情
  const params = new URLSearchParams()
  params.append('contentId', row.contentId)

  request.get('/works/detail', { params }).then((response) => {
    if (response.data.status === true) {
      const work = response.data.payload.work
      if (work.videoUrl) {
        currentVideoUrl.value = buildVideoURL(work.videoUrl)
        currentVideoTitle.value = work.title
        currentVideoDescription.value = work.description || ''
        videoPlayerVisible.value = true
      } else {
        ElMessage.warning('查看该作品视频不存在')
      }
    } else {
      ElMessage.error('获取查看作品详情失败')
    }
  }).catch(() => {
    ElMessage.error('获取查看作品详情失败')
  })
}

// 删除审核记录
const handleDelete = (row) => {
  request.post('/manual-review/records/remove', { id: row.id }).then((response) => {
    if (response.data.status === true) {
      fetch()
      ElMessage.success('删除成功！')
    } else {
      ElMessage.error(response.data.message || '删除失败！')
    }
  }).catch(() => {
    ElMessage.error('删除失败！')
  })
}
</script>

<style scoped lang="scss">
.review-record {
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

// 分页
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
