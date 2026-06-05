<script setup lang="ts">
// 状态栏：展示当前会话状态（idle / listening / thinking / speaking）
import { computed } from 'vue';
import type { SessionStatus } from '@/types/chat';

const props = defineProps<{ status: SessionStatus; isMicActive: boolean }>();

const label = computed(() => {
  switch (props.status) {
    case 'thinking': return 'Thinking…';
    case 'speaking': return 'Speaking…';
    case 'listening': return 'Listening…';
    default: return 'Ready';
  }
});

const color = computed(() => {
  switch (props.status) {
    case 'thinking': return '#f59e0b';
    case 'speaking': return '#10b981';
    case 'listening': return '#ef4444';
    default: return '#64748b';
  }
});
</script>

<template>
  <div class="status-bar" :style="{ borderColor: color }">
    <span class="dot" :style="{ background: color }" />
    <span class="label">{{ label }}</span>
    <span class="mic" v-if="isMicActive">🎙️ Mic on</span>
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
.mic { margin-left: auto; color: #ef4444; font-weight: 600; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.35; } }
</style>
