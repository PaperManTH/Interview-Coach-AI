<script setup lang="ts">
import { computed } from 'vue';
import type { SessionStatus } from '@/types/chat';
import { useInterviewStore } from '@/stores/interviewStore';

const props = defineProps<{ status: SessionStatus; isMicActive: boolean }>();
const store = useInterviewStore();

const label = computed(() => {
  switch (props.status) {
    case 'thinking':   return 'Thinking';
    case 'speaking':   return 'Speaking';
    case 'listening':  return 'Listening';
    case 'processing': return 'Processing';
    default:           return 'Ready';
  }
});

const zhLabel = computed(() => {
  switch (props.status) {
    case 'thinking':   return '思考中';
    case 'speaking':   return '说话中';
    case 'listening':  return '聆听中';
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
      <span class="status-dot" :style="{ background: color, boxShadow: `0 0 0 4px ${color}20` }" />
      <div class="status-texts">
        <span class="status-en">{{ label }}</span>
        <span class="status-zh">{{ zhLabel }}</span>
      </div>
    </div>

    <div class="status-right">
      <span class="chip" :class="{ connected: store.isWsConnected, disconnected: !store.isWsConnected }">
        <span class="chip-dot"></span>
        {{ store.isWsConnected ? '已连接' : '已断开' }}
      </span>
      <span v-if="isMicActive" class="chip mic-on">
        <span class="chip-dot"></span>
        麦克风
      </span>
    </div>
  </div>
</template>

<style scoped>
.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 24px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(4px);
  border-top: 1px solid rgba(148, 163, 184, 0.1);
  font-size: 13px;
  flex-shrink: 0;
}

.status-left { display: flex; align-items: center; gap: 9px; }
.status-dot {
  width: 7px; height: 7px; border-radius: 50%;
  animation: status-pulse 1.6s infinite ease-in-out;
}
.status-texts { display: flex; align-items: baseline; gap: 6px; }
.status-en {
  font-size: 11px;
  font-weight: 700;
  color: #334155;
  letter-spacing: 0.03em;
  text-transform: uppercase;
}
.status-zh {
  font-size: 11px;
  color: #94a3b8;
}

.status-right { display: flex; align-items: center; gap: 6px; }
.chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 9px;
  border-radius: 999px;
  font-size: 10.5px;
  font-weight: 600;
  background: rgba(241, 245, 249, 0.6);
  color: #64748b;
  backdrop-filter: blur(2px);
}
.chip-dot {
  width: 5px; height: 5px; border-radius: 50%;
  background: currentColor;
  opacity: 0.7;
}
.chip.connected {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}
.chip.disconnected {
  background: rgba(239, 68, 68, 0.08);
  color: #dc2626;
}
.chip.disconnected .chip-dot { animation: blink 1s infinite; }
.chip.mic-on {
  background: rgba(59, 130, 246, 0.1);
  color: #2563eb;
}

@keyframes status-pulse {
  0%,100% { opacity: 1; transform: scale(1); }
  50%     { opacity: 0.4; transform: scale(1.3); }
}
@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: 0.3; } }

@media (max-width: 640px) {
  .status-bar { padding: 8px 16px; }
  .status-zh { display: none; }
}
</style>
