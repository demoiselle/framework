/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.observability.metrics;

// Feature: cross-cutting-improvements, Property 1: Contagem monotônica do @Counted

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for monotonic counting of the {@link MetricsAdapter} contract.
 *
 * <p><b>Validates: Requirements 1.1</b></p>
 *
 * <p>Property 1: For any N ∈ [1, 1000] invocations and any arbitrary counter name,
 * {@code getCount()} after N calls to {@code increment()} must return exactly N.</p>
 */
class CountedMonotonicPropertyTest {

    /**
     * In-memory implementation of {@link MetricsAdapter} that uses a
     * {@link ConcurrentHashMap} to track counter values. This tests the
     * MetricsAdapter contract with a real counting implementation.
     */
    static class InMemoryMetricsAdapter implements MetricsAdapter {

        private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

        @Override
        public void increment(String counterName) {
            counters.computeIfAbsent(counterName, k -> new AtomicLong(0L)).incrementAndGet();
        }

        @Override
        public long getCount(String counterName) {
            AtomicLong counter = counters.get(counterName);
            return counter == null ? 0L : counter.get();
        }
    }

    @Property(tries = 100)
    @Tag("cross-cutting-improvements")
    @Tag("property-1-contagem-monotonica")
    void afterNIncrementsGetCountReturnsN(
            @ForAll @IntRange(min = 1, max = 1000) int n,
            @ForAll("counterNames") String counterName) {

        MetricsAdapter adapter = new InMemoryMetricsAdapter();

        for (int i = 0; i < n; i++) {
            adapter.increment(counterName);
        }

        assertEquals(n, adapter.getCount(counterName),
                "getCount() must return exactly N after N calls to increment()");
    }

    @Provide
    Arbitrary<String> counterNames() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .withChars('.', '-', '_')
                .ofMinLength(1)
                .ofMaxLength(50);
    }
}
