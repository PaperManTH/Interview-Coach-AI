<script setup lang="ts">
// 单条消息气泡：区分用户/AI/系统，支持流式光标 & 语音标记
import type { ChatMessage } from '@/types/chat';

defineProps<{ message: ChatMessage }>();
</script>

<template>
  <!-- 系统消息 -->
  <div v-if="message.role === 'system'" class="system-message">
    <span class="system-dot"></span>
    <span class="system-content">{{ message.content }}</span>
  </div>

  <!-- 用户/AI 气泡 -->
  <div v-else class="bubble-row" :class="message.role">
    <div v-if="message.role === 'ai'" class="avatar ai" aria-hidden="true">🤖</div>

    <div class="bubble-wrap">
      <div class="role-row">
        <span class="role-name">{{ message.role === 'ai' ? 'Interviewer' : 'You' }}</span>
        <span v-if="message.isVoice" class="voice-badge">
          <span class="voice-dot"></span>
          Voice
        </span>
      </div>

      <div class="bubble" :class="{ streaming: message.streaming, voice: message.isVoice, user: message.role === 'user', ai: message.role === 'ai' }">
        <p class="content">{{ message.content }}<span v-if="message.streaming" class="caret">▍</span></p>
      </div>
    </div>

    <div v-if="message.role === 'user'" class="avatar user" aria-hidden="true">👤</div>
  </div>
</template>

<style scoped>
/* 系统消息：居中灰色胶囊 */
.system-message {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin: 18px 0;
}
.system-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: #94a3b8;
}
.system-content {
  padding: 6px 16px;
  background: #f1f5f9;
  border-radius: 999px;
  font-size: 12px;
  color: #64748b;
  text-align: center;
}

/* 气泡行布局 */
.bubble-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 18px;
  animation: fade-in 240ms cubic-bezier(0.16, 1, 0.3, 1);
}
@keyframes fade-in {
  from { opacity: 0; transform: translateY(6px); }
  to   { opacity: 1; transform: translateY(0); }
}
.bubble-row.user { justify-content: flex-end; }
.bubble-row.ai   { justify-content: flex-start; }

.avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.avatar.ai   { background: linear-gradient(135deg, #dbeafe, #e0e7ff); }
.avatar.user { background: linear-gradient(135deg, #fef3c7, #fde68a); }

.bubble-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: min(72%, 640px);
}
.bubble-row.user .bubble-wrap { align-items: flex-end; }

.role-row {
  display: flex; align-items: center; gap: 8px;
  padding: 0 4px;
}
.bubble-row.user .role-row { flex-direction: row-reverse; }
.role-name {
  font-size: 11px;
  color: #64748b;
  font-weight: 600;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}
.voice-badge {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #ecfeff;
  color: #0891b2;
  font-size: 10px;
  font-weight: 600;
}
.voice-dot {
  width: 6px; height: 6px; border-radius: 50%; background: #06b6d4;
  animation: pulse 1.2s ease-in-out infinite;
}
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.4; } }

.bubble {
  padding: 14px 18px;
  border-radius: 16px;
  line-height: 1.6;
  font-size: 14.5px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  transition: box-shadow 200ms;
}
.bubble.ai {
  background: #fff;
  color: #0f172a;
  border: 1px solid #f1f5f9;
  border-bottom-left-radius: 4px;
}
.bubble.user {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25);
}
.bubble.voice {
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  color: #fff;
}
.bubble.streaming { outline: none; }

.content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}
.caret {
  display: inline-block;
  margin-left: 2px;
  color: inherit;
  animation: blink 1s steps(1) infinite;
}
@keyframes blink { 50% { opacity: 0; } }

@media (max-width: 640px) {
  .bubble-wrap { max-width: 82%; }
  .bubble { padding: 12px 14px; font-size: 14px; }
}
</style>
