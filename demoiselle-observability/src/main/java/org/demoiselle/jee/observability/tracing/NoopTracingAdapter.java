package org.demoiselle.jee.observability.tracing;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementação noop usada quando OpenTelemetry não está no classpath.
 * Executa o callable diretamente sem criar spans.
 */
@ApplicationScoped
public class NoopTracingAdapter implements TracingAdapter {

    @Override
    public <T> T executeInSpan(String module, String operation, SpanCallable<T> callable) throws Exception {
        return callable.call();
    }
}
