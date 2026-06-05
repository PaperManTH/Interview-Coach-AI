<script setup lang="ts">
// 聊天记录列表：遍历消息并自动滚到底部
import { watch, nextTick, ref } from 'vue';
import type { ChatMessage } from '@/types/chat';
import ChatMessageComponent from './ChatMessage.vue';

const props = defineProps<{ messages: ChatMessage[] }>();
const scrollEl = ref<HTMLDivElement | null>(null);

async function scrollToBottom() {
  await nextTick();
  if (scrollEl.value) scrollEl.value.scrollTop = scrollEl.value.scrollHeight;
}

// 新消息或流式内容变化时滚动
watch(
  () => [props.messages.length, props.messages.map((m) => m.content.length).join(',')],
  scrollToBottom,
  { immediate: true }
);
</script>

<template>
  <div ref="scrollEl" class="chat-list">
    <div v-if="messages.length === 0" class="empty">
      <p>Waiting for the interviewer to start…</p>
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
  padding: 24px 32px;
  background: #f8fafc;
}
.empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #94a3b8;
  font-size: 14px;
}
</style>
