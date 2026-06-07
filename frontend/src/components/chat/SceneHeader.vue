<script setup lang="ts">
import { useRouter } from 'vue-router';
import { computed } from 'vue';
import { getSceneMeta, getStageMeta, INTERVIEW_STAGES } from '@/types/scene';
import type { InterviewStage } from '@/types/scene';
import voiceIcon from '@/assets/voice_icon.png';
import homePng from '@/assets/hr_Interview.png';

const props = defineProps<{
  sceneKey: string | null;
  isDynamic?: boolean;
  currentStage?: InterviewStage | null;
  stageProgress?: number;
}>();

const router = useRouter();

const meta = computed(() => (props.sceneKey ? getSceneMeta(props.sceneKey) : undefined));
const accent = computed(() => {
  if (props.isDynamic && props.currentStage) {
    return getStageMeta(props.currentStage)?.accent ?? '#2563eb';
  }
  return meta.value?.accent ?? '#2563eb';
});
const iconSrc = computed(() => {
  if (props.isDynamic && props.currentStage) {
    return getStageMeta(props.currentStage)?.icon ?? voiceIcon;
  }
  return meta.value?.icon ?? voiceIcon;
});

/** 动态模式标题 */
const dynamicTitle = computed(() => {
  if (!props.isDynamic) return '';
  const stageMeta = props.currentStage ? getStageMeta(props.currentStage) : undefined;
  return stageMeta?.label ?? 'Dynamic Interview';
});

const dynamicSubtitle = computed(() => {
  if (!props.isDynamic) return '';
  const stageMeta = props.currentStage ? getStageMeta(props.currentStage) : undefined;
  return stageMeta?.descriptionZh ?? '动态面试流程';
});

function goBack() {
  router.push('/');
}
</script>

<template>
  <header class="scene-header" :style="{ '--accent': accent }">
    <button class="back-btn" @click="goBack" aria-label="back">
      <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="m15 18-6-6 6-6"/>
      </svg>
      <span>Back</span>
    </button>

    <div class="title-wrap">
      <div class="icon"><img :src="iconSrc" alt="" /></div>
      <div class="titles">
        <!-- 动态模式标题 -->
        <template v-if="isDynamic">
          <h1>{{ dynamicTitle }}</h1>
          <p>{{ dynamicSubtitle }}</p>
          <!-- 阶段进度指示器 -->
          <div class="stage-progress-bar">
            <div
              v-for="(stage, idx) in INTERVIEW_STAGES"
              :key="stage.key"
              class="stage-dot"
              :class="{
                active: currentStage === stage.key,
                completed: !currentStage || INTERVIEW_STAGES.findIndex(s => s.key === currentStage) > idx
              }"
              :style="{ '--dot-accent': stage.accent }"
              :title="stage.label + ' - ' + stage.labelZh"
            >
              {{ idx + 1 }}
            </div>
            <div class="progress-track">
              <div class="progress-fill" :style="{ width: (stageProgress ?? 0) + '%' }"></div>
            </div>
          </div>
        </template>

        <!-- 传统模式标题 -->
        <template v-else>
          <h1>{{ meta?.title ?? 'Interview Session' }}</h1>
          <p>{{ meta?.subtitle ?? 'Practice your English interview in real-time.' }}</p>
        </template>
      </div>
    </div>

    <div class="header-right">
      <button class="home-btn" @click="goBack" title="返回首页">
        <img :src="homePng" alt="home" />
      </button>
    </div>
  </header>
</template>

<style scoped>
.scene-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 24px;
  background: rgba(255, 255, 255, 0.55);
  backdrop-filter: blur(6px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
  flex-shrink: 0;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 1px solid rgba(148, 163, 184, 0.25);
  background: rgba(255, 255, 255, 0.6);
  color: #475569;
  padding: 8px 14px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 150ms;
  backdrop-filter: blur(4px);
  flex-shrink: 0;
}
.back-btn:hover {
  background: rgba(241, 245, 249, 0.8);
  border-color: rgba(148, 163, 184, 0.45);
  color: #1e293b;
  transform: translateX(-2px);
}

.title-wrap { display: flex; align-items: center; gap: 14px; flex: 1; min-width: 0; }
.icon {
  width: 42px; height: 42px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--accent), color-mix(in srgb, var(--accent) 70%, #000));
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.1);
  padding: 7px;
  flex-shrink: 0;
}
.icon img {
  width: 100%; height: 100%;
  object-fit: contain; display: block;
  filter: brightness(0) invert(1);
}
.titles { min-width: 0; }
.titles h1 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.01em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.titles p {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===== 阶段进度指示器 ===== */
.stage-progress-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
}
.stage-dot {
  width: 22px; height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: #94a3b8;
  background: rgba(148, 163, 184, 0.15);
  border: 1.5px solid rgba(148, 163, 184, 0.3);
  flex-shrink: 0;
  transition: all 300ms cubic-bezier(0.16, 1, 0.3, 1);
}
.stage-dot.active {
  color: #fff;
  background: var(--dot-accent, #2563eb);
  border-color: var(--dot-accent, #2563eb);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.2);
  transform: scale(1.1);
}
.stage-dot.completed {
  color: #fff;
  background: var(--dot-accent, #2563eb);
  border-color: var(--dot-accent, #2563eb);
  opacity: 0.7;
}

.progress-track {
  flex: 1;
  height: 3px;
  background: rgba(148, 163, 184, 0.2);
  border-radius: 2px;
  overflow: hidden;
  min-width: 40px;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #2563eb, #6366f1);
  border-radius: 2px;
  transition: width 400ms ease-out;
}

/* 右侧首页按钮 */
.header-right { flex-shrink: 0; }
.home-btn {
  width: 36px; height: 36px;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(255, 255, 255, 0.55);
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  padding: 7px;
  transition: all 150ms;
  backdrop-filter: blur(4px);
}
.home-btn:hover {
  background: rgba(241, 245, 249, 0.7);
  border-color: rgba(148, 163, 184, 0.4);
  transform: translateY(-1px);
}
.home-btn img {
  width: 100%; height: 100%;
  object-fit: contain; display: block;
  opacity: 0.6;
}
.home-btn:hover img { opacity: 0.85; }

@media (max-width: 640px) {
  .scene-header { padding: 12px 16px; gap: 10px; }
  .back-btn span { display: none; }
  .icon { width: 36px; height: 36px; padding: 6px; border-radius: 10px; }
  .titles h1 { font-size: 15px; }
  .titles p { display: none; }
  .stage-dot { width: 20px; height: 20px; font-size: 10px; }
}
</style>
