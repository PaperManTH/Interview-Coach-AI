package com.interviewcoach.service.impl;

import com.interviewcoach.service.UserConfigService;
import com.interviewcoach.userconfig.UserProviderConfig;
import com.interviewcoach.userconfig.UserProviderConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserConfigServiceImpl implements UserConfigService {

    private final UserProviderConfigRepository repository;

    @Override
    public UserProviderConfig getConfig(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public UserProviderConfig saveConfig(String userId, UserProviderConfig config) {
        config.setUserId(userId);
        log.info("[Config] 保存用户配置 userId={}, asrType={}, ttsType={}, llmType={}", 
                userId, config.getAsrType(), config.getTtsType(), config.getLlmType());
        return repository.save(config);
    }

    @Override
    public void deleteConfig(String userId) {
        repository.deleteByUserId(userId);
        log.info("[Config] 删除用户配置 userId={}", userId);
    }

    @Override
    public boolean hasConfig(String userId) {
        return repository.existsByUserId(userId);
    }
}
