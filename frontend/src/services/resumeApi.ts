/**
 * 简历上传 API 服务
 *
 * 使用 XMLHttpRequest 实现带进度回调的上传，
 * 支持请求超时、网络异常、HTTP 错误状态码的完整错误处理。
 */
import type {
  ApiResponse,
  ResumeResponse,
  UploadProgressEvent,
  UploadResult,
} from '@/types/resume';

const UPLOAD_URL = '/api/resume/upload';
const TIMEOUT_MS = 60_000; // 60s 超时

/** 上传错误分类 */
export class ResumeUploadError extends Error {
  constructor(
    message: string,
    public readonly code: 'VALIDATION' | 'NETWORK' | 'TIMEOUT' | 'SERVER' | 'UNKNOWN',
    public readonly httpStatus?: number,
    public readonly apiCode?: number,
    public readonly apiMessage?: string,
    public readonly traceId?: string,
  ) {
    super(message);
    this.name = 'ResumeUploadError';
  }
}

/**
 * 上传简历文件到后端解析。
 *
 * @param file    文件对象
 * @param userId  用户 ID（可选，匿名时不传）
 * @param onProgress  上传进度回调
 * @returns 解析后的简历数据
 * @throws ResumeUploadError
 */
export function uploadResume(
  file: File,
  userId?: string,
  onProgress?: (e: UploadProgressEvent) => void,
): Promise<UploadResult> {
  return new Promise((resolve, reject) => {
    // 1. 构造 FormData
    const formData = new FormData();
    formData.append('file', file);

    // 2. 创建 XHR
    const xhr = new XMLHttpRequest();
    xhr.open('POST', UPLOAD_URL);
    xhr.timeout = TIMEOUT_MS;

    // 3. 设置请求头（X-User-Id 放在 header，FormData 的 Content-Type 让浏览器自动设置含 boundary）
    if (userId && userId.trim()) {
      xhr.setRequestHeader('X-User-Id', userId.trim());
    }

    // 4. 上传进度
    if (onProgress) {
      xhr.upload.addEventListener('progress', (e) => {
        if (e.lengthComputable) {
          onProgress({
            loaded: e.loaded,
            total: e.total,
            percent: Math.round((e.loaded / e.total) * 100),
          });
        }
      });
    }

    // 5. 响应处理
    xhr.addEventListener('load', () => {
      try {
        const raw = xhr.responseText;
        let body: ApiResponse<ResumeResponse>;

        try {
          body = JSON.parse(raw);
        } catch {
          reject(new ResumeUploadError(
            '响应数据格式异常',
            'SERVER',
            xhr.status,
          ));
          return;
        }

        // HTTP 成功 & 业务码成功
        if (xhr.status >= 200 && xhr.status < 300 && body.code === 0) {
          resolve({
            response: body.data,
            fileName: file.name,
            fileSize: file.size,
          });
          return;
        }

        // 业务错误
        reject(new ResumeUploadError(
          body.message || '上传失败',
          'SERVER',
          xhr.status,
          body.code,
          body.message,
          body.traceId,
        ));
      } catch (err) {
        if (err instanceof ResumeUploadError) {
          reject(err);
        } else {
          reject(new ResumeUploadError(
            '响应解析异常',
            'UNKNOWN',
            xhr.status,
          ));
        }
      }
    });

    // 6. 网络异常
    xhr.addEventListener('error', () => {
      reject(new ResumeUploadError(
        '网络连接失败，请检查网络后重试',
        'NETWORK',
      ));
    });

    // 7. 请求超时
    xhr.addEventListener('timeout', () => {
      reject(new ResumeUploadError(
        '上传超时，文件可能过大或网络较慢',
        'TIMEOUT',
      ));
    });

    // 8. 请求取消
    xhr.addEventListener('abort', () => {
      reject(new ResumeUploadError(
        '上传已取消',
        'NETWORK',
      ));
    });

    // 9. 发送
    xhr.send(formData);
  });
}
