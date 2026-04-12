<template>
  <div class="works-view" :class="{ 'dark-theme': isDark }">
    <div class="header-section">
      <h2 class="page-title">我的作品</h2>
      <div class="header-buttons">
        <ElButton type="primary" @click="openUploadDialog">
          <i class="bi bi-brush menu-icon"></i>
          发布作品
        </ElButton>
      </div>
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
      <ElFormItem label="状态">
        <ElSelect v-model="filterModel.status" placeholder="全部" clearable style="width: 150px">
          <ElOption label="全部" value="" />
          <ElOption label="审核通过" value="approved" />
          <ElOption label="人工审核中" value="reviewing" />
          <ElOption label="审核不通过" value="rejected" />
          <ElOption label="人工审核不通过" value="manual_rejected" />
          <ElOption label="申诉中" value="appealing" />
          <ElOption label="申诉驳回" value="appeal_rejected" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem>
        <ElButton native-type="submit" type="primary">筛选</ElButton>
      </ElFormItem>
      <ElFormItem class="ml-auto">
        <ElButton
          v-if="reviewingWorksCount > 0"
          type="success"
          @click="viewAuditResults"
        >
          <i class="bi bi-hourglass-split"></i>
          开启AI审核 ({{ reviewingWorksCount }})
        </ElButton>
        <ElButton v-if="!viewReviewMode" @click="enterReviewMode">
          <i class="bi bi-robot"></i>
          AI审核结果
        </ElButton>
        <ElButton v-else @click="exitReviewMode">
          <i class="bi bi-x-lg"></i>
          退出查看
        </ElButton>
        <ElButton v-if="!viewAppealMode" @click="enterAppealMode">
          <i class="bi bi-pen"></i>
          申诉
        </ElButton>
        <ElButton v-else @click="exitAppealMode">
          <i class="bi bi-x-lg"></i>
          退出申诉
        </ElButton>
        <ElButton v-if="!editMode" type="warning" @click="enterEditMode">
          <i class="bi bi-pencil"></i>
          编辑作品
        </ElButton>
        <ElButton v-else @click="cancelEditMode">
          <i class="bi bi-x-lg"></i>
          退出编辑
        </ElButton>
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
        :review-status="work.status"
        :show-delete="editMode"
        :show-review="viewReviewMode && isWorkReviewed(work.status)"
        :show-appeal="viewAppealMode && canAppeal(work.status)"
        @delete="handleDeleteWork(work)"
        @review="handleViewReview(work)"
        @appeal="handleAppeal(work)"
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

    <!-- 发布模态框 -->
    <ElDialog
      v-model="uploadDialogVisible"
      title="发布作品"
      width="600"
      align-center
    >
      <ElForm ref="uploadFormRef" :model="uploadModel" :rules="uploadRules" label-width="100">
        <ElFormItem prop="title" label="作品标题">
          <ElInput
            v-model="uploadModel.title"
            placeholder="请输入作品标题"
            maxlength="100"
            show-word-limit
          />
        </ElFormItem>

        <ElFormItem prop="coverUrl" label="封面图片">
          <div class="cover-uploader" @click="uploadCover">
            <img v-if="uploadModel.coverUrl" :src="buildCoverURL(uploadModel.coverUrl)" class="cover-preview" />
            <div v-else class="upload-placeholder">
              <i class="bi bi-image"></i>
              <div class="upload-text">点击上传封面</div>
            </div>
          </div>
        </ElFormItem>

        <ElFormItem prop="videoUrl" label="视频文件">
          <ElButton type="primary" @click="uploadVideo">
            <i class="bi bi-upload me-2"></i>
            {{ uploadModel.videoUrl ? '重新上传视频' : '上传视频' }}
          </ElButton>
          <div class="upload-tip">文件大小不超过100MB</div>
          <div v-if="uploadModel.videoUrl" class="video-info">
            <i class="bi bi-check-circle-fill text-success"></i>
            视频已上传
          </div>
        </ElFormItem>

        <ElFormItem prop="description" label="作品描述">
          <ElInput
            v-model="uploadModel.description"
            type="textarea"
            :rows="4"
            placeholder="请输入作品描述"
            maxlength="500"
            show-word-limit
          />
        </ElFormItem>

        <ElFormItem label="发布地址">
          <div class="location-display">
            <i class="bi bi-geo-alt"></i>
            <span v-if="userInfo.province || userInfo.city">
              {{ userInfo.province }} {{ userInfo.city }}
            </span>
            <span v-else class="no-location">暂无地址信息</span>
          </div>
        </ElFormItem>
      </ElForm>

      <template #footer>
        <div class="dialog-footer">
          <ElButton @click="uploadDialogVisible = false">取消</ElButton>
          <ElButton type="primary" @click="submitWork">发布</ElButton>
        </div>
      </template>
    </ElDialog>

    <!-- 视频播放器 -->
    <PlayVideo
      v-model:visible="videoPlayerVisible"
      :video-url="currentVideoUrl"
      :video-title="currentVideoTitle"
      :description="currentVideoDescription"
    />

