package com.interviewcoach.exception;

/**
 * 业务异常 — 可携带 HTTP 状态码和错误码。
 */
public class BusinessException extends RuntimeException {

    private final String code;
    private final int statusCode;

    public BusinessException(String code, String message) {
        this(400, code, message);
    }

    public BusinessException(int statusCode, String code, String message) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
    }

    public String getCode() { return code; }
    public int getStatusCode() { return statusCode; }
}
