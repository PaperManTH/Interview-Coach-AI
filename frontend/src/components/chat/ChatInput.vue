<script setup lang="ts">
import { computed } from 'vue';
import { useInterviewStore } from '@/stores/interviewStore';

const store = useInterviewStore();

const props = defineProps<{ disabled?: boolean }>();
const emit = defineEmits<{
  (e: 'send', text: string): void;
  (e: 'toggleMic'): void;
}>(); 

const text = defineModel<string>({ default: '' });

function onSend() {
  if (!text.value.trim() || props.disabled) return;
  emit('send', text.value);
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
  const mins = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
});

// 麦克风按钮禁用逻辑：录音时允许点击停止，思考时禁用
const micDisabled = computed(() => {
  return props.disabled && store.status !== 'listening';
});
</script>

<template>
  <div class="chat-input">
    <button 
      class="mic-btn" 
      :class="{ active: store.isMicActive, recording: store.status === 'listening' }"
      :disabled="micDisabled" 
      @click="emit('toggleMic')" 
      aria-label="mic"
    >
      <span v-if="!store.isMicActive">🎙️</span>
      <span v-else-if="store.status === 'listening'">🔴</span>
      <span v-else>⏹️</span>
    </button>
    
    <div v-if="store.status === 'listening'" class="recording-indicator">
      <span class="recording-dot"></span>
      <span class="recording-time">{{ formattedDuration }}</span>
    </div>
    
    <textarea
      v-model="text"
      :disabled="disabled || store.status === 'listening'"
      placeholder="Type your answer in English… (Enter to send, Shift+Enter for newline)"
      rows="2"
      @keydown="onEnter"
    />
    <button class="send-btn" :disabled="disabled || !text.trim() || store.status !== 'idle'" @click="onSend">Send</button>
  </div>
</template>

<style scoped>
.chat-input {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  background: #ffffff;
  border-top: 1px solid #e2e8f0;
  align-items: flex-end;
}
.mic-btn {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  border: none;
  background: #eff6ff;
  font-size: 20px;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s;
}
.mic-btn:hover:not(:disabled) { 
  background: #dbeafe; 
}
.mic-btn.active {
  background: #fee2e2;
}
.mic-btn.recording {
  background: #ef4444;
  color: white;
  animation: pulse 1s infinite;
}
@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}
.mic-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.recording-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fee2e2;
  border-radius: 8px;
  color: #dc2626;
  font-size: 13px;
  font-weight: 600;
}
.recording-dot {
  width: 8px;
  height: 8px;
  background: #dc2626;
  border-radius: 50%;
  animation: blink 1s infinite;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
.recording-time {
  font-family: monospace;
}

textarea {
  flex: 1;
  resize: none;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  outline: none;
  background: #f8fafc;
  font-family: inherit;
  transition: all 0.2s;
}
textarea:focus { 
  border-color: #2563eb; 
  background: #fff; 
}
textarea:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.send-btn {
  height: 48px;
  padding: 0 22px;
  border-radius: 12px;
  border: none;
  background: #2563eb;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  transition: background 0.2s;
}
.send-btn:disabled { 
  background: #94a3b8; 
  cursor: not-allowed; 
}
.send-btn:not(:disabled):hover { 
  background: #1d4ed8; 
}
</style>