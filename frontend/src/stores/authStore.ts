/**
 * 认证状态管理 (Pinia Store)
 * 适配：JWT 无状态令牌 + State 防 CSRF
 */
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import {
  loginWithGithub,
  fetchUserInfo,
  logoutApi,
  buildGithubOAuthUrl,
} from '@/services/auth';

export const useAuthStore = defineStore('auth', () => {
  const userId = ref('');
  const username = ref('');
  const avatarUrl = ref('');
  const token = ref('');
  const loading = ref(false);

  const isLoggedIn = computed(() => !!token.value);

  /**
   * 初始化：从 localStorage 恢复 JWT，校验有效性。
   */
  async function init() {
    const saved = localStorage.getItem('auth_token');
    if (!saved) return;
    try {
      loading.value = true;
      const user = await fetchUserInfo(saved);
      userId.value = user.userId;
      username.value = user.username;
      avatarUrl.value = user.avatarUrl;
      token.value = saved;
    } catch {
      localStorage.removeItem('auth_token');
    } finally {
      loading.value = false;
    }
  }

  /**
   * 跳转到 GitHub OAuth 授权页（附带防 CSRF state）。
   */
  async function redirectToGithub() {
    try {
      const url = await buildGithubOAuthUrl();
      window.location.href = url;
    } catch (e) {
      console.error('[Auth] 获取 state 失败', e);
    }
  }

  /**
   * 用 GitHub 回调的 code + state 完成登录。
   * 注意：state 的前端校验已在 LoginCallback.vue 中完成（一次性消费），此处不再重复校验。
   */
  async function handleCallback(code: string) {
    const params = new URLSearchParams(window.location.search);
    const state = params.get('state') || '';

    loading.value = true;
    try {
      const user = await loginWithGithub(code, state);
      userId.value = user.userId;
      username.value = user.username;
      avatarUrl.value = user.avatarUrl;
      token.value = user.token;
      localStorage.setItem('auth_token', user.token);
    } finally {
      loading.value = false;
    }
  }

  /**
   * 登出（JWT 无状态，仅清除客户端存储）。
   */
  async function logout() {
    if (token.value) {
      try { await logoutApi(token.value); } catch { /* 忽略 */ }
    }
    userId.value = '';
    username.value = '';
    avatarUrl.value = '';
    token.value = '';
    localStorage.removeItem('auth_token');
    sessionStorage.removeItem('csrf_state');
  }

  return {
    userId,
    username,
    avatarUrl,
    token,
    loading,
    isLoggedIn,
    init,
    redirectToGithub,
    handleCallback,
    logout,
  };
});
