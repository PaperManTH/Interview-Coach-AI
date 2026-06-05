<script setup lang="ts">
// 场景卡片组件：渲染单个面试场景，点击跳转到 Interview 页
import { useRouter } from 'vue-router';
import type { InterviewSceneMeta } from '@/types/scene';

const props = defineProps<{ scene: InterviewSceneMeta }>();
const router = useRouter();

function goInterview() {
  router.push({ name: 'Interview', params: { scene: props.scene.key } });
}
</script>

<template>
  <button class="card" :style="{ borderColor: scene.accent }" @click="goInterview">
    <div class="icon" :style="{ background: scene.accent }">{{ scene.icon }}</div>
    <h3>{{ scene.title }}</h3>
    <p class="subtitle">{{ scene.subtitle }}</p>
    <p class="desc">{{ scene.description }}</p>
    <span class="cta" :style="{ color: scene.accent }">Start →</span>
  </button>
</template>

<style scoped>
.card {
  text-align: left;
  background: #fff;
  border: 2px solid transparent;
  border-radius: 18px;
  padding: 22px;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s, border-color 0.15s;
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.04);
  font-family: inherit;
}
.card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
}
.icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  margin-bottom: 10px;
}
h3 { margin: 0 0 4px; font-size: 18px; color: #0f172a; }
.subtitle { margin: 0; color: #475569; font-size: 13px; font-weight: 600; }
.desc { margin: 14px 0 18px; color: #64748b; font-size: 13.5px; line-height: 1.55; }
.cta { font-size: 13px; font-weight: 700; }
</style>
