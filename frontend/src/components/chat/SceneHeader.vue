<script setup lang="ts">
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
    <button class="back-btn" @click="goBack" aria-label="back">
      <span class="back-en">‹ Back</span>
      <span class="back-zh">返回</span>
    </button>
    <div class="title-wrap">
      <div class="icon" :style="{ background: accent }">{{ meta?.icon ?? '🎙️' }}</div>
      <div class="titles">
        <div class="title-bilingual">
          <span class="en">{{ meta?.title ?? 'Interview Session' }}</span>
          <span class="zh">{{ meta?.titleZh ?? '面试会话' }}</span>
        </div>
        <div class="desc-bilingual">
          <span class="en">{{ meta?.description ?? 'Practice your English interview in real-time.' }}</span>
          <span class="zh">{{ meta?.descriptionZh ?? '实时练习您的英语面试。' }}</span>
        </div>
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
  display: flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: #f1f5f9;
  color: #334155;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
}
.back-btn:hover { background: #e2e8f0; }
.back-en {
  font-weight: 600;
}
.back-zh {
  font-size: 12px;
  opacity: 0.8;
}
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
.titles {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.title-bilingual {
  display: flex;
  flex-direction: column;
}
.title-bilingual .en {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}
.title-bilingual .zh {
  font-size: 12px;
  color: #64748b;
}
.desc-bilingual {
  display: flex;
  flex-direction: column;
}
.desc-bilingual .en {
  font-size: 13px;
  color: #64748b;
}
.desc-bilingual .zh {
  font-size: 11px;
  color: #94a3b8;
}
</style>
