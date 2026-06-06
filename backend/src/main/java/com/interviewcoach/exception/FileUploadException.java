package com.interviewcoach.exception;

/**
 * 文件上传异常 — 文件格式/大小/解析失败。
 */
public class FileUploadException extends RuntimeException {

    private final String code;
    private final int statusCode;

    public FileUploadException(int statusCode, String code, String message) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
    }

    public FileUploadException(int statusCode, String code, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.code = code;
    }

    public String getCode() { return code; }
    public int getStatusCode() { return statusCode; }
}
