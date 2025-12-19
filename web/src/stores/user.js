import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

// 登录状态持久化和鉴权
export const useUserStore = defineStore('user', () => {
    const user = ref(null) // 存储用户信息

    const name = computed(() => user.value?.user?.username || '')

    // 获取用户角色，默认为用户
    const role = computed(() => user.value?.user?.role || 'user')

    // 判断是否为用户
    const isUser = computed(() => role.value === 'user')

    // 判断是否为管理员
    const isAdmin = computed(() => role.value === 'admin')

    // JWT token的登录状态检查
    const logged = computed(() => {
        if (user.value === null) return false
        return user.value?.token !== null && user.value?.token !== undefined
    })

    // 刷新页面后从本地存储恢复登录状态
    const granted = () => {
        return logged.value
    }

    const login = (data) => {
        user.value = data // 设置用户信息
        cache('user', data)
    }

    const logout = () => {
        user.value = null // 清除用户信息
        clear('user')
    }

    // 更新当前用户信息
    const updateUser = (userData) => {
        if (user.value && user.value.user) {
            user.value = {
                ...user.value,
                user: { ...user.value.user, ...userData }
            }
            cache('user', user.value)
        }
    }

    const load = (key) => {
        return JSON.parse(window.localStorage.getItem(key))
    }

    // 本地存储管理
    const cache = (key, value) => {
        window.localStorage.setItem(key, JSON.stringify(value))
    }

    const clear = (key) => {
        window.localStorage.removeItem(key)
    }

    // 初始化：从本地存储恢复用户信息
    const storedUser = window.localStorage.getItem('user')
    if (storedUser) {
        try {
            const parsedUser = JSON.parse(storedUser)
            user.value = parsedUser

            // JWT token由Spring Security自动验证，不需要检查过期时间
        } catch (error) {
            // 解析存储的用户数据失败，清除用户信息
            console.error('Failed to parse stored user data:', error)
            clear('user')
        }
    }

    return { user,name,logged,isUser,isAdmin,
        login,logout,updateUser,load,cache,
        clear,granted }
})
