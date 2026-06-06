<script setup lang="ts">
/**
 * GitHub OAuth2 回调页。
 * 校验 state（防 CSRF），用 code 换取 JWT，跳转首页。
 */
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { verifyCallbackState } from '@/services/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const error = ref('');
const loading = ref(true);

onMounted(async () => {
  const code = route.query.code as string;
  if (!code) {
    error.value = '缺少登录授权码，请重新登录';
    loading.value = false;
    return;
  }

  // 前端校验 state
  if (!verifyCallbackState()) {
    error.value = '安全校验失败，请重新登录';
    loading.value = false;
    return;
  }

  try {
    await auth.handleCallback(code);
    router.replace('/');
  } catch (e: any) {
    error.value = e.message || '登录失败，请重试';
    loading.value = false;
  }
});
</script>

<template>
  <div class="callback-page">
    <template v-if="loading">
      <div class="spinner"></div>
      <p>正在登录...</p>
    </template>
    <template v-else>
      <div class="error-icon">!</div>
      <p class="error-text">{{ error }}</p>
      <button class="retry-btn" @click="router.push('/login')">重新登录</button>
    </template>
  </div>
</template>

<style scoped>
.callback-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: #0d1117;
  color: #c9d1d9;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
.spinner {
  width: 36px; height: 36px;
  border: 3px solid #30363d;
  border-top-color: #58a6ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.error-icon {
  width: 48px; height: 48px;
  border-radius: 50%;
  background: #da3633;
  color: white;
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.error-text { font-size: 14px; color: #8b949e; }
.retry-btn {
  padding: 8px 20px;
  background: #238636;
  border: 1px solid #2ea043;
  border-radius: 6px;
  color: white;
  font-size: 14px;
  cursor: pointer;
}
.retry-btn:hover { background: #2ea043; }
</style>
