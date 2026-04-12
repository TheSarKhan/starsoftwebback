package az.starsoft.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory token-bucket rate limiter, keyed by (bucket, client identifier).
 *
 * For a single-instance deployment this is enough to defend the contact form
 * and login endpoint from spam / brute force. If we ever scale horizontally,
 * swap the backing map for Redis.
 */
@Component
public class RateLimiter {

    @Value("${app.ratelimit.contact-per-hour}")
    private int contactPerHour;

    @Value("${app.ratelimit.login-per-minute}")
    private int loginPerMinute;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean allowContact(String clientKey) {
        return allow("contact:" + clientKey, contactPerHour, Duration.ofHours(1));
    }

    public boolean allowLogin(String clientKey) {
        return allow("login:" + clientKey, loginPerMinute, Duration.ofMinutes(1));
    }

    private synchronized boolean allow(String key, int capacity, Duration window) {
        Instant now = Instant.now();
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(capacity, now));

        // Refill: linear over the window
        long elapsedNanos = Duration.between(bucket.lastRefill, now).toNanos();
        double tokensToAdd = (double) elapsedNanos / window.toNanos() * capacity;
        bucket.tokens = Math.min(capacity, bucket.tokens + tokensToAdd);
        bucket.lastRefill = now;

        if (bucket.tokens >= 1.0) {
            bucket.tokens -= 1.0;
            return true;
        }
        return false;
    }

    private static final class Bucket {
        double tokens;
        Instant lastRefill;

        Bucket(int capacity, Instant now) {
            this.tokens = capacity;
            this.lastRefill = now;
        }
    }
}
