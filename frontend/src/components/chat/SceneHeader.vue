<script setup lang="ts">
// 场景标题条：显示当前场景名/子标题 + 返回按钮
import { useRouter } from 'vue-router';
import { computed } from 'vue';
import { getSceneMeta } from '@/types/scene';
import voiceIcon from '@/assets/voice_icon.png';

const props = defineProps<{ sceneKey: string | null }>();
const router = useRouter();

const meta = computed(() => (props.sceneKey ? getSceneMeta(props.sceneKey) : undefined));
const accent = computed(() => meta.value?.accent ?? '#2563eb');
const iconSrc = computed(() => meta.value?.icon ?? voiceIcon);

function goBack() {
  router.push('/');
}
</script>

<template>
  <header class="scene-header" :style="{ '--accent': accent }">
    <button class="back-btn" @click="goBack" aria-label="back">
      <span class="back-icon">‹</span>
      <span>Back</span>
    </button>
    <div class="title-wrap">
      <div class="icon"><img :src="iconSrc" alt="" /></div>
      <div class="titles">
        <h1>{{ meta?.title ?? 'Interview Session' }}</h1>
        <p>{{ meta?.subtitle ?? 'Practice your English interview in real-time.' }}</p>
      </div>
    </div>
  </header>
</template>

<style scoped>
.scene-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 18px 32px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
  position: relative;
}
.scene-header::after {
  content: '';
  position: absolute;
  left: 32px;
  right: 32px;
  bottom: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--accent), transparent);
  opacity: 0.6;
  border-radius: 2px;
}
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #475569;
  padding: 8px 16px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}
.back-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  color: #1e293b;
  transform: translateX(-2px);
}
.back-icon { font-size: 20px; line-height: 1; }
.title-wrap { display: flex; align-items: center; gap: 16px; flex: 1; }
.icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--accent), color-mix(in srgb, var(--accent) 70%, #000));
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.1);
  padding: 8px;
}
.icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  filter: brightness(0) invert(1);
}
.titles h1 { margin: 0; font-size: 19px; font-weight: 700; color: #0f172a; letter-spacing: -0.01em; }
.titles p { margin: 3px 0 0; font-size: 13px; color: #64748b; }

@media (max-width: 640px) {
  .scene-header { padding: 14px 20px; gap: 12px; }
  .scene-header::after { left: 20px; right: 20px; }
  .back-btn span:not(.back-icon) { display: none; }
  .icon { width: 40px; height: 40px; padding: 6px; border-radius: 12px; }
  .titles h1 { font-size: 16px; }
  .titles p { font-size: 12px; }
}
</style>
