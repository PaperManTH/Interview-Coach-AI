package com.interviewcoach.common;

/**
 * 面试相关常量定义
 */
public final class InterviewConstants {

    private InterviewConstants() {}

    // ========== 面试阶段 ==========
    public static final String STAGE_WARMUP = "warmup";
    public static final String STAGE_TECHNICAL = "technical";
    public static final String STAGE_PRESSURE = "pressure";

    // ========== 面试焦点类型 ==========
    public static final String FOCUS_GENERAL = "general";
    public static final String FOCUS_RESUME = "resume";
    public static final String FOCUS_TECHNICAL = "technical";

    // ========== 面试模式 ==========
    public static final String MODE_DYNAMIC = "dynamic";
    public static final String MODE_TRADITIONAL = "traditional";

    // ========== 阶段轮数配置 ==========
    public static final int ROUNDS_PER_STAGE = 3;

    // ========== 场景类型（兼容旧模式） ==========
    public static final String SCENE_HR = "hr";
    public static final String SCENE_TECHNICAL = "technical";
    public static final String SCENE_PRESSURE = "pressure";

    // ========== 消息发送者标识 ==========
    public static final String SENDER_USER = "user";
    public static final String SENDER_ASSISTANT = "assistant";
    public static final String SENDER_SYSTEM = "system";
}