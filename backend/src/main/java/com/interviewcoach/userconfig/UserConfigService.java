package com.interviewcoach.userconfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户配置服务。
 * 管理用户级别的 Provider 配置。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserConfigService {

    private final UserProviderConfigRepository repository;

    public UserProviderConfig getConfig(String userId) {
        return repository.findByUserId(userId);
    }

    public UserProviderConfig saveConfig(String userId, UserProviderConfig config) {
        config.setUserId(userId);
        log.info("[Config] 保存用户配置 userId={}, asrType={}, ttsType={}, llmType={}", 
                userId, config.getAsrType(), config.getTtsType(), config.getLlmType());
        return repository.save(config);
    }

    public void deleteConfig(String userId) {
        repository.deleteByUserId(userId);
        log.info("[Config] 删除用户配置 userId={}", userId);
    }

    public boolean hasConfig(String userId) {
        return repository.existsByUserId(userId);
    }
}
