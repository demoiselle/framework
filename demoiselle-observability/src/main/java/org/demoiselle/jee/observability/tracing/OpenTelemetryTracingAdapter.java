package org.demoiselle.jee.observability.tracing;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

/**
 * Implementação real que cria spans OpenTelemetry com atributos do módulo Demoiselle.
 * <p>
 * Registrada programaticamente pela {@code ObservabilityExtension} via
 * {@code AfterBeanDiscovery.addBean()} quando OpenTelemetry está no classpath.
 * Não possui anotação de escopo CDI para evitar ambiguidade (WELD-001409)
 * com o bean sintético. Respeita o sampling configurado no SDK.
 */
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
