/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.store;

/**
 * Atomic storage SPI for security counters (rate limiting, brute-force lockout,
 * replay protection, etc.).
 *
 * <p>Implementations MUST provide atomic, thread-safe operations. The default
 * implementation is {@link LocalSecurityStore}, an in-memory store with
 * per-entry expiration and a global cap to bound memory usage. Applications
 * that run in a cluster may provide an alternative CDI implementation backed by
 * a shared store (e.g. Redis, Hazelcast) as long as the semantics below are
 * preserved.</p>
 *
 * <p>The store organises entries into logical namespaces so that unrelated
 * concerns (rate limit vs. brute force vs. replay) never collide on a shared
 * key.</p>
 *
 * @author SERPRO
 */
public interface SecurityStore {

    /**
     * Atomically increments the counter identified by {@code namespace}/{@code key}
     * within a sliding window and returns the resulting count.
     *
     * <p>The window is anchored on the first increment; once {@code windowMillis}
     * have elapsed since the window start the counter is reset before the
     * increment is applied. Implementations MUST perform the read, expiry check
     * and increment as a single atomic operation.</p>
     *
     * @param namespace    logical bucket (e.g. {@code "ratelimit"})
     * @param key          entry key (e.g. resolved client key)
     * @param windowMillis window length in milliseconds; when {@code <= 0} the
     *                     counter never expires on its own
     * @return the count after this increment (always {@code >= 1})
     */
    long incrementAndGet(String namespace, String key, long windowMillis);

    /**
     * Returns the current count for the entry without mutating it, honouring
     * window expiry.
     *
     * @param namespace logical bucket
     * @param key       entry key
     * @return the current count, or {@code 0} when absent or expired
     */
    long currentCount(String namespace, String key);

    /**
     * Returns the timestamp (epoch millis) of the oldest recorded event still
     * inside the current window, or {@code -1} when the entry is absent/expired.
     *
     * <p>Used to compute an accurate {@code Retry-After}.</p>
     *
     * @param namespace logical bucket
     * @param key       entry key
     * @return epoch millis of the window start, or {@code -1}
     */
    long windowStartMillis(String namespace, String key);

    /**
     * Atomically records a lockout: sets an absolute expiry (epoch millis) for
     * the entry. A subsequent {@link #lockRemainingMillis(String, String)} will
     * report the remaining time until {@code untilEpochMillis}.
     *
     * @param namespace        logical bucket
     * @param key              entry key
     * @param untilEpochMillis absolute expiry timestamp in epoch millis
     */
    void lock(String namespace, String key, long untilEpochMillis);

    /**
     * Returns the remaining lockout time in milliseconds for the entry, or
     * {@code -1} when the entry is not locked (or the lock already expired). An
     * expired lock is cleared as a side effect.
     *
     * @param namespace logical bucket
     * @param key       entry key
     * @return remaining lockout millis, or {@code -1}
     */
    long lockRemainingMillis(String namespace, String key);

    /**
     * Atomically records a single-use marker with the given time-to-live and
     * reports whether it was newly inserted. Used for replay protection: the
     * first call for a given key returns {@code true}; any subsequent call
     * within the TTL returns {@code false}.
     *
     * @param namespace  logical bucket
     * @param key        entry key (e.g. a challenge {@code jti})
     * @param ttlMillis  how long the marker is retained
     * @return {@code true} if the marker was newly stored, {@code false} if it
     *         already existed (replay)
     */
    boolean markIfAbsent(String namespace, String key, long ttlMillis);

    /**
     * Removes the entry identified by {@code namespace}/{@code key}.
     *
     * @param namespace logical bucket
     * @param key       entry key
     */
    void remove(String namespace, String key);

    /**
     * Clears every entry in the store. Primarily intended for tests and
     * administrative reset.
     */
    void clear();

    /**
     * Approximate number of live entries currently held by the store. Used for
     * observability and to assert the cap in tests.
     *
     * @return the number of entries
     */
    long size();
}
