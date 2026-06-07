package com.interviewcoach.controller;

import com.interviewcoach.entity.UserProviderConfig;
import com.interviewcoach.entity.dto.common.ApiResponse;
import com.interviewcoach.entity.dto.userconfig.UserConfigRequest;
import com.interviewcoach.entity.dto.userconfig.UserConfigResponse;
import com.interviewcoach.service.UserConfigService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户配置控制器。
 * 提供用户级 Provider 配置的 REST API。
 */
@Slf4j
@RestController
@RequestMapping("/api/user/config")
@RequiredArgsConstructor
public class UserConfigController {

    private final UserConfigService userConfigService;

    /**
     * 获取用户配置。
     * @param userId  用户 ID
     * @return  用户配置信息
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserConfigResponse>> getConfig(@RequestParam String userId) {
        UserProviderConfig config = userConfigService.getConfig(userId);
        return ResponseEntity.ok(ApiResponse.ok(UserConfigResponse.fromEntity(config)));
    }

    /**
     * 保存用户配置。
     * @param userId   用户 ID
     * @param request  配置请求体
     * @return  保存后的配置信息
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserConfigResponse>> saveConfig(
            @RequestParam String userId,
            @RequestBody UserConfigRequest request) {
        
        UserProviderConfig config = new UserProviderConfig();
        config.setAsrType(request.getAsrType());
        config.setAsrApiKey(request.getAsrApiKey());
        config.setAsrBaseUrl(request.getAsrBaseUrl());
        config.setAsrRegion(request.getAsrRegion());
        config.setTtsType(request.getTtsType());
        config.setTtsApiKey(request.getTtsApiKey());
        config.setTtsBaseUrl(request.getTtsBaseUrl());
        config.setTtsVoice(request.getTtsVoice());
        config.setTtsRegion(request.getTtsRegion());
        config.setLlmType(request.getLlmType());
        config.setLlmApiKey(request.getLlmApiKey());
        config.setLlmBaseUrl(request.getLlmBaseUrl());
        config.setLlmModel(request.getLlmModel());
        config.setLlmRegion(request.getLlmRegion());

        UserProviderConfig saved = userConfigService.saveConfig(userId, config);
        return ResponseEntity.ok(ApiResponse.ok(UserConfigResponse.fromEntity(saved)));
    }

    /**
     * 删除用户配置。
     * @param userId  用户 ID
     * @return  删除结果
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@RequestParam String userId) {
        userConfigService.deleteConfig(userId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    /**
     * 检查用户是否有配置。
     * @param userId  用户 ID
     * @return  是否存在配置
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Boolean>> hasConfig(@RequestParam String userId) {
        return ResponseEntity.ok(ApiResponse.ok(userConfigService.hasConfig(userId)));
    }
}