<!-- AI审核结果抽屉 -->
    <AIreviewCard
      v-model:drawerVisible="reviewDrawerVisible"
      :review-data="currentReview"
      :work-title="currentWorkTitle"
    />
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElButton, ElForm, ElFormItem, ElInput, ElSelect,
  ElOption, ElPagination, ElDialog, ElMessage, ElMessageBox, ElNotification
} from 'element-plus'
import WorkCard from '@/components/WorkCard.vue'
import PlayVideo from '@/components/PlayVideo.vue'
import AIreviewCard from '@/components/AIreviewCard.vue'
import request from '@/utils/request'
import { buildCoverURL, buildVideoURL, buildAvatarURL } from '@/utils/helper'
import { useTheme } from '@/utils/useTheme'
import 'bootstrap-icons/font/bootstrap-icons.css'

const { isDark } = useTheme()

// 作品列表
const works = ref([])

// 编辑模式
const editMode = ref(false)

// 查看审核结果模式
const viewReviewMode = ref(false)

// 申诉模式
const viewAppealMode = ref(false)

// 正在AI审核的作品数量 (status="pending")
const reviewingWorksCount = ref(0)

// 视频播放器状态
const videoPlayerVisible = ref(false)
const currentVideoUrl = ref('')
const currentVideoTitle = ref('')
const currentVideoDescription = ref('')

// 用户信息
const userInfo = reactive({
  province: '',
  city: ''
})

// 筛选表单
const filterModel = reactive({
  keywords: '',
  status: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 8,
  total: 0
})

// 获取作品列表
const fetchWorks = () => {
  const params = new URLSearchParams()
  params.append('page', pagination.currentPage)
  params.append('pageSize', pagination.pageSize)

  if (filterModel.keywords) {
    params.append('keywords', filterModel.keywords)
  }
  if (filterModel.status) {
    params.append('status', filterModel.status)
  }

  request.get('/works', { params }).then((response) => {
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

      // 获取正在审核的作品数量
      fetchreviewingWorksCount()
    }
  }).catch(() => {
    ElMessage.error('获取作品列表失败')
  })
}

// 获取正在审核的作品数量
const fetchreviewingWorksCount = () => {
  const params = new URLSearchParams()
  params.append('status', 'pending')
  params.append('page', 1)
  params.append('pageSize', 1)

  request.get('/works', { params }).then((response) => {
    if (response.data.status === true) {
      const total = response.data.payload.pagination.total
      reviewingWorksCount.value = Math.min(total, 3)
    }
  }).catch(() => {})
}

watch(
  () => [pagination.currentPage, pagination.pageSize],
  () => {
    fetchWorks()
  },
  { immediate: true }
)

// 获取用户信息(包括地址)
const fetchUserInfo = () => {
  request.get('/user/current').then((response) => {
    if (response.data.status === true) {
      const userData = response.data.payload.user
      userInfo.province = userData.province || ''
      userInfo.city = userData.city || ''
    }
  }).catch((error) => {
    console.error('获取用户信息失败:', error)
  })
}

// 页面加载时获取用户信息
onMounted(() => {
  fetchUserInfo()
})

// 上传对话框
const uploadDialogVisible = ref(false)
const uploadFormRef = ref(null)
const uploadModel = reactive({
  title: '',
  coverUrl: '',
  videoUrl: '',
  description: ''
})

