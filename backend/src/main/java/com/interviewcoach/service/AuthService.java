package com.interviewcoach.service;

import com.interviewcoach.entity.dto.auth.LoginResponse;

/**
 * 认证服务接口 - GitHub OAuth2 + JWT 无状态令牌 + State 防 CSRF。
 */
public interface AuthService {

    /**
     * 生成防 CSRF 的 state 并暂存。
     */
    String generateState();

    /**
     * 校验 state 是否有效（一次性使用）。
     */
    boolean validateState(String state);

    /**
     * GitHub OAuth2 登录 + 构建完整响应。
     * Service 层处理全部业务逻辑（用户创建/更新、配置初始化、JWT 生成）。
     *
     * @param code  GitHub 返回的授权码
     * @param state CSRF 防护 state
     * @return LoginResponse（含 userId / username / avatarUrl / token / config）
     * @throws IllegalArgumentException 当 state 校验失败
     */
    LoginResponse loginWithGithubAndBuildResponse(String code, String state);

    /**
     * 根据 JWT 获取当前用户信息。
     * 无 DB token 查证——仅验签 + 过期 + 查用户最新昵称头像。
     *
     * @param token JWT（不含 Bearer 前缀）
     * @return LoginResponse，验证失败返回 null
     */
    LoginResponse getCurrentUserByToken(String token);

    /**
     * 验证 JWT 签名和过期时间（无数据库查询）。
     */
    String validateToken(String token);

    /**
     * 登出（JWT 无状态，仅清理后端的 state/token 缓存）。
     */
    void logout(String userId);
}
