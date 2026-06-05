package com.interviewcoach.config.userconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户 Provider 配置 Repository（内存实现）。
 */
@Slf4j
@Repository
public class UserProviderConfigRepository {

    private final Map<String, UserProviderConfig> configMap = new ConcurrentHashMap<>();

    public UserProviderConfig findByUserId(String userId) {
        return configMap.get(userId);
    }

    public boolean existsByUserId(String userId) {
        return configMap.containsKey(userId);
    }

    public UserProviderConfig save(UserProviderConfig config) {
        config.setUpdatedAt(java.time.LocalDateTime.now());
        if (config.getCreatedAt() == null) {
            config.setCreatedAt(java.time.LocalDateTime.now());
        }
        configMap.put(config.getUserId(), config);
        log.debug("[Config] 保存配置 userId={}", config.getUserId());
        return config;
    }

    public void deleteByUserId(String userId) {
        configMap.remove(userId);
        log.debug("[Config] 删除配置 userId={}", userId);
    }
}
