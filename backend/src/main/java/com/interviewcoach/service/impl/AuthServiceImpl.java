package com.interviewcoach.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interviewcoach.entity.User;
import com.interviewcoach.mapper.UserMapper;
import com.interviewcoach.service.AuthService;
import com.interviewcoach.service.UserConfigService;
import com.interviewcoach.userconfig.UserProviderConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * 认证服务实现 - GitHub OAuth2 登录。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserConfigService userConfigService;

    @Override
    public UserProviderConfig loginWithGithub(String code) {
        log.info("[Auth] GitHub OAuth2 登录 code={}", code.length() > 10 ? code.substring(0, 10) + "..." : code);

        // 用 code 换 GitHub access_token，再拿用户信息
        GithubUserInfo gh = exchangeCodeForUserInfo(code);
        String userId = "github_" + gh.getGithubId();

        // 查 user 表，不存在则创建
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) {
            user = new User();
            user.setUserId(userId);
            user.setGithubId(gh.getGithubId());
            user.setUsername(gh.getUsername());
            user.setAvatarUrl(gh.getAvatarUrl());
            user.setEmail(gh.getEmail());
            userMapper.insert(user);
            log.info("[Auth] 新用户注册 userId={}, username={}", userId, gh.getUsername());
        }

        // 生成 token 并更新
        String token = generateToken(userId);
        user.setToken(token);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 自动创建默认配置
        UserProviderConfig config = userConfigService.getConfig(userId);
        if (config == null) {
            config = createDefaultConfig(userId);
            userConfigService.saveConfig(userId, config);
        }

        log.info("[Auth] GitHub 登录成功 userId={}, username={}", userId, user.getUsername());
        return config;
    }

    @Override
    public String validateToken(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            return decoded.split(":")[0];
        } catch (Exception e) {
            log.warn("[Auth] Token 验证失败");
            return null;
        }
    }

    @Override
    public String generateToken(String userId) {
        return Base64.getEncoder().encodeToString((userId + ":" + UUID.randomUUID()).getBytes());
    }

    @Override
    public void logout(String userId) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user != null) {
            user.setToken(null);
            userMapper.updateById(user);
        }
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

    /**
     * 通过 GitHub OAuth2 code 获取用户信息（Mock 实现，后续接入 GitHub API）。
     */
    private GithubUserInfo exchangeCodeForUserInfo(String code) {
        // TODO: 接入 GitHub OAuth API
        // POST https://github.com/login/oauth/access_token
        // GET  https://api.github.com/user
        GithubUserInfo info = new GithubUserInfo();
        info.setGithubId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        info.setUsername("github_user");
        info.setAvatarUrl("https://avatars.githubusercontent.com/u/0");
        info.setEmail(null);
        return info;
    }

    private static class GithubUserInfo {
        private String githubId;
        private String username;
        private String avatarUrl;
        private String email;

        public String getGithubId() { return githubId; }
        public void setGithubId(String githubId) { this.githubId = githubId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
