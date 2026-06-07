import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import type { Pinia } from 'pinia';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/pages/Home.vue'),
    meta: { title: 'Home' },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/Login.vue'),
    meta: { title: 'Login', guest: true },
  },
  {
    path: '/login/callback',
    name: 'LoginCallback',
    component: () => import('@/pages/LoginCallback.vue'),
    meta: { title: 'Logging in...', guest: true },
  },
  {
    path: '/interview/:scene?',
    name: 'Interview',
    component: () => import('@/pages/Interview.vue'),
    meta: { title: 'Interview Session', requiresAuth: true },
  },
  {
    path: '/interview/dynamic',
    name: 'DynamicInterview',
    component: () => import('@/pages/Interview.vue'),
    meta: { title: 'Dynamic Interview', requiresAuth: true },
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/pages/Settings.vue'),
    meta: { title: 'Settings', requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/pages/Profile.vue'),
    meta: { title: 'Profile', requiresAuth: true },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

/** Pinia 实例引用，由 main.ts 注入 */
let piniaInstance: Pinia | null = null;

export function injectPinia(pinia: Pinia) {
  piniaInstance = pinia;
}

// 路由守卫
router.beforeEach(async (to) => {
  const auth = useAuthStore(piniaInstance!);

  // 从 localStorage 恢复 token
  if (!auth.token) {
    const saved = localStorage.getItem('auth_token');
    if (saved) {
      try {
        const { fetchUserInfo } = await import('@/services/auth');
        const user = await fetchUserInfo(saved);
        auth.$patch({
          userId: user.userId,
          username: user.username,
          avatarUrl: user.avatarUrl,
          token: saved,
        });
      } catch {
        localStorage.removeItem('auth_token');
      }
    }
  }

  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { name: 'Login', query: { redirect: to.fullPath } };
  }
  if (to.meta.guest && auth.isLoggedIn) {
    return '/';
  }
});

export default router;
