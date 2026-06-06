/**
 * 简历上传 Composable
 *
 * 封装文件校验、上传、状态管理与本地持久化逻辑。
 */
import { ref, computed, watch } from 'vue';
import { ResumeUploadError, uploadResume } from '@/services/resumeApi';
import type {
  UploadStatus,
  UploadProgressEvent,
  UploadResult,
  ResumeResponse,
} from '@/types/resume';

const LOCAL_STORAGE_KEY = 'icai:resume-result';

/** 支持的文件扩展名正则 */
const ALLOWED_EXTENSIONS = /\.(pdf|doc|docx|txt|md|rtf)$/i;

export function useResumeUpload(maxSizeMB = 10) {
  // ============ 状态 ============
  const file = ref<File | null>(null);
  const status = ref<UploadStatus>('idle');
  const progress = ref(0);
  const errorMessage = ref('');
  const errorCode = ref('');
  const traceId = ref('');
  const result = ref<ResumeResponse | null>(null);

  const maxBytes = maxSizeMB * 1024 * 1024;

  // ============ 计算属性 ============
  const formattedSize = computed(() => {
    if (!file.value) return '';
    return humanSize(file.value.size);
  });

  const canUpload = computed(() => {
    return !!file.value && status.value !== 'uploading' && status.value !== 'success';
  });

  const hasResult = computed(() => !!result.value);
  const parsedData = computed(() => result.value?.parsedData ?? null);
  const skills = computed(() => parsedData.value?.skills ?? []);
  const possibleQuestions = computed(() => parsedData.value?.possibleQuestions ?? []);
  const resumeTextPreview = computed(() => result.value?.resumeTextPreview ?? '');

  // ============ 初始化：从 localStorage 恢复 ============
  function restoreFromStorage() {
    try {
      const raw = localStorage.getItem(LOCAL_STORAGE_KEY);
      if (raw) {
        const data = JSON.parse(raw);
        if (data?.id) {
          result.value = data as ResumeResponse;
          status.value = 'success';
        }
      }
    } catch { /* noop */ }
  }

  // ============ 文件校验 ============
  function validateFile(f: File): string | null {
    // 类型校验
    if (!ALLOWED_EXTENSIONS.test(f.name)) {
      return '不支持的文件类型。请上传 PDF / DOC / DOCX / TXT 格式。';
    }
    // 大小校验
    if (f.size > maxBytes) {
      return `文件过大（${humanSize(f.size)}），最大允许 ${maxSizeMB} MB。`;
    }
    // 空文件校验
    if (f.size === 0) {
      return '文件内容为空，请选择有效文件。';
    }
    return null;
  }

  // ============ 选择文件 ============
  function selectFile(f: File) {
    reset();
    const err = validateFile(f);
    if (err) {
      status.value = 'error';
      errorMessage.value = err;
      errorCode.value = 'VALIDATION';
      return false;
    }
    file.value = f;
    status.value = 'idle';
    return true;
  }

  // ============ 上传 ============
  async function upload(userId?: string): Promise<UploadResult | null> {
    if (!file.value) return null;

    status.value = 'uploading';
    errorMessage.value = '';
    errorCode.value = '';
    traceId.value = '';
    progress.value = 0;

    try {
      const res = await uploadResume(
        file.value,
        userId,
        (e: UploadProgressEvent) => {
          progress.value = e.percent;
        },
      );

      result.value = res.response;
      status.value = 'success';
      progress.value = 100;

      // 持久化到 localStorage
      try {
        localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(res.response));
      } catch { /* noop */ }

      return res;
    } catch (err) {
      status.value = 'error';
      if (err instanceof ResumeUploadError) {
        errorMessage.value = err.message;
        errorCode.value = err.code;
        traceId.value = err.traceId ?? '';
      } else {
        errorMessage.value = '未知错误，请重试';
        errorCode.value = 'UNKNOWN';
      }
      return null;
    }
  }

  // ============ 重置 ============
  function reset() {
    file.value = null;
    status.value = 'idle';
    progress.value = 0;
    errorMessage.value = '';
    errorCode.value = '';
    traceId.value = '';
    result.value = null;
  }

  // ============ 清除本地存储 ============
  function clearStorage() {
    try { localStorage.removeItem(LOCAL_STORAGE_KEY); } catch { /* noop */ }
    reset();
  }

  // ============ 工具函数 ============
  function humanSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  }

  return {
    // 状态
    file,
    status,
    progress,
    errorMessage,
    errorCode,
    traceId,
    result,
    parsedData,
    skills,
    possibleQuestions,
    resumeTextPreview,
    // 计算属性
    formattedSize,
    canUpload,
    hasResult,
    // 方法
    selectFile,
    upload,
    reset,
    clearStorage,
    restoreFromStorage,
    validateFile,
  };
}
