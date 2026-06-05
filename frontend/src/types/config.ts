/**
 * 用户配置类型定义
 */
export interface UserProviderConfig {
  userId: string;
  asrType: string;
  llmType: string;
  ttsType: string;
  asrConfig?: ProviderConfig;
  llmConfig?: ProviderConfig;
  ttsConfig?: ProviderConfig;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProviderConfig {
  apiKey?: string;
  apiSecret?: string;
  baseUrl?: string;
  model?: string;
  voice?: string;
  [key: string]: any;
}

export interface UserConfig extends UserProviderConfig {}

/**
 * 提供商信息
 */
export interface ProviderInfo {
  type: string;
  name: string;
  models?: string[];
  voices?: string[];
  available: boolean;
}
