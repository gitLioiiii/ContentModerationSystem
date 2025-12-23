<template>
  <aside
    class="base-navigator"
    :class="{ 'is-collapsed': sidebarCollapsed, 'is-hidden': sidebarStore.isHidden }"
    :style="{
      width: sidebarCollapsed ? '60px' : '220px',
    }"
  >
    <!-- 左上角顶部Logo -->
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

    <!-- 菜单 收缩使用条件渲染-->
    <ElMenu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        :unique-opened="false"
        class="sidebar-menu"
        router
      >
        <!-- 发现 -->
        <ElMenuItem index="/" :route="{ name: 'index' }" class="menu-item">
          <i class="bi bi-camera menu-icon"></i>
          <span v-if="!sidebarCollapsed">发现</span>
        </ElMenuItem>

        <!-- 用户作品 -->
        <ElMenuItem index="work" :route="{ name: 'work' }" class="menu-item">
          <i class="bi bi-brush menu-icon"></i>
          <span v-if="!sidebarCollapsed">用户作品</span>
        </ElMenuItem>

        <!-- 关于我们 -->
        <ElMenuItem index="about_us" :route="{ name: 'about_us' }" class="menu-item">
          <i class="bi bi-lightning-charge menu-icon"></i>
          <span v-if="!sidebarCollapsed">关于我们</span>
        </ElMenuItem>

        <!-- 用户管理 -->
        <ElMenuItem index="users" :route="{ name: 'user_index' }" class="menu-item">
          <i class="bi bi-person menu-icon"></i>
          <span v-if="!sidebarCollapsed">用户管理</span>
        </ElMenuItem>

        <!-- AI审核 -->
        <ElSubMenu index="text">
          <template #title><i class="bi bi-anthropic menu-icon"></i><span v-if="!sidebarCollapsed">自动审核</span></template>
          <ElMenuItem index="text" :route="{ name: 'text' }" class="sub-menu-item">
            <i class="bi bi-fonts menu-icon"></i>
            <span v-if="!sidebarCollapsed">文本审核</span>
          </ElMenuItem>
          <ElMenuItem index="image" :route="{ name: 'image' }" class="sub-menu-item">
            <i class="bi bi-image menu-icon"></i>
            <span v-if="!sidebarCollapsed">图像审核</span>
          </ElMenuItem>
          <ElMenuItem index="video" :route="{ name: 'video' }" class="sub-menu-item">
            <i class="bi bi-camera-video menu-icon"></i>
            <span v-if="!sidebarCollapsed">视频抽帧审核</span>
          </ElMenuItem>
        </ElSubMenu>

        <!-- 人工审核 -->
        <ElSubMenu index="manual-review">
          <template #title><i class="bi bi-person-check menu-icon"></i><span v-if="!sidebarCollapsed">人工审核</span></template>
          <ElMenuItem index="record" :route="{ name: 'record' }" class="sub-menu-item">
            <i class="bi bi-clock-history menu-icon"></i>
            <span v-if="!sidebarCollapsed">审核记录</span>
          </ElMenuItem>
          <ElMenuItem index="queue" :route="{ name: 'queue' }" class="sub-menu-item">
            <i class="bi bi-list-ul menu-icon"></i>
            <span v-if="!sidebarCollapsed">待审核队列</span>
          </ElMenuItem>
        </ElSubMenu>

        <!-- 规则管理 -->
        <ElSubMenu index="sensitive">
          <template #title><i class="bi bi-bookmark menu-icon"></i><span v-if="!sidebarCollapsed">规则管理</span></template>
          <ElMenuItem index="list" :route="{ name: 'list' }" class="sub-menu-item">
              <i class="bi bi-file-earmark-text menu-icon"></i>
              <span v-if="!sidebarCollapsed">审核报表</span>
          </ElMenuItem>
          <ElMenuItem index="sensitive" :route="{ name: 'sensitive_index' }" class="sub-menu-item">
              <i class="bi bi-book menu-icon"></i>
              <span v-if="!sidebarCollapsed">敏感词库</span>
          </ElMenuItem>
          <!-- <ElMenuItem index="" class="sub-menu-item">
              <i class="bi bi-bar-chart menu-icon"></i>
              <span v-if="!sidebarCollapsed">审核阈值</span>
          </ElMenuItem> -->
        </ElSubMenu>
    </ElMenu>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ElMenu, ElMenuItem, ElSubMenu } from 'element-plus'
import { useSidebarStore } from '@/stores/sidebar'
import 'bootstrap-icons/font/bootstrap-icons.css'

const route = useRoute()
const sidebarStore = useSidebarStore()

// 侧边栏折叠状态 - 从 store 获取
const sidebarCollapsed = computed(() => sidebarStore.isCollapsed)

// 当前激活的菜单
const activeMenu = computed(() => route.path)
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

// Element Plus 菜单样式覆盖
.sidebar-menu {
  height: calc(100vh - 50px);
  padding: 0.5rem;
  overflow-y: auto;
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
