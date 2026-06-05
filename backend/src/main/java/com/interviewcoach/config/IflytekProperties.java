package com.interviewcoach.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 讯飞星火 WebAPI 配置。
 * 使用 HTTP POST 方式调用，MD5 签名鉴权，无需 msc.cfg。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.iflytek")
public class IflytekProperties {

    /** 讯飞应用 APPID */
    private String appId = "6497f9b2";

    /** 讯飞 API Key（控制台获取） */
    private String apiKey = "";

    /** TTS 发音人 */
    private String ttsVoice = "xiaoyan";
}
