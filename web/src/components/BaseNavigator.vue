<template>
  <aside
    class="base-navigator"
    :class="{ 'is-collapsed': sidebarCollapsed, 'is-hidden': sidebarStore.isHidden }"
    :style="{
      width: sidebarCollapsed ? '60px' : '220px',
    }"
  >
    <!-- Logo 区域 -->
    <div class="logo-section">
      <RouterLink class="logo-link" to="/" tabindex="-1">
        <div class="logo-image-wrapper">
          <img
            src="/favicon.ico"
            class="logo-img"
            alt="内容审核系统"
          />
        </div>
        <span v-if="!sidebarCollapsed" class="logo-text">内容审核系统</span>
      </RouterLink>
    </div>

    <!-- 菜单滚动区域 -->
    <ElScrollbar class="menu-scrollbar" @scroll="handleScroll">
      <!-- 顶部阴影 -->
      <div
        class="scrollbar-shadow scrollbar-top-shadow"
        :class="{ 'is-visible': showTopShadow }"
      ></div>

      <!-- 菜单 -->
      <ElMenu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        :unique-opened="false"
        class="sidebar-menu"
        router
      >
        <!-- 内容发布 -->
        <ElMenuItem index="/" class="menu-item">
          <template #title>
            <i class="bi bi-send menu-icon"></i>
            <span class="menu-title">内容发布</span>
          </template>
        </ElMenuItem>

        <!-- A. 自动审核 -->
        <ElSubMenu index="auto-review" class="sub-menu">
          <template #title>
            <i class="bi bi-check-circle menu-icon"></i>
            <span class="menu-title">自动审核</span>
          </template>
          <ElMenuItem index="/auto-review/text" class="sub-menu-item">
            <template #title>
              <i class="bi bi-fonts menu-icon"></i>
              <span>文本审核</span>
            </template>
          </ElMenuItem>
          <ElMenuItem index="/auto-review/image" class="sub-menu-item">
            <template #title>
              <i class="bi bi-image menu-icon"></i>
              <span>图像审核</span>
            </template>
          </ElMenuItem>
          <ElMenuItem index="/auto-review/video" class="sub-menu-item">
            <template #title>
              <i class="bi bi-camera-video menu-icon"></i>
              <span>视频审核</span>
            </template>
          </ElMenuItem>
        </ElSubMenu>

        <!-- B. 人工审核 -->
        <ElSubMenu index="manual-review" class="sub-menu">
          <template #title>
            <i class="bi bi-person-check menu-icon"></i>
            <span class="menu-title">人工审核</span>
          </template>
          <ElMenuItem index="/manual-review/queue" class="sub-menu-item">
            <template #title>
              <i class="bi bi-list-ul menu-icon"></i>
              <span>待审核队列</span>
            </template>
          </ElMenuItem>
          <ElMenuItem index="/manual-review/operation" class="sub-menu-item">
            <template #title>
              <i class="bi bi-check-square menu-icon"></i>
              <span>审核操作</span>
            </template>
          </ElMenuItem>
          <ElMenuItem index="/manual-review/history" class="sub-menu-item">
            <template #title>
              <i class="bi bi-clock-history menu-icon"></i>
              <span>审核记录</span>
            </template>
          </ElMenuItem>
        </ElSubMenu>

        <!-- C. 规则管理 -->
        <ElSubMenu index="rule-management" class="sub-menu">
          <template #title>
            <i class="bi bi-gear menu-icon"></i>
            <span class="menu-title">规则管理</span>
          </template>
          <ElMenuItem index="/rule-management/sensitive-words" class="sub-menu-item">
            <template #title>
              <i class="bi bi-book menu-icon"></i>
              <span>敏感词库</span>
            </template>
          </ElMenuItem>
          <ElMenuItem index="/rule-management/threshold" class="sub-menu-item">
            <template #title>
              <i class="bi bi-bar-chart menu-icon"></i>
              <span>审核阈值</span>
            </template>
          </ElMenuItem>
          <ElMenuItem index="/rule-management/report" class="sub-menu-item">
            <template #title>
              <i class="bi bi-file-earmark-text menu-icon"></i>
              <span>审核报表</span>
            </template>
          </ElMenuItem>
        </ElSubMenu>
      </ElMenu>

      <!-- 底部阴影 -->
      <div
        class="scrollbar-shadow scrollbar-bottom-shadow"
        :class="{ 'is-visible': showBottomShadow }"
      ></div>
    </ElScrollbar>
  </aside>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ElMenu, ElMenuItem, ElSubMenu, ElScrollbar } from 'element-plus'
