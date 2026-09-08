/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.idempotency;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

import org.demoiselle.jee.core.api.idempotency.IdempotencyRecord;
import org.demoiselle.jee.core.api.idempotency.IdempotencyStore;

/**
 * Default, in-memory {@link IdempotencyStore}.
 *
 * <p>
 * Records are held in a {@link ConcurrentHashMap} and slots are claimed
 * atomically via {@link ConcurrentHashMap#compute(Object, java.util.function.BiFunction)},
 * so two concurrent requests for the same fingerprint can never both win the
 * {@link #begin(String, long)} race. Expired records are treated as absent and
 * lazily replaced on access.
 * </p>
 *
 * <p>
 * This implementation requires no external database or broker. It is marked as
 * an {@link Alternative} with a low priority so applications can override it by
 * providing their own {@link IdempotencyStore} bean without an ambiguity error;
 * enable it via {@code beans.xml} or rely on it directly in tests.
 * </p>
 *
 * @author SERPRO
 */
@ApplicationScoped
@Alternative
@Priority(1)
public class LocalIdempotencyStore implements IdempotencyStore {

    private final ConcurrentHashMap<String, IdempotencyRecord> store = new ConcurrentHashMap<>();
    private final Clock clock;

    /** Default constructor using the system UTC clock. */
    public LocalIdempotencyStore() {
        this(Clock.systemUTC());
    }

    /**
     * Constructor allowing a custom {@link Clock}, primarily for deterministic
     * TTL testing.
     *
     * @param clock the clock to use
     */
    public LocalIdempotencyStore(Clock clock) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @Override
    public Optional<IdempotencyRecord> begin(String fingerprint, long ttlSeconds) {
        Instant now = clock.instant();
        // Holder for the pre-existing live record, if any.
        IdempotencyRecord[] existing = new IdempotencyRecord[1];
        store.compute(fingerprint, (key, current) -> {
            if (current == null || current.isExpired(now)) {
                existing[0] = null;
                return IdempotencyRecord.inProgress(fingerprint, ttlSeconds, now);
            }
            existing[0] = current;
            return current; // keep the live record untouched
        });
        return Optional.ofNullable(existing[0]);
    }

    @Override
    public void complete(String fingerprint, int httpStatus, byte[] body, Map<String, List<String>> headers) {
        Instant now = clock.instant();
        store.computeIfPresent(fingerprint, (key, current) -> {
            if (current.isExpired(now)) {
                return null; // evict expired
            }
            return current.completeWith(httpStatus, body, headers);
        });
    }

    @Override
    public void abort(String fingerprint) {
        store.remove(fingerprint);
    }

    @Override
    public Optional<IdempotencyRecord> find(String fingerprint) {
        Instant now = clock.instant();
        IdempotencyRecord record = store.get(fingerprint);
        if (record == null) {
            return Optional.empty();
        }
        if (record.isExpired(now)) {
            // Lazily evict, but only if it's still the same expired mapping.
            store.remove(fingerprint, record);
            return Optional.empty();
        }
        return Optional.of(record);
    }

    /**
     * Number of live entries (test/diagnostic helper). Expired entries may still
     * be counted until they are lazily evicted.
     *
     * @return the current map size
     */
    public int size() {
        return store.size();
    }
}