const uploadRules = reactive({
  title: [
    { required: true, message: '请输入作品标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在1~100个字符', trigger: 'change' }
  ],
  coverUrl: [
    { required: true, message: '请上传封面图片', trigger: 'change' }
  ],
  videoUrl: [
    { required: true, message: '请上传视频文件', trigger: 'change' }
  ]
})

// 打开发布模态框
const openUploadDialog = () => {
  // 重置表单（包括清空字段值和验证状态）
  if (uploadFormRef.value) {
    uploadFormRef.value.resetFields()
  }

  uploadDialogVisible.value = true
}


// 上传封面
const uploadCover = () => {
  const fileInput = document.createElement('input')
  fileInput.setAttribute('type', 'file')
  fileInput.setAttribute('accept', 'image/*')
  fileInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) {
      const file = e.target.files[0]

      // 验证
      const isImage = file.type.startsWith('image/')
      const isLt5M = file.size / 1024 / 1024 < 5

      if (!isImage) {
        ElMessage.error('只能上传图片文件!')
        return
      }
      if (!isLt5M) {
        ElMessage.error('图片大小不能超过 5MB!')
        return
      }

      // 上传图片
      const formData = new FormData()
      formData.append('file', file, file.name)
      request.post('/upload/covers', formData).then((response) => {
        if (response.data.status === true) {
          uploadModel.coverUrl = response.data.payload.filename
          ElMessage.success('封面上传成功')
        } else {
          ElMessage.error('封面上传失败')
        }
      }).catch(() => {
        ElMessage.error('封面上传失败')
      })
    }
  })
  fileInput.click()
}

// 上传视频
const uploadVideo = () => {
  const fileInput = document.createElement('input')
  fileInput.setAttribute('type', 'file')
  fileInput.setAttribute('accept', 'video/*')
  fileInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) {
      const file = e.target.files[0]

      // 验证文件
      const isVideo = file.type.startsWith('video/')
      const isLt100M = file.size / 1024 / 1024 < 100

      if (!isVideo) {
        ElMessage.error('只能上传视频文件!')
        return
      }
      if (!isLt100M) {
        ElMessage.error('视频大小不能超过 100MB!')
        return
      }

      // 上传文件
      const formData = new FormData()
      formData.append('file', file, file.name)
      request.post('/upload/videos', formData).then((response) => {
        if (response.data.status === true) {
          uploadModel.videoUrl = response.data.payload.filename
          ElMessage.success('视频上传成功')
        } else {
          ElMessage.error('视频上传失败')
        }
      }).catch(() => {
        ElMessage.error('视频上传失败')
      })
    }
  })
  fileInput.click()
}

// 提交作品
const submitWork = () => {
  uploadFormRef.value.validate().then((valid) => {
    if (valid) {
      const workData = {
        title: uploadModel.title,
        description: uploadModel.description,
        coverUrl: uploadModel.coverUrl,
        videoUrl: uploadModel.videoUrl,
        location: {
          province: userInfo.province,
          city: userInfo.city
        }
      }

      request.post('/works/create', workData).then((response) => {
        if (response.data.status === true) {
          ElMessage.success('作品发布成功')
          uploadDialogVisible.value = false
          // 刷新作品列表
          fetchWorks()
        } else {
          ElMessage.error(response.data.message || '作品发布失败')
        }
      }).catch(() => {
        ElMessage.error('作品发布失败')
      })
    }
  }).catch(() => {
    ElMessage.warning('请完善表单信息')
  })
}

// 查看AI审核结果
const viewAuditResults = () => {
  // 筛选出审核中的作品
  const pendingWorks = works.value.filter(work => work.status === 'pending')

  if (pendingWorks.length === 0) {
    ElMessage.info('暂无审核中的作品')
    return
  }

  // 显示审核中作品的信息
  const workTitles = pendingWorks.map(work => work.title).join('、')
  ElMessageBox.confirm(
    `以下作品正在审核中：${workTitles}。是否立即触发AI审核？`,
    '审核中的作品',
    {
      confirmButtonText: '立即审核',
      cancelButtonText: '取消',
      type: 'info',
    }
  ).then(() => {
    // 触发审核
    triggerModerationForPendingWorks(pendingWorks)
  }).catch(() => {
    // 用户取消
  })
}

