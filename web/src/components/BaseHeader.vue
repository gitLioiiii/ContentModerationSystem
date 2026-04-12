<template>
  <!-- Header 容器 -->
  <header
    class="base-header-container"
    :class="{ 'dark-theme': isDark }"
    :style="{
      height: '50px',
      left: sidebarStore.isCollapsed ? '4rem' : '14rem',
      position: 'fixed',
      top: '0',
      width: sidebarStore.isCollapsed ? 'calc(100% - 4rem)' : 'calc(100% - 14rem)',
      zIndex: 20,
      marginTop: '0px',
    }"
  >
    <!-- 左侧按钮组 -->
    <div class="header-left">
      <!-- 折叠侧边栏按钮 -->
      <ElButton class="header-btn" text @click="toggleSidebar" >
        <i class="bi bi-list"></i>
      </ElButton>

      <!-- 刷新按钮 -->
      <ElButton class="header-btn" text @click="handleRefresh" >
        <i class="bi bi-arrow-clockwise"></i>
      </ElButton>
    </div>

    <!-- 中间面包屑导航区域 -->
    <div class="header-center">
      <ElBreadcrumb :separator-icon="ArrowRight">
        <ElBreadcrumbItem :to="{ path: '/' }">
          {{ breadcrumb.parent }}
        </ElBreadcrumbItem>
        <ElBreadcrumbItem>
          {{ breadcrumb.current }}
        </ElBreadcrumbItem>
      </ElBreadcrumb>
    </div>

    <!-- 中间占位区域 -->
    <div class="header-spacer"></div>

    <!-- 右侧工具栏 -->
    <div class="header-right">
      <!-- 主题切换按钮 -->
      <ElButton class="header-btn theme-toggle" circle text
        :aria-label="isDark ? 'light' : 'dark'" @click="toggleTheme"
      >
        <i v-if="!isDark" class="bi bi-sun"></i>
        <i v-else class="bi bi-moon-stars"></i>
      </ElButton>

      <!-- 全屏按钮 -->
      <ElButton
        class="header-btn"
        circle
        text
        @click="toggleFullscreen"
      >
        <i class="bi bi-fullscreen"></i>
      </ElButton>

      <!-- 用户菜单 -->
      <ElDropdown trigger="click" @command="handleUserCommand">
        <div class="user-avatar-wrapper">
          <ElTag v-if="userStore.logged" size="default">
            {{ userStore.isAdmin ? '管理员:' : '用户:' }}
          </ElTag>
          <div style="margin-right: 1rem;">{{ userStore.name }}</div>
          <ElAvatar
            :size="32"
            :src="userAvatar"
            class="user-avatar"
          >
            <span v-if="userStore.logged">
              {{ userStore.name?.charAt(0)?.toUpperCase() || 'U' }}
            </span>
            <span v-else><i class="bi bi-person"></i></span>
          </ElAvatar>
        </div>
        <template #dropdown>
          <ElDropdownMenu>
            <ElDropdownItem v-if="userStore.logged" command="profile">
              <i class="bi bi-person"></i>
              <span>个人信息</span>
            </ElDropdownItem>
            <ElDropdownItem v-if="userStore.logged" divided command="logout">
              <i class="bi bi-box-arrow-right"></i>
              <span>退出登录</span>
            </ElDropdownItem>
            <ElDropdownItem v-else command="login">
              <i class="bi bi-box-arrow-in-right"></i>
              <span>登录</span>
            </ElDropdownItem>
          </ElDropdownMenu>
        </template>
      </ElDropdown>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElButton, ElBreadcrumb, ElBreadcrumbItem, ElDropdown, ElDropdownMenu, ElDropdownItem, ElAvatar, ElMessage, ElTag } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useSidebarStore } from '@/stores/sidebar'
import { useTheme } from '@/utils/useTheme'
import { buildURL } from '@/utils/helper'
import request from '@/utils/request'
import 'bootstrap-icons/font/bootstrap-icons.css'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const sidebarStore = useSidebarStore()
const { isDark, toggleTheme } = useTheme()

// 用户头像
const userAvatar = computed(() => {
  // 从 userStore 中获取用户头像路径
  const avatar = userStore.user?.user?.avatar
  return avatar ? buildURL(avatar) : ''
})

// 面包屑导航
const breadcrumb = computed(() => {
  const meta = route.meta || {}
  return {
    parent: meta.breadcrumbParent,
    current: meta.breadcrumbCurrent || route.name || '',
  }
})

// 切换侧边栏折叠/展开
const toggleSidebar = () => {
  sidebarStore.toggleCollapse()
}

// 刷新页面
const handleRefresh = () => {
  location.reload()
}

// 全屏切换
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 用户菜单命令处理
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push({ name: 'Personal' })
      break
    case 'logout':
      logout()
      break
    case 'login':
      router.push({ name: 'login' })
      break
    default:
      break
  }
}

// 退出登录
const logout = () => {
  request
    .post('/logout')
    .then((response) => {
      console.log(response)
    })
    .finally(() => {
      userStore.logout()
      userStore.clear('user')
      ElMessage.success('已成功退出。')
      router.push({ name: 'login' })
    })
}
</script>