import { useSidebarStore } from '@/stores/sidebar'
import 'bootstrap-icons/font/bootstrap-icons.css'

const route = useRoute()
const sidebarStore = useSidebarStore()

// 侧边栏折叠状态 - 从 store 获取
const sidebarCollapsed = computed(() => sidebarStore.isCollapsed)

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 滚动阴影显示状态
const showTopShadow = ref(false)
const showBottomShadow = ref(false)

// 处理滚动事件
const handleScroll = (event) => {
  const scrollTop = event.scrollTop
  const scrollHeight = event.scrollHeight
  const clientHeight = event.clientHeight

  showTopShadow.value = scrollTop > 10
  showBottomShadow.value = scrollTop + clientHeight < scrollHeight - 10
}
</script>

<style scoped lang="scss">
.base-navigator {
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  background-color: hsl(0 0% 100%);
  border-right: 1px solid hsl(214.3 31.8% 91.4%);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  z-index: 201;

  &.is-hidden {
    transform: translateX(-100%);
  }
}

// Logo 区域
.logo-section {
  height: 50px;
  border-bottom: 1px solid hsl(214.3 31.8% 91.4% / 0.5);
  display: flex;
  align-items: center;
  padding: 0 0.75rem;
}

.logo-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  width: 100%;
  transition: all 0.3s ease;

  &:hover {
    .logo-text {
      color: hsl(221.2 83.2% 53.3%);
    }
  }
}

.logo-image-wrapper {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo-text {
  color: hsl(222.2 84% 4.9%);
  font-size: 1.125rem;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.3s ease;
}

// 菜单滚动区域
.menu-scrollbar {
  height: calc(100vh - 50px);
  position: relative;

  :deep(.el-scrollbar__wrap) {
    overflow-x: hidden;
  }

  :deep(.el-scrollbar__view) {
    padding: 0.5rem;
  }
}

// 滚动阴影
.scrollbar-shadow {
  pointer-events: none;
  position: absolute;
  z-index: 10;
  height: 3rem;
  width: 100%;
  opacity: 0;
  transition: opacity 0.3s ease-in-out;
  left: 0;

  &.is-visible {
    opacity: 1;
  }
}

.scrollbar-top-shadow {
  top: 0;
  background: linear-gradient(to bottom, hsl(0 0% 100%), hsl(0 0% 100% / 0.9), transparent);
}

.scrollbar-bottom-shadow {
  bottom: 0;
  background: linear-gradient(to top, hsl(0 0% 100%), hsl(0 0% 100% / 0.9), transparent);
}

// Element Plus 菜单样式覆盖
.sidebar-menu {
  border: none;
  background-color: transparent;

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 40px;
    line-height: 40px;
    border-radius: 0.375rem;
    margin-bottom: 0.25rem;
    color: hsl(215.4 16.3% 46.9%);
    transition: all 0.15s ease;

    &:hover {
      background-color: hsl(210 40% 96.1%) !important;
      color: hsl(222.2 84% 4.9%);
    }

    &.is-active {
      background-color: hsl(210 40% 96.1%) !important;
      color: hsl(221.2 83.2% 53.3%) !important;
      font-weight: 500;
    }
  }

  // 菜单图标
  .menu-icon {
    font-size: 1.125rem;
    margin-right: 0.625rem;
    width: 1.125rem;
    height: 1.125rem;
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }

  // 菜单标题
  .menu-title {
    font-size: 0.875rem;
  }

  // 子菜单项
  :deep(.el-menu-item.sub-menu-item) {
    padding-left: 3rem !important;
    font-size: 0.875rem;

    .menu-icon {
      font-size: 1rem;
      margin-right: 0.5rem;
    }
  }

  // 子菜单图标箭头
  :deep(.el-sub-menu__icon-arrow) {
    font-size: 0.75rem;
    margin-top: -0.125rem;
    transition: transform 0.3s ease;
  }

  :deep(.el-sub-menu.is-opened .el-sub-menu__icon-arrow) {
    transform: rotate(180deg);
  }

  // 折叠状态
  &.el-menu--collapse {
    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      padding: 0 !important;
      display: flex;
      align-items: center;
      justify-content: center;

      .menu-icon {
        margin-right: 0;
      }
    }
  }

  // 移除默认的选中样式
  :deep(.el-menu-item.is-active) {
    &::before {
      display: none;
    }
  }
}

// 响应式
@media (max-width: 1024px) {
  .base-navigator {
    &:not(.is-hidden) {
      box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
    }
  }
}
</style>
