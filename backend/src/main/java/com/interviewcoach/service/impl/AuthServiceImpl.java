package com.interviewcoach.service.impl;

import com.interviewcoach.service.AuthService;
import com.interviewcoach.service.UserConfigService;
import com.interviewcoach.userconfig.UserProviderConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;

/**
 * 认证服务实现
 * 仅支持 GitHub OAuth2 登录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserConfigService userConfigService;

    @Override
    public UserProviderConfig loginWithGithub(String code) {
        log.info("[Auth] GitHub OAuth2 登录 code={}", code.substring(0, 10) + "...");
        
        String githubUserId = exchangeCodeForUserId(code);
        String userId = "github_" + githubUserId;
        
        UserProviderConfig config = userConfigService.getConfig(userId);
        if (config == null) {
            config = createDefaultConfig(userId);
            userConfigService.saveConfig(userId, config);
        }
        
        log.info("[Auth] GitHub 登录成功 userId={}", userId);
        return config;
    }

    @Override
    public String validateToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            return decoded.split(":")[0];
        } catch (Exception e) {
            log.warn("[Auth] Token 验证失败", e);
            return null;
        }
    }

    @Override
    public String generateToken(String userId) {
        String token = userId + ":" + UUID.randomUUID().toString();
        return Base64.getEncoder().encodeToString(token.getBytes());
    }

    @Override
    public void logout(String userId) {
        log.info("[Auth] 用户登出 userId={}", userId);
    }

    private UserProviderConfig createDefaultConfig(String userId) {
        UserProviderConfig config = new UserProviderConfig();
        config.setUserId(userId);
        config.setAsrType("mock");
        config.setLlmType("mock");
        config.setTtsType("mock");
        return config;
    }

    private String exchangeCodeForUserId(String code) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
