package com.interviewcoach.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历解析结果实体。
 * 对应表 resume_profile。
 */
@Data
@TableName("resume_profile")
public class ResumeProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private String userId;

    /** 原始简历文本（PDF/DOCX/TXT 提取后的纯文本） */
    @TableField("resume_text")
    private String resumeText;

    /** LLM 结构化解析 JSON */
    @TableField("parsed_json")
    private String parsedJson;

    /** 原始文件名 */
    @TableField("file_name")
    private String fileName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
