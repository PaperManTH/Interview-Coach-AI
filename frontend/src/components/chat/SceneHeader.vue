<script setup lang="ts">
// 场景标题条：显示当前场景名/子标题 + 返回按钮
import { useRouter } from 'vue-router';
import { computed } from 'vue';
import { getSceneMeta } from '@/types/scene';

const props = defineProps<{ sceneKey: string | null }>();
const router = useRouter();

const meta = computed(() => (props.sceneKey ? getSceneMeta(props.sceneKey) : undefined));
const accent = computed(() => meta.value?.accent ?? '#2563eb');

function goBack() {
  router.push('/');
}
</script>

<template>
  <header class="scene-header" :style="{ borderColor: accent }">
    <button class="back-btn" @click="goBack" aria-label="back">‹ Back</button>
    <div class="title-wrap">
      <div class="icon" :style="{ background: accent }">{{ meta?.icon ?? '🎙️' }}</div>
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
  gap: 16px;
  padding: 16px 24px;
  background: #ffffff;
  border-bottom: 3px solid #2563eb;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}
.back-btn {
  border: none;
  background: #f1f5f9;
  color: #334155;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
}
.back-btn:hover { background: #e2e8f0; }
.title-wrap { display: flex; align-items: center; gap: 14px; flex: 1; }
.icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}
.titles h1 { margin: 0; font-size: 18px; color: #0f172a; }
.titles p { margin: 2px 0 0; font-size: 13px; color: #64748b; }
</style>
