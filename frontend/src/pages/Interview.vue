<script setup lang="ts">
import bgPng from '@/assets/background.png';
import { onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useInterviewStore } from '@/stores/interviewStore';
import SceneHeader from '@/components/chat/SceneHeader.vue';
import ChatMessageList from '@/components/chat/ChatMessageList.vue';
import StatusBar from '@/components/chat/StatusBar.vue';
import ChatInput from '@/components/chat/ChatInput.vue';
import { isValidSceneKey } from '@/types/scene';
import type { InterviewFocus } from '@/types/scene';

const route = useRoute();
const router = useRouter();
const store = useInterviewStore();

const isDynamicMode = computed(() => route.name === 'DynamicInterview');
const busy = computed(() => store.status !== 'idle' || store.isCompleted);

onMounted(async () => {
  // 支持通过 ?resumeSessionId=xxx 恢复之前的会话
  const resumeSessionId = route.query.resumeSessionId as string;
  if (resumeSessionId) {
    await store.resumeSession(resumeSessionId);
    return;
  }

  if (isDynamicMode.value) {
    const focus = (route.query.focus as InterviewFocus) || 'general';
    store.startDynamicSession(focus);
  } else {
    const scene = String(route.params.scene ?? '');
    if (!isValidSceneKey(scene)) {
      router.replace('/');
      return;
    }
    if (store.scene !== scene) store.startSession(scene);
  }
});

watch(() => route.params.scene, (scene) => {
  if (isDynamicMode.value) return;
  const s = String(scene ?? '');
  if (!isValidSceneKey(s)) return;
  store.startSession(s);
});
</script>

<template>
  <div class="page-wrapper" :style="{ '--bg-image': `url(${bgPng})` }">
    <div class="interview">
      <SceneHeader
        :scene-key="isDynamicMode ? null : store.scene"
        :is-dynamic="isDynamicMode"
        :current-stage="store.currentStage"
        :stage-progress="store.stageProgress"
        :stage-round="store.stageRound"
      />
      <ChatMessageList :messages="store.messages" />
      <StatusBar :status="store.status" :is-mic-active="store.isMicActive" />
      <ChatInput :disabled="busy" @send="store.sendUserMessage" @toggle-mic="store.toggleMic" />
    </div>
  </div>
</template>

<style scoped>
.page-wrapper {
  min-height: 100vh;
  width: 100%;
  background-image: var(--bg-image);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.page-wrapper::before {
  content: '';
  position: fixed;
  inset: 0;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.5) 0%, rgba(15, 23, 42, 0.25) 50%, rgba(15, 23, 42, 0.45) 100%);
  pointer-events: none;
  z-index: 0;
}

.interview {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100%;
  max-width: min(960px, 100%);
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.48);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-left: 1px solid rgba(148, 163, 184, 0.12);
  border-right: 1px solid rgba(148, 163, 184, 0.12);
}

@media (max-width: 640px) {
  .interview {
    border-left: none;
    border-right: none;
  }
}
</style>
