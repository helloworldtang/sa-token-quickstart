package com.tangtang.satoken.apikey.limiter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiter {

    @Value("${app.rate-limit:60}")
    private int maxRequests;   // 每分钟最多 N 次

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public boolean allowRequest(String ip) {
        String key = "rate_limit:" + ip;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(key, 60, TimeUnit.SECONDS);
        }
        return count != null && count <= maxRequests;
    }

    public int getRemaining(String ip) {
        String count = stringRedisTemplate.opsForValue().get("rate_limit:" + ip);
        if (count == null) return maxRequests;
        return Math.max(0, maxRequests - Integer.parseInt(count));
    }

    public int getMaxRequests() {
        return maxRequests;
    }
}
