package com.interviewcoach.entity.dto.resume;

import lombok.Data;

import java.util.List;

/**
 * LLM 简历结构化输出 DTO。
 */
@Data
public class ResumeParsedData {

    /** 候选人概览 */
    private String candidateSummary;

    /** 技能列表 */
    private List<String> skills;

    /** 项目经验 */
    private List<String> projects;

    /** 工作经历 */
    private List<String> workExperience;

    /** 候选人优势 */
    private List<String> strengths;

    /** 可能的面试问题 */
    private List<String> possibleQuestions;
}
