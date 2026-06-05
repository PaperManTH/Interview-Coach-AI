package com.interviewcoach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Interview Coach AI 启动类。
 * 通过 @ConfigurationPropertiesScan 统一扫描 @ConfigurationProperties 类，
 */
@SpringBootApplication
@ConfigurationPropertiesScan("com.interviewcoach.config")
public class InterviewCoachApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewCoachApplication.class, args);
    }
}
