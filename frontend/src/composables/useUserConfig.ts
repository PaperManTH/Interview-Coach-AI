/**
 * 用户配置组合式函数
 */
import { ref } from 'vue';
import type { UserConfig } from '@/types/config';
import { UserConfigApi } from '@/services/userConfig';

export function useUserConfig() {
  const config = ref<UserConfig | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);

  const fetchConfig = async (userId: string) => {
    loading.value = true;
    error.value = null;
    
    try {
      config.value = await UserConfigApi.getConfig(userId);
    } catch (e) {
      error.value = e instanceof Error ? e.message : '获取配置失败';
      console.error('[useUserConfig] 获取配置失败:', e);
    } finally {
      loading.value = false;
    }
  };

  const updateConfig = async (newConfig: UserConfig) => {
    loading.value = true;
    error.value = null;
    
    try {
      config.value = await UserConfigApi.updateConfig(newConfig);
      return true;
    } catch (e) {
      error.value = e instanceof Error ? e.message : '更新配置失败';
      console.error('[useUserConfig] 更新配置失败:', e);
      return false;
    } finally {
      loading.value = false;
    }
  };

  return {
    config,
    loading,
    error,
    fetchConfig,
    updateConfig,
  };
}
