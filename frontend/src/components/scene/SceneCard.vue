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
  <button
    class="scene-card"
    :style="{ '--accent': scene.accent }"
    @click="goInterview"
    type="button"
  >
    <div class="top-row">
      <div class="icon-wrap">
        <img class="icon" :src="scene.icon" alt="" />
      </div>
      <span class="key-tag">{{ scene.key.toUpperCase() }}</span>
    </div>

    <div class="title">
      <BilingualText :en="scene.title" :zh="scene.titleZh" size="lg" />
    </div>

    <p class="desc">
      <BilingualText :en="scene.description" :zh="scene.descriptionZh" size="base" />
    </p>

    <div class="cta-row">
      <span class="cta">
        <span class="cta-text">Start</span>
        <span class="cta-zh">开始</span>
        <span class="arrow">→</span>
      </span>
    </div>
  </button>
</template>

<style scoped>
.scene-card {
  --card-pad: 24px;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: var(--card-pad);
  text-align: left;
  background: #fff;
  border: 1px solid var(--border-light, #e2e8f0);
  border-radius: var(--radius-lg, 16px);
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(15,23,42,0.06));
  cursor: pointer;
  transition: all 240ms cubic-bezier(0.16, 1, 0.3, 1);
  overflow: hidden;
  font-family: inherit;
}
.scene-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--accent), transparent);
  opacity: 0;
  transition: opacity 240ms;
}
.scene-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px -8px rgba(15, 23, 42, 0.12), 0 4px 10px -4px rgba(15, 23, 42, 0.06);
  border-color: var(--accent);
}
.scene-card:hover::before { opacity: 1; }

.top-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.icon-wrap {
  width: 52px; height: 52px;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  background: color-mix(in srgb, var(--accent) 14%, #fff);
  transition: all 240ms;
}
.scene-card:hover .icon-wrap {
  background: linear-gradient(135deg, var(--accent), color-mix(in srgb, var(--accent) 70%, #000));
}
.icon {
  width: 40px;
  height: 40px;
  object-fit: contain;
  display: block;
  transition: transform 240ms;
}
.scene-card:hover .icon { transform: scale(1.08); }

.key-tag {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 10%, #fff);
}

.title :deep(.en) { font-size: 19px; font-weight: 700; color: #0f172a; }
.title :deep(.zh) { font-size: 13px; color: #475569; }

.desc { margin: 0; flex: 1; }
.desc :deep(.en) {
  font-size: 14px;
  color: #475569;
  line-height: 1.55;
}
.desc :deep(.zh) {
  font-size: 12px;
  color: #94a3b8;
}

.cta-row { margin-top: auto; padding-top: 8px; border-top: 1px solid #f1f5f9; }
.cta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--accent);
  font-weight: 700;
  font-size: 14px;
}
.cta-zh { font-size: 12px; opacity: 0.8; font-weight: 500; }
.arrow {
  margin-left: 4px;
  transition: transform 200ms;
}
.scene-card:hover .arrow { transform: translateX(4px); }
</style>
