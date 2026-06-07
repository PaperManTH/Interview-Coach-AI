package com.interviewcoach.entity.dto.websocket;

/**
 * WebSocket 消息类型枚举。
 * TEXT/CHAT 用于普通聊天；HEARTBEAT 用于客户端心跳；ACK/ERROR 用于控制面；
 * VOICE_CHUNK/VOICE_START/VOICE_END 预留给后续语音分片传输。
 */
public enum MessageType {

    /** 客户端/服务端通用文本消息 */
    TEXT,

    /** 聊天业务消息（等价 TEXT，保留便于后续业务分流） */
    CHAT,

    /** 客户端心跳 PING，服务端应返回 PONG */
    HEARTBEAT,

    /** 服务端心跳响应 */
    PONG,

    /** 服务端/客户端确认回执 */
    ACK,

    /** 错误 / 系统通知 */
    ERROR,

    /** 语音分片开始（预留） */
    VOICE_START,

    /** 语音分片数据（预留） */
    VOICE_CHUNK,

    /** 语音分片结束（预留） */
    VOICE_END,

    /** TTS 合成的音频（Base64编码） */
    AUDIO,

    /** 语音识别后生成的文字（用户语音的文本结果） */
    VOICE_TEXT,

    /** 阶段切换通知（动态面试模式） */
    STAGE_TRANSITION
}
