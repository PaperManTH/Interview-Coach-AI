<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from 'vue';
import { useInterviewStore } from '@/stores/interviewStore';
import voiceIcon from '@/assets/voice_icon.png';

const store = useInterviewStore();

const props = defineProps<{ disabled?: boolean }>();
const emit = defineEmits<{
  (e: 'send', text: string): void;
  (e: 'toggleMic'): void;
}>();

const text = defineModel<string>({ default: '' });
const textareaRef = ref<HTMLTextAreaElement | null>(null);

function onSend() {
  const value = (text.value ?? '').trim();
  if (!value || props.disabled) return;
  emit('send', value);
  text.value = '';
}

function onEnter(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    onSend();
  }
}

const formattedDuration = computed(() => {
  const seconds = store.recordingDuration;
  const mins = Math.floor(seconds / 60).toString().padStart(2, '0');
  const secs = (seconds % 60).toString().padStart(2, '0');
  return `${mins}:${secs}`;
});

const micDisabled = computed(() => props.disabled && store.status !== 'listening');

// 自动增高 textarea
function autoResize() {
  const el = textareaRef.value;
  if (!el) return;
  el.style.height = 'auto';
  el.style.height = Math.min(el.scrollHeight, 140) + 'px';
}

onMounted(autoResize);
onBeforeUnmount(() => {});
</script>

<template>
  <div class="chat-input">
    <!-- 麦克风按钮 -->
    <button
      class="mic-btn"
      :class="{ active: store.isMicActive, recording: store.status === 'listening' }"
      :disabled="micDisabled"
      @click="emit('toggleMic')"
      :title="store.isMicActive ? 'Stop recording' : 'Start voice input'"
      aria-label="toggle microphone"
    >
      <img v-if="!store.isMicActive" :src="voiceIcon" class="mic-icon" alt="mic" />
      <span v-else-if="store.status === 'listening'" class="rec-pulse" />
      <span v-else class="stop-mark">■</span>
    </button>

    <!-- 录音计时 -->
    <div v-if="store.status === 'listening'" class="recording-pill">
      <span class="rec-dot"></span>
      <span class="rec-time">{{ formattedDuration }}</span>
    </div>

    <!-- 文本输入 -->
    <textarea
      ref="textareaRef"
      v-model="text"
      :disabled="disabled || store.status === 'listening'"
      placeholder="Type your answer in English…  (Enter to send, Shift+Enter for newline)"
      rows="1"
      @keydown="onEnter"
      @input="autoResize"
    />

    <!-- 发送按钮 -->
    <button
      class="send-btn"
      :disabled="disabled || !text.trim() || store.status !== 'idle'"
      @click="onSend"
      aria-label="send message"
    >
      <span class="send-text">Send</span>
      <span class="send-icon">↵</span>
    </button>
  </div>
</template>

<style scoped>
.chat-input {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 14px 24px 20px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border-top: 1px solid #f1f5f9;
}

.mic-btn {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  padding: 8px;
  transition: all 180ms cubic-bezier(0.16, 1, 0.3, 1);
}
.mic-icon {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}
.mic-btn:hover:not(:disabled) {
  background: #eff6ff;
  border-color: #93c5fd;
  transform: translateY(-1px);
}
.mic-btn.active {
  background: #fef2f2;
  border-color: #fecaca;
}
.mic-btn.recording {
  background: #ef4444;
  border-color: #ef4444;
  color: #fff;
  animation: pulse 1.2s infinite;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.35);
}
.mic-btn.recording .rec-pulse {
  width: 14px;
  height: 14px;
  border-radius: 4px;
  background: #fff;
  animation: blink 0.8s infinite;
}
.stop-mark {
  font-size: 16px;
  color: #dc2626;
  font-weight: 700;
}
.mic-btn:disabled { opacity: 0.45; cursor: not-allowed; transform: none; }
@keyframes pulse {
  0%,100% { transform: scale(1); box-shadow: 0 4px 12px rgba(239,68,68,0.35); }
  50%     { transform: scale(1.06); box-shadow: 0 6px 18px rgba(239,68,68,0.5); }
}
@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: 0.4; } }

.recording-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 12px;
  color: #b91c1c;
  font-size: 12px;
  font-weight: 700;
  height: 44px;
  flex-shrink: 0;
  white-space: nowrap;
}
.rec-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #ef4444;
  animation: blink 1s infinite;
}
.rec-time { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; letter-spacing: 0.05em; }
@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: 0.3; } }

textarea {
  flex: 1;
  resize: none;
  padding: 12px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  outline: none;
  background: #fff;
  color: #0f172a;
  font-family: inherit;
  line-height: 1.6;
  min-height: 44px;
  max-height: 140px;
  transition: all 160ms;
}
textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
  background: #fff;
}
textarea::placeholder { color: #94a3b8; }
textarea:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f8fafc;
}

.send-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 44px;
  padding: 0 20px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.02em;
  cursor: pointer;
  transition: all 180ms cubic-bezier(0.16, 1, 0.3, 1);
  flex-shrink: 0;
  white-space: nowrap;
}
.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.35);
}
.send-btn:active:not(:disabled) { transform: translateY(0); }
.send-btn:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
  box-shadow: none;
}
.send-icon { font-size: 16px; line-height: 1; }

@media (max-width: 640px) {
  .chat-input { padding: 12px 14px 16px; gap: 8px; }
  .mic-btn { width: 40px; height: 40px; font-size: 16px; }
  .send-btn { padding: 0 14px; height: 40px; font-size: 13px; }
  .send-text { display: none; }
  textarea { font-size: 13px; padding: 10px 12px; min-height: 40px; }
  .recording-pill { padding: 6px 10px; height: 40px; font-size: 11px; }
}
</style>
