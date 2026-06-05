/**
 * 用户配置 API 服务
 */
import type { UserConfig } from '@/types/config';
import { API_BASE_URL, API_ENDPOINTS } from '@/constants';

export class UserConfigApi {
  
  /**
   * 获取用户配置
   */
  static async getConfig(userId: string): Promise<UserConfig> {
    const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.GET_CONFIG}?userId=${userId}`);
    if (!response.ok) {
      throw new Error(`获取配置失败: ${response.status}`);
    }
    return response.json();
  }

  /**
   * 更新用户配置
   */
  static async updateConfig(config: UserConfig): Promise<UserConfig> {
    const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.UPDATE_CONFIG}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(config),
    });
    if (!response.ok) {
      throw new Error(`更新配置失败: ${response.status}`);
    }
    return response.json();
  }
}
