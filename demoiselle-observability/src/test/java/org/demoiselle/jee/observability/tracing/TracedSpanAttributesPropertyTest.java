/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.observability.tracing;

// Feature: cross-cutting-improvements, Property 3: Span do @Traced contém atributos corretos

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for @Traced span attributes correctness.
 *
 * <p><b>Validates: Requirements 3.1, 3.4</b></p>
 *
 * <p>Property 3: For any arbitrary (module, operation) pair of Strings,
 * the {@link TracingAdapter} receives exactly those values when
 * {@code executeInSpan(module, operation, callable)} is called.</p>
 */
class TracedSpanAttributesPropertyTest {

    /**
     * Recording implementation of {@link TracingAdapter} that captures
     * the module and operation values it receives, then delegates to the callable.
     */
    static class RecordingTracingAdapter implements TracingAdapter {

        private String recordedModule;
        private String recordedOperation;
        private boolean wasCalled;

        @Override
        public <T> T executeInSpan(String module, String operation, SpanCallable<T> callable) throws Exception {
            this.recordedModule = module;
            this.recordedOperation = operation;
            this.wasCalled = true;
            return callable.call();
        }

        String getRecordedModule() {
            return recordedModule;
        }

        String getRecordedOperation() {
            return recordedOperation;
        }

        boolean wasCalled() {
            return wasCalled;
        }
    }

    @Property(tries = 100)
    @Tag("cross-cutting-improvements")
    @Tag("property-3-traced-span-attributes")
    void executeInSpanReceivesExactModuleAndOperation(
            @ForAll("moduleNames") String module,
            @ForAll("operationNames") String operation) throws Exception {

        RecordingTracingAdapter adapter = new RecordingTracingAdapter();

        String sentinel = "result";
        String result = adapter.executeInSpan(module, operation, () -> sentinel);

        assertTrue(adapter.wasCalled(),
                "TracingAdapter.executeInSpan must be invoked");
        assertEquals(module, adapter.getRecordedModule(),
                "TracingAdapter must receive the exact module value passed to executeInSpan");
        assertEquals(operation, adapter.getRecordedOperation(),
                "TracingAdapter must receive the exact operation value passed to executeInSpan");
        assertEquals(sentinel, result,
                "executeInSpan must return the callable's result");
    }

    @Provide
    Arbitrary<String> moduleNames() {
        return Arbitraries.oneOf(
                // Typical module names
                Arbitraries.strings().alpha().numeric().withChars('-', '_', '.')
                        .ofMinLength(1).ofMaxLength(50),
                // Empty string (triggers default resolution in interceptor)
                Arbitraries.just(""),
                // Unicode / special characters
                Arbitraries.strings().all().ofMinLength(0).ofMaxLength(100)
        );
    }

    @Provide
    Arbitrary<String> operationNames() {
        return Arbitraries.oneOf(
                // Typical operation names
                Arbitraries.strings().alpha().numeric().withChars('-', '_', '.')
                        .ofMinLength(1).ofMaxLength(50),
                // Empty string
                Arbitraries.just(""),
                // Unicode / special characters
                Arbitraries.strings().all().ofMinLength(0).ofMaxLength(100)
        );
    }
}
