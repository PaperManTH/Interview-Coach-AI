package com.interviewcoach.service;

import com.interviewcoach.userconfig.UserProviderConfig;

/**
 * 认证服务接口
 * 仅支持 GitHub OAuth2 登录
 */
public interface AuthService {

    /**
     * GitHub OAuth2 登录
     * @param code GitHub 返回的授权码
     * @return 用户配置
     */
    UserProviderConfig loginWithGithub(String code);

    /**
     * 验证 token
     * @param token 访问令牌
     * @return 用户 ID
     */
    String validateToken(String token);

    /**
     * 生成 JWT token
     * @param userId 用户 ID
     * @return JWT 令牌
     */
    String generateToken(String userId);

    /**
     * 登出
     * @param userId 用户 ID
     */
    void logout(String userId);
}
