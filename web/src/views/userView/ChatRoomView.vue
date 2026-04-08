<template>
  <div class="chat-room" :class="{ 'dark-theme': isDark }">
    <div class="header-section">
      <h2 class="page-title">聊天室</h2>
    </div>

    <!-- 消息列表 -->
    <div ref="messageListRef" class="message-list">
      <div v-if="messages.length === 0" class="empty-chat">
        <i class="bi bi-chat-square-text"></i>
        <p>暂无消息，快来发第一条吧</p>
      </div>

      <div
        v-for="(msg, index) in messages"
        :key="index"
        class="message-item"
        :class="{ 'is-self': msg.userId === currentUserId, 'is-system': msg.type === 'system' }"
      >
        <!-- 系统消息 -->
        <div v-if="msg.type === 'system'" class="system-message">
          {{ msg.content }}
        </div>

        <!-- 普通消息 -->
        <template v-else>
          <img
            v-if="msg.userId !== currentUserId"
            :src="buildAvatarUrl(msg.avatar)"
            class="msg-avatar"
            alt="avatar"
          />
          <div class="msg-body">
            <div class="msg-meta">
              <span class="msg-name">{{ msg.name || msg.username }}</span>
              <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
            </div>
            <div class="msg-bubble">{{ msg.content }}</div>
          </div>
          <img
            v-if="msg.userId === currentUserId"
            :src="buildAvatarUrl(msg.avatar)"
            class="msg-avatar"
            alt="avatar"
          />
        </template>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <ElInput
        v-model="inputContent"
        :disabled="!connected"
        placeholder="输入消息..."
        @keyup.enter="sendMessage"
        maxlength="500"
        show-word-limit
        clearable
      />
      <ElButton type="primary" :disabled="!connected || !inputContent.trim()" @click="sendMessage">
        <i class="bi bi-send"></i>
        发送
      </ElButton>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElInput, ElButton, ElMessage } from 'element-plus'
import { Client } from '@stomp/stompjs'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useTheme } from '@/utils/useTheme'
import 'bootstrap-icons/font/bootstrap-icons.css'

const { isDark } = useTheme()
const userStore = useUserStore()

const messages = ref([])
const inputContent = ref('')
const connected = ref(false)
const messageListRef = ref(null)

// 当前用户信息
const currentUserId = userStore.user?.user?.id || ''
const currentUsername = userStore.user?.user?.username || ''
const currentName = userStore.user?.user?.name || currentUsername
const currentAvatar = userStore.user?.user?.avatar || ''

// 构建头像URL
const buildAvatarUrl = (avatarPath) => {
  if (!avatarPath) return ''
  if (avatarPath.startsWith('http')) return avatarPath
  return `${import.meta.env.APP_BASE_URL}${avatarPath}`
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  if (isNaN(date.getTime())) return ''
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  if (isToday) return `${hours}:${minutes}`
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

// 加载历史消息
const loadHistory = () => {
  request.get('/chat/history').then((response) => {
    if (response.data.status === true) {
      messages.value = response.data.payload.messages || []
      scrollToBottom()
    }
  }).catch(() => {
    ElMessage.warning('加载聊天记录失败')
  })
}

// WebSocket STOMP 客户端
let stompClient = null

const connectWebSocket = () => {
  stompClient = new Client({
    brokerURL: `ws://localhost:8080/ws`,
  })

  stompClient.onConnect = () => {
    connected.value = true
    // 订阅聊天频道
    stompClient.subscribe('/topic/chat', (frame) => {
      const msg = JSON.parse(frame.body)
      messages.value.push(msg)
      scrollToBottom()
    })
  }

  stompClient.onDisconnect = () => {
    connected.value = false
  }

  stompClient.onStompError = (frame) => {
    console.error('STOMP error:', frame.headers['message'])
    connected.value = false
  }

  stompClient.onWebSocketClose = () => {
    connected.value = false
  }

  stompClient.activate()
}

// 发送消息
const sendMessage = () => {
  const content = inputContent.value.trim()
  if (!content || !stompClient || !connected.value) return

  const message = {
    userId: currentUserId,
    username: currentUsername,
    name: currentName,
    avatar: currentAvatar,
    content: content,
    type: 'text',
  }

  stompClient.publish({
    destination: '/app/chat.send',
    body: JSON.stringify(message),
  })

  inputContent.value = ''
}

onMounted(() => {
  loadHistory()
  connectWebSocket()
})

onUnmounted(() => {
  if (stompClient) {
    stompClient.deactivate()
  }
})
</script>

<style scoped lang="scss">
.chat-room {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 50px - 1.5rem - 4rem);
  max-width: 900px;
  margin: 0 auto;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-shrink: 0;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }
}

// 消息列表
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 12px;
  margin-bottom: 16px;
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;

  i {
    font-size: 60px;
    margin-bottom: 12px;
    opacity: 0.4;
  }

  p {
    font-size: 14px;
  }
}

// 消息项
.message-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 10px;

  &.is-self {
    flex-direction: row-reverse;

    .msg-meta {
      text-align: right;
    }

    .msg-bubble {
      background: #409eff;
      color: #fff;
      border-radius: 12px 2px 12px 12px;
    }
  }

  &.is-system {
    justify-content: center;
  }
}

.system-message {
  font-size: 12px;
  color: #909399;
  background: rgba(0, 0, 0, 0.04);
  padding: 4px 12px;
  border-radius: 10px;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.msg-body {
  max-width: 60%;
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.msg-name {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.msg-time {
  font-size: 11px;
  color: #c0c4cc;
}

.msg-bubble {
  display: inline-block;
  padding: 10px 14px;
  background: #fff;
  border-radius: 2px 12px 12px 12px;
  font-size: 14px;
  line-height: 1.5;
  color: #303133;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

// 输入区域
.input-area {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
  padding-top: 8px;

  .el-input {
    flex: 1;

    :deep(.el-input__wrapper) {
      min-height: 5rem;
    }
  }
}

// 暗黑主题
.chat-room.dark-theme {
  .page-title {
    color: #e5e5e5;
  }

  .message-list {
    background: #1e1e1e;
  }

  .empty-chat {
    color: #6a6a6a;
  }

  .msg-bubble {
    background: #2a2a2a;
    color: #e5e5e5;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  }

  .message-item.is-self .msg-bubble {
    background: #409eff;
    color: #fff;
  }

  .system-message {
    color: #6a6a6a;
    background: rgba(255, 255, 255, 0.04);
  }

  .msg-name {
    color: #a0a0a0;
  }

  .msg-time {
    color: #5a5a5a;
  }
}
</style>
