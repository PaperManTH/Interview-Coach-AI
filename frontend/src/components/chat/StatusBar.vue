<script setup lang="ts">
import { computed } from 'vue';
import type { SessionStatus } from '@/types/chat';
import { useInterviewStore } from '@/stores/interviewStore';

const props = defineProps<{ status: SessionStatus; isMicActive: boolean }>();
const store = useInterviewStore();

const label = computed(() => {
  switch (props.status) {
    case 'thinking':   return 'Thinking…';
    case 'speaking':   return 'Speaking…';
    case 'listening':  return 'Listening…';
    case 'processing': return 'Processing…';
    default:           return 'Ready';
  }
});

const zhLabel = computed(() => {
  switch (props.status) {
    case 'thinking':   return '正在思考';
    case 'speaking':   return '正在说话';
    case 'listening':  return '正在聆听';
    case 'processing': return '处理中';
    default:           return '就绪';
  }
});

const color = computed(() => {
  switch (props.status) {
    case 'thinking':   return '#f59e0b';
    case 'speaking':   return '#10b981';
    case 'listening':  return '#ef4444';
    case 'processing': return '#8b5cf6';
    default:           return '#64748b';
  }
});
</script>

<template>
  <div class="status-bar">
    <div class="status-left">
      <span class="status-dot" :style="{ background: color }" />
      <div class="status-texts">
        <span class="status-label">{{ label }}</span>
        <span class="status-zh">{{ zhLabel }}</span>
      </div>
    </div>

    <div class="status-right">
      <span class="chip" :class="{ connected: store.isWsConnected, disconnected: !store.isWsConnected }">
        <span class="chip-dot"></span>
        <span>{{ store.isWsConnected ? 'Connected' : 'Disconnected' }}</span>
      </span>
      <span v-if="isMicActive" class="chip mic">
        <span class="chip-dot"></span>
        Mic On
      </span>
    </div>
  </div>
</template>

<style scoped>
.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-top: 1px solid #f1f5f9;
  font-size: 13px;
}
.status-left { display: flex; align-items: center; gap: 10px; }
.status-dot {
  width: 8px; height: 8px; border-radius: 50%;
  box-shadow: 0 0 0 4px color-mix(in srgb, currentColor 15%, transparent);
  animation: pulse 1.4s infinite ease-in-out;
}
.status-texts { display: flex; flex-direction: column; gap: 1px; line-height: 1.2; }
.status-label { font-size: 12px; font-weight: 700; color: #0f172a; letter-spacing: 0.04em; text-transform: uppercase; }
.status-zh    { font-size: 11px; color: #94a3b8; }

.status-right { display: flex; align-items: center; gap: 8px; }
.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  background: #f1f5f9;
  color: #64748b;
}
.chip-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: currentColor;
  opacity: 0.7;
}
.chip.connected {
  background: #dcfce7;
  color: #15803d;
}
.chip.disconnected {
  background: #fee2e2;
  color: #b91c1c;
}
.chip.disconnected .chip-dot { animation: blink 1s infinite; }
.chip.mic {
  background: #dbeafe;
  color: #1d4ed8;
}

@keyframes pulse { 0%,100% { opacity: 1; transform: scale(1); } 50% { opacity: 0.45; transform: scale(1.2); } }
@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: 0.3; } }

@media (max-width: 640px) {
  .status-bar { padding: 10px 16px; }
  .status-zh { display: none; }
}
</style>
