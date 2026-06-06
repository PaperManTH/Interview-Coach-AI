/**
 * API 配置常量
 */
export const API_BASE_URL = '';
export const WS_BASE_URL = '';

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
  { value: 'iflytek', label: '讯飞星火' },
] as const;

// TTS 提供商
export const TTS_PROVIDERS = [
  { value: 'mock', label: '测试模式 (Mock)' },
  { value: 'openai', label: 'OpenAI TTS' },
  { value: 'iflytek', label: '讯飞星火' },
] as const;

// TTS 语音选项 - 通用（OpenAI）
export const TTS_VOICES = [
  { value: 'alloy', label: 'Alloy (中性)' },
  { value: 'echo', label: 'Echo (男声)' },
  { value: 'fable', label: 'Fable (英式)' },
  { value: 'nova', label: 'Nova (女声)' },
  { value: 'onyx', label: 'Onyx (深沉)' },
  { value: 'shimmer', label: 'Shimmer (柔和)' },
] as const;

// 讯飞超拟人合成发音人 - 中文
export const IFLYTEK_CN_VOICES = [
  { value: 'x6_lingxiaoxuan_pro', label: '聆小璇 - 女声' },
  { value: 'x6_lingfeiyi_pro', label: '聆飞逸 - 男声' },
  { value: 'x6_lingxiaoyue_pro', label: '聆小玥 - 女声' },
  { value: 'x6_lingyuyan_pro', label: '聆玉言 - 女声' },
  { value: 'x5_lingyuzhao_flow', label: '聆玉昭 - 女声' },
] as const;

// 讯飞超拟人合成发音人 - 英文
export const IFLYTEK_EN_VOICES = [
  { value: 'x5_EnUs_Lila_flow', label: 'Lila - 女声（美式）' },
  { value: 'x5_EnUs_Grant_flow', label: 'Grant - 男声（美式）' },
] as const;
