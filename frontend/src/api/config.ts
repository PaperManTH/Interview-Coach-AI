// 配置相关 API 调用
export interface UserConfig {
  userId: string;
  asrType: string;
  asrConfigured: boolean;
  asrBaseUrl: string;
  ttsType: string;
  ttsConfigured: boolean;
  ttsBaseUrl: string;
  ttsVoice: string;
  llmType: string;
  llmConfigured: boolean;
  llmBaseUrl: string;
  llmModel: string;
}

export interface UserConfigRequest {
  asrType?: string;
  asrApiKey?: string;
  asrBaseUrl?: string;
  ttsType?: string;
  ttsApiKey?: string;
  ttsBaseUrl?: string;
  ttsVoice?: string;
  llmType?: string;
  llmApiKey?: string;
  llmBaseUrl?: string;
  llmModel?: string;
}

const BASE_URL = '';

export async function getUserConfig(userId: string): Promise<UserConfig | null> {
  try {
    const response = await fetch(`${BASE_URL}/api/user/config?userId=${userId}`);
    const data = await response.json();
    return data.data || null;
  } catch (error) {
    console.error('获取用户配置失败:', error);
    return null;
  }
}

export async function saveUserConfig(userId: string, config: UserConfigRequest): Promise<UserConfig | null> {
  try {
    const response = await fetch(`${BASE_URL}/api/user/config?userId=${userId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(config),
    });
    const data = await response.json();
    return data.data || null;
  } catch (error) {
    console.error('保存用户配置失败:', error);
    return null;
  }
}

export async function deleteUserConfig(userId: string): Promise<boolean> {
  try {
    const response = await fetch(`${BASE_URL}/api/user/config?userId=${userId}`, {
      method: 'DELETE',
    });
    return response.ok;
  } catch (error) {
    console.error('删除用户配置失败:', error);
    return false;
  }
}

export async function hasUserConfig(userId: string): Promise<boolean> {
  try {
    const response = await fetch(`${BASE_URL}/api/user/config/exists?userId=${userId}`);
    const data = await response.json();
    return data.data || false;
  } catch (error) {
    console.error('检查用户配置失败:', error);
    return false;
  }
}
