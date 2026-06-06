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
const textareaFocused = ref(false);

const showPlaceholder = computed(() => !text.value && !textareaFocused.value);

function focusTextarea() {
  textareaRef.value?.focus();
}

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

function autoResize() {
  const el = textareaRef.value;
  if (!el) return;
  const maxH = window.innerHeight * 0.35;
  el.style.height = 'auto';
  el.style.height = Math.min(el.scrollHeight, maxH) + 'px';
}

onMounted(() => {
  autoResize();
  window.addEventListener('resize', autoResize);
});
onBeforeUnmount(() => {
  window.removeEventListener('resize', autoResize);
});
</script>

<template>
  <div class="chat-input">
    <!-- 麦克风按钮 -->
    <button
      class="mic-btn"
      :class="{ active: store.isMicActive, recording: store.status === 'listening' }"
      :disabled="micDisabled"
      @click="emit('toggleMic')"
      :title="store.isMicActive ? '停止录音' : '开始语音输入'"
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
    <div class="textarea-wrap">
      <textarea
        ref="textareaRef"
        v-model="text"
        :disabled="disabled || store.status === 'listening'"
        rows="1"
        @keydown="onEnter"
        @input="autoResize"
        @focus="textareaFocused = true"
        @blur="textareaFocused = false"
      />
      <div
        v-if="showPlaceholder"
        class="custom-placeholder"
        @click="focusTextarea"
      >
        <span class="ph-en">Enter</span>
        <span class="ph-zh">请输入</span>
      </div>
    </div>

    <!-- 发送按钮 -->
    <button
      class="send-btn"
      :disabled="disabled || !text.trim() || store.status !== 'idle'"
      @click="onSend"
      aria-label="send message"
    >
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <line x1="22" y1="2" x2="11" y2="13"/>
        <polygon points="22 2 15 22 11 13 2 9 22 2"/>
      </svg>
    </button>
  </div>
</template>

<style scoped>
.chat-input {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 14px 24px 20px;
  background: rgba(255, 255, 255, 0.45);
  backdrop-filter: blur(6px);
  border-top: 1px solid rgba(148, 163, 184, 0.1);
  flex-shrink: 0;
}

/* ============ 麦克风按钮 ============ */
.mic-btn {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  padding: 8px;
  transition: all 180ms cubic-bezier(0.16, 1, 0.3, 1);
  backdrop-filter: blur(4px);
}
.mic-icon {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  opacity: 0.5;
}
.mic-btn:hover:not(:disabled) {
  background: rgba(239, 246, 255, 0.8);
  border-color: rgba(59, 130, 246, 0.3);
  transform: translateY(-1px);
}
.mic-btn:hover:not(:disabled) .mic-icon { opacity: 0.7; }
.mic-btn.active {
  background: rgba(254, 242, 242, 0.8);
  border-color: rgba(239, 68, 68, 0.2);
}
.mic-btn.recording {
  background: #ef4444;
  border-color: #ef4444;
  animation: mic-pulse 1.2s infinite;
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
  font-size: 14px;
  color: #dc2626;
  font-weight: 700;
}
.mic-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  transform: none;
}
@keyframes mic-pulse {
  0%,100% { transform: scale(1); box-shadow: 0 4px 12px rgba(239,68,68,0.35); }
  50%     { transform: scale(1.06); box-shadow: 0 6px 18px rgba(239,68,68,0.5); }
}
@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: 0.4; } }

/* ============ 录音计时 ============ */
.recording-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: rgba(254, 242, 242, 0.8);
  border: 1px solid rgba(239, 68, 68, 0.15);
  border-radius: 12px;
  color: #b91c1c;
  font-size: 12px;
  font-weight: 700;
  height: 42px;
  flex-shrink: 0;
  white-space: nowrap;
  backdrop-filter: blur(4px);
}
.rec-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: #ef4444;
  animation: blink 1s infinite;
}
.rec-time {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  letter-spacing: 0.05em;
}

/* ============ 文本输入 ============ */
.textarea-wrap {
  flex: 1;
  position: relative;
}
textarea {
  width: 100%;
  resize: none;
  padding: 11px 16px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 12px;
  font-size: 14px;
  outline: none;
  background: rgba(255, 255, 255, 0.65);
  color: #0f172a;
  font-family: inherit;
  line-height: 1.6;
  min-height: 42px;
  max-height: 35vh;
  overflow-y: auto;
  transition: height 120ms cubic-bezier(0.16, 1, 0.3, 1), border-color 160ms, background 160ms, box-shadow 160ms;
  backdrop-filter: blur(4px);
  box-sizing: border-box;
}

.custom-placeholder {
  position: absolute;
  top: 11px;
  left: 16px;
  right: 16px;
  display: flex;
  align-items: baseline;
  gap: 5px;
  cursor: text;
  line-height: 1.6;
  font-size: 14px;
  user-select: none;
}
.ph-en {
  font-weight: 700;
  color: #334155;
  letter-spacing: 0.03em;
  text-transform: uppercase;
  font-size: 11px;
}
.ph-zh {
  color: #94a3b8;
  font-size: 13px;
}
textarea:focus {
  border-color: #2563eb;
  background: rgba(255, 255, 255, 0.85);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}
textarea:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  background: rgba(241, 245, 249, 0.4);
}

/* ============ 发送按钮 ============ */
.send-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  cursor: pointer;
  transition: all 180ms cubic-bezier(0.16, 1, 0.3, 1);
  flex-shrink: 0;
}
.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.35);
}
.send-btn:active:not(:disabled) { transform: translateY(0); }
.send-btn:disabled {
  background: rgba(203, 213, 225, 0.6);
  cursor: not-allowed;
  box-shadow: none;
}

@media (max-width: 640px) {
  .chat-input { padding: 10px 12px 14px; gap: 8px; }
  .mic-btn { width: 38px; height: 38px; border-radius: 10px; }
  .send-btn { width: 38px; height: 38px; border-radius: 10px; }
  textarea { font-size: 13px; padding: 10px 12px; min-height: 38px; }
  .recording-pill { padding: 6px 10px; height: 38px; font-size: 11px; }
}
</style>
