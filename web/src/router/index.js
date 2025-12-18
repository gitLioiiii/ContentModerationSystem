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
        breadcrumbParent: '发现',
        breadcrumbCurrent: '发现新内容',
      },
    },
    // 用户作品
    {
      path: '/work',
      name: 'work',
      component: () => import('@/views/userView/WorksView.vue'),
      meta: {
        breadcrumbParent: '作品',
        breadcrumbCurrent: '用户的作品页面',
      },
    },
    //用户管理
    {
      path: '/users',
      name: 'user_base',
      component: () => import('@/views/adminView/users/BaseView.vue'),
      children: [
        {
          path: "",
          name: 'user_index',
          component: () => import('@/views/adminView/users/IndexView.vue'),
        },
        {
          path: "create",
          name: 'user_create',
          component: () => import('@/views/adminView/users/CreateView.vue'),
        },
      ],
    },
    // AI审核
    {
      path: '/text',
      name: 'text',
      component: () => import('@/views/adminView/ai/TextReview.vue'),
      meta: {
        breadcrumbParent: '自动审核',
        breadcrumbCurrent: '文本审核',
      },
    },
    {
      path: '/image',
      name: 'image',
      component: () => import('@/views/adminView/ai/ImageReview.vue'),
      meta: {
        breadcrumbParent: '自动审核',
        breadcrumbCurrent: '图像审核',
      },
    },
    {
      path: '/video',
      name: 'video',
      component: () => import('@/views/adminView/ai/VideoReview.vue'),
      meta: {
        breadcrumbParent: '自动审核',
        breadcrumbCurrent: '视频抽帧审核',
      },
    },
    //人工审核
    {
      path: '/queue',
      name: 'queue',
      component: () => import('@/views/adminView/manual/ContentQueue.vue'),
      meta: {
        breadcrumbParent: '人工审核',
        breadcrumbCurrent: '待审核队列审核',
      },
    },
    // 敏感词管理
    {
      path: '/sensitive',
      name: 'sensitive_base',
      component: () => import('@/views/adminView/sensitive/BaseView.vue'),
      children: [
        {
          path: '',
          name: 'sensitive_index',
          component: () => import('@/views/adminView/sensitive/IndexView.vue'),
          meta: {
            breadcrumbParent: '规则管理',
            breadcrumbCurrent: '敏感词列表',
          },
        },
        {
          path: 'create',
          name: 'sensitive_create',
          component: () => import('@/views/adminView/sensitive/CreateView.vue'),
          meta: {
            breadcrumbParent: '规则管理',
            breadcrumbCurrent: '新增敏感词',
          },
        },
      ],
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
    //登录、注册
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
