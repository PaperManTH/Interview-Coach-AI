<script setup lang="ts">
// 用户输入区：文本 + 发送按钮 + 麦克风按钮
import { ref } from 'vue';

const props = defineProps<{ disabled?: boolean }>();
const emit = defineEmits<{
  (e: 'send', text: string): void;
  (e: 'toggleMic'): void;
}>(); 

const text = ref('');

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
</script>

<template>
  <div class="chat-input">
    <button class="mic-btn" :disabled="disabled" @click="emit('toggleMic')" aria-label="mic">
      🎙️
    </button>
    <textarea
      v-model="text"
      :disabled="disabled"
      placeholder="Type your answer in English… (Enter to send, Shift+Enter for newline)"
      rows="2"
      @keydown="onEnter"
    />
    <button class="send-btn" :disabled="disabled || !text.trim()" @click="onSend">Send</button>
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
}
.mic-btn:hover { background: #dbeafe; }
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
}
textarea:focus { border-color: #2563eb; background: #fff; }
.send-btn {
  height: 48px;
  padding: 0 22px;
  border-radius: 12px;
  border: none;
  background: #2563eb;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}
.send-btn:disabled { background: #94a3b8; cursor: not-allowed; }
.send-btn:not(:disabled):hover { background: #1d4ed8; }
</style>
