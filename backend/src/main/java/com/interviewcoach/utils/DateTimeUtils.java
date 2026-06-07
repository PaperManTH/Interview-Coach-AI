package com.interviewcoach.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具类
 */
public class DateTimeUtils {

    private static final DateTimeFormatter DEFAULT_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static final DateTimeFormatter COMPACT_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private DateTimeUtils() {}

    public static String formatNow() {
        return LocalDateTime.now().format(DEFAULT_FORMATTER);
    }

    public static String formatCompact() {
        return LocalDateTime.now().format(COMPACT_FORMATTER);
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DEFAULT_FORMATTER) : "";
    }
}