// 触发审核
const triggerModerationForPendingWorks = async (pendingWorks) => {
  ElNotification.info({
    title: '开始审核',
    message: `正在审核 ${pendingWorks.length} 个作品`,
    offset: 100,
    duration: 18000
  })

  let successCount = 0
  let failCount = 0
  let reviewingCount = 0  // 需要人工审核的数量

  for (const work of pendingWorks) {
    try {
      const params = new URLSearchParams()
      params.append('contentId', work.id)

      const response = await request.post('/moderation/work', params, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      })

      if (response.data.status === true) {
        const Status = response.data.payload.Status
        const statusText = Status === 'approved' ? '通过' :
                          Status === 'rejected' ? '未通过' : '需人工审核'

        if (Status === 'reviewing') {
          reviewingCount++
          ElMessage.warning(`作品"${work.title}"需要人工审核`)
        } else {
          successCount++
          ElMessage.success(`作品"${work.title}"审核完成 - ${statusText}`)
        }
      } else {
        failCount++
        ElMessage.error(`作品"${work.title}"审核失败`)
      }
    } catch (error) {
      failCount++
      console.error('审核失败:', error)
      ElMessage.error(`作品"${work.title}"审核失败`)
    }
  }

  // 刷新列表
  fetchWorks()

  // 显示总体结果
  if (reviewingCount > 0) {
    ElNotification.warning({
      title: '审核完成',
      message: `AI审核完成！通过: ${successCount}，需人工审核: ${reviewingCount}${failCount > 0 ? `，失败: ${failCount}` : ''}`,
      offset: 100,
      duration: 20000
    })
  } else if (failCount === 0) {
    ElNotification.success({
      title: '审核完成',
      message: `全部审核完成！成功: ${successCount}`,
      offset: 100,
      duration: 18000
    })
  } else {
    ElNotification.warning({
      title: '审核完成',
      message: `审核完成！成功: ${successCount}，失败: ${failCount}`,
      offset: 100,
      duration: 18000
    })
  }
}

// 进入编辑模式
const enterEditMode = () => {
  editMode.value = true
  viewReviewMode.value = false  // 退出查看模式
  viewAppealMode.value = false  // 退出申诉模式
}

// 退出编辑模式
const cancelEditMode = () => {
  editMode.value = false
}

// 进入查看审核结果模式
const enterReviewMode = () => {
  viewReviewMode.value = true
  editMode.value = false  // 退出编辑模式
  viewAppealMode.value = false  // 退出申诉模式
}

// 退出查看审核结果模式
const exitReviewMode = () => {
  viewReviewMode.value = false
}

// 进入申诉模式
const enterAppealMode = () => {
  viewAppealMode.value = true
  editMode.value = false  // 退出编辑模式
  viewReviewMode.value = false  // 退出查看模式
}

// 退出申诉模式
const exitAppealMode = () => {
  viewAppealMode.value = false
}

// 删除单个作品
const handleDeleteWork = (work) => {
  ElMessageBox.confirm(
    `确定要删除该作品"${work.title}"吗？`,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success',
    }
  ).then(() => {
    // 发送删除请求
    request.post('/works/delete', {
      id: work.id
    }).then((response) => {
      if (response.data.status === true) {
        ElMessage.success('删除成功')
        fetchWorks() // 刷新列表
      } else {
        ElMessage.error(response.data.message || '删除失败')
      }
    }).catch(() => {
      ElMessage.error('删除失败')
    })
  }).catch(() => {
    // 用户取消删除
  })
}

// 点击视频卡片播放视频
const handlePlayVideo = (work) => {
  currentVideoUrl.value = work.videoUrl
  currentVideoTitle.value = work.title
  currentVideoDescription.value = work.description || ''
  videoPlayerVisible.value = true
}

// AI审核结果抽屉
const reviewDrawerVisible = ref(false)
const currentReview = ref(null)
const currentWorkTitle = ref('')  // 当前查看的作品标题

// 判断作品是否已审核（可以查看审核结果）
const isWorkReviewed = (status) => {
  return status === 'approved' || status === 'rejected' || status === 'reviewing'
}

// 判断作品是否可以申诉（被拒绝的作品可以申诉）
const canAppeal = (status) => {
  return status === 'rejected' || status === 'manual_rejected' || status === 'appeal_rejected'
}

