package com.interviewcoach.controller;

import com.interviewcoach.entity.dto.common.ApiResponse;
import com.interviewcoach.entity.dto.resume.ResumeResponse;
import com.interviewcoach.service.ResumeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * POST /api/resume/upload
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
}
