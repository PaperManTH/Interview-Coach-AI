/**
 * API 配置常量
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
export const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080';

// API 端点
export const API_ENDPOINTS = {
  USER_CONFIG: '/api/user/config',
  GET_CONFIG: '/api/user/config',
  UPDATE_CONFIG: '/api/user/config',
  WS_INTERVIEW: '/ws/interview',
};

// LLM 提供商
export const LLM_PROVIDERS = [
  { value: 'mock', label: '测试模式 (Mock)' },
  { value: 'deepseek', label: 'DeepSeek 深度求索' },
  { value: 'qwen', label: '通义千问 (阿里百炼)' },
  { value: 'glm', label: '智谱 GLM' },
  { value: 'kimi', label: 'Moonshot Kimi' },
  { value: 'openai', label: 'OpenAI' },
  { value: 'azure', label: 'Azure OpenAI' },
] as const;

// ASR 提供商
export const ASR_PROVIDERS = [
  { value: 'mock', label: '测试模式 (Mock)' },
  { value: 'openai', label: 'OpenAI Whisper' },
  { value: 'azure', label: 'Azure Speech' },
] as const;

// TTS 提供商
export const TTS_PROVIDERS = [
  { value: 'mock', label: '测试模式 (Mock)' },
  { value: 'openai', label: 'OpenAI TTS' },
  { value: 'azure', label: 'Azure TTS' },
] as const;

// TTS 语音选项
export const TTS_VOICES = [
  { value: 'alloy', label: 'Alloy (中性)' },
  { value: 'echo', label: 'Echo (男声)' },
  { value: 'fable', label: 'Fable (英式)' },
  { value: 'nova', label: 'Nova (女声)' },
  { value: 'onyx', label: 'Onyx (深沉)' },
  { value: 'shimmer', label: 'Shimmer (柔和)' },
  { value: 'zh-CN-XiaoxiaoNeural', label: '晓晓 (Azure 中文女声)' },
  { value: 'zh-CN-YunxiNeural', label: '云希 (Azure 中文男声)' },
] as const;
