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

import BaseHeader from '@/components/BaseHeader.vue'
import BaseSidebar from '@/components/BaseNavigator.vue'
import BaseFooter from '@/components/BaseFooter.vue'

const route = useRoute()
const sidebarStore = useSidebarStore()
const HideChrome = computed(() => route.meta?.HideChrome === true)
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
</style>
