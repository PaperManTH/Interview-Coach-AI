<script setup lang="ts">
// 单条消息气泡：区分用户/AI，支持流式输出光标
import type { ChatMessage } from '@/types/chat';

defineProps<{ message: ChatMessage }>();
</script>

<template>
  <div class="bubble-row" :class="message.role">
    <div class="bubble" :class="{ streaming: message.streaming }">
      <div class="role">{{ message.role === 'ai' ? 'Interviewer' : 'You' }}</div>
      <p class="content">{{ message.content }}<span v-if="message.streaming" class="caret">▍</span></p>
    </div>
  </div>
</template>

<style scoped>
.bubble-row { display: flex; margin-bottom: 14px; }
.bubble-row.user { justify-content: flex-end; }
.bubble-row.ai { justify-content: flex-start; }
.bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 14px;
  line-height: 1.55;
  font-size: 14.5px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
}
.bubble-row.user .bubble {
  background: #2563eb;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.bubble-row.ai .bubble {
  background: #fff;
  color: #0f172a;
  border-bottom-left-radius: 4px;
}
.bubble.streaming { outline: 1px dashed #cbd5e1; }
.role { font-size: 11px; opacity: 0.75; margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px; }
.content { margin: 0; white-space: pre-wrap; word-break: break-word; }
.caret { margin-left: 2px; animation: blink 1s steps(1) infinite; }
@keyframes blink { 50% { opacity: 0; } }
</style>
