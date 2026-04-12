import { ref, watchEffect } from 'vue'

// 全局单例主题状态（模块级别，所有组件共享同一个 ref）
const isDark = ref(localStorage.getItem('theme') === 'dark')

// 同步到 DOM 和 localStorage
watchEffect(() => {
  document.documentElement.classList.toggle('dark-theme', isDark.value)
  document.body.classList.toggle('dark-theme', isDark.value)
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
})

export function useTheme() {
  const toggleTheme = () => {
    isDark.value = !isDark.value
  }
  return { isDark, toggleTheme }
}
