<script setup lang="ts">
// 首页：场景选择
import { INTERVIEW_SCENES } from '@/types/scene';
import SceneCard from '@/components/scene/SceneCard.vue';
</script>

<template>
  <div class="home">
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
  padding: 48px 32px 80px;
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
