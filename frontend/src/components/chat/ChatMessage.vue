<script setup lang="ts">
import { ref } from 'vue';
import type { ChatMessage } from '@/types/chat';
import { useInterviewStore } from '@/stores/interviewStore';

const props = defineProps<{ message: ChatMessage }>();
const store = useInterviewStore();

const isPlaying = ref(false);

async function togglePlay() {
  if (isPlaying.value) {
    store.stopSpeaking();
    isPlaying.value = false;
  } else {
    isPlaying.value = true;
    await store.playAiMessage(props.message.id);
    isPlaying.value = false;
  }
}
</script>

<template>
  <div class="bubble-row" :class="message.role">
    <div class="bubble" :class="{ streaming: message.streaming }">
      <div class="role-row">
        <span class="role">{{ message.role === 'ai' ? 'Interviewer' : 'You' }}</span>
        <button 
          v-if="message.role === 'ai' && !message.streaming" 
          class="play-btn"
          :class="{ playing: isPlaying }"
          @click="togglePlay"
          aria-label="Play audio"
        >
          <svg v-if="!isPlaying" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="5 3 19 12 5 21 5 3"/>
          </svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <rect x="6" y="4" width="4" height="16"/>
            <rect x="14" y="4" width="4" height="16"/>
          </svg>
        </button>
      </div>
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

.role-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.role { 
  font-size: 11px; 
  opacity: 0.75; 
  margin-bottom: 4px; 
  text-transform: uppercase; 
  letter-spacing: 0.5px; 
}
.play-btn {
  background: transparent;
  border: none;
  color: #64748b;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}
.play-btn:hover {
  background: #f1f5f9;
  color: #334155;
}
.play-btn.playing {
  color: #2563eb;
}

.content { margin: 0; white-space: pre-wrap; word-break: break-word; }
.caret { margin-left: 2px; animation: blink 1s steps(1) infinite; }
@keyframes blink { 50% { opacity: 0; } }
</style>
