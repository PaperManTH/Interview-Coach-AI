<script setup lang="ts">
import { useRouter } from 'vue-router';
import type { InterviewSceneMeta } from '@/types/scene';
import BilingualText from '@/components/BilingualText.vue';

const props = defineProps<{ scene: InterviewSceneMeta }>();
const router = useRouter();

function goInterview() {
  router.push({ name: 'Interview', params: { scene: props.scene.key } });
}
</script>

<template>
  <button class="card" :style="{ borderColor: scene.accent }" @click="goInterview">
    <div class="icon" :style="{ background: scene.accent }">{{ scene.icon }}</div>
    <h3>
      <BilingualText :en="scene.title" :zh="scene.titleZh" />
    </h3>
    <p class="desc">
      <BilingualText :en="scene.description" :zh="scene.descriptionZh" />
    </p>
    <span class="cta" :style="{ color: scene.accent }">
      <span class="cta-en">Start</span>
      <span class="cta-zh">开始</span>
      <span class="arrow">→</span>
    </span>
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
  margin-bottom: 14px;
}
h3 { 
  margin: 0 0 12px; 
}
h3 :deep(.en) {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}
h3 :deep(.zh) {
  font-size: 13px;
  color: #64748b;
}
.desc { 
  margin: 0 0 18px; 
}
.desc :deep(.en) {
  font-size: 13.5px;
  color: #475569;
  line-height: 1.55;
}
.desc :deep(.zh) {
  font-size: 12px;
  color: #94a3b8;
}
.cta { 
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 700;
}
.cta-en {
  color: inherit;
}
.cta-zh {
  font-size: 12px;
  opacity: 0.8;
  color: inherit;
}
.arrow {
  margin-left: 2px;
}
</style>
