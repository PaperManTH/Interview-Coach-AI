<script setup lang="ts">
import { INTERVIEW_SCENES } from '@/types/scene';
import SceneCard from '@/components/scene/SceneCard.vue';
import { useRouter } from 'vue-router';
import BilingualText from '@/components/BilingualText.vue';

const router = useRouter();
</script>

<template>
  <div class="home">
    <header class="header">
      <div class="logo">
        <span class="logo-icon">🎯</span>
        <div>
          <span class="logo-title">Interview Coach AI</span>
          <span class="logo-subtitle">AI 面试教练</span>
        </div>
      </div>
      <button class="settings-btn" @click="router.push('/settings')">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/>
          <circle cx="12" cy="12" r="3"/>
        </svg>
        <span>Settings</span>
      </button>
    </header>

    <section class="hero">
      <div class="hero-inner">
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
        <div class="feature-icon">💬</div>
        <BilingualText en="Real-time Conversation" zh="实时对话" />
        <p class="feature-desc">
          <BilingualText en="Engage in natural conversations" zh="进行自然对话" />
        </p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">🎙️</div>
        <BilingualText en="Voice Recognition" zh="语音识别" />
        <p class="feature-desc">
          <BilingualText en="Speak and get instant feedback" zh="说话并获得即时反馈" />
        </p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">🎯</div>
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
  font-size: 32px;
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
  font-size: 28px;
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
