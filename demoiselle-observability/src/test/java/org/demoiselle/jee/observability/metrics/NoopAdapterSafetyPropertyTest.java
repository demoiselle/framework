/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.observability.metrics;

// Feature: cross-cutting-improvements, Property 2: Segurança dos adapters noop

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for noop adapter safety.
 *
 * <p><b>Validates: Requirements 1.8, 3.3</b></p>
 *
 * <p>Property 2: For any arbitrary String, {@code NoopMetricsAdapter.increment()}
 * does not throw an exception and {@code getCount()} returns 0.</p>
 */
class NoopAdapterSafetyPropertyTest {

    @Property(tries = 100)
    @Tag("cross-cutting-improvements")
    @Tag("property-2-noop-adapter-safety")
    void noopMetricsAdapterIncrementNeverThrowsAndCountIsAlwaysZero(
            @ForAll("arbitraryStrings") String counterName) {

        NoopMetricsAdapter adapter = new NoopMetricsAdapter();

        // increment() must not throw for any input
        assertDoesNotThrow(() -> adapter.increment(counterName),
                "NoopMetricsAdapter.increment() must never throw for any input");

        // getCount() must always return 0 regardless of prior increments
        assertEquals(0L, adapter.getCount(counterName),
                "NoopMetricsAdapter.getCount() must always return 0");
    }

    @Property(tries = 100)
    @Tag("cross-cutting-improvements")
    @Tag("property-2-noop-adapter-safety")
    void noopMetricsAdapterGetCountNeverThrowsAndReturnsZero(
            @ForAll("arbitraryStrings") String counterName) {

        NoopMetricsAdapter adapter = new NoopMetricsAdapter();

        // getCount() must not throw even without prior increment
        long count = assertDoesNotThrow(() -> adapter.getCount(counterName),
                "NoopMetricsAdapter.getCount() must never throw for any input");

        assertEquals(0L, count,
                "NoopMetricsAdapter.getCount() must return 0 for any counter name");
    }

    @Provide
    Arbitrary<String> arbitraryStrings() {
        return Arbitraries.oneOf(
                // Normal counter names
                Arbitraries.strings().alpha().numeric().withChars('.', '-', '_')
                        .ofMinLength(0).ofMaxLength(100),
                // Empty string
                Arbitraries.just(""),
                // Strings with special/unicode characters
                Arbitraries.strings().all().ofMinLength(0).ofMaxLength(200),
                // Null-like edge cases: whitespace-only strings
                Arbitraries.strings().whitespace().ofMinLength(1).ofMaxLength(20)
        );
    }
}
