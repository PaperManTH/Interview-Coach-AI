import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

// 路由统一收口在 pages。每个页面自身负责 UI 与状态。
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/pages/Home.vue'),
    meta: { title: 'Home' }
  },
  {
    path: '/interview/:scene',
    name: 'Interview',
    component: () => import('@/pages/Interview.vue'),
    meta: { title: 'Interview Session' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/pages/Settings.vue'),
    meta: { title: 'Settings' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
