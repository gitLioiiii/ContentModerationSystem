import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 发现
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
    // 关于我们
    {
      path: '/about_us',
      name: 'about_us',
      component: () => import('@/views/DeniedView.vue'),
      meta: {
        breadcrumbParent: '关于我们',
        breadcrumbCurrent: '关于我们页面',
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
      meta: {
        breadcrumbParent: '用户管理',
        breadcrumbCurrent: '用户管理页面',
      },
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
    {
      path: '/record',
      name: 'record',
      component: () => import('@/views/adminView/manual/RecordView.vue'),
      meta: {
        breadcrumbParent: '审核记录',
        breadcrumbCurrent: '审核记录页面',
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
    {
      path: '/list',
      name: 'list',
      component: () => import('@/views/adminView/sensitive/ReviewList.vue'),
      meta: {
        breadcrumbParent: '审核记录',
        breadcrumbCurrent: '审核记录页面',
      },
    },
    // 审核阈值配置
    {
      path: '/review-config',
      name: 'review_config',
      component: () => import('@/views/adminView/sensitive/ReviewConfigView.vue'),
      meta: {
        breadcrumbParent: '规则管理',
        breadcrumbCurrent: '审核阈值配置',
      },
    },
    // 聊天室
    {
      path: '/chat',
      name: 'chat',
      component: () => import('@/views/userView/ChatRoomView.vue'),
      meta: {
        breadcrumbParent: '发现',
        breadcrumbCurrent: '聊天室',
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
        breadcrumbCurrent: '个人中心',
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
