<script setup lang="ts">
defineProps<{
  en: string;
  zh: string;
  class?: string;
}>();
</script>

<template>
  <div :class="['bilingual', $props.class]">
    <span class="en">{{ en }}</span>
    <span class="zh">{{ zh }}</span>
  </div>
</template>

<style scoped>
.bilingual {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.bilingual .en {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.4;
}

.bilingual .zh {
  font-size: 12px;
  color: #64748b;
  line-height: 1.3;
}

.bilingual.small .en {
  font-size: 12px;
}

.bilingual.small .zh {
  font-size: 11px;
}

.bilingual.large .en {
  font-size: 18px;
}

.bilingual.large .zh {
  font-size: 14px;
}
</style>
