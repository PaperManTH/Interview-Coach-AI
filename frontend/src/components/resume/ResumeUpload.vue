<script setup lang="ts">
/**
 * 简历上传组件
 *
 * 对接 POST /api/resume/upload 真实接口，支持：
 * - 拖拽/点击选择文件
 * - 客户端校验（类型、大小）
 * - 带进度的 XHR 上传
 * - 后端解析结果展示（技能 / 可能问题）
 * - 网络异常、超时、业务错误的友好提示
 */
import { ref, computed, watch, onMounted } from 'vue';
import resumePng from '@/assets/data_analysis.png';
import cloudPng from '@/assets/chat_icon.png';
import filePng from '@/assets/tool_icon.png';
import { useResumeUpload } from '@/composables/useResumeUpload';
import { useAuthStore } from '@/stores/authStore';

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
  (e: 'uploaded', payload: { name: string; size: number; result: any }): void;
  (e: 'error', message: string): void;
}>();

const auth = useAuthStore();
const {
  file,
  status,
  progress,
  errorMessage,
  errorCode,
  parsedData,
  skills,
  possibleQuestions,
  resumeTextPreview,
  formattedSize,
  canUpload,
  selectFile,
  upload,
  reset,
  restoreFromStorage,
} = useResumeUpload(props.maxSizeMB);

// 弹窗控制
const _open = ref(false);
const isOpen = computed({
  get: () => props.modelValue !== undefined ? props.modelValue : _open.value,
  set: (v) => {
    if (props.modelValue !== undefined) emit('update:modelValue', v);
    else _open.value = v;
  },
});

const isDragging = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);

const ACCEPT = props.accept ?? '.pdf,.doc,.docx,.txt,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,text/plain';

onMounted(() => {
  restoreFromStorage();
});

// ============ 弹窗控制 ============
function open() { isOpen.value = true; }
function close() {
  if (status.value === 'uploading') return;
  isOpen.value = false;
}

function pickFile() { fileInput.value?.click(); }

// ============ 文件选择 ============
function onFileInput(e: Event) {
  const target = e.target as HTMLInputElement;
  const selected = target.files?.[0] ?? null;
  target.value = '';
  if (selected && selectFile(selected)) {
    startUpload();
  }
}

function onDrop(e: DragEvent) {
  e.preventDefault();
  isDragging.value = false;
  const f = e.dataTransfer?.files?.[0];
  if (f && selectFile(f)) {
    startUpload();
  }
}

function onDragOver(e: DragEvent) {
  e.preventDefault();
  isDragging.value = true;
}

function onDragLeave() { isDragging.value = false; }

// ============ 上传 ============
async function startUpload() {
  const result = await upload(auth.userId || undefined);
  if (result) {
    emit('uploaded', {
      name: result.fileName,
      size: result.fileSize,
      result: result.response,
    });
  } else {
    emit('error', errorMessage.value);
  }
}

/** 重新上传：回到空闲状态 */
function resetAndRetry() {
  reset();
}

/** 父组件清除简历时同步清除内部状态 */
function clearResult() {
  reset();
}

/** 一键快速上传：打开弹窗并弹出文件选择器，选完自动上传 */
function quickUpload() {
  reset();
  isOpen.value = true;
  fileInput.value?.click();
}

// 打开弹窗时，如果是成功或错误态，重置为空闲让用户重新上传
watch(isOpen, (v) => {
  if (v && (status.value === 'success' || status.value === 'error')) {
    reset();
  }
});

defineExpose({ open, close, clearResult, quickUpload });
</script>

