package com.interviewcoach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Interview Coach AI 启动类。
 * 排除 OpenAI 自动配置，由 ChatModelConfig 按条件创建 ChatModel
 */
@SpringBootApplication(exclude = {
        org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class
})
@ConfigurationPropertiesScan("com.interviewcoach.config")
public class InterviewCoachApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewCoachApplication.class, args);
    }
}
