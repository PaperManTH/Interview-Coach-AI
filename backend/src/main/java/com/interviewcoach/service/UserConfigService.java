package com.interviewcoach.service;

import com.interviewcoach.config.userconfig.UserProviderConfig;

/**
 * 用户配置服务接口
 */
public interface UserConfigService {

    /**
     * 获取用户配置
     * @param userId 用户ID
     * @return 用户配置
     */
    UserProviderConfig getConfig(String userId);

    /**
     * 保存用户配置
     * @param userId 用户ID
     * @param config 配置对象
     * @return 保存后的配置
     */
    UserProviderConfig saveConfig(String userId, UserProviderConfig config);

    /**
     * 删除用户配置
     * @param userId 用户ID
     */
    void deleteConfig(String userId);

    /**
     * 检查用户是否有配置
     * @param userId 用户ID
     * @return 是否存在配置
     */
    boolean hasConfig(String userId);
}
