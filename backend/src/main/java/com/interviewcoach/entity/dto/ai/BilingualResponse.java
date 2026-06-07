package com.interviewcoach.entity.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 双语响应实体类
 * 用于接收和处理标准化的双语 JSON 格式输出
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BilingualResponse {

    /**
     * 中文内容
     * 仅包含纯中文字符，不混杂任何非中文字符
     */
    @JsonProperty("chinese")
    private String chinese;

    /**
     * 英文内容
     * 仅包含纯英文字符，不混杂任何非英文字符
     */
    @JsonProperty("english")
    private String english;

    /**
     * 验证响应是否有效
     */
    public boolean isValid() {
        return chinese != null && !chinese.trim().isEmpty()
                && english != null && !english.trim().isEmpty();
    }

    /**
     * LLM 调用失败时的降级回复
     */
    public static BilingualResponse fallback() {
        return new BilingualResponse(
            "抱歉，AI 服务暂时不可用，请稍后重试。",
            "Sorry, the AI service is temporarily unavailable. Please try again later."
        );
    }

    /**
     * 获取格式化的双语文本（用于显示）
     */
    public String getFormattedText() {
        StringBuilder sb = new StringBuilder();
        if (chinese != null && !chinese.trim().isEmpty()) {
            sb.append(chinese);
        }
        if (english != null && !english.trim().isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(english);
        }
        return sb.toString();
    }
}