<script setup lang="ts">
import { computed } from 'vue';
import type { SessionStatus } from '@/types/chat';
import { useInterviewStore } from '@/stores/interviewStore';

const props = defineProps<{ status: SessionStatus; isMicActive: boolean }>();
const store = useInterviewStore();

const label = computed(() => {
  switch (props.status) {
    case 'thinking': return 'Thinking…';
    case 'speaking': return 'Speaking…';
    case 'listening': return 'Listening…';
    case 'processing': return 'Processing…';
    default: return 'Ready';
  }
});

const color = computed(() => {
  switch (props.status) {
    case 'thinking': return '#f59e0b';
    case 'speaking': return '#10b981';
    case 'listening': return '#ef4444';
    case 'processing': return '#8b5cf6';
    default: return '#64748b';
  }
});
</script>

<template>
  <div class="status-bar" :style="{ borderColor: color }">
    <span class="dot" :style="{ background: color }" />
    <span class="label">{{ label }}</span>
    
    <div class="status-right">
      <span class="ws-status" :class="{ connected: store.isWsConnected, disconnected: !store.isWsConnected }">
        <span class="ws-dot"></span>
        {{ store.isWsConnected ? 'Connected' : 'Disconnected' }}
      </span>
      <span class="mic" v-if="isMicActive">🎙️ Mic on</span>
    </div>
  </div>
</template>

<style scoped>
.status-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 24px;
  background: #ffffff;
  border-top: 2px solid #e2e8f0;
  font-size: 13px;
  color: #334155;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  animation: pulse 1.2s infinite ease-in-out;
}
.label { font-weight: 600; letter-spacing: 0.3px; }

.status-right {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-left: auto;
}

.ws-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}
.ws-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #94a3b8;
}
.ws-status.connected .ws-dot {
  background: #22c55e;
}
.ws-status.disconnected .ws-dot {
  background: #ef4444;
  animation: blink 1s infinite;
}
.ws-status.connected {
  color: #22c55e;
}
.ws-status.disconnected {
  color: #ef4444;
}

.mic { color: #ef4444; font-weight: 600; }

@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.35; } }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }
</style>
