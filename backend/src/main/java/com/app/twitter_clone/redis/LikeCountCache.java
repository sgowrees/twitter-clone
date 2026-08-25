package com.app.twitter_clone.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class LikeCountCache {

    private static final String KEY_PREFIX = "post:likecount:";
    private static final Duration TTL = Duration.ofSeconds(30);

    private final RedisTemplate<String, Object> redisTemplate;

    public LikeCountCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Long get(Long postId) {
        Object cached = redisTemplate.opsForValue().get(KEY_PREFIX + postId);
        if (cached == null) {
            return null;
        }
        return Long.valueOf(cached.toString());
    }

    public void set(Long postId, long count) {
        redisTemplate.opsForValue().set(KEY_PREFIX + postId, count, TTL);
    }

    // Called whenever a like/unlike happens, so the next read doesn't serve a stale count
    public void evict(Long postId) {
        redisTemplate.delete(KEY_PREFIX + postId);
    }
}