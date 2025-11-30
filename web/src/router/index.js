import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'index',
      component: () => import('@/views/userView/IndexView.vue'),
      // 处理面包屑
      meta: {
        breadcrumbParent: '内容发布',
        breadcrumbCurrent: '发布新内容',
      },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { HideChrome: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
      meta: { HideChrome: true },
    },
    {
      path: '/denied',
      name: 'denied',
      component: () => import('@/views/DeniedView.vue'),
      meta: { HideChrome: true },
    },
    // 自动审核路由
    {
      path: '/auto-review/text',
      name: 'auto-review-text',
      component: () => import('@/views/auto-review/TextReview.vue'),
      meta: {
        breadcrumbParent: '自动审核',
        breadcrumbCurrent: '文本审核',
      },
    },
    {
      path: '/auto-review/image',
      name: 'auto-review-image',
      component: () => import('@/views/auto-review/ImageReview.vue'),
      meta: {
        breadcrumbParent: '自动审核',
        breadcrumbCurrent: '图像审核',
      },
    },
    {
      path: '/auto-review/video',
      name: 'auto-review-video',
      component: () => import('@/views/auto-review/VideoReview.vue'),
      meta: {
        breadcrumbParent: '自动审核',
        breadcrumbCurrent: '视频审核',
      },
    },
    // 用户
    // 个人信息路由
    {
      path: '/Personal',
      name: 'Personal',
      component: () => import('@/views/userView/PersonalView.vue'),
      meta: {
        breadcrumbParent: '用户中心',
        breadcrumbCurrent: '个人信息',
      },
    },
  ]
})

// 路由守卫
router.beforeEach((to) => {
    const userStore = useUserStore()

    // 检查是否需要登录
    if (to.name !== 'login' && to.name !== 'register' && !userStore.logged) {
        return { name: 'login' }
    }

    // 如果已登录但token过期，自动退出
    if (userStore.logged && userStore.user?.token?.expireAt) {
        const now = new Date()
        const expireAt = new Date(userStore.user.token.expireAt)
        if (now > expireAt) {
            userStore.logout()
            return { name: 'login' }
        }
    }
})


export default router
