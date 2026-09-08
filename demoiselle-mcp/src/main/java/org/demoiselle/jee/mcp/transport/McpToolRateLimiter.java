/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.transport;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Atomic per-session sliding-window rate limiter for MCP tool invocations.
 *
 * <p>Self-contained (no dependency on the optional {@code demoiselle-security}
 * module). Each key (typically {@code sessionId}) is throttled independently;
 * the counter resets once the window elapses. All state transitions happen
 * through {@link ConcurrentHashMap#compute} so counting is correct under
 * concurrency. Entries expire lazily and a global cap bounds memory.</p>
 *
 * @author SERPRO
 */
final class McpToolRateLimiter {

    private record Window(long count, long windowStart, long expiresAt) {}

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final int maxKeys;

    McpToolRateLimiter(int maxKeys) {
        this.maxKeys = maxKeys > 0 ? maxKeys : 10_000;
    }

    /**
     * Records an invocation for {@code key} and reports whether the caller is
     * within the limit.
     *
     * @param key           the throttling key
     * @param maxRequests   maximum invocations allowed within the window
     * @param windowSeconds the sliding window length in seconds
     * @return {@code -1} when allowed, otherwise the {@code Retry-After} seconds
     */
    int recordAndCheck(String key, int maxRequests, int windowSeconds) {
        final long now = System.currentTimeMillis();
        final long windowMillis = windowSeconds * 1000L;
        evictIfNeeded(now);

        Window w = windows.compute(key, (k, existing) -> {
            if (existing == null || now >= existing.expiresAt) {
                return new Window(1, now, now + windowMillis);
            }
            return new Window(existing.count + 1, existing.windowStart, existing.expiresAt);
        });

        if (w.count > maxRequests) {
            int retryAfter = (int) ((w.windowStart + windowMillis - now) / 1000) + 1;
            return Math.min(Math.max(retryAfter, 1), windowSeconds);
        }
        return -1;
    }

    void remove(String key) {
        windows.remove(key);
    }

    long size() {
        return windows.size();
    }

    private void evictIfNeeded(long now) {
        if (windows.size() < maxKeys) {
            return;
        }
        windows.entrySet().removeIf(e -> now >= e.getValue().expiresAt);
    }
}
