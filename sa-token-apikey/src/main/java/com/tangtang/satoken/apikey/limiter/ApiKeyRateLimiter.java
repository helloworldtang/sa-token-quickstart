package com.tangtang.satoken.apikey.limiter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class ApiKeyRateLimiter {

    @Value("${app.apikey-rate-limit:100}")
    private int maxRequestsPerMinute;

    @Value("${app.apikey-daily-limit:10000}")
    private int maxRequestsPerDay;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** 分钟级限流 */
    public boolean allowRequest(String apiKey) {
        String key = "apikey_rate_limit:" + apiKey;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, 60, TimeUnit.SECONDS);
        }
        return count != null && count <= maxRequestsPerMinute;
    }

    /** 每日限流 */
    public boolean allowDailyRequest(String apiKey) {
        String key = "apikey_daily_limit:" + apiKey;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            // ★ 过期时间设为当天结束，而不是固定的 24h
            stringRedisTemplate.expire(key, secondsUntilEndOfDay(), TimeUnit.SECONDS);
        }
        return count != null && count <= maxRequestsPerDay;
    }

    /** 使用统计 */
    public ApiKeyUsageStats getUsageStats(String apiKey) {
        ApiKeyUsageStats stats = new ApiKeyUsageStats();
        stats.setApiKey(apiKey);

        String minute = stringRedisTemplate.opsForValue().get("apikey_rate_limit:" + apiKey);
        String daily = stringRedisTemplate.opsForValue().get("apikey_daily_limit:" + apiKey);

        int minuteUsed = minute != null ? Integer.parseInt(minute) : 0;
        int dailyUsed = daily != null ? Integer.parseInt(daily) : 0;

        stats.setTotalRequests(dailyUsed); // 总请求数使用每日计数
        stats.setMinuteRequests(minuteUsed);
        stats.setMinuteLimit(maxRequestsPerMinute);
        stats.setDayRequests(dailyUsed);
        stats.setDayLimit(maxRequestsPerDay);
        stats.setUniqueIps(1); // 简化实现，实际应该统计不同IP数

        return stats;
    }

    /** 重置分钟限流（管理功能） */
    public void resetRateLimit(String apiKey) {
        stringRedisTemplate.delete("apikey_rate_limit:" + apiKey);
    }

    /** 重置每日限流（管理功能） */
    public void resetDailyLimit(String apiKey) {
        stringRedisTemplate.delete("apikey_daily_limit:" + apiKey);
    }

    private long secondsUntilEndOfDay() {
        long now = System.currentTimeMillis();
        long endOfDay = ((now / 86400000) + 1) * 86400000;
        return (endOfDay - now) / 1000;
    }

    // ===== 内部类：使用统计 =====
    public static class ApiKeyUsageStats {
        private String apiKey;
        private int totalRequests;
        private int minuteRequests;
        private int minuteLimit;
        private int dayRequests;
        private int dayLimit;
        private int uniqueIps;

        // getters & setters
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public int getTotalRequests() { return totalRequests; }
        public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }
        public int getMinuteRequests() { return minuteRequests; }
        public void setMinuteRequests(int minuteRequests) { this.minuteRequests = minuteRequests; }
        public int getMinuteLimit() { return minuteLimit; }
        public void setMinuteLimit(int minuteLimit) { this.minuteLimit = minuteLimit; }
        public int getDayRequests() { return dayRequests; }
        public void setDayRequests(int dayRequests) { this.dayRequests = dayRequests; }
        public int getDayLimit() { return dayLimit; }
        public void setDayLimit(int dayLimit) { this.dayLimit = dayLimit; }
        public int getUniqueIps() { return uniqueIps; }
        public void setUniqueIps(int uniqueIps) { this.uniqueIps = uniqueIps; }
    }
}
