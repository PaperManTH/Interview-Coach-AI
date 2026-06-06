package com.interviewcoach.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interviewcoach.entity.UserProviderConfig;
import com.interviewcoach.mapper.UserProviderConfigMapper;
import com.interviewcoach.service.UserConfigService;

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

    private final UserProviderConfigMapper mapper;

    @Override
    public UserProviderConfig getConfig(String userId) {
        return mapper.selectOne(
                new LambdaQueryWrapper<UserProviderConfig>()
                        .eq(UserProviderConfig::getUserId, userId)
        );
    }

    @Override
    public UserProviderConfig saveConfig(String userId, UserProviderConfig config) {
        // 如果 apiKey 为空，保留已有值
        UserProviderConfig existing = getConfig(userId);
        if (existing != null) {
            if (isBlank(config.getAsrApiKey())) {
                config.setAsrApiKey(existing.getAsrApiKey());
            }
            if (isBlank(config.getTtsApiKey())) {
                config.setTtsApiKey(existing.getTtsApiKey());
            }
            if (isBlank(config.getLlmApiKey())) {
                config.setLlmApiKey(existing.getLlmApiKey());
            }
            config.setId(existing.getId());
        }

        config.setUserId(userId);
        log.info("[Config] 保存用户配置 userId={}, asrType={}, ttsType={}, llmType={}",
                userId, config.getAsrType(), config.getTtsType(), config.getLlmType());
        mapper.insertOrUpdate(config);
        return getConfig(userId);
    }

    @Override
    public void deleteConfig(String userId) {
        mapper.delete(new LambdaQueryWrapper<UserProviderConfig>()
                .eq(UserProviderConfig::getUserId, userId));
        log.info("[Config] 删除用户配置 userId={}", userId);
    }

    @Override
    public boolean hasConfig(String userId) {
        return mapper.exists(new LambdaQueryWrapper<UserProviderConfig>()
                .eq(UserProviderConfig::getUserId, userId));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
