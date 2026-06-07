package com.interviewcoach.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewcoach.entity.ResumeProfile;
import com.interviewcoach.mapper.ResumeProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * AI 工具：从对话中提取用户信息并更新到简历记录中。
 * 作为 Spring AI FunctionCallback 注册，LLM 可在对话中自动调用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeTool implements FunctionCallback {

    private final ResumeProfileMapper resumeProfileMapper;

    private final ObjectMapper objectMapper;

    /** ThreadLocal 传递 userId */
    private static final ThreadLocal<String> CURRENT_USER_ID = new ThreadLocal<>();

    @Override
    public String getName() {
        return "updateUserResume";
    }

    @Override
    public String getDescription() {
        return "当用户在对话中透露个人信息、技能、项目经验、工作经历时，调用此工具更新简历数据库。" +
               "参数: candidateSummary(候选人概括), skills(技能列表), projects(项目经验列表), " +
               "workExperience(工作经历列表), strengths(优势列表)";
    }

    @Override
    public String getInputTypeSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "candidateSummary": {"type": "string", "description": "一句话概括候选人"},
                    "skills": {"type": "array", "items": {"type": "string"}, "description": "技能列表"},
                    "projects": {"type": "array", "items": {"type": "string"}, "description": "项目经验"},
                    "workExperience": {"type": "array", "items": {"type": "string"}, "description": "工作经历"},
                    "strengths": {"type": "array", "items": {"type": "string"}, "description": "候选人优势"}
                  }
                }
                """;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String call(String input) {
        log.info("[Tool-Resume] 收到更新请求: {}", input);

        // 通过 ThreadLocal 获取 userId
        String userId = CURRENT_USER_ID.get();
        if (userId == null) {
            log.warn("[Tool-Resume] 无法确定用户身份");
            return "{\"success\": false, \"message\": \"无法确定用户身份\"}";
        }

        try {
            Map<String, Object> params = objectMapper.readValue(input, Map.class);
            ResumeProfile profile = getOrCreateProfile(userId);

            // 合并已有数据和新数据
            String existingJson = profile.getParsedJson();
            Map<String, Object> existing = (existingJson != null && !existingJson.isBlank() && !"{}".equals(existingJson))
                    ? objectMapper.readValue(existingJson, Map.class)
                    : null;

            Map<String, Object> merged = mergeResumeData(existing, params);
            profile.setParsedJson(objectMapper.writeValueAsString(merged));

            if (profile.getId() != null) {
                resumeProfileMapper.updateById(profile);
            } else {
                resumeProfileMapper.insert(profile);
            }

            log.info("[Tool-Resume] 简历已更新 userId={}", userId);
            return "{\"success\": true, \"message\": \"简历信息已更新\"}";
        } catch (Exception e) {
            log.error("[Tool-Resume] 更新失败: {}", e.getMessage(), e);
            return "{\"success\": false, \"message\": \"更新失败: " + e.getMessage() + "\"}";
        }
    }

    /**
     * 合并已有简历数据和新提取的数据，新数据优先。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> mergeResumeData(Map<String, Object> existing, Map<String, Object> incoming) {
        if (existing == null) return incoming;
        for (String key : List.of("skills", "projects", "workExperience", "strengths")) {
            if (incoming.containsKey(key) && incoming.get(key) instanceof List) {
                List<Object> existingList = (List<Object>) existing.getOrDefault(key, List.of());
                List<Object> newList = (List<Object>) incoming.get(key);
                // 去重合并
                for (Object item : newList) {
                    if (!existingList.contains(item)) {
                        existingList.add(item);
                    }
                }
                existing.put(key, existingList);
            }
        }
        if (incoming.containsKey("candidateSummary")) {
            existing.put("candidateSummary", incoming.get("candidateSummary"));
        }
        return existing;
    }

    private ResumeProfile getOrCreateProfile(String userId) {
        List<ResumeProfile> profiles = resumeProfileMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ResumeProfile>()
                        .eq(ResumeProfile::getUserId, userId)
                        .orderByDesc(ResumeProfile::getId)
                        .last("LIMIT 1")
        );
        if (!profiles.isEmpty()) return profiles.get(0);

        ResumeProfile p = new ResumeProfile();
        p.setUserId(userId);
        p.setParsedJson("{}");
        return p;
    }

    /**
     * 设置当前用户 ID（在调用 LLM 前设置）。
     */
    public static void setCurrentUserId(String userId) {
        CURRENT_USER_ID.set(userId);
    }

    /**
     * 清除当前用户 ID（在调用 LLM 后清除）。
     */
    public static void clearCurrentUserId() {
        CURRENT_USER_ID.remove();
    }
}
