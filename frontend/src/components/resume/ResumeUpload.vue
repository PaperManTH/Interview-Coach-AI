<script setup lang="ts">
// 简历上传模块：拖拽/点击选择 -> 校验 -> 进度反馈 -> 成功/失败提示
// 采用前端本地解析 + localStorage 暂存策略，便于与未来后端接口平滑对接
import { ref, computed, watch } from 'vue';
import resumePng from '@/assets/data_analysis.png';
import cloudPng from '@/assets/chat_icon.png';
import filePng from '@/assets/tool_icon.png';

const props = defineProps<{
  /** 受控模式：外部控制弹窗开关 */
  modelValue?: boolean;
  /** 最大文件大小（MB） */
  maxSizeMB?: number;
  /** 支持的 MIME 类型/扩展名 */
  accept?: string;
  /** 隐藏默认触发按钮，由父组件自行触发 open() */
  hideTrigger?: boolean;
}>();
const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void;
  (e: 'uploaded', payload: { name: string; size: number; text: string }): void;
  (e: 'error', message: string): void;
}>();

const _open = ref(false);
const isOpen = computed({
  get: () => props.modelValue !== undefined ? props.modelValue : _open.value,
  set: (v) => {
    if (props.modelValue !== undefined) emit('update:modelValue', v);
    else _open.value = v;
  },
});

const file = ref<File | null>(null);
const status = ref<'idle' | 'validating' | 'uploading' | 'success' | 'error'>('idle');
const progress = ref(0);
const errorMsg = ref('');
const resumeText = ref('');
const isDragging = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);

const MAX_BYTES = (props.maxSizeMB ?? 10) * 1024 * 1024;
const ACCEPT = props.accept ?? '.pdf,.doc,.docx,.txt,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,text/plain';

const formattedSize = computed(() => file.value ? humanSize(file.value.size) : '');
const canUpload = computed(() => !!file.value && status.value !== 'uploading');

function humanSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

function openDialog() { isOpen.value = true; }
function closeDialog() {
  if (status.value === 'uploading') return;
  isOpen.value = false;
}

function pickFile() { fileInput.value?.click(); }

function onFileInput(e: Event) {
  const target = e.target as HTMLInputElement;
  const selected = target.files?.[0] ?? null;
  if (selected) handleFile(selected);
  target.value = '';
}

function onDrop(e: DragEvent) {
  e.preventDefault();
  isDragging.value = false;
  const f = e.dataTransfer?.files?.[0];
  if (f) handleFile(f);
}

function onDragOver(e: DragEvent) {
  e.preventDefault();
  isDragging.value = true;
}

function onDragLeave() { isDragging.value = false; }

function handleFile(f: File) {
  status.value = 'idle';
  errorMsg.value = '';
  progress.value = 0;
  resumeText.value = '';

  const name = f.name.toLowerCase();
  const allowed = /\.(pdf|doc|docx|txt|md|rtf)$/i;
  if (!allowed.test(name)) {
    file.value = null;
    status.value = 'error';
    errorMsg.value = '不支持的文件类型。请上传 PDF / DOC / DOCX / TXT 格式。';
    return;
  }
  if (f.size > MAX_BYTES) {
    file.value = null;
    status.value = 'error';
    errorMsg.value = `文件过大（${humanSize(f.size)}），最大允许 ${props.maxSizeMB ?? 10} MB。`;
    return;
  }
  file.value = f;
}

async function startUpload() {
  if (!file.value) return;
  status.value = 'uploading';
  errorMsg.value = '';
  progress.value = 0;

  // 进度动画 + 文本内容读取（仅纯文本直接解析，其它格式标记为已上传）
  const isText = /\.(txt|md|rtf)$/i.test(file.value.name);
  let text = '';

  if (isText) {
    try {
      text = await readAsTextWithProgress(file.value, (p) => { progress.value = Math.min(80, p * 0.8); });
    } catch {
      text = '';
    }
  }

  // 模拟后端上传进度（纯前端 MVP；对接后端时替换为真实 XHR/fetch 上传）
  await animateProgress(80, 100, 50);

  // 元数据写入本地，方便面试场景后续使用
  resumeText.value = text || `[已上传简历] ${file.value.name}（${humanSize(file.value.size)}）`;
  try {
    const payload = { name: file.value.name, size: file.value.size, uploadedAt: Date.now() };
    localStorage.setItem('icai:last-resume', JSON.stringify(payload));
  } catch { /* noop */ }

  status.value = 'success';
  progress.value = 100;
  emit('uploaded', { name: file.value.name, size: file.value.size, text: resumeText.value });
}

