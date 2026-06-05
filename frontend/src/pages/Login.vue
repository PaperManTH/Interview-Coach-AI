<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const auth = useAuthStore();

function handleLogin() {
  auth.redirectToGithub();
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="logo-section">
        <span class="logo-icon">🎯</span>
        <h1>Interview Coach AI</h1>
        <p class="subtitle">AI 面试教练 · 实时英语面试练习</p>
      </div>

      <button class="github-btn" @click="handleLogin" :disabled="auth.loading">
        <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
          <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z"/>
        </svg>
        <span>{{ auth.loading ? '登录中...' : 'Sign in with GitHub' }}</span>
      </button>

      <p class="terms-text">
        登录即表示同意服务条款和隐私政策。API Key 仅加密保存于本地。
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0d1117 0%, #161b22 50%, #0d1117 100%);
}
.login-card {
  background: #21262d;
  border: 1px solid #30363d;
  border-radius: 12px;
  padding: 48px 40px;
  width: 400px;
  max-width: 90vw;
  text-align: center;
  box-shadow: 0 8px 24px rgba(0,0,0,0.3);
}
.logo-section { margin-bottom: 36px; }
.logo-icon { font-size: 48px; display: block; margin-bottom: 16px; }
h1 { color: #f0f6fc; font-size: 22px; margin: 0 0 8px; }
.subtitle { color: #8b949e; font-size: 14px; margin: 0; }

.github-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 32px;
  background: #238636;
  color: #fff;
  border: 1px solid #2ea043;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  width: 100%;
  justify-content: center;
}
.github-btn:hover { background: #2ea043; }
.github-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.terms-text {
  color: #484f58;
  font-size: 12px;
  margin: 24px 0 0;
  line-height: 1.5;
}
</style>
