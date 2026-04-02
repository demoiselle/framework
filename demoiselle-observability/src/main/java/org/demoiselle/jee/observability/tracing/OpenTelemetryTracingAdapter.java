package org.demoiselle.jee.observability.tracing;

import jakarta.enterprise.context.ApplicationScoped;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

/**
 * Implementação real que cria spans OpenTelemetry com atributos do módulo Demoiselle.
 * Registrada condicionalmente pela ObservabilityExtension quando OpenTelemetry está no classpath.
 * Respeita o sampling configurado no SDK — não força amostragem própria.
 */
@ApplicationScoped
public class OpenTelemetryTracingAdapter implements TracingAdapter {

    private static final AttributeKey<String> DEMOISELLE_MODULE = AttributeKey.stringKey("demoiselle.module");
    private static final AttributeKey<String> DEMOISELLE_OPERATION = AttributeKey.stringKey("demoiselle.operation");

    private Tracer getTracer() {
        return GlobalOpenTelemetry.getTracer("demoiselle-observability");
    }

    @Override
    public <T> T executeInSpan(String module, String operation, SpanCallable<T> callable) throws Exception {
        Span span = getTracer()
                .spanBuilder(module + "." + operation)
                .setAllAttributes(Attributes.of(
                        DEMOISELLE_MODULE, module,
                        DEMOISELLE_OPERATION, operation
                ))
                .startSpan();

        try {
            T result = callable.call();
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}