function readAsTextWithProgress(f: File, onProgress: (p: number) => void): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onprogress = (e) => {
      if (e.lengthComputable) onProgress(e.loaded / e.total);
    };
    reader.onload = () => resolve(String(reader.result ?? ''));
    reader.onerror = () => reject(reader.error);
    reader.readAsText(f, 'utf-8');
  });
}

function animateProgress(from: number, to: number, totalMs: number): Promise<void> {
  return new Promise((resolve) => {
    const start = performance.now();
    const step = (now: number) => {
      const t = Math.min(1, (now - start) / totalMs);
      progress.value = Math.round(from + (to - from) * t);
      if (t < 1) requestAnimationFrame(step);
      else resolve();
    };
    requestAnimationFrame(step);
  });
}

function resetAll() {
  file.value = null;
  status.value = 'idle';
  progress.value = 0;
  errorMsg.value = '';
  resumeText.value = '';
}

// 打开时重置状态
watch(isOpen, (v) => { if (v) resetAll(); });

// 对外暴露方法供父组件调用
defineExpose({ open: openDialog, close: closeDialog });
</script>

<template>
  <!-- 入口按钮：可独立使用 -->
  <slot v-if="!hideTrigger" name="trigger" :open="openDialog">
    <button class="resume-trigger" @click="openDialog" type="button">
      <img :src="resumePng" class="trigger-icon" alt="resume" />
      <span class="trigger-texts">
        <span class="trigger-title">Upload Resume</span>
        <span class="trigger-sub">上传简历 · 让面试官更懂你</span>
      </span>
      <span class="trigger-chevron">→</span>
    </button>
  </slot>

  <!-- 遮罩 + 弹窗 -->
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="isOpen" class="modal-backdrop" @click.self="closeDialog">
        <div class="modal" role="dialog" aria-modal="true" aria-label="Upload Resume">
          <header class="modal-header">
            <div class="header-title">
              <img :src="resumePng" class="header-icon" alt="resume" />
              <div>
                <h3>Upload Your Resume</h3>
                <p>上传简历 · 帮助 AI 面试官提出更有针对性的问题</p>
              </div>
            </div>
            <button class="close-btn" @click="closeDialog" aria-label="close">✕</button>
          </header>

          <div class="modal-body">
            <!-- 拖拽区 -->
            <label
              v-if="status !== 'success'"
              class="dropzone"
              :class="{ 'is-dragging': isDragging, 'has-file': file }"
              @drop="onDrop"
              @dragover="onDragOver"
              @dragleave="onDragLeave"
            >
              <input ref="fileInput" type="file" :accept="ACCEPT" hidden @change="onFileInput" />
              <img :src="cloudPng" class="dropzone-icon" v-if="!file" alt="" />
              <img :src="filePng" class="dropzone-icon has-file-icon" v-else alt="" />
              <div class="dropzone-title">
                {{ file ? file.name : 'Drop your resume here, or click to browse' }}
              </div>
              <div class="dropzone-sub">
                {{ file ? formattedSize : '支持 PDF / DOC / DOCX / TXT，最大 10MB' }}
              </div>
              <button type="button" class="choose-btn" @click.stop="pickFile">
                {{ file ? '更换文件' : '选择文件' }}
              </button>
            </label>

            <!-- 错误 -->
            <div v-if="status === 'error' && errorMsg" class="alert error">
              <span class="alert-icon">⚠️</span>
              <span>{{ errorMsg }}</span>
            </div>

            <!-- 进度 -->
            <div v-if="status === 'uploading'" class="progress-wrap">
              <div class="progress-bar"><div class="progress-fill" :style="{ width: progress + '%' }"></div></div>
              <div class="progress-text">
                <span>上传中…</span>
                <span>{{ progress }}%</span>
              </div>
            </div>

            <!-- 成功 -->
            <div v-if="status === 'success'" class="success-card">
              <div class="success-circle">✓</div>
              <h4>Upload Successful</h4>
              <p class="success-sub">简历已上传，将用于个性化面试准备。</p>
              <div class="success-meta">
                <div class="meta-item"><span class="meta-label">文件</span><span class="meta-value">{{ file?.name }}</span></div>
                <div class="meta-item"><span class="meta-label">大小</span><span class="meta-value">{{ formattedSize }}</span></div>
              </div>
            </div>
          </div>

          <footer class="modal-footer">
            <button class="btn btn-secondary" @click="closeDialog" type="button" :disabled="status === 'uploading'">
              {{ status === 'success' ? '完成' : '取消' }}
            </button>
            <button
              v-if="status !== 'success'"
              class="btn btn-primary"
              :disabled="!canUpload"
              @click="startUpload"
              type="button"
            >
              <span v-if="status === 'uploading'">上传中…</span>
              <span v-else>上传简历</span>
            </button>
            <button
              v-else
              class="btn btn-secondary"
              @click="resetAll"
              type="button"
            >
              重新上传
            </button>
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* ============ 入口按钮 ============ */
.resume-trigger {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
  padding: 18px 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 1.5px dashed #cbd5e1;
  border-radius: 14px;
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  transition: all 200ms cubic-bezier(0.16, 1, 0.3, 1);
}
.resume-trigger:hover {
  border-color: #2563eb;
  background: linear-gradient(135deg, #eff6ff 0%, #ffffff 100%);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.1);
}
.trigger-icon {
  width: 44px;
  height: 44px;
  object-fit: contain;
  display: block;
  border-radius: 12px;
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  padding: 6px;
  box-sizing: border-box;
  flex-shrink: 0;
}
.trigger-texts { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.trigger-title { font-size: 15px; font-weight: 600; color: #0f172a; }
.trigger-sub { font-size: 12px; color: #64748b; }
.trigger-chevron { color: #94a3b8; font-size: 18px; transition: transform 200ms; }
.resume-trigger:hover .trigger-chevron { transform: translateX(4px); color: #2563eb; }

/* ============ 弹窗 ============ */
.modal-backdrop {
  position: fixed; inset: 0;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
  padding: 20px;
}
.modal {
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 24px 48px -12px rgba(15, 23, 42, 0.25);
  overflow: hidden;
  animation: pop-in 240ms cubic-bezier(0.16, 1, 0.3, 1);
}
@keyframes pop-in {
  from { opacity: 0; transform: translateY(12px) scale(0.98); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

.modal-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  padding: 24px 24px 16px;
  border-bottom: 1px solid #f1f5f9;
}
.header-title { display: flex; gap: 14px; align-items: center; }
.header-icon {
  width: 44px;
  height: 44px;
  object-fit: contain;
  display: block;
  border-radius: 12px;
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  padding: 6px;
  box-sizing: border-box;
  flex-shrink: 0;
}
.header-title h3 { margin: 0 0 4px; font-size: 18px; font-weight: 700; color: #0f172a; }
.header-title p  { margin: 0; font-size: 12px; color: #64748b; }

.close-btn {
  border: none; background: #f1f5f9;
  width: 32px; height: 32px; border-radius: 8px;
  color: #64748b; font-size: 14px; cursor: pointer;
  transition: all 120ms;
}
.close-btn:hover { background: #e2e8f0; color: #1e293b; }

.modal-body { padding: 20px 24px; }

/* 拖拽区 */
.dropzone {
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  padding: 32px 20px;
  border: 2px dashed #cbd5e1;
  border-radius: 14px;
  background: #f8fafc;
  cursor: pointer;
  transition: all 200ms;
}
.dropzone:hover { border-color: #3b82f6; background: #eff6ff; }
.dropzone.is-dragging {
  border-color: #2563eb; background: #dbeafe;
  transform: scale(1.01);
}
.dropzone.has-file { border-color: #10b981; background: #ecfdf5; }
.dropzone-icon {
  width: 48px;
  height: 48px;
  object-fit: contain;
  display: block;
  margin-bottom: 12px;
}
.dropzone-icon.has-file-icon { /* 使用宽高 48 / 48，保持与无文件图标一致尺寸 */ }
.dropzone-title {
  font-size: 14px; font-weight: 600; color: #0f172a;
  margin-bottom: 4px; text-align: center;
  max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.dropzone-sub { font-size: 12px; color: #64748b; margin-bottom: 14px; }
.choose-btn {
  padding: 8px 18px;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 13px; font-weight: 600;
  color: #334155;
  cursor: pointer;
  transition: all 120ms;
}
.choose-btn:hover { background: #f1f5f9; border-color: #cbd5e1; }

/* 告警 */
.alert {
  display: flex; align-items: center; gap: 10px;
  margin-top: 14px;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 13px;
}
.alert.error { background: #fee2e2; color: #991b1b; }
.alert-icon { font-size: 16px; }

/* 进度条 */
.progress-wrap { margin-top: 18px; }
.progress-bar {
  height: 8px;
  background: #e2e8f0;
  border-radius: 999px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #6366f1);
  border-radius: 999px;
  transition: width 120ms linear;
}
.progress-text {
  display: flex; justify-content: space-between;
  margin-top: 8px;
  font-size: 12px; color: #64748b;
}

/* 成功 */
.success-card {
  padding: 24px 12px 8px;
  text-align: center;
}
.success-circle {
  width: 64px; height: 64px; margin: 0 auto 14px;
  border-radius: 50%;
  background: linear-gradient(135deg, #10b981, #059669);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 32px; font-weight: 700;
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.35);
  animation: pop 400ms cubic-bezier(0.16, 1, 0.3, 1);
}
@keyframes pop {
  0% { transform: scale(0.4); opacity: 0; }
  60% { transform: scale(1.08); opacity: 1; }
  100% { transform: scale(1); }
}
.success-card h4 { margin: 0 0 4px; font-size: 17px; font-weight: 700; color: #0f172a; }
.success-sub { margin: 0 0 16px; font-size: 13px; color: #64748b; }
.success-meta {
  display: grid; grid-template-columns: 1fr 1fr; gap: 10px;
  padding: 14px;
  background: #f8fafc; border-radius: 12px;
  text-align: left;
}
.meta-item { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.meta-label { font-size: 11px; color: #94a3b8; font-weight: 500; }
.meta-value {
  font-size: 12px; color: #334155; font-weight: 600;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

/* 底部按钮 */
.modal-footer {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 16px 24px 20px;
  border-top: 1px solid #f1f5f9;
  background: #fafafa;
}
.btn {
  display: inline-flex; align-items: center; justify-content: center; gap: 6px;
  padding: 10px 20px;
  border: 1px solid transparent;
  border-radius: 10px;
  font-size: 14px; font-weight: 600;
  cursor: pointer;
  transition: all 120ms;
  white-space: nowrap;
}
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.25);
}
.btn-primary:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(37, 99, 235, 0.35); }
.btn-secondary {
  background: #fff; color: #334155; border-color: #e2e8f0;
}
.btn-secondary:hover { background: #f1f5f9; border-color: #cbd5e1; }

/* ============ 过渡动画 ============ */
.modal-enter-active,
.modal-leave-active { transition: opacity 200ms; }
.modal-enter-from,
.modal-leave-to { opacity: 0; }
.modal-enter-active .modal,
.modal-leave-active .modal { transition: transform 240ms cubic-bezier(0.16, 1, 0.3, 1), opacity 240ms; }
.modal-enter-from .modal,
.modal-leave-to .modal { transform: translateY(16px) scale(0.98); opacity: 0; }

/* ============ 响应式 ============ */
@media (max-width: 540px) {
  .modal-header { padding: 18px 18px 14px; }
  .modal-body   { padding: 16px 18px; }
  .modal-footer { padding: 14px 18px 16px; }
  .header-title h3 { font-size: 16px; }
  .dropzone { padding: 24px 16px; }
  .success-meta { grid-template-columns: 1fr; }
}
</style>
