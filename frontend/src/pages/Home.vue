<script setup lang="ts">
// 首页：场景选择
import { INTERVIEW_SCENES } from '@/types/scene';
import SceneCard from '@/components/scene/SceneCard.vue';
import { useRouter } from 'vue-router';

const router = useRouter();
</script>

<template>
  <div class="home">
    <header class="header">
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
        <div class="tag">AI Coach · Real-time · English</div>
        <h1>Practice your English interview with an AI coach.</h1>
        <p>Choose a scenario and start a live conversation — follow-up questions included.</p>
      </div>
    </section>
    <section class="grid">
      <SceneCard v-for="scene in INTERVIEW_SCENES" :key="scene.key" :scene="scene" />
    </section>
  </div>
</template>

<style scoped>
.home {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #eef2ff 100%);
  padding: 24px 32px 80px;
}

.header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 24px;
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
.hero { display: flex; justify-content: center; }
.hero-inner { max-width: 780px; text-align: center; }
.tag {
  display: inline-block;
  padding: 4px 12px;
  background: #dbeafe;
  color: #1d4ed8;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.6px;
  margin-bottom: 16px;
}
h1 { font-size: 34px; margin: 0 0 10px; color: #0f172a; line-height: 1.25; }
p { color: #475569; font-size: 15.5px; }
.grid {
  margin: 48px auto 0;
  max-width: 980px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}
</style>
