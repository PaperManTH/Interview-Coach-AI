package com.interviewcoach.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewcoach.entity.dto.ai.BilingualResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 双语响应解析器
 * 负责解析、验证和处理 AI 返回的双语 JSON 格式
 */
@Slf4j
public class BilingualResponseParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * JSON 格式正则：匹配 { "chinese": "...", "english": "..." }
     */
    private static final Pattern JSON_PATTERN = Pattern.compile(
            "\\{\\s*\"chinese\"\\s*:\\s*\"[\\s\\S]*?\"\\s*,\\s*\"english\"\\s*:\\s*\"[\\s\\S]*?\"\\s*\\}",
            Pattern.MULTILINE
    );

    /**
     * 从 AI 响应中提取并解析双语内容
     *
     * @param aiResponse AI 原始响应
     * @return BilingualResponse 对象，解析失败返回 null
     */
    public static BilingualResponse parse(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            log.warn("[Parser] AI 响应为空");
            return null;
        }

        try {
            // 尝试直接解析
            BilingualResponse response = tryDirectParse(aiResponse);
            if (response != null && response.isValid()) {
                return response;
            }

            // 尝试从文本中提取 JSON
            response = tryExtractJson(aiResponse);
            if (response != null && response.isValid()) {
                return response;
            }

            log.warn("[Parser] 无法解析双语 JSON，使用降级处理");
            return fallbackParse(aiResponse);

        } catch (Exception e) {
            log.error("[Parser] 解析失败: {}", e.getMessage());
            return fallbackParse(aiResponse);
        }
    }

    /**
     * 直接解析 JSON
     */
    private static BilingualResponse tryDirectParse(String text) {
        try {
            String trimmed = text.trim();
            if (trimmed.startsWith("{")) {
                return objectMapper.readValue(trimmed, BilingualResponse.class);
            }
        } catch (Exception e) {
            log.debug("[Parser] 直接解析失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从文本中提取 JSON 块
     */
    private static BilingualResponse tryExtractJson(String text) {
        Matcher matcher = JSON_PATTERN.matcher(text);
        if (matcher.find()) {
            String jsonStr = matcher.group();
            try {
                return objectMapper.readValue(jsonStr, BilingualResponse.class);
            } catch (Exception e) {
                log.debug("[Parser] JSON 提取解析失败: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 降级解析：将非标准格式转换为双语响应
     * 尝试智能分割中英文内容
     */
    private static BilingualResponse fallbackParse(String text) {
        String chinese = extractChinese(text);
        String english = extractEnglish(text);

        if (chinese.isEmpty() && english.isEmpty()) {
            // 完全无法解析，将原文放入中文部分
            return new BilingualResponse(text, "");
        }

        return new BilingualResponse(chinese, english);
    }

    /**
     * 提取中文内容
     */
    private static String extractChinese(String text) {
        // 匹配中文字符、标点和常见符号（使用Unicode范围）
        Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5\\u3000-\\u303f\\uff00-\\uffef\\s]+");
        Matcher matcher = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            sb.append(matcher.group());
        }
        return sb.toString().trim();
    }

    /**
     * 提取英文内容
     */
    private static String extractEnglish(String text) {
        // 匹配英文字符、标点和常见符号
        Pattern pattern = Pattern.compile("[a-zA-Z0-9\\s.,!?;:'\"()\\[\\]{}@#$%^&*\\-+=<>?/\\\\|~`]+");
        Matcher matcher = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String group = matcher.group().trim();
            if (!group.isEmpty()) {
                sb.append(group).append(" ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * 验证 JSON 格式是否符合规范
     */
    public static boolean validateFormat(String text) {
        BilingualResponse response = parse(text);
        if (response == null || !response.isValid()) {
            return false;
        }

        // 验证中文部分是否纯净
        if (!isPureChinese(response.getChinese())) {
            log.warn("[Validator] 中文部分包含非中文字符");
            return false;
        }

        // 验证英文部分是否纯净
        if (!isPureEnglish(response.getEnglish())) {
            log.warn("[Validator] 英文部分包含非英文字符");
            return false;
        }

        return true;
    }

    /**
     * 检查是否为纯中文
     */
    private static boolean isPureChinese(String text) {
        if (text == null || text.isEmpty()) return true;
        // 允许中文标点和空白字符（使用Unicode范围）
        return text.matches("^[\\u4e00-\\u9fa5\\u3000-\\u303f\\uff00-\\uffef\\s]+$");
    }

    /**
     * 检查是否为纯英文
     */
    private static boolean isPureEnglish(String text) {
        if (text == null || text.isEmpty()) return true;
        // 允许英文标点和空白字符
        return text.matches("^[a-zA-Z0-9\\s.,!?;:'\"()\\[\\]{}@#$%^&*\\-+=<>?/\\\\|~`]+$");
    }

    /**
     * 构建 JSON 格式的提示词指令
     */
    public static String buildFormatInstruction() {
        return """
                【重要输出格式要求】
                你必须严格按照以下 JSON 格式输出，不要添加任何其他内容：
                {
                    "chinese": "纯中文内容，仅包含中文字符和中文标点",
                    "english": "纯英文内容，仅包含英文字符和英文标点"
                }
                
                格式规范：
                1. 必须是合法的 JSON 格式
                2. chinese 字段只能包含中文字符和中文标点符号
                3. english 字段只能包含英文字符和英文标点符号
                4. 两部分内容必须语义对应，信息完整
                5. 不要在 JSON 外添加任何说明文字或代码块标记
                """;
    }
}