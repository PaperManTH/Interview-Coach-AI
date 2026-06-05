/**
 * API 配置常量
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
export const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080';

// API 端点
export const API_ENDPOINTS = {
  // 用户配置
  USER_CONFIG: '/api/user/config',
  GET_CONFIG: '/api/user/config',
  UPDATE_CONFIG: '/api/user/config',
  
  // WebSocket
  WS_INTERVIEW: '/ws/interview',
};

// 提供商类型
export const PROVIDER_TYPES = {
  ASR: {
    MOCK: 'mock',
    IFLYTEK: 'iflytek',
    OPENAI: 'openai',
    AZURE: 'azure',
  },
  LLM: {
    MOCK: 'mock',
    OPENAI: 'openai',
    AZURE: 'azure',
    QIANWEN: 'qianwen',
    DOUBAO: 'doubao',
  },
  TTS: {
    MOCK: 'mock',
    IFLYTEK: 'iflytek',
    OPENAI: 'openai',
    AZURE: 'azure',
  },
} as const;

// 提供商显示名称
export const PROVIDER_NAMES = {
  mock: '模拟',
  iflytek: '讯飞',
  openai: 'OpenAI',
  azure: 'Azure',
  qianwen: '千问',
  doubao: '豆包',
} as const;
