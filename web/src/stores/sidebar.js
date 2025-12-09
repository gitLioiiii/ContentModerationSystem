import { ref } from 'vue'
import { defineStore } from 'pinia'

// 侧边栏状态
export const useSidebarStore = defineStore('sidebar', () => {
  // 侧边栏是否隐藏
  const isHidden = ref(false)

  // 侧边栏是否折叠(只显示图标)
  const isCollapsed = ref(false)

  // 切换侧边栏显示/隐藏
  function toggle() {
    isHidden.value = !isHidden.value
  }

  // 显示侧边栏
  function show() {
    isHidden.value = false
  }

  // 隐藏侧边栏
  function hide() {
    isHidden.value = true
  }

  // 切换侧边栏折叠/展开
  function toggleCollapse() {
    isCollapsed.value = !isCollapsed.value
  }

  // 折叠侧边栏
  function collapse() {
    isCollapsed.value = true
  }

  // 展开侧边栏
  function expand() {
    isCollapsed.value = false
  }

  return {
    isHidden,
    isCollapsed,
    toggle,
    show,
    hide,
    toggleCollapse,
    collapse,
    expand,
  }
})
