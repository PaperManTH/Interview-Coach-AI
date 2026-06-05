<script setup lang="ts">
// Interview Chat 主页：组装 Header / ChatMessageList / StatusBar / ChatInput
import { onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useInterviewStore } from '@/stores/interviewStore';
import SceneHeader from '@/components/chat/SceneHeader.vue';
import ChatMessageList from '@/components/chat/ChatMessageList.vue';
import StatusBar from '@/components/chat/StatusBar.vue';
import ChatInput from '@/components/chat/ChatInput.vue';
import { isValidSceneKey } from '@/types/scene';

const route = useRoute();
const router = useRouter();
const store = useInterviewStore();

const sceneFromRoute = computed(() => String(route.params.scene ?? ''));
const busy = computed(() => store.status !== 'idle');

onMounted(() => {
  const scene = sceneFromRoute.value;
  if (!isValidSceneKey(scene)) {
    router.replace('/');
    return;
  }
  if (store.scene !== scene) store.startSession(scene);
});

// 路由参数变化时重新初始化会话（方便从首页切换到不同场景）
watch(sceneFromRoute, (scene) => {
  if (!isValidSceneKey(scene)) return;
  store.startSession(scene);
});
</script>

<template>
  <div class="interview">
    <SceneHeader :scene-key="store.scene" />
    <ChatMessageList :messages="store.messages" />
    <StatusBar :status="store.status" :is-mic-active="store.isMicActive" />
    <ChatInput :disabled="busy" @send="store.sendUserMessage" @toggle-mic="store.toggleMic" />
  </div>
</template>

<style scoped>
.interview {
  display: flex;
  flex-direction: column;
  height: 100vh;
}
</style>
