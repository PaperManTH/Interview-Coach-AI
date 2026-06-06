package com.interviewcoach.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewcoach.entity.ResumeProfile;
import com.interviewcoach.entity.dto.resume.ResumeParsedData;
import com.interviewcoach.entity.dto.resume.ResumeResponse;
import com.interviewcoach.exception.FileUploadException;
import com.interviewcoach.mapper.ResumeProfileMapper;
import com.interviewcoach.service.ResumeService;
import com.interviewcoach.service.SpringAiService;
import com.interviewcoach.util.ResumeParserUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 简历解析服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeProfileMapper mapper;

    private final SpringAiService aiService;

    private final ObjectMapper objectMapper;

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10 MB
    
    private static final String ALLOWED_EXT = ".pdf,.docx,.txt,.md,.rtf";

    private static final String RESUME_ANALYZER_PROMPT =
            """
                    You are a resume analyzer. Extract interview-related information from the resume below.
                    Return JSON only, no explanation.

                    {
                      "candidateSummary": "一句话概括候选人",
                      "skills": ["技能1", "技能2"],
                      "projects": ["项目经历1", "项目经历2"],
                      "workExperience": ["工作经历1"],
                      "strengths": ["优势1", "优势2"],
                      "possibleQuestions": ["面试官可能问的问题1", "面试官可能问的问题2"]
                    }
                    """;

    @Override
    public ResumeResponse uploadAndParse(MultipartFile file, String userId) {
        // 1. 校验
        validate(file);

        // 2. 解析文件 → 纯文本
        String resumeText;
        try {
            resumeText = ResumeParserUtils.parse(file);
        } catch (IOException e) {
            log.error("[Resume] 文件解析失败: {}", e.getMessage());
            throw new FileUploadException(400, "PARSE_FAILED", "文件解析失败: " + e.getMessage(), e);
        }

        if (resumeText == null || resumeText.isBlank()) {
            throw new FileUploadException(400, "EMPTY_CONTENT", "简历内容为空，无法解析");
        }

        log.info("[Resume] 文本提取完成 userId={}, 字符数={}", userId, resumeText.length());

        // 3. LLM 结构化提取
        String parsedJson;
        try {
            parsedJson = analyzeWithLLM(resumeText);
        } catch (Exception e) {
            log.error("[Resume] LLM 结构化提取失败: {}", e.getMessage());
            parsedJson = "{}";
        }

        // 4. 入库（同一用户覆盖旧记录）
        ResumeProfile profile = new ResumeProfile();
        profile.setUserId(userId);
        profile.setResumeText(resumeText);
        profile.setParsedJson(parsedJson);
        profile.setFileName(file.getOriginalFilename());

        // 查旧记录
        ResumeProfile existing = mapper.selectOne(
                new LambdaQueryWrapper<ResumeProfile>()
                        .eq(ResumeProfile::getUserId, userId));
        if (existing != null) {
            profile.setId(existing.getId());
            mapper.updateById(profile);
            log.info("[Resume] 覆盖更新 id={}, userId={}", existing.getId(), userId);
        } else {
            mapper.insert(profile);
            log.info("[Resume] 新增入库 id={}, userId={}", profile.getId(), userId);
        }

        // 5. 构建响应
        return buildResponse(profile);
    }

    // ---- 校验 ----

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException(400, "EMPTY_FILE", "上传文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new FileUploadException(413, "FILE_TOO_LARGE",
                    String.format("文件过大（%.1f MB），最大允许 10 MB", file.getSize() / 1024.0 / 1024.0));
        }
        String name = file.getOriginalFilename();
        if (name == null) {
            throw new FileUploadException(400, "NO_FILENAME", "文件名为空");
        }
        String lower = name.toLowerCase();
        boolean allowed = false;
        for (String ext : ALLOWED_EXT.split(",")) {
            if (lower.endsWith(ext.trim())) { allowed = true; break; }
        }
        if (!allowed) {
            throw new FileUploadException(400, "UNSUPPORTED_FORMAT",
                    "不支持的文件格式，仅支持 PDF / DOCX / TXT");
        }
    }

    // ---- LLM ----

    private String analyzeWithLLM(String resumeText) {
        String truncated = resumeText.length() > 6000
                ? resumeText.substring(0, 6000) + "\n...(truncated)"
                : resumeText;

        String raw = aiService.chatWithSystem(RESUME_ANALYZER_PROMPT, truncated);
        log.info("[Resume] LLM 原始返回长度: {}", raw != null ? raw.length() : 0);

        String json = cleanJson(raw);
        try {
            objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.info("[Resume] Mock/非 JSON 响应，使用关键词兜底提取");
            json = fallbackExtract(resumeText);
        }
        return json;
    }

    /**
     * LLM 不可用（Mock 模式）时的关键词兜底提取。
     * 从简历文本中提取基础结构，确保用户看到有意义的结果。
     */
    private String fallbackExtract(String text) {
        List<String> skills = new ArrayList<>();
        List<String> questions = new ArrayList<>();

        // 常见技术关键词匹配
        String[][] skillPatterns = {
                {"Java"}, {"Spring"}, {"Spring Boot"}, {"Spring Cloud"}, {"MyBatis"}, {"MyBatis-Plus"},
                {"MySQL"}, {"Redis"}, {"MongoDB"}, {"PostgreSQL"}, {"Elasticsearch"},
                {"Docker"}, {"Kubernetes"}, {"K8s"}, {"Linux"}, {"Git"},
                {"RabbitMQ"}, {"Kafka"}, {"RocketMQ"}, {"Nginx"},
                {"微服务"}, {"分布式"}, {"JVM"}, {"多线程"}, {"并发"},
                {"HTML"}, {"CSS"}, {"JavaScript"}, {"TypeScript"}, {"Vue"}, {"React"},
                {"Python"}, {"Go"}, {"C++"}, {"Node.js"},
                {"RESTful"}, {"WebSocket"}, {"RPC"}, {"gRPC"},
                {"AI"}, {"大模型"}, {"LLM"}, {"RAG"}, {"Function Call"},
                {"设计模式"}, {"数据结构"}, {"算法"},
                {"IoT"}, {"物联网"}, {"嵌入式"},
        };
        for (String[] p : skillPatterns) {
            String kw = p[0];
            // 短关键词(<=3字符)用词边界匹配，避免 "Go" 误匹配 "Google"
            boolean found;
            if (kw.length() <= 3) {
                found = Pattern.compile("\\b" + Pattern.quote(kw) + "\\b",
                        Pattern.CASE_INSENSITIVE).matcher(text).find();
            } else {
                found = text.toLowerCase().contains(kw.toLowerCase());
            }
            if (found) {
                if (!skills.contains(kw)) skills.add(kw);
            }
        }
        if (skills.isEmpty()) {
            skills.add("简历已上传，请配置 LLM API Key 以获得完整解析");
        }

        // 生成基础问题
        if (!skills.isEmpty()) {
            questions.add("请简要介绍一下你自己");
            if (skills.contains("Java")) questions.add("请谈谈你对 Java 多线程的理解");
            if (skills.contains("Spring Boot")) questions.add("Spring Boot 自动配置的原理是什么？");
            if (skills.contains("MySQL") || skills.contains("Redis")) {
                questions.add("你在项目中是如何使用数据库和缓存的？");
            }
            questions.add("描述一个你遇到的最有挑战性的技术问题，以及你是如何解决的");
        }

        String candidateSummary = "简历已上传（Mock 模式）";
        if (!skills.isEmpty()) {
            candidateSummary = "候选人具有 " + String.join("、", skills.subList(0, Math.min(5, skills.size()))) + " 等相关技能";
        }

        try {
            return objectMapper.writeValueAsString(Map.of(
                    "candidateSummary", candidateSummary,
                    "skills", skills,
                    "projects", List.of(),
                    "workExperience", List.of(),
                    "strengths", List.of(),
                    "possibleQuestions", questions
            ));
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private String cleanJson(String raw) {
        if (raw == null) return "{}";
        String s = raw.trim();
        if (s.startsWith("```")) {
            int start = s.indexOf('\n');
            int end = s.lastIndexOf("```");
            if (start >= 0 && end > start) {
                s = s.substring(start + 1, end).trim();
            }
        }
        return s;
    }

    // ---- 响应构建 ----

    private ResumeResponse buildResponse(ResumeProfile profile) {
        ResumeResponse resp = new ResumeResponse();
        resp.setId(profile.getId());
        resp.setFileName(profile.getFileName());
        resp.setResumeTextPreview(profile.getResumeText() != null
                ? profile.getResumeText().substring(0, Math.min(profile.getResumeText().length(), 500))
                : "");

        if (profile.getParsedJson() != null && !profile.getParsedJson().isBlank()
                && !"{}".equals(profile.getParsedJson())) {
            try {
                ResumeParsedData data = objectMapper.readValue(profile.getParsedJson(), ResumeParsedData.class);
                resp.setParsedData(data);
            } catch (JsonProcessingException e) {
                log.warn("[Resume] parsed_json 反序列化失败");
            }
        }
        return resp;
    }
}
