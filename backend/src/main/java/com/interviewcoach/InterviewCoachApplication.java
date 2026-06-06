package com.interviewcoach;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Interview Coach AI 启动类。
 */
@SpringBootApplication(exclude = {
        org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class
})
@ConfigurationPropertiesScan("com.interviewcoach.config")
@MapperScan("com.interviewcoach.mapper")
public class InterviewCoachApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewCoachApplication.class, args);
    }
}
