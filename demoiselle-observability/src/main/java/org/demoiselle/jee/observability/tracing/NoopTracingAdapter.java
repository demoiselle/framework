package org.demoiselle.jee.observability.tracing;

/**
 * Implementação noop usada quando OpenTelemetry não está no classpath.
 * Executa o callable diretamente sem criar spans.
 * <p>
 * Registrada programaticamente pela {@code ObservabilityExtension} via
 * {@code AfterBeanDiscovery.addBean()}. Não possui anotação de escopo CDI
 * para evitar ambiguidade (WELD-001409) com o bean sintético.
 */
public class NoopTracingAdapter implements TracingAdapter {

    @Override
    public <T> T executeInSpan(String module, String operation, SpanCallable<T> callable) throws Exception {
        return callable.call();
    }
}
