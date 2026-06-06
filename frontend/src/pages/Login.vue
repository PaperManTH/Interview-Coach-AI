<script setup lang="ts">
import logoPng from '@/assets/logo.png';
import bgPng from '@/assets/background.png';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const auth = useAuthStore();

function handleLogin() {
  auth.redirectToGithub();
}
</script>

<template>
  <div class="page-wrapper" :style="{ '--bg-image': `url(${bgPng})` }">
    <!-- 返回首页 — 固定在视口左上角 -->
    <button class="back-btn" @click="router.push('/')">
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="m15 18-6-6 6-6"/>
      </svg>
      <span>返回首页</span>
    </button>

    <div class="login-page">
      <div class="login-card">
        <!-- Logo -->
        <div class="logo-section">
          <img :src="logoPng" class="logo-icon" alt="logo" />
          <h1>Interview Coach AI</h1>
          <p class="subtitle">AI 面试教练 · 实时英语面试练习</p>
        </div>

        <!-- 分隔线 -->
        <div class="divider">
          <span class="divider-label">选择登录方式</span>
        </div>

        <!-- GitHub 登录按钮 -->
        <button class="github-btn" @click="handleLogin" :disabled="auth.loading">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z"/>
          </svg>
          <span>{{ auth.loading ? '登录中...' : 'Sign in with GitHub' }}</span>
        </button>

        <!-- 条款 -->
        <p class="terms-text">
          登录即表示同意服务条款和隐私政策
        </p>

      </div>
    </div>
  </div>
</template>

<style scoped>
/* ============ 全宽背景层 ============ */
.page-wrapper {
  min-height: 100vh;
  width: 100%;
  background-image: var(--bg-image);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

/* 背景暗色遮罩 */
.page-wrapper::before {
  content: '';
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, rgba(15, 23, 42, 0.55), rgba(30, 41, 59, 0.45), rgba(15, 23, 42, 0.55));
  pointer-events: none;
  z-index: 0;
}

/* ============ 内容层 ============ */
.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  padding: 40px clamp(16px, 4vw, 32px) 60px;
}

/* ============ 返回按钮 ============ */
.back-btn {
  position: absolute;
  top: 28px;
  left: clamp(16px, 4vw, 48px);
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 10px;
  color: #475569;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 150ms;
  backdrop-filter: blur(6px);
}
.back-btn:hover {
  background: rgba(241, 245, 249, 0.8);
  border-color: rgba(148, 163, 184, 0.5);
  color: #1e293b;
  transform: translateX(-2px);
}

/* ============ 登录卡片 ============ */
.login-card {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 20px;
  padding: 48px 40px;
  width: min(400px, 100%);
  text-align: center;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06), 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 200ms cubic-bezier(0.16, 1, 0.3, 1);
}
.login-card:hover {
  background: rgba(255, 255, 255, 0.58);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* ============ Logo 区域 ============ */
.logo-section {
  margin-bottom: 32px;
}
.logo-icon {
  width: 72px;
  height: 72px;
  object-fit: contain;
  display: block;
  margin: 0 auto 18px;
  border-radius: 18px;
  transition: transform 200ms cubic-bezier(0.16, 1, 0.3, 1);
}
.login-card:hover .logo-icon {
  transform: scale(1.05);
}
h1 {
  color: #0f172a;
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: -0.01em;
}
.subtitle {
  color: #64748b;
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}

/* ============ 分隔线 ============ */
.divider {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
}
.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(148, 163, 184, 0.3), transparent);
}
.divider-label {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

/* ============ GitHub 按钮 ============ */
.github-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 13px 32px;
  background: linear-gradient(135deg, #1e293b, #0f172a);
  color: #f1f5f9;
  border: 1px solid rgba(30, 41, 59, 0.3);
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 180ms cubic-bezier(0.16, 1, 0.3, 1);
  width: 100%;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.12);
}
.github-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #0f172a, #020617);
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.2);
}
.github-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.1);
}
.github-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

/* ============ 条款文本 ============ */
.terms-text {
  color: #94a3b8;
  font-size: 12px;
  margin: 0;
  line-height: 1.6;
}
.terms-text:first-of-type {
  margin-top: 24px;
}
.privacy-note {
  margin-top: 2px;
  font-size: 11.5px;
}
</style>
