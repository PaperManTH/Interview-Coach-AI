package com.interviewcoach.controller;

import com.interviewcoach.entity.dto.common.ApiResponse;
import com.interviewcoach.entity.dto.resume.ResumeParsedData;
import com.interviewcoach.entity.dto.resume.ResumeResponse;
import com.interviewcoach.service.ResumeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 简历上传 / 解析控制器。
 */
@Slf4j
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * 上传简历文件并解析。
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResumeResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            userId = "anonymous";
        }

        log.info("[ResumeController] 收到简历上传 userId={}, name={}, size={}",
                userId, file.getOriginalFilename(), file.getSize());

        ResumeResponse resp = resumeService.uploadAndParse(file, userId);
        return ResponseEntity.ok(ApiResponse.ok(resp));
    }

    /**
     * 手动录入简历信息（前端拼接文本 → 后端 LLM 解析）。
     */
    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<ResumeResponse>> saveManual(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            userId = "anonymous";
        }
        String resumeText = body.get("resumeText");
        log.info("[ResumeController] 手动录入简历 userId={}, textLen={}", userId,
                resumeText != null ? resumeText.length() : 0);
        ResumeResponse resp = resumeService.saveManual(resumeText, userId);
        return ResponseEntity.ok(ApiResponse.ok(resp));
    }

    /**
     * 获取已保存的手动简历。
     */
    @GetMapping("/manual")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getManual(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            userId = "anonymous";
        }
        log.info("[ResumeController] 获取手动简历 userId={}", userId);
        ResumeParsedData data = resumeService.getManual(userId);
        if (data == null) {
            return ResponseEntity.ok(ApiResponse.ok(Map.of("exists", false)));
        }
        return ResponseEntity.ok(ApiResponse.ok(Map.of("exists", true, "data", data)));
    }
}
