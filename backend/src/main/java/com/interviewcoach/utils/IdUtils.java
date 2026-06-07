package com.interviewcoach.utils;

import java.util.UUID;

/**
 * ID生成工具类
 */
public class IdUtils {

    private IdUtils() {}

    /**
     * 生成UUID
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成带前缀的ID
     */
    public static String uuidWithPrefix(String prefix) {
        return prefix + "_" + uuid();
    }
}
