<script setup lang="ts">
defineProps<{
  en: string;
  zh: string;
  class?: string;
  size?: 'sm' | 'base' | 'lg' | 'xl' | 'hero';
  align?: 'left' | 'center';
}>();
</script>

<template>
  <div :class="['bilingual', { [`size-${size}`]: size, [`align-${align}`]: align }, $props.class]">
    <span class="en">{{ en }}</span>
    <span v-if="zh" class="zh">{{ zh }}</span>
  </div>
</template>

<style scoped>
.bilingual {
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: left;
}
.bilingual.align-center { align-items: center; text-align: center; }

.bilingual .en {
  font-weight: 600;
  color: var(--text-primary, #1e293b);
  line-height: 1.35;
  letter-spacing: -0.01em;
}
.bilingual .zh {
  color: var(--text-muted, #64748b);
  font-weight: 400;
  line-height: 1.5;
}

/* Size variants */
.bilingual.size-sm .en { font-size: 12px; }
.bilingual.size-sm .zh { font-size: 11px; }

.bilingual.size-base .en { font-size: 14px; }
.bilingual.size-base .zh { font-size: 12px; }

.bilingual.size-lg .en { font-size: 17px; }
.bilingual.size-lg .zh { font-size: 13px; }

.bilingual.size-xl .en { font-size: 22px; }
.bilingual.size-xl .zh { font-size: 14px; }

.bilingual.size-hero .en {
  font-size: clamp(28px, 4.5vw, 44px);
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--text-primary, #0f172a);
}
.bilingual.size-hero .zh {
  font-size: clamp(14px, 1.8vw, 18px);
  color: var(--text-secondary, #334155);
}
</style>
