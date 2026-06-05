package com.interviewcoach.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException e) {
        log.warn("[Exception] 业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, e.getCode(), e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("[Exception] 参数异常: message={}", e.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("[Exception] 系统异常", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM_ERROR", "系统内部错误");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", status.value());
        body.put("message", message);
        body.put("error", code);
        body.put("timestamp", LocalDateTime.now().toString());
        return new ResponseEntity<>(body, status);
    }
}
