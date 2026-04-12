<template>
  <ElDialog
    v-model="dialogVisible"
    width="70%"
    align-center
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    @close="handleClose"
    class="video-dialog"
  >
    <!-- 自定义标题和描述 -->
    <template #header>
      <div class="custom-header">
        <h3 class="video-title">{{ videoTitle }}</h3>
        <div v-if="description" class="video-description">{{ description }}</div>
      </div>
    </template>

    <div class="video-container">
      <video
        v-if="dialogVisible && videoUrl"
        ref="videoRef"
        :src="videoUrl"
        controls
        autoplay
        class="video-player"
        @error="handleVideoError"
      >
        您的浏览器不支持视频播放。
      </video>
      <div v-if="videoError" class="video-error">
        <i class="bi bi-exclamation-triangle"></i>
        <p>视频加载失败</p>
      </div>
    </div>
  </ElDialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElDialog, ElMessage } from 'element-plus'
import 'bootstrap-icons/font/bootstrap-icons.css'

const props = defineProps({
  // 控制对话框显示/隐藏
  visible: {
    type: Boolean,
    default: false
  },
  // 视频URL
  videoUrl: {
    type: String,
    default: ''
  },
  // 视频标题
  videoTitle: {
    type: String,
    default: '视频播放'
  },
  // 作品描述
  description: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible'])

// 对话框显示状态
const dialogVisible = ref(props.visible)
const videoRef = ref(null)
const videoError = ref(false)

// 监听 props.visible 的变化
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
  if (!newVal) {
    // 关闭时暂停视频
    if (videoRef.value) {
      videoRef.value.pause()
    }
    videoError.value = false
  }
})

// 监听 dialogVisible 的变化，同步到父组件
watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

// 处理对话框关闭
const handleClose = () => {
  if (videoRef.value) {
    videoRef.value.pause()
  }
  videoError.value = false
}

// 处理视频加载错误
const handleVideoError = () => {
  videoError.value = true
  ElMessage.error('视频加载失败，请检查视频文件')
}
</script>

<style scoped lang="scss">
.video-dialog {
  :deep(.el-dialog) {
    max-width: 1200px;
    border-radius: 8px;
  }

  :deep(.el-dialog__header) {
    padding: 20px 24px;
    border-bottom: 1px solid #e4e7ed;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.custom-header {
  .video-title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    line-height: 1.4;
  }

  .video-description {
    margin-top: 12px;
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-word;
    max-height: 120px;
    overflow-y: auto;
    padding-right: 8px;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background-color: #dcdfe6;
      border-radius: 2px;

      &:hover {
        background-color: #c0c4cc;
      }
    }

    &::-webkit-scrollbar-track {
      background-color: transparent;
    }
  }
}

.video-container {
  width: 100%;
  background-color: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  position: relative;

  .video-player {
    width: 100%;
    height: auto;
    max-height: 80vh;
    outline: none;
  }

  .video-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #f56c6c;
    padding: 40px;
    gap: 16px;

    i {
      font-size: 48px;
    }

    p {
      font-size: 16px;
      margin: 0;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .video-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 0 auto;
    }

    :deep(.el-dialog__header) {
      padding: 16px 20px;
    }
  }

  .custom-header {
    .video-title {
      font-size: 18px;
    }

    .video-description {
      font-size: 13px;
      margin-top: 10px;
      max-height: 100px;
    }
  }

  .video-container {
    min-height: 300px;

    .video-player {
      max-height: 60vh;
    }
  }
}

@media (max-width: 480px) {
  .video-dialog {
    :deep(.el-dialog__header) {
      padding: 14px 16px;
    }
  }

  .custom-header {
    .video-title {
      font-size: 16px;
    }

    .video-description {
      font-size: 12px;
      margin-top: 8px;
      max-height: 80px;
    }
  }

  .video-container {
    min-height: 200px;

    .video-player {
      max-height: 50vh;
    }
  }
}
</style>
