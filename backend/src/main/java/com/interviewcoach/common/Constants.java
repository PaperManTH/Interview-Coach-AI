package com.interviewcoach.common;

/**
 * 通用常量定义
 */
public class Constants {

    private Constants() {}

    // ========== 通用状态码 ==========
    public static final String SUCCESS = "SUCCESS";
    public static final String FAIL = "FAIL";
    
    // ========== 业务状态码 ==========
    public static final String CODE_INVALID_PARAM = "INVALID_PARAM";
    public static final String CODE_NOT_FOUND = "NOT_FOUND";
    public static final String CODE_UNAUTHORIZED = "UNAUTHORIZED";
    public static final String CODE_FORBIDDEN = "FORBIDDEN";
    public static final String CODE_SYSTEM_ERROR = "SYSTEM_ERROR";
    
    // ========== Provider 类型 ==========
    public static final String PROVIDER_MOCK = "MOCK";
    public static final String PROVIDER_IFLYTEK = "IFLYTEK";
    public static final String PROVIDER_OPENAI = "OPENAI";
    public static final String PROVIDER_AZURE = "AZURE";
    public static final String PROVIDER_QIANWEN = "QIANWEN";
    public static final String PROVIDER_DOUBAO = "DOUBAO";
    
    // ========== WebSocket 消息类型 ==========
    public static final String MSG_TYPE_TEXT = "text";
    public static final String MSG_TYPE_AUDIO = "audio";
    public static final String MSG_TYPE_STATUS = "status";
    public static final String MSG_TYPE_ERROR = "error";
    
    // ========== 面试状态 ==========
    public static final String INTERVIEW_STATUS_IDLE = "idle";
    public static final String INTERVIEW_STATUS_LISTENING = "listening";
    public static final String INTERVIEW_STATUS_THINKING = "thinking";
    public static final String INTERVIEW_STATUS_SPEAKING = "speaking";
    
    // ========== 时间常量 ==========
    public static final long HEARTBEAT_INTERVAL_MS = 30_000L;
    public static final long SESSION_TIMEOUT_MS = 90_000L;
    public static final int DEFAULT_TIMEOUT_SECONDS = 30;
}
