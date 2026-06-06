package com.interviewcoach.service;

import com.interviewcoach.entity.dto.resume.ResumeResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 简历解析服务接口。
 */
public interface ResumeService {

    /**
     * 上传并解析简历文件。
     *
     * @param file   MultipartFile（PDF/DOCX/TXT）
     * @param userId 当前用户 ID
     * @return 解析响应
     */
    ResumeResponse uploadAndParse(MultipartFile file, String userId);
}
