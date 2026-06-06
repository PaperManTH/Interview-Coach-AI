package com.interviewcoach.entity.dto.resume;

import lombok.Data;

/**
 * 简历上传 / 解析响应 DTO。
 */
@Data
public class ResumeResponse {

    /** 持久化后的 ID */
    private Long id;

    /** 原始文件名 */
    private String fileName;

    /** 提取的纯文本（截断前 500 字符） */
    private String resumeTextPreview;

    /** LLM 结构化解析结果 */
    private ResumeParsedData parsedData;
}
