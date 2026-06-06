<script setup lang="ts">
import { watch, nextTick, ref } from 'vue';
import type { ChatMessage } from '@/types/chat';
import ChatMessageComponent from './ChatMessage.vue';
import chatPng from '@/assets/chat_icon.png';

const props = defineProps<{ messages: ChatMessage[] }>();
const scrollEl = ref<HTMLDivElement | null>(null);

async function scrollToBottom() {
  await nextTick();
  if (scrollEl.value) scrollEl.value.scrollTop = scrollEl.value.scrollHeight;
}

watch(
  () => [props.messages.length, props.messages.map((m) => m.content.length).join(',')],
  scrollToBottom,
  { immediate: true }
);
</script>

<template>
  <div ref="scrollEl" class="chat-list">
    <div v-if="messages.length === 0" class="empty">
      <div class="empty-icon">
        <img :src="chatPng" alt="" />
      </div>
      <p class="empty-title">面试即将开始</p>
      <p class="empty-desc">面试官正在准备中，请稍候...</p>
    </div>
    <ChatMessageComponent
      v-for="msg in messages"
      :key="msg.id"
      :message="msg"
    />
  </div>
</template>

<style scoped>
.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px clamp(16px, 4vw, 32px);
  scroll-behavior: smooth;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 12px;
  opacity: 0.6;
}
.empty-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.12), rgba(99, 102, 241, 0.08));
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  margin-bottom: 4px;
}
.empty-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  opacity: 0.5;
}
.empty-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #475569;
}
.empty-desc {
  margin: 0;
  font-size: 13px;
  color: #94a3b8;
}

@media (max-width: 640px) {
  .chat-list { padding: 16px 12px; }
}
</style>
