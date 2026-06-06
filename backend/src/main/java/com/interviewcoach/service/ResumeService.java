package com.interviewcoach.service;

import com.interviewcoach.entity.dto.resume.ResumeParsedData;
import com.interviewcoach.entity.dto.resume.ResumeResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 简历解析服务接口。
 */
public interface ResumeService {

    /**
     * 上传并解析简历文件。
     */
    ResumeResponse uploadAndParse(MultipartFile file, String userId);

    /**
     * 手动录入简历信息（前端直接传结构化数据，后端不调 LLM）。
     */
    ResumeResponse saveManual(ResumeParsedData data, String userId);

    /**
     * 获取用户已保存的手动简历（resumeText 为 null 的记录）。
     */
    ResumeParsedData getManual(String userId);
}
