package com.interviewcoach.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.interviewcoach.entity.User;
import com.interviewcoach.entity.dto.auth.LoginResponse;
import com.interviewcoach.entity.dto.userconfig.UserConfigResponse;
import com.interviewcoach.exception.OAuthException;
import com.interviewcoach.mapper.UserMapper;
import com.interviewcoach.service.AuthService;
import com.interviewcoach.service.UserConfigService;
import com.interviewcoach.userconfig.UserProviderConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现 - GitHub OAuth2 + JWT 无状态令牌 + State 防 CSRF。
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserConfigService userConfigService;
    private final SecretKey jwtKey;
    private final String githubClientId;
    private final String githubClientSecret;
    private final RestClient restClient;

    /** 后端暂存 state，key=state value=过期时间戳(ms) */
    private final Map<String, Long> stateStore = new ConcurrentHashMap<>();

    public AuthServiceImpl(UserMapper userMapper,
                           UserConfigService userConfigService,
                           @Value("${app.auth.jwt-secret}") String jwtSecret,
                           @Value("${app.auth.github.client-id}") String githubClientId,
                           @Value("${app.auth.github.client-secret}") String githubClientSecret,
                           RestClient.Builder restClientBuilder) {
        this.userMapper = userMapper;
        this.userConfigService = userConfigService;
        this.jwtKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.githubClientId = githubClientId;
        this.githubClientSecret = githubClientSecret;
        this.restClient = restClientBuilder.build();
    }

    // ==================== State 防 CSRF ====================

    @Override
    public String generateState() {
        cleanExpiredStates();
        String state = UUID.randomUUID().toString().replace("-", "");
        stateStore.put(state, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5));
        log.debug("[Auth] 生成 state={}...", state.substring(0, 8));
        return state;
    }

    @Override
    public boolean validateState(String state) {
        if (state == null || state.isBlank()) return false;
        Long expireAt = stateStore.remove(state);
        if (expireAt == null) {
            log.warn("[Auth] state 无效或已使用: {}", state.substring(0, Math.min(8, state.length())));
            return false;
        }
        if (System.currentTimeMillis() > expireAt) {
            log.warn("[Auth] state 已过期: {}", state.substring(0, Math.min(8, state.length())));
            return false;
        }
        return true;
    }

    private void cleanExpiredStates() {
        long now = System.currentTimeMillis();
        stateStore.entrySet().removeIf(e -> now > e.getValue());
    }

    // ==================== JWT ====================

    @Override
    public String validateToken(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(jwtKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            log.debug("[Auth] JWT 验证失败: {}", e.getMessage());
            return null;
        }
    }

    private String generateToken(String userId, String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer("interview-coach")
                .subject(userId)
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600 * 24 * 7)))
                .signWith(jwtKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 从 JWT 中提取 username claim。
     */
    private String getUsernameFromJwt(String token) {
        try {
            return Jwts.parser().verifyWith(jwtKey).build()
                    .parseSignedClaims(token).getPayload()
                    .get("username", String.class);
        } catch (Exception e) {
            return "";
        }
    }

    // ==================== 登录 / 当前用户 ====================

    @Override
    public LoginResponse loginWithGithubAndBuildResponse(String code, String state) {
        // CSRF state 校验 — 返回 403
        if (!validateState(state)) {
            throw new OAuthException(403, "CSRF_INVALID", "CSRF 校验失败，state 无效或已过期");
        }

        log.info("[Auth] GitHub OAuth2 登录 code={}",
                code.length() > 10 ? code.substring(0, 10) + "..." : code);

        // OAuth 交换 — 网络/API 失败时返回明确错误码，而非 500
        GithubUserInfo gh;
        try {
            gh = exchangeCodeForUserInfo(code);
        } catch (OAuthException e) {
            throw e; // 直接透传，保留原始状态码和错误信息
        } catch (Exception e) {
            log.error("[Auth] GitHub OAuth 交换失败", e);
            throw new OAuthException(502, "OAUTH_EXCHANGE_FAILED",
                    "GitHub 授权服务器返回异常，请稍后重试", e);
        }
        String userId = "github_" + gh.getGithubId();
        String username = gh.getUsername();

        // 查 user 表，不存在则创建
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
        if (user == null) {
            user = new User();
            user.setUserId(userId);
            user.setGithubId(gh.getGithubId());
            user.setUsername(username);
            user.setAvatarUrl(gh.getAvatarUrl());
            user.setEmail(gh.getEmail());
            userMapper.insert(user);
            log.info("[Auth] 新用户注册 userId={}, username={}", userId, username);
        } else {
            user.setUsername(username);
            user.setAvatarUrl(gh.getAvatarUrl());
            user.setEmail(gh.getEmail());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        }

        // 自动创建默认配置
        UserProviderConfig config = userConfigService.getConfig(userId);
        if (config == null) {
            config = createDefaultConfig(userId);
            userConfigService.saveConfig(userId, config);
        }

        // 生成 JWT
        String token = generateToken(userId, username);

        LoginResponse response = new LoginResponse();
        response.setUserId(userId);
        response.setUsername(username);
        response.setAvatarUrl(gh.getAvatarUrl());
        response.setToken(token);
        response.setConfig(UserConfigResponse.fromEntity(config));

        log.info("[Auth] 登录成功 userId={}, username={}", userId, username);
        return response;
    }

    @Override
    public LoginResponse getCurrentUserByToken(String token) {
        String userId = validateToken(token);
        if (userId == null) return null;

        String jwtUsername = getUsernameFromJwt(token);

        // 查用户表（仅获取最新昵称/头像）
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUserId, userId));

        LoginResponse response = new LoginResponse();
        response.setUserId(userId);
        response.setUsername(user != null ? user.getUsername() : jwtUsername);
        response.setAvatarUrl(user != null ? user.getAvatarUrl() : null);
        response.setToken(token);
        return response;
    }

    // ==================== 登出 ====================

    @Override
    public void logout(String userId) {
        log.info("[Auth] 用户登出 userId={} (JWT 无状态，客户端需清除 token)", userId);
    }

    // ==================== 内部方法 ====================

    private UserProviderConfig createDefaultConfig(String userId) {
        UserProviderConfig config = new UserProviderConfig();
        config.setUserId(userId);
        config.setAsrType("mock");
        config.setLlmType("mock");
        config.setTtsType("mock");
        return config;
    }

    /**
     * 通过 GitHub OAuth2 code 换取 access_token，再用 token 获取用户信息。
     */
    private GithubUserInfo exchangeCodeForUserInfo(String code) {
        // Step 1: code → access_token
        log.debug("[Auth] 正在向 GitHub 交换 access_token...");

        String accessToken;
        try {
            var tokenResp = restClient.post()
                    .uri("https://github.com/login/oauth/access_token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "client_id", githubClientId,
                            "client_secret", githubClientSecret,
                            "code", code
                    ))
                    .retrieve()
                    .body(Map.class);

            if (tokenResp == null || tokenResp.containsKey("error")) {
                String errDesc = tokenResp != null ? String.valueOf(tokenResp.getOrDefault("error_description", tokenResp.get("error"))) : "unknown";
                log.error("[Auth] GitHub 返回错误: {}", errDesc);
                throw new OAuthException(400, "GITHUB_AUTH_FAILED",
                        "GitHub 授权失败: " + errDesc);
            }

            accessToken = (String) tokenResp.get("access_token");
            if (accessToken == null || accessToken.isBlank()) {
                throw new OAuthException(400, "GITHUB_TOKEN_MISSING",
                        "GitHub 未返回 access_token");
            }
        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Auth] GitHub access_token 交换异常", e);
            throw new OAuthException(502, "OAUTH_EXCHANGE_FAILED",
                    "GitHub 授权服务器返回异常，请稍后重试", e);
        }

        log.debug("[Auth] access_token 获取成功");

        // Step 2: access_token → user info
        Map<String, Object> userInfo;
        try {
            userInfo = restClient.get()
                    .uri("https://api.github.com/user")
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            if (userInfo == null) {
                throw new OAuthException(502, "GITHUB_USER_FAILED",
                        "GitHub 用户信息接口无响应");
            }
        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Auth] GitHub 用户信息获取异常", e);
            throw new OAuthException(502, "OAUTH_EXCHANGE_FAILED",
                    "GitHub 获取用户信息失败，请稍后重试", e);
        }

        GithubUserInfo info = new GithubUserInfo();
        info.setGithubId(String.valueOf(userInfo.get("id")));
        info.setUsername(String.valueOf(userInfo.getOrDefault("login", "")));
        info.setAvatarUrl(String.valueOf(userInfo.getOrDefault("avatar_url", "")));
        info.setEmail(userInfo.get("email") != null ? String.valueOf(userInfo.get("email")) : null);

        log.info("[Auth] GitHub 用户信息获取成功: id={}, login={}",
                info.getGithubId(), info.getUsername());
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
