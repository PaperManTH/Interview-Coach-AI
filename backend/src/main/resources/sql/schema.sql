-- ============================================================
-- Interview Coach AI 建表脚本
-- 数据库: interview_coach (utf8mb4)
-- ============================================================

CREATE DATABASE IF NOT EXISTS interview_coach
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE interview_coach;

-- ============================================================
-- 1. 用户表（GitHub OAuth2 登录）
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id     VARCHAR(64)  NOT NULL COMMENT '用户ID(github_xxx)',
    github_id   VARCHAR(32)  NOT NULL COMMENT 'GitHub用户ID',
    username    VARCHAR(128) NOT NULL COMMENT 'GitHub用户名',
    avatar_url  VARCHAR(512) DEFAULT NULL  COMMENT '头像URL',
    email       VARCHAR(256) DEFAULT NULL  COMMENT '邮箱',
    token       VARCHAR(512) DEFAULT NULL  COMMENT '当前Session Token',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_github_id (github_id),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表(GitHub OAuth2)';

-- ============================================================
-- 2. 用户 Provider 配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS user_provider_config (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id     VARCHAR(64)  NOT NULL COMMENT '用户ID',
    asr_type    VARCHAR(32)  DEFAULT 'mock' COMMENT 'ASR提供商',
    asr_api_key VARCHAR(256) DEFAULT NULL  COMMENT 'ASR API Key',
    asr_base_url VARCHAR(512) DEFAULT NULL COMMENT 'ASR 自定义地址',
    tts_type    VARCHAR(32)  DEFAULT 'mock' COMMENT 'TTS提供商',
    tts_api_key VARCHAR(256) DEFAULT NULL  COMMENT 'TTS API Key',
    tts_base_url VARCHAR(512) DEFAULT NULL COMMENT 'TTS 自定义地址',
    tts_voice   VARCHAR(64)  DEFAULT 'alloy' COMMENT 'TTS 语音',
    llm_type    VARCHAR(32)  DEFAULT 'mock' COMMENT 'LLM提供商',
    llm_api_key VARCHAR(256) DEFAULT NULL  COMMENT 'LLM API Key',
    llm_base_url VARCHAR(512) DEFAULT NULL COMMENT 'LLM 自定义地址',
    llm_model   VARCHAR(128) DEFAULT 'deepseek-chat' COMMENT 'LLM 模型',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户Provider配置';

-- ============================================================
-- 3. 面试会话表
-- ============================================================
CREATE TABLE IF NOT EXISTS interview_session (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    session_id      VARCHAR(64)  NOT NULL COMMENT '会话UUID',
    user_id         VARCHAR(64)  NOT NULL COMMENT '用户ID',
    status          VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/PAUSED/ENDED',
    interview_type  VARCHAR(32)  DEFAULT 'general' COMMENT '面试类型: technical/hr/english/general',
    started_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    ended_at        DATETIME     DEFAULT NULL COMMENT '结束时间',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试会话';

-- ============================================================
-- 4. 会话消息表（聊天记录）
-- ============================================================
CREATE TABLE IF NOT EXISTS interview_message (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    session_id  VARCHAR(64)  NOT NULL COMMENT '会话ID',
    message_id  VARCHAR(64)  NOT NULL COMMENT '消息UUID',
    sender      VARCHAR(32)  NOT NULL COMMENT '发送方: user/assistant/system',
    type        VARCHAR(16)  NOT NULL DEFAULT 'TEXT' COMMENT '消息类型: TEXT/AUDIO/HEARTBEAT/ACK',
    content     TEXT         DEFAULT NULL COMMENT '消息内容',
    timestamp_ms BIGINT      NOT NULL COMMENT '消息时间戳(ms)',
    metadata    TEXT         DEFAULT NULL COMMENT '扩展元数据(JSON)',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_message_id (message_id),
    INDEX idx_session_id (session_id),
    INDEX idx_sender (sender),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话消息';
