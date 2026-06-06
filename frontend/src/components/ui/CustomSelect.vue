<template>
  <div class="custom-select" :class="{ open: isOpen, disabled }" ref="rootRef">
    <button
      ref="triggerRef"
      class="select-trigger"
      @click="toggle"
      @keydown="handleKeydown"
      :disabled="disabled"
      :aria-expanded="isOpen"
      type="button"
    >
      <span class="selected-text" :class="{ placeholder: !selectedLabel }">
        {{ selectedLabel || placeholder }}
      </span>
      <svg class="chevron" xmlns="http://www.w3.org/2000/svg" width="12" height="8" viewBox="0 0 12 8">
        <path d="M1 1.5l5 5 5-5" stroke="currentColor" stroke-width="1.8" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </button>
    <Teleport to="body">
      <Transition name="dropdown">
        <ul
          v-if="isOpen"
          class="options-panel"
          :class="{ 'flip-up': flipUp }"
          :style="panelStyle"
          role="listbox"
        >
          <li
            v-for="opt in options"
            :key="opt.value"
            class="option-item"
            :class="{ selected: opt.value === modelValue }"
            role="option"
            :aria-selected="opt.value === modelValue"
            @click.stop="select(opt.value)"
          >
            {{ opt.label }}
            <svg v-if="opt.value === modelValue" class="check-icon" xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </li>
        </ul>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue';

interface Option {
  value: string;
  label: string;
}

const props = withDefaults(defineProps<{
  modelValue?: string;
  options: readonly Option[] | Option[];
  placeholder?: string;
  disabled?: boolean;
}>(), {
  modelValue: '',
  placeholder: '请选择',
  disabled: false,
});

const emit = defineEmits<{
  'update:modelValue': [value: string];
}>();

const isOpen = ref(false);
const flipUp = ref(false);
const rootRef = ref<HTMLElement | null>(null);
const triggerRef = ref<HTMLElement | null>(null);

const selectedLabel = computed(() => {
  const opt = props.options.find(o => o.value === props.modelValue);
  return opt?.label ?? '';
});

const panelStyle = ref<Record<string, string>>({});

/** 面板估算高度（最多220px的max-height），留余量用250px + 4px gap */
const ESTIMATED_PANEL_HEIGHT = 224;

function updatePanelPosition() {
  if (!triggerRef.value) return;
  const rect = triggerRef.value.getBoundingClientRect();
  const viewportHeight = window.innerHeight;
  const spaceBelow = viewportHeight - rect.bottom;
  const spaceAbove = rect.top;

  // 下方空间不足，上方空间足够 → 向上展开
  if (spaceBelow < ESTIMATED_PANEL_HEIGHT && spaceAbove > spaceBelow) {
    flipUp.value = true;
    panelStyle.value = {
      position: 'fixed',
      bottom: `${viewportHeight - rect.top + 4}px`,
      left: `${rect.left}px`,
      width: `${rect.width}px`,
    };
  } else {
    flipUp.value = false;
    panelStyle.value = {
      position: 'fixed',
      top: `${rect.bottom + 4}px`,
      left: `${rect.left}px`,
      width: `${rect.width}px`,
    };
  }
}

function toggle() {
  if (props.disabled) return;
  isOpen.value = !isOpen.value;
  if (isOpen.value) {
    nextTick(updatePanelPosition);
  }
}

function select(value: string) {
  emit('update:modelValue', value);
  isOpen.value = false;
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' || e.key === ' ') {
    e.preventDefault();
    toggle();
  } else if (e.key === 'Escape') {
    isOpen.value = false;
  }
}

function onClickOutside(e: MouseEvent) {
  if (rootRef.value && !rootRef.value.contains(e.target as Node)) {
    isOpen.value = false;
  }
}

// 滚动/窗口变化时更新位置
watch(isOpen, (val) => {
  if (val) {
    window.addEventListener('scroll', updatePanelPosition, true);
    window.addEventListener('resize', updatePanelPosition);
  } else {
    window.removeEventListener('scroll', updatePanelPosition, true);
    window.removeEventListener('resize', updatePanelPosition);
  }
});

onMounted(() => document.addEventListener('click', onClickOutside));
onUnmounted(() => {
  document.removeEventListener('click', onClickOutside);
  window.removeEventListener('scroll', updatePanelPosition, true);
  window.removeEventListener('resize', updatePanelPosition);
});
</script>

<style scoped>
.custom-select {
  position: relative;
  width: 100%;
  font-size: 13.5px;
  user-select: none;
}

.select-trigger {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 10px 14px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 10px;
  background-color: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(4px);
  color: #0f172a;
  font-size: inherit;
  font-family: inherit;
  cursor: pointer;
  transition: all 150ms cubic-bezier(0.16, 1, 0.3, 1);
  box-sizing: border-box;
  text-align: left;
}

.select-trigger:hover:not(:disabled) {
  border-color: rgba(30, 64, 175, 0.35);
  background-color: rgba(255, 255, 255, 0.75);
}

.custom-select.open .select-trigger {
  border-color: #1e40af;
  background-color: rgba(255, 255, 255, 0.85);
  box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.1);
}

.select-trigger:disabled {
  background: rgba(241, 245, 249, 0.5);
  color: #94a3b8;
  cursor: not-allowed;
}

.selected-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-text.placeholder {
  color: #94a3b8;
}

.chevron {
  flex-shrink: 0;
  color: #64748b;
  transition: transform 200ms cubic-bezier(0.16, 1, 0.3, 1);
}

/* 向下展开：箭头指向下；向上展开：箭头指向上（不旋转即默认朝上） */
.custom-select.open .chevron {
  color: #1e40af;
  transform: rotate(180deg);
}
</style>

<!-- 非 scoped：Teleport 到 body 后 scoped 样式不生效 -->
<style>
.options-panel {
  z-index: 9999;
  max-height: 220px;
  overflow-y: auto;
  margin: 0;
  padding: 6px;
  list-style: none;
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
}

.option-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 12px;
  border-radius: 7px;
  color: #334155;
  cursor: pointer;
  transition: background-color 100ms;
}

.option-item:hover {
  background: rgba(30, 64, 175, 0.06);
  color: #0f172a;
}

.option-item.selected {
  background: rgba(30, 64, 175, 0.08);
  color: #1e40af;
  font-weight: 600;
}

.check-icon {
  flex-shrink: 0;
  color: #1e40af;
}

/* Transition — 向下展开（默认） */
.dropdown-enter-active {
  transition: opacity 150ms ease, transform 150ms cubic-bezier(0.16, 1, 0.3, 1);
}
.dropdown-leave-active {
  transition: opacity 100ms ease, transform 100ms cubic-bezier(0.16, 1, 0.3, 1);
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* Transition — 向上展开 */
.options-panel.flip-up.dropdown-enter-from,
.options-panel.flip-up.dropdown-leave-to {
  transform: translateY(6px);
}
</style>
