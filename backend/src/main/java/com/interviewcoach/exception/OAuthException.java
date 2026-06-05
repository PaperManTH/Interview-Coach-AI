package com.interviewcoach.exception;

/**
 * OAuth 认证异常 — 用于 GitHub OAuth 交换失败等场景。
 */
public class OAuthException extends RuntimeException {

    private final String code;
    private final int statusCode;

    public OAuthException(int statusCode, String code, String message) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
    }

    public OAuthException(int statusCode, String code, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.code = code;
    }

    public String getCode() { return code; }
    public int getStatusCode() { return statusCode; }
}
