<script setup lang="ts">
import { INTERVIEW_SCENES } from '@/types/scene';
import SceneCard from '@/components/scene/SceneCard.vue';
import { useRouter } from 'vue-router';
import BilingualText from '@/components/BilingualText.vue';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const auth = useAuthStore();
</script>

<template>
  <div class="home">
    <header class="header">
      <div class="logo">
        <img src="@/assets/logo.png" class="logo-icon" alt="Interview Coach AI" />
        <div>
          <span class="logo-title">Interview Coach AI</span>
          <span class="logo-subtitle">AI 面试教练</span>
        </div>
      </div>
      <div class="header-actions">
        <button v-if="auth.isLoggedIn" class="settings-btn" @click="router.push('/settings')">
          <img src="@/assets/setting_icon.png" class="settings-icon" alt="Settings" />
          <span>Settings</span>
        </button>
        <div v-if="auth.isLoggedIn" class="user-info">
          <img v-if="auth.avatarUrl" :src="auth.avatarUrl" class="avatar" alt="" />
          <span class="username">{{ auth.username }}</span>
          <button class="logout-btn" @click="auth.logout()">退出</button>
        </div>
        <button v-else class="login-btn" @click="router.push('/login')">登录</button>
      </div>
    </header>

    <section class="hero">
      <div class="hero-inner">
        <div v-if="!auth.isLoggedIn" class="login-hint">
          <button class="hero-login-btn" @click="router.push('/login')">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
              <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z"/>
            </svg>
            登录后开始面试练习
          </button>
        </div>
        <div class="tag">
          <span class="tag-icon">✨</span>
          <span>AI Coach · Real-time · English</span>
          <span class="tag-zh">AI 教练 · 实时 · 英语</span>
        </div>
        
        <h1 class="hero-title">
          <BilingualText 
            en="Practice your English interview with an AI coach." 
            zh="与 AI 教练一起练习英语面试" 
          />
        </h1>
        
        <p class="hero-description">
          <BilingualText 
            en="Choose a scenario and start a live conversation — follow-up questions included." 
            zh="选择一个场景，开始实时对话 - 包含跟进问题" 
          />
        </p>
      </div>
    </section>

    <section class="features">
      <div class="feature-card">
        <img src="@/assets/chat_icon.png" class="feature-icon" alt="Chat" />
        <BilingualText en="Real-time Conversation" zh="实时对话" />
        <p class="feature-desc">
          <BilingualText en="Engage in natural conversations" zh="进行自然对话" />
        </p>
      </div>
      <div class="feature-card">
        <img src="@/assets/voice_icon.png" class="feature-icon" alt="Voice" />
        <BilingualText en="Voice Recognition" zh="语音识别" />
        <p class="feature-desc">
          <BilingualText en="Speak and get instant feedback" zh="说话并获得即时反馈" />
        </p>
      </div>
      <div class="feature-card">
        <img src="@/assets/tool_icon.png" class="feature-icon" alt="Scenarios" />
        <BilingualText en="Multiple Scenarios" zh="多种场景" />
        <p class="feature-desc">
          <BilingualText en="HR, technical, and pressure interviews" zh="HR、技术、压力面试" />
        </p>
      </div>
    </section>

    <section class="section-header">
      <BilingualText class="large" en="Choose Your Scenario" zh="选择你的场景" />
    </section>

    <section class="grid">
      <SceneCard v-for="scene in INTERVIEW_SCENES" :key="scene.key" :scene="scene" />
    </section>

    <footer class="footer">
      <BilingualText en="Practice makes perfect" zh="熟能生巧" />
    </footer>
  </div>
</template>

<style scoped>
.home {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #eef2ff 50%, #f8fafc 100%);
  padding: 24px 32px 80px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 48px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
}

.logo-title {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.logo-subtitle {
  display: block;
  font-size: 12px;
  color: #64748b;
}

.settings-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #64748b;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.settings-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  color: #334155;
}

.settings-icon {
  width: 18px;
  height: 18px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 28px; height: 28px;
  border-radius: 50%;
  border: 2px solid #e2e8f0;
}

.username {
  font-size: 13px;
  color: #334155;
  font-weight: 500;
}

.logout-btn {
  padding: 4px 10px;
  background: none;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  color: #94a3b8;
  font-size: 12px;
  cursor: pointer;
}
.logout-btn:hover { color: #ef4444; border-color: #ef4444; }

.login-btn {
  padding: 8px 20px;
  background: #1e293b;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.login-btn:hover { background: #334155; }

.login-hint {
  margin-bottom: 20px;
}

.hero-login-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: #238636;
  color: #fff;
  border: 1px solid #2ea043;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
}
.hero-login-btn:hover { background: #2ea043; }

.hero {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
}

.hero-inner {
  max-width: 780px;
  text-align: center;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  color: #1d4ed8;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 20px;
}

.tag-icon {
  font-size: 14px;
}

.tag-zh {
  color: #3b82f6;
  opacity: 0.8;
}

.hero-title {
  margin-bottom: 16px;
}

.hero-title :deep(.en) {
  font-size: 36px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
}

.hero-title :deep(.zh) {
  font-size: 20px;
  color: #475569;
}

.hero-description {
  max-width: 500px;
  margin: 0 auto;
}

.hero-description :deep(.en) {
  font-size: 16px;
  color: #64748b;
}

.hero-description :deep(.zh) {
  font-size: 14px;
  color: #94a3b8;
}

.features {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 48px;
  flex-wrap: wrap;
}

.feature-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  min-width: 140px;
}

.feature-icon {
  width: 32px;
  height: 32px;
  margin-bottom: 12px;
}

.feature-card :deep(.en) {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.feature-card :deep(.zh) {
  font-size: 12px;
  color: #64748b;
}

.feature-desc {
  margin-top: 8px;
  text-align: center;
}

.feature-desc :deep(.en) {
  font-size: 12px;
  color: #94a3b8;
}

.feature-desc :deep(.zh) {
  font-size: 11px;
  color: #94a3b8;
}

.section-header {
  text-align: center;
  margin-bottom: 24px;
}

.section-header :deep(.en) {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
}

.section-header :deep(.zh) {
  font-size: 14px;
  color: #64748b;
}

.grid {
  margin: 0 auto;
  max-width: 980px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}

.footer {
  text-align: center;
  margin-top: 60px;
  padding-top: 32px;
  border-top: 1px solid #e2e8f0;
}

.footer :deep(.en) {
  font-size: 14px;
  color: #94a3b8;
}

.footer :deep(.zh) {
  font-size: 12px;
  color: #cbd5e1;
}

@media (max-width: 768px) {
  .home {
    padding: 16px 20px 60px;
  }

  .hero-title :deep(.en) {
    font-size: 28px;
  }

  .hero-title :deep(.zh) {
    font-size: 16px;
  }

  .features {
    gap: 16px;
  }

  .feature-card {
    min-width: 120px;
    padding: 16px 20px;
  }
}
</style>
