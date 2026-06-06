/**
 * 简历相关类型定义
 */

/** LLM 结构化解析结果 */
export interface ResumeParsedData {
  candidateSummary: string;
  skills: string[];
  projects: string[];
  workExperience: string[];
  strengths: string[];
  possibleQuestions: string[];
}

/** POST /api/resume/upload 响应 data 字段 */
export interface ResumeResponse {
  id: number;
  fileName: string;
  resumeTextPreview: string;
  parsedData: ResumeParsedData | null;
}

/** 统一 API 响应封装 */
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
  traceId: string;
}

/** 上传状态枚举 */
export type UploadStatus = 'idle' | 'validating' | 'uploading' | 'success' | 'error';

/** 上传进度事件 */
export interface UploadProgressEvent {
  loaded: number;
  total: number;
  percent: number;
}

/** 上传结果 */
export interface UploadResult {
  response: ResumeResponse;
  fileName: string;
  fileSize: number;
}
