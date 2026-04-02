package org.demoiselle.jee.observability.tracing;

/**
 * Abstração que encapsula o acesso ao OpenTelemetry para criação de spans.
 * Quando a API não está no classpath, a implementação NoopTracingAdapter é usada.
 */
public interface TracingAdapter {

    /**
     * Executa o callable dentro de um span de tracing.
     *
     * @param module    nome do módulo Demoiselle (ex: "security-jwt")
     * @param operation nome da operação (ex: "token.issue")
     * @param callable  código a ser executado dentro do span
     * @param <T>       tipo de retorno do callable
     * @return resultado da execução do callable
     * @throws Exception se o callable lançar exceção
     */
    <T> T executeInSpan(String module, String operation, SpanCallable<T> callable) throws Exception;

    /**
     * Interface funcional para código executado dentro de um span.
     *
     * @param <T> tipo de retorno
     */
    @FunctionalInterface
    interface SpanCallable<T> {
        T call() throws Exception;
    }
}
