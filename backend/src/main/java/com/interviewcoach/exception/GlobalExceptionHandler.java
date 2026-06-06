package com.interviewcoach.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import com.interviewcoach.entity.dto.common.ApiResponse;

/**
 * 全局异常处理器 — 统一返回 ApiResponse 格式。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("[Exception] 业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return build(e.getStatusCode(), e.getCode(), e.getMessage());
    }

    @ExceptionHandler(OAuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleOAuthException(OAuthException e) {
        log.warn("[Exception] OAuth 异常: status={}, code={}, message={}",
                e.getStatusCode(), e.getCode(), e.getMessage());
        return build(e.getStatusCode(), e.getCode(), e.getMessage());
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileUploadException(FileUploadException e) {
        log.warn("[Exception] 文件上传异常: status={}, code={}, message={}",
                e.getStatusCode(), e.getCode(), e.getMessage());
        return build(e.getStatusCode(), e.getCode(), e.getMessage());
    }

    // ==================== 文件上传 ====================

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("[Exception] 文件大小超限: {}", e.getMessage());
        return build(413, "FILE_TOO_LARGE", "文件大小超过服务器限制");
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipartException(MultipartException e) {
        log.warn("[Exception] 文件上传格式错误: {}", e.getMessage());
        return build(400, "MULTIPART_ERROR", "文件上传请求格式错误");
    }

    // ==================== 参数校验 ====================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("[Exception] 参数异常: {}", e.getMessage());
        return build(HttpStatus.BAD_REQUEST.value(), "INVALID_ARGUMENT", e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("[Exception] 缺少必填参数: {}", e.getParameterName());
        return build(HttpStatus.BAD_REQUEST.value(), "MISSING_PARAM",
                "缺少必填参数: " + e.getParameterName());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException e) {
        log.warn("[Exception] 缺少必填 Header: {}", e.getHeaderName());
        return build(HttpStatus.UNAUTHORIZED.value(), "MISSING_HEADER",
                "缺少必填请求头: " + e.getHeaderName());
    }

    // ==================== 兜底 ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[Exception] 系统异常", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR.value(), "SYSTEM_ERROR",
                "系统内部错误");
    }

    // ==================== 工具方法 ====================

    private ResponseEntity<ApiResponse<Void>> build(int httpStatus, String errorCode, String message) {
        ApiResponse<Void> body = new ApiResponse<>();
        body.setCode(httpStatus);
        body.setMessage(message);
        body.setData(null);
        body.setTimestamp(System.currentTimeMillis());
        body.setTraceId(java.util.UUID.randomUUID().toString());
        return ResponseEntity.status(httpStatus).body(body);
    }
}