<style scoped lang="scss">
// Header 容器
.base-header-container {
  display: flex;
  width: 100%;
  flex: 0 0 auto;
  align-items: center;
  padding-left: 0.5rem;
  border-bottom: 1px solid hsl(214.3 31.8% 91.4%);
  background-color: hsl(0 0% 100%);
  transition: margin-top 200ms;
}

// 左侧按钮组
.header-left {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

// 中间面包屑区域
.header-center {
  display: none;
  margin-left: 0.5rem;

  @media (min-width: 1024px) {
    display: block;
  }
}

// 中间占位
.header-spacer {
  display: flex;
  align-items: center;
  height: 100%;
  min-width: 0;
  flex: 1;
}

// 右侧工具栏
.header-right {
  display: flex;
  align-items: center;
  height: 100%;
  min-width: 0;
  flex-shrink: 0;
  gap: 0.25rem;
  padding-right: 0.5rem;
}

// 通用 Header 按钮样式
.header-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 2rem;
  width: 2rem;
  padding: 0.25rem;
  font-size: 1.125rem;
  color: hsl(222.2 84% 4.9% / 0.8);
  border: none;
  border-radius: 0.375rem;
  background: transparent;
  cursor: pointer;
  transition: all 150ms;

  &:hover {
    background-color: hsl(210 40% 96.1%);
    color: hsl(222.2 47.4% 11.2%);
    animation: shrink 0.3s ease-in-out;
  }

  &:focus-visible {
    outline: 2px solid transparent;
    outline-offset: 2px;
    box-shadow: 0 0 0 1px hsl(215 20.2% 65.1%);
  }

  &:disabled {
    pointer-events: none;
    cursor: not-allowed;
    opacity: 0.5;
  }

  i {
    font-size: 1rem;
  }
}

// 搜索按钮特殊样式
.search-btn {
  width: auto;
  height: 2rem;
  padding: 0.125rem 0.5rem;
  border-radius: 1rem;
  margin-right: 0.25rem;

  @media (min-width: 768px) {
    background-color: hsl(210 40% 96.1%);
    margin-right: 1rem;
  }

  .search-content {
    display: flex;
    align-items: center;
    gap: 0.75rem;

    i {
      font-size: 1rem;
      color: hsl(215.4 16.3% 46.9%);
    }

    .search-text {
      display: none;
      font-size: 0.75rem;
      color: hsl(215.4 16.3% 46.9%);

      @media (min-width: 768px) {
        display: block;
      }
    }

    .search-shortcut {
      display: none;
      padding: 0.25rem 0.375rem;
      font-size: 0.75rem;
      line-height: 1;
      color: hsl(215.4 16.3% 46.9%);
      background-color: hsl(0 0% 100%);
      border-radius: 0.25rem;
      border-top-right-radius: 0.75rem;

      @media (min-width: 768px) {
        display: block;
      }

      kbd {
        font-family: inherit;
        font-size: inherit;
      }
    }
  }

  &:hover {
    .search-content {
      i,
      .search-text,
      .search-shortcut {
        color: hsl(222.2 84% 4.9%);
      }
    }
  }
}

// 主题切换按钮
.theme-toggle {
  &:hover {
    animation: shrink 0.3s ease-in-out;
  }
}

// 用户头像
.user-avatar-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  padding: 0.375rem;
  margin-left: 0.25rem;
  border-radius: 9999px;
  cursor: pointer;
  transition: background-color 150ms;

  &:hover {
    background-color: hsl(210 40% 96.1%);
  }
}

.user-avatar {
  flex-shrink: 0;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  padding: 0.5rem 1rem;

  i {
    font-size: 0.875rem;
  }

  &:hover {
    background-color: hsl(210 40% 96.1%);
    color: hsl(222.2 47.4% 11.2%);
  }
}

:deep(.el-dropdown-menu__item--divided) {
  border-top: 1px solid hsl(214.3 31.8% 91.4%);
  margin-top: 0.25rem;
  padding-top: 0.75rem;
}

// 动画
@keyframes shrink {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(0.95);
  }
}

// 响应式
@media (max-width: 1024px) {
  .base-header-container {
    left: 0 !important;
    width: 100% !important;
  }
}

// 暗黑主题
.base-header-container.dark-theme {
  background-color: #1e1e1e;
  border-bottom-color: #3a3a3a;

  .header-btn {
    color: rgba(229, 229, 229, 0.8);

    &:hover {
      background-color: #3a3a3a;
      color: #e5e5e5;
    }

    &:focus-visible {
      box-shadow: 0 0 0 1px #555;
    }
  }

  .user-avatar-wrapper {
    &:hover {
      background-color: #3a3a3a;
    }
  }

  :deep(.el-breadcrumb__inner) {
    color: #a0a0a0;
  }

  :deep(.el-breadcrumb__separator) {
    color: #666;
  }

  :deep(.el-tag) {
    background-color: #2a2a2a;
    border-color: #3a3a3a;
    color: #e5e5e5;
  }
}
</style>
