package com.interviewcoach.controller;

import com.interviewcoach.entity.dto.auth.LoginResponse;
import com.interviewcoach.entity.dto.common.ApiResponse;
import com.interviewcoach.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器 - 纯委托 Service，不含任何 Mapper / JWT 依赖。
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 生成 state。
     * @return  state
     */
    @PostMapping("/state")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateState() {
        String state = authService.generateState();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("state", state)));
    }

    /**
     * 登录。
     * @param code  GitHub 登录返回的 code
     * @param state GitHub 登录返回的 state
     * @return  登录结果
     */
    @PostMapping("/login/github")
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithGithub(
            @RequestParam String code,
            @RequestParam String state) {
        // 输入校验
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code 不能为空");
        }
        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("state 不能为空");
        }

        LoginResponse response = authService.loginWithGithubAndBuildResponse(code, state);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 获取当前用户信息。
     * @param auth  Authorization header
     * @return  当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse>> me(@RequestHeader("Authorization") String auth) {
        String token = extractToken(auth);
        if (token.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "缺少 Authorization header"));
        }
        LoginResponse response = authService.getCurrentUserByToken(token);
        if (response == null) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "未登录或 token 已过期"));
        }
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 登出。
     * @param auth  Authorization header
     * @return  登出结果
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String auth) {
        String token = extractToken(auth);
        if (!token.isEmpty()) {
            String userId = authService.validateToken(token);
            if (userId != null) {
                authService.logout(userId);
            }
        }
        return ResponseEntity.ok(ApiResponse.ok());
    }

    /**
     * 验证 token。
     * @param auth  Authorization header
     * @return  用户 ID
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestHeader("Authorization") String auth) {
        String token = extractToken(auth);
        if (token.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "缺少 Authorization header"));
        }
        String userId = authService.validateToken(token);
        if (userId != null) {
            return ResponseEntity.ok(ApiResponse.ok(userId));
        }
        return ResponseEntity.status(401).body(ApiResponse.error(401, "无效的 token"));
    }

    /**
     * 从 Authorization header 中提取 token。
     * @param authorizationHeader  Authorization header
     * @return  token
     */
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return "";
        }
        String trimmed = authorizationHeader.trim();
        if (trimmed.toLowerCase().startsWith("bearer ")) {
            return trimmed.substring(7).trim();
        }
        return trimmed;
    }
}
