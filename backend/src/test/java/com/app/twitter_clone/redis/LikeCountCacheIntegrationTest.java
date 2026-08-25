package com.app.twitter_clone.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// @Testcontainers starts a real Redis in a throwaway Docker container just
// for this test class, and tears it down when it's done - no manual
// "docker run redis" needed, no risk of colliding with your dev Redis.
@SpringBootTest
@Testcontainers
class LikeCountCacheIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private LikeCountCache likeCountCache;

    @Test
    void setThenGet_returnsTheCachedValue() {
        Long postId = 9001L;

        likeCountCache.set(postId, 42);

        assertEquals(42L, likeCountCache.get(postId));
    }

    @Test
    void get_returnsNullOnCacheMiss() {
        Long postId = 9002L; // never set

        assertNull(likeCountCache.get(postId));
    }

    @Test
    void evict_removesTheCachedValue() {
        Long postId = 9003L;

        likeCountCache.set(postId, 7);
        assertEquals(7L, likeCountCache.get(postId));

        likeCountCache.evict(postId);

        assertNull(likeCountCache.get(postId));
    }

    @Test
    void cachedValue_expiresAfterTtl() {
        Long postId = 9004L;

        likeCountCache.set(postId, 5);
        assertEquals(5L, likeCountCache.get(postId));

        // TTL is 30s in LikeCountCache - polling instead of Thread.sleep(31s)
        // keeps the test fast when it passes, still correct when it doesn't.
        await().atMost(35, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> assertNull(likeCountCache.get(postId)));
    }
}