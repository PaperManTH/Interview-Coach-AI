package com.interviewcoach.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Spring AI 服务
 * 提供快速的 LLM 调用功能
 * 当前使用 Mock 模式，可随时切换为真实 LLM
 */
@Slf4j
@Service
public class SpringAiService {

    public String chat(String message) {
        log.info("[SpringAI] 调用 LLM: {}", message.substring(0, Math.min(message.length(), 50)));
        
        // Mock 回复 - 实际项目中替换为真实 LLM 调用
        String response = generateMockResponse(message);
        
        log.info("[SpringAI] 响应: {}", response.substring(0, Math.min(response.length(), 50)));
        return response;
    }

    public String chatWithTemplate(String template, Map<String, Object> params) {
        String message = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue().toString());
        }
        return chat(message);
    }

    public String chatWithSystem(String systemMessage, String userMessage) {
        return chat(userMessage);
    }

    private String generateMockResponse(String message) {
        if (message.contains("hello") || message.contains("你好")) {
            return "你好！我是面试教练 AI，很高兴为你服务！请问你想练习哪方面的面试问题呢？";
        }
        if (message.contains("问题") || message.contains("面试")) {
            return "好的！以下是一个常见的面试问题：\n\n" +
                   "Q: 请介绍一下你自己？\n\n" +
                   "这个问题是面试官了解你的第一步。回答时可以包括：\n" +
                   "- 你的专业背景\n" +
                   "- 相关工作经验\n" +
                   "- 为什么对这个职位感兴趣\n" +
                   "- 你的核心优势";
        }
        if (message.contains("技术") || message.contains("technical")) {
            return "技术面试通常包括：\n\n" +
                   "1. 数据结构与算法\n" +
                   "2. 系统设计\n" +
                   "3. 编程语言基础知识\n" +
                   "4. 数据库知识\n\n" +
                   "需要我为你模拟一个技术问题吗？";
        }
        if (message.contains("HR") || message.contains("hr") || message.contains("人事")) {
            return "HR 面试问题通常关注：\n\n" +
                   "- 你的职业规划\n" +
                   "- 为什么选择我们公司\n" +
                   "- 你的优缺点\n" +
                   "- 薪资期望\n\n" +
                   "需要我为你提供示例回答吗？";
        }
        return "这是一个很好的问题！让我为你提供一些建议...\n\n" +
               "（实际项目中这里会调用真实的 LLM 服务）\n\n" +
               "提示：你可以问我关于面试技巧、常见问题、技术问题等方面的内容！";
    }
}
