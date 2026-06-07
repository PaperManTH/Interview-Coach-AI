package com.interviewcoach.common;

/**
 * 通用常量定义
 */
public final class Constants {

    private Constants() {}

    // ========== Provider 类型 ==========
    public static final String PROVIDER_MOCK = "mock";
    public static final String PROVIDER_IFLYTEK = "iflytek";
    public static final String PROVIDER_OPENAI = "openai";
    public static final String PROVIDER_AZURE = "azure";
    public static final String PROVIDER_QIANWEN = "qwen";

    // ========== 会话状态 ==========
    public static final String SESSION_STATUS_ACTIVE = "ACTIVE";
    public static final String SESSION_STATUS_PAUSED = "PAUSED";
    public static final String SESSION_STATUS_ENDED = "ENDED";
    
    /** 用于查询时兼容大小写（活跃和暂停状态） */
    public static final String[] SESSION_STATUS_ACTIVE_OR_PAUSED = {"ACTIVE", "active", "PAUSED", "paused"};

    // ========== 面试类型 ==========
    public static final String INTERVIEW_TYPE_DYNAMIC = "dynamic";
    public static final String INTERVIEW_TYPE_GENERAL = "general";

    // ========== 默认值 ==========
    public static final String DEFAULT_USER_ID = "anonymous";

    // ========== 文件上传 ==========
    public static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L;
    public static final String ALLOWED_FILE_EXTENSIONS = ".pdf,.docx,.txt,.md,.rtf";

}
