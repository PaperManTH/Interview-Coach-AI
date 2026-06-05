package com.interviewcoach.controller;

import com.interviewcoach.dto.common.ApiResponse;
import com.interviewcoach.dto.userconfig.UserConfigResponse;
import com.interviewcoach.service.AuthService;
import com.interviewcoach.userconfig.UserProviderConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 仅支持 GitHub OAuth2 登录
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/github")
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithGithub(@RequestParam String code) {
        UserProviderConfig config = authService.loginWithGithub(code);
        String token = authService.generateToken(config.getUserId());
        
        LoginResponse response = new LoginResponse();
        response.setUserId(config.getUserId());
        response.setToken(token);
        response.setConfig(UserConfigResponse.fromEntity(config));
        
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        String userId = authService.validateToken(token.replace("Bearer ", ""));
        if (userId != null) {
            authService.logout(userId);
        }
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestHeader("Authorization") String token) {
        String userId = authService.validateToken(token.replace("Bearer ", ""));
        if (userId != null) {
            return ResponseEntity.ok(ApiResponse.ok(userId));
        }
        return ResponseEntity.status(401).body(ApiResponse.error(401, "无效的 token"));
    }

    public static class LoginResponse {
        private String userId;
        private String token;
        private UserConfigResponse config;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public UserConfigResponse getConfig() { return config; }
        public void setConfig(UserConfigResponse config) { this.config = config; }
    }
}
