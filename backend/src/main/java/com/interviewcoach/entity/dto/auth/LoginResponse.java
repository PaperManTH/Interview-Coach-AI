package com.interviewcoach.entity.dto.auth;

import com.interviewcoach.entity.dto.userconfig.UserConfigResponse;

/**
 * 登录 / 当前用户响应 DTO。
 */
public class LoginResponse {

    private String userId;
    private String username;
    private String avatarUrl;
    private String token;
    private UserConfigResponse config;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserConfigResponse getConfig() { return config; }
    public void setConfig(UserConfigResponse config) { this.config = config; }
}