<template>
  <!-- 隐藏的文件输入，始终渲染以支持 quickUpload 同步触发 -->
  <input ref="fileInput" type="file" :accept="ACCEPT" hidden @change="onFileInput" />

  <!-- 入口按钮 -->
  <slot v-if="!hideTrigger" name="trigger" :open="open">
    <button class="resume-trigger" @click="open" type="button">
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
      <div v-if="isOpen" class="modal-backdrop" @click.self="close">
        <div class="modal" role="dialog" aria-modal="true" aria-label="Upload Resume">

          <!-- Header -->
          <header class="modal-header">
            <div class="header-title">
              <img :src="resumePng" class="header-icon" alt="resume" />
              <div>
                <h3>Upload Your Resume</h3>
                <p>上传简历 · 帮助 AI 面试官提出更有针对性的问题</p>
              </div>
            </div>
            <button class="close-btn" @click="close" aria-label="close" :disabled="status === 'uploading'">✕</button>
          </header>

          <div class="modal-body">
            <!-- 拖拽区（非成功态） -->
            <label
              v-if="status !== 'success'"
              class="dropzone"
              :class="{ 'is-dragging': isDragging, 'has-file': !!file }"
              @drop="onDrop"
              @dragover="onDragOver"
              @dragleave="onDragLeave"
            >
              <img :src="cloudPng" class="dropzone-icon" v-if="!file" alt="" />
              <img :src="filePng" class="dropzone-icon has-file-icon" v-else alt="" />
              <div class="dropzone-title">
                {{ file ? file.name : 'Drop your resume here, or click to browse' }}
              </div>
              <div class="dropzone-sub">
                {{ file ? formattedSize : `支持 PDF / DOC / DOCX / TXT，最大 ${props.maxSizeMB ?? 10}MB` }}
              </div>
              <button type="button" class="choose-btn" @click.stop="pickFile">
                {{ file ? '更换文件' : '选择文件' }}
              </button>
            </label>

            <!-- 校验错误（选择文件阶段） -->
            <div v-if="status === 'error' && errorCode === 'VALIDATION'" class="alert error">
              <span class="alert-icon">⚠️</span>
              <span>{{ errorMessage }}</span>
            </div>

            <!-- 上传进度 -->
            <div v-if="status === 'uploading'" class="progress-wrap">
              <div class="progress-bar"><div class="progress-fill" :style="{ width: progress + '%' }"></div></div>
              <div class="progress-text">
                <span>上传解析中…</span>
                <span>{{ progress }}%</span>
              </div>
            </div>

            <!-- 网络/服务器错误 -->
            <div v-if="status === 'error' && errorCode !== 'VALIDATION'" class="alert error">
              <span class="alert-icon">⚠️</span>
              <div class="error-detail">
                <span class="error-msg">{{ errorMessage }}</span>
                <span v-if="errorCode" class="error-meta">
                  {{ errorCode === 'NETWORK' ? '网络异常' : errorCode === 'TIMEOUT' ? '请求超时' : errorCode === 'SERVER' ? '服务器错误' : '未知错误' }}
                </span>
              </div>
            </div>

            <!-- 成功：展示解析结果 -->
            <div v-if="status === 'success'" class="result-card">
              <div class="success-banner">
                <span class="success-check">✓</span>
                <div>
                  <h4>简历解析成功</h4>
                  <p class="success-file">{{ file?.name }} · {{ formattedSize }}</p>
                </div>
              </div>

              <!-- 文本预览 -->
              <div v-if="resumeTextPreview" class="result-section">
                <div class="result-label">文本预览</div>
                <div class="result-preview">{{ resumeTextPreview }}</div>
              </div>

              <!-- 技能 -->
              <div v-if="skills.length" class="result-section">
                <div class="result-label">技能标签</div>
                <div class="skill-tags">
                  <span v-for="s in skills" :key="s" class="skill-tag">{{ s }}</span>
                </div>
              </div>

              <!-- 可能问题 -->
              <div v-if="possibleQuestions.length" class="result-section">
                <div class="result-label">AI 可能提问的方向</div>
                <ul class="question-list">
                  <li v-for="(q, i) in possibleQuestions" :key="i">{{ q }}</li>
                </ul>
              </div>

              <!-- LLM 未解析出结构化数据 -->
              <div v-if="!parsedData" class="alert warning">
                <span class="alert-icon">ℹ️</span>
                <span>简历文本已提取，但结构化解析尚未完成。您仍可正常使用面试功能。</span>
              </div>
            </div>
          </div>

          <footer class="modal-footer">
            <button class="btn btn-secondary" @click="close" type="button" :disabled="status === 'uploading'">
              {{ status === 'success' ? '完成' : '取消' }}
            </button>
            <!-- 未上传时显示上传按钮 -->
            <button
              v-if="status !== 'success' && status !== 'error'"
              class="btn btn-primary"
              :disabled="!canUpload"
              @click="startUpload"
              type="button"
            >
              <span v-if="status === 'uploading'">解析中…</span>
              <span v-else>上传并解析</span>
            </button>
            <!-- 校验错误可重选文件 -->
            <button
              v-if="status === 'error' && errorCode === 'VALIDATION'"
              class="btn btn-primary"
              @click="pickFile"
              type="button"
            >
              重新选择
            </button>
            <!-- 网络/服务器错误可重试 -->
            <button
              v-if="status === 'error' && errorCode !== 'VALIDATION'"
              class="btn btn-primary"
              @click="startUpload"
              type="button"
            >
              重试
            </button>
            <!-- 成功后可重新上传 -->
            <button
              v-if="status === 'success'"
              class="btn btn-secondary"
              @click="resetAndRetry"
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
  transition: all 200ms;
}
.resume-trigger:hover {
  border-color: #3b82f6;
  background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
  box-shadow: 0 2px 12px rgba(59, 130, 246, 0.12);
}
.trigger-icon { width: 36px; height: 36px; object-fit: contain; }
.trigger-texts { flex: 1; text-align: left; }
.trigger-title { display: block; font-size: 15px; font-weight: 700; color: #0f172a; }
.trigger-sub { display: block; font-size: 12px; color: #64748b; margin-top: 2px; }
.trigger-chevron { font-size: 18px; color: #94a3b8; transition: transform 200ms; }
.resume-trigger:hover .trigger-chevron { transform: translateX(4px); color: #3b82f6; }

/* ============ 遮罩 ============ */
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.55);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

/* ============ 弹窗 ============ */
.modal {
  width: min(560px, 94vw);
  max-height: 88vh;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 20px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
}

/* ============ Header ============ */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px 28px 0;
}
.header-title { display: flex; align-items: center; gap: 12px; }
.header-icon { width: 42px; height: 42px; object-fit: contain; }
.header-title h3 { font-size: 18px; font-weight: 700; color: #0f172a; margin: 0; }
.header-title p { font-size: 12px; color: #64748b; margin: 2px 0 0; }
.close-btn {
  width: 32px; height: 32px;
  border-radius: 50%;
  border: 1px solid rgba(148, 163, 184, 0.25);
  background: rgba(255, 255, 255, 0.7);
  font-size: 16px;
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 150ms;
}
.close-btn:hover { background: rgba(239, 68, 68, 0.08); color: #dc2626; border-color: rgba(239, 68, 68, 0.25); }
.close-btn:disabled { opacity: 0.4; cursor: not-allowed; }

/* ============ Body ============ */
.modal-body { padding: 20px 28px 24px; flex: 1; }

/* dropzone */
.dropzone {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 32px 20px;
  border: 2px dashed #cbd5e1;
  border-radius: 14px;
  background: rgba(248, 250, 252, 0.6);
  cursor: pointer;
  transition: all 200ms;
  text-align: center;
}
.dropzone:hover { border-color: #94a3b8; background: rgba(241, 245, 249, 0.8); }
.dropzone.is-dragging { border-color: #3b82f6; background: rgba(219, 234, 254, 0.5); }
.dropzone.has-file { border-style: solid; border-color: #3b82f6; background: rgba(239, 246, 255, 0.5); }
.dropzone-icon { width: 48px; height: 48px; object-fit: contain; opacity: 0.6; }
.dropzone-icon.has-file-icon { opacity: 1; }
.dropzone-title { font-size: 14px; font-weight: 600; color: #334155; }
.dropzone-sub { font-size: 12px; color: #94a3b8; }
.choose-btn {
  padding: 8px 20px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  background: #fff;
  font-size: 13px;
  color: #334155;
  cursor: pointer;
  transition: all 150ms;
}
.choose-btn:hover { border-color: #3b82f6; color: #3b82f6; }

/* alert */
.alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  margin-top: 12px;
  font-size: 13px;
}
.alert.error {
  background: rgba(254, 242, 242, 0.8);
  border: 1px solid rgba(248, 113, 113, 0.3);
  color: #991b1b;
}
.alert.warning {
  background: rgba(254, 252, 232, 0.8);
  border: 1px solid rgba(250, 204, 21, 0.3);
  color: #92400e;
}
.alert-icon { flex-shrink: 0; font-size: 16px; line-height: 1.4; }
.error-detail { display: flex; flex-direction: column; gap: 4px; }
.error-msg { font-weight: 500; }
.error-meta { font-size: 11px; color: #94a3b8; }

/* progress */
.progress-wrap { margin-top: 16px; }
.progress-bar {
  height: 8px;
  border-radius: 4px;
  background: #e2e8f0;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #6366f1);
  border-radius: 4px;
  transition: width 200ms ease;
}
.progress-text {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #64748b;
}

/* result card */
.result-card { display: flex; flex-direction: column; gap: 16px; }
.success-banner {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.08), rgba(59, 130, 246, 0.08));
  border: 1px solid rgba(34, 197, 94, 0.2);
}
.success-check {
  width: 40px; height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  flex-shrink: 0;
}
.success-banner h4 { font-size: 16px; font-weight: 700; color: #0f172a; margin: 0; }
.success-file { font-size: 12px; color: #64748b; margin: 2px 0 0; }

.result-section { margin-top: 4px; }
.result-label {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.result-preview {
  max-height: 120px;
  overflow-y: auto;
  padding: 10px 14px;
  border-radius: 10px;
  background: rgba(241, 245, 249, 0.7);
  border: 1px solid rgba(148, 163, 184, 0.15);
  font-size: 12px;
  line-height: 1.6;
  color: #334155;
  white-space: pre-wrap;
  word-break: break-all;
}

.skill-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.skill-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.2);
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 500;
}

.question-list {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: #334155;
  line-height: 1.8;
}
.question-list li::marker { color: #3b82f6; }

/* ============ Footer ============ */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 28px 22px;
  border-top: 1px solid rgba(148, 163, 184, 0.12);
}
.btn {
  padding: 10px 22px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 150ms;
  border: none;
}
.btn-primary {
  background: linear-gradient(135deg, #1e40af, #1e3a5f);
  color: #fff;
  box-shadow: 0 2px 8px rgba(30, 64, 175, 0.35);
}
.btn-primary:hover:not(:disabled) { box-shadow: 0 4px 14px rgba(30, 64, 175, 0.45); transform: translateY(-1px); }
.btn-primary:disabled { opacity: 0.45; cursor: not-allowed; }
.btn-secondary {
  background: rgba(241, 245, 249, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.25);
  color: #334155;
}
.btn-secondary:hover:not(:disabled) { background: rgba(226, 232, 240, 0.8); }
.btn-secondary:disabled { opacity: 0.45; cursor: not-allowed; }

/* ============ Transition ============ */
.modal-enter-active { transition: opacity 250ms ease; }
.modal-leave-active { transition: opacity 200ms ease; }
.modal-enter-from { opacity: 0; }
.modal-leave-to { opacity: 0; }
.modal-enter-active .modal { animation: scale-in 250ms cubic-bezier(0.16, 1, 0.3, 1); }
.modal-leave-active .modal { animation: scale-out 200ms ease forwards; }

@keyframes scale-in {
  from { transform: scale(0.94) translateY(12px); opacity: 0; }
  to   { transform: scale(1) translateY(0); opacity: 1; }
}
@keyframes scale-out {
  from { transform: scale(1) translateY(0); opacity: 1; }
  to   { transform: scale(0.94) translateY(12px); opacity: 0; }
}
</style>
