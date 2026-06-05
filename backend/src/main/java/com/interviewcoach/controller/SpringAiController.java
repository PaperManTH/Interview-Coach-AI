package com.interviewcoach.controller;

import com.interviewcoach.entity.dto.common.ApiResponse;
import com.interviewcoach.service.SpringAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Spring AI 快速测试控制器
 * 提供简单的 LLM 调用接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class SpringAiController {

    private final SpringAiService springAiService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<String>> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        log.info("[AI] 收到消息: {}", message);
        
        String response = springAiService.chat(message);
        
        log.info("[AI] 返回响应: {}", response);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/chat/template")
    public ResponseEntity<ApiResponse<String>> chatWithTemplate(@RequestBody Map<String, Object> request) {
        String template = (String) request.get("template");
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) request.get("params");
        
        log.info("[AI] 使用模板: {}", template.substring(0, Math.min(template.length(), 50)));
        
        String response = springAiService.chatWithTemplate(template, params);
        
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/chat/system")
    public ResponseEntity<ApiResponse<String>> chatWithSystem(@RequestBody Map<String, String> request) {
        String system = request.get("system");
        String message = request.get("message");
        
        String response = springAiService.chatWithSystem(system, message);
        
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
