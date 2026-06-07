<script setup lang="ts">
import type { ChatMessage } from '@/types/chat';
import aiPng from '@/assets/chat_icon.png';

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
    <!-- AI 头像 -->
    <div v-if="message.role === 'ai'" class="avatar-wrap ai-avatar">
      <img :src="aiPng" alt="AI" />
    </div>

    <div class="bubble-wrap">
      <div class="role-row">
        <span class="role-name">{{ message.role === 'ai' ? '面试官' : '你' }}</span>
        <span class="role-en">{{ message.role === 'ai' ? 'Interviewer' : 'You' }}</span>
        <span v-if="message.isVoice" class="voice-badge">语音</span>
      </div>

      <div
        class="bubble"
        :class="{
          streaming: message.streaming,
          voice: message.isVoice,
          user: message.role === 'user',
          ai: message.role === 'ai'
        }"
      >
        <p class="content">
          {{ message.content }}<span v-if="message.streaming" class="caret">▍</span>
        </p>
        <!-- 中文翻译 -->
        <p v-if="message.role === 'ai' && message.chinese" class="translation">
          {{ message.chinese }}
        </p>
      </div>
    </div>

    <!-- 用户头像（显示在右侧） -->
    <div v-if="message.role === 'user'" class="avatar-wrap user-avatar">
      <span class="avatar-letter">Y</span>
    </div>
  </div>
</template>

<style scoped>
/* ============ 系统消息 ============ */
.system-message {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin: 16px 0;
}
.system-dot {
  width: 5px; height: 5px; border-radius: 50%;
  background: #94a3b8;
}
.system-content {
  padding: 5px 14px;
  background: rgba(148, 163, 184, 0.12);
  border-radius: 999px;
  font-size: 11.5px;
  color: #64748b;
  text-align: center;
  backdrop-filter: blur(4px);
}

/* ============ 气泡行 ============ */
.bubble-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 20px;
  animation: fade-in 240ms cubic-bezier(0.16, 1, 0.3, 1);
}
@keyframes fade-in {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}
.bubble-row.user { justify-content: flex-end; }
.bubble-row.ai   { justify-content: flex-start; }

/* ============ 头像 ============ */
.avatar-wrap {
  width: 38px; height: 38px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}
.ai-avatar {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(99, 102, 241, 0.1));
  padding: 7px;
}
.ai-avatar img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  opacity: 0.6;
}
.user-avatar {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
}
.avatar-letter {
  font-size: 15px;
  font-weight: 700;
  line-height: 1;
}

/* ============ 气泡容器 ============ */
.bubble-wrap {
  display: flex;
  flex-direction: column;
  gap: 5px;
  max-width: min(70%, 580px);
}
.bubble-row.user .bubble-wrap { align-items: flex-end; }
.bubble-row.ai   .bubble-wrap { align-items: flex-start; }

.role-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 4px;
  flex-wrap: wrap;
}
.bubble-row.user .role-row { flex-direction: row-reverse; }
.role-name {
  font-size: 11px;
  color: #475569;
  font-weight: 600;
}
.role-en {
  font-size: 10.5px;
  color: #94a3b8;
  font-weight: 400;
}
.voice-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 7px;
  border-radius: 999px;
  background: rgba(6, 182, 212, 0.1);
  color: #0891b2;
  font-size: 10px;
  font-weight: 600;
}

/* ============ 气泡 ============ */
.bubble {
  padding: 12px 16px;
  border-radius: 14px;
  line-height: 1.65;
  font-size: 14px;
  transition: box-shadow 200ms;
}
.bubble.ai {
  background: rgba(255, 255, 255, 0.7);
  color: #0f172a;
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-bottom-left-radius: 4px;
  backdrop-filter: blur(4px);
}
.bubble.user {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 10px rgba(37, 99, 235, 0.2);
}

.bubble.voice {
  border-left: 3px solid #6366f1;
}

.content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.translation {
  margin: 8px 0 0;
  padding-top: 8px;
  border-top: 1px solid rgba(148, 163, 184, 0.15);
  font-size: 13px;
  line-height: 1.6;
  color: #64748b;
  white-space: pre-wrap;
  word-break: break-word;
}

.caret {
  display: inline-block;
  margin-left: 1px;
  color: inherit;
  animation: blink 1s steps(1) infinite;
}
@keyframes blink { 50% { opacity: 0; } }

@media (max-width: 640px) {
  .bubble-wrap { max-width: 82%; }
  .bubble { padding: 10px 14px; font-size: 13.5px; }
  .avatar-wrap { width: 32px; height: 32px; border-radius: 10px; }
  .ai-avatar { padding: 6px; }
  .avatar-letter { font-size: 13px; }
}
</style>
