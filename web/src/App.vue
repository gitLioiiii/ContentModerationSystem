<template>
  <div class="app-container" :class="{ 'no-chrome': HideChrome }">
    <BaseHeader v-if="!HideChrome" />
    <BaseSidebar v-if="!HideChrome" />
    <main
      :class="[
        'base-main',
        {
          'no-component': HideChrome,
          'sidebar-hidden': sidebarStore.isHidden,
          'sidebar-collapsed': sidebarStore.isCollapsed,
        },
      ]"
    >
      <RouterView />
    </main>
    <BaseFooter v-if="!HideChrome" />
  </div>
</template>

<script setup>
import { RouterView, useRoute } from 'vue-router'
import { computed } from 'vue'
import { useSidebarStore } from '@/stores/sidebar'
import { useTheme } from '@/utils/useTheme'

import BaseHeader from '@/components/BaseHeader.vue'
import BaseSidebar from '@/components/BaseNavigator.vue'
import BaseFooter from '@/components/BaseFooter.vue'

const route = useRoute()
const sidebarStore = useSidebarStore()
const HideChrome = computed(() => route.meta?.HideChrome === true)

// 改变颜色主题
useTheme()
</script>

<style lang="scss">
.el-pagination {
  margin: 2rem 0rem;
}

body {
  margin: 0rem;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 应用容器 */
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 主内容区域 */
.base-main {
  flex: 1;
  margin-top: 50px; /* Header 高度 */
  margin-left: 220px; /* Sidebar 展开宽度 */
  padding: 2rem;
  background-color: #f5f7fa;
  min-height: calc(100vh - 50px);
  transition: margin-left 0.3s ease;
}

.base-main.sidebar-hidden {
  margin-left: 0;
}

.base-main.sidebar-collapsed {
  margin-left: 60px; /* Sidebar 折叠宽度 */
}

.base-main.sidebar-hidden.sidebar-collapsed {
  margin-left: 0;
}

.base-main.no-component {
  margin-top: 0;
  margin-left: 0;
  padding: 0;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .base-main {
    margin-left: 220px; /* 移动端默认展开宽度 */
  }

  .base-main.sidebar-collapsed {
    margin-left: 60px; /* 移动端折叠宽度 */
  }
}

/* ======= 暗黑主题全局样式 ======= */

/* 主内容区域 */
html.dark-theme .base-main {
  background-color: #121212;
}

html.dark-theme body {
  background-color: #121212;
}

/* Element Plus 暗黑主题 */
html.dark-theme {
  --el-bg-color: #1e1e1e;
  --el-bg-color-overlay: #2a2a2a;
  --el-bg-color-page: #121212;
  --el-text-color-primary: #e5e5e5;
  --el-text-color-regular: #ccc;
  --el-text-color-secondary: #a0a0a0;
  --el-text-color-placeholder: #6a6a6a;
  --el-border-color: #3a3a3a;
  --el-border-color-light: #3a3a3a;
  --el-border-color-lighter: #2a2a2a;
  --el-border-color-extra-light: #2a2a2a;
  --el-fill-color: #2a2a2a;
  --el-fill-color-light: #333;
  --el-fill-color-lighter: #3a3a3a;
  --el-fill-color-blank: #1e1e1e;
  --el-mask-color: rgba(0, 0, 0, 0.7);
  --el-box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.3);
  --el-box-shadow-light: 0 2px 8px 0 rgba(0, 0, 0, 0.2);
  --el-box-shadow-lighter: 0 1px 4px 0 rgba(0, 0, 0, 0.15);

  color-scheme: dark;
}
</style>
