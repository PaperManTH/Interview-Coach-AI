<script setup lang="ts">
// 单条消息气泡：区分用户/AI/系统，支持流式输出光标和语音条样式
import type { ChatMessage } from '@/types/chat';

defineProps<{ message: ChatMessage }>();
</script>

<template>
  <!-- 系统消息：居中灰色展示 -->
  <div v-if="message.role === 'system'" class="system-message">
    <span class="system-content">{{ message.content }}</span>
  </div>
  
  <!-- 用户/AI 消息：气泡展示 -->
  <div v-else class="bubble-row" :class="message.role">
    <div class="bubble" :class="{ streaming: message.streaming, voice: message.isVoice }">
      <div class="role">
        <span v-if="message.isVoice" class="voice-icon">🎙</span>
        {{ message.role === 'ai' ? 'Interviewer' : 'You' }}
      </div>
      <p class="content">{{ message.content }}<span v-if="message.streaming" class="caret">▍</span></p>
    </div>
  </div>
</template>

<style scoped>
/* 系统消息：居中灰色，类似微信时间提示 */
.system-message {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 12px 0;
}
.system-content {
  padding: 6px 16px;
  background: rgba(0, 0, 0, 0.06);
  border-radius: 20px;
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
}

/* 用户/AI 消息气泡 */
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
  transition: all 0.2s ease;
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
.bubble.voice {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25);
}
.bubble.streaming { outline: 1px dashed #cbd5e1; }
.role {
  font-size: 11px;
  opacity: 0.75;
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.voice-icon {
  display: inline-block;
  font-size: 12px;
  animation: pulse 1.2s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.7; }
}
.content { margin: 0; white-space: pre-wrap; word-break: break-word; }
.caret { margin-left: 2px; animation: blink 1s steps(1) infinite; }
@keyframes blink { 50% { opacity: 0; } }
</style>