// 处理查看审核结果
const handleViewReview = (work) => {
  fetchReviewResult(work.id, work.title)
}

// 处理申诉
const handleAppeal = (work) => {
  ElMessageBox.prompt(
    `请输入申诉理由`,
    `申诉作品"${work.title}"`,
    {
      confirmButtonText: '提交申诉',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请详细说明您的申诉理由...',
      inputValidator: (value) => {
        if (!value || value.trim().length === 0) {
          return '申诉理由不能为空'
        }
        return true
      }
    }
  ).then(({ value }) => {
    // 发送申诉请求
    const appealData = {
      workId: work.id,
      reason: value.trim()
    }

    console.log('提交申诉数据:', appealData)

    request.post('/works/appeal', appealData).then((response) => {
      console.log('申诉响应:', response.data)
      if (response.data.status === true) {
        ElMessage.success('申诉提交成功，等待审核')
        fetchWorks() // 刷新列表
      } else {
        ElMessage.error(response.data.message || '申诉提交失败')
      }
    }).catch((error) => {
      console.error('申诉提交错误:', error)
      ElMessage.error('申诉提交失败: ' + (error.response?.data?.message || error.message || '网络错误'))
    })
  }).catch(() => {
    // 用户取消申诉
  })
}

// 获取审核结果
const fetchReviewResult = (workId, workTitle) => {
  const params = new URLSearchParams()
  params.append('contentId', workId)

  request.get('/moderation/work/result', { params }).then((response) => {
    if (response.data.status === true) {
      currentReview.value = response.data.payload.review
      currentWorkTitle.value = workTitle  // 保存作品标题
      reviewDrawerVisible.value = true
    } else {
      ElMessage.error(response.data.message || '该作品暂无审核结果')
    }
  }).catch((error) => {
    console.error('获取审核结果失败:', error)
    ElMessage.error('获取审核结果失败')
  })
}

</script>

<style scoped lang="scss">
.works-view {
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

  .header-buttons {
    display: flex;
    gap: 0.2rem;
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

  .ml-auto {
    margin-left: auto;
  }
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

// 上传组件样式
.cover-uploader {
  width: 100%;
  cursor: pointer;

  .cover-preview {
    width: 100%;
    height: 200px;
    object-fit: cover;
    border-radius: 8px;
    display: block;
  }

  .upload-placeholder {
    width: 40%;
    height: 6rem;
    border: 1px dashed #dcdfe6;
    border-radius: 1rem;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      border-color: #409eff;
      background-color: #f5f7fa;
    }

    i {
      font-size: 48px;
      color: #c0c4cc;
      margin-bottom: 8px;
    }

    .upload-text {
      font-size: 14px;
      color: #606266;
    }
  }
}

.upload-tip {
  font-size: 0.8rem;
  color: #909399;
  margin-top: 1rem;
}

.video-info {
  margin-top: 1rem;
  color: #67c23a;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 8px;

  i {
    font-size: 1rem;
  }
}

.form-tip {
  font-size: 1rem;
  color: #909399;
  margin-top: 0.2rem;
}

.location-display {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 0.8rem;
  background-color: #f5f7fa;
  border-radius: 6px;
  color: #606266;
  font-size: 0.8rem;

  i {
    color: #9FDB1D;
    font-size: 0.7rem;
  }

  .no-location {
    color: #909399;
  }
}

// Bootstrap icons
.menu-icon {
  font-size: 1.125rem;
  margin-right: 0.625rem;
  width: 1.125rem;
  height: 1.125rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.text-success {
  color: #67c23a;
}

// 暗黑主题
.works-view.dark-theme {
  .page-title {
    color: #e5e5e5;
  }

  .filter-form {
    background-color: #1e1e1e;
  }

  .empty-state {
    color: #6a6a6a;
  }

  .upload-placeholder {
    border-color: #3a3a3a;

    &:hover {
      border-color: #409eff;
      background-color: #2a2a2a;
    }

    i {
      color: #555;
    }

    .upload-text {
      color: #a0a0a0;
    }
  }

  .upload-tip {
    color: #6a6a6a;
  }

  .location-display {
    background-color: #2a2a2a;
    color: #a0a0a0;

    .no-location {
      color: #6a6a6a;
    }
  }
}
</style>
