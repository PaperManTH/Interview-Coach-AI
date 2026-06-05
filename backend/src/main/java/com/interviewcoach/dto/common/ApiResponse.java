package com.interviewcoach.dto.common;

import lombok.Data;

import java.util.UUID;

/**
 * 统一 REST 响应封装。
 *
 * @param <T> 业务数据类型
 */
@Data
public class ApiResponse<T> {

    /** 业务状态码，0 表示成功。 */
    private Integer code;

    /** 提示信息。 */
    private String message;

    /** 业务数据。 */
    private T data;

    /** 响应时间戳（毫秒）。 */
    private Long timestamp;

    /** 请求追踪 ID，便于排障。 */
    private String traceId;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.setCode(0);
        resp.setMessage("ok");
        resp.setData(data);
        resp.setTimestamp(System.currentTimeMillis());
        resp.setTraceId(UUID.randomUUID().toString());
        return resp;
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.setCode(code);
        resp.setMessage(message);
        resp.setTimestamp(System.currentTimeMillis());
        resp.setTraceId(UUID.randomUUID().toString());
        return resp;
    }
}
