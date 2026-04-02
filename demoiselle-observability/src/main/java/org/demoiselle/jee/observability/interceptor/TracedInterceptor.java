package org.demoiselle.jee.observability.interceptor;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.demoiselle.jee.observability.annotation.Traced;
import org.demoiselle.jee.observability.tracing.TracingAdapter;

import java.io.Serializable;

@Traced
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 1)
public class TracedInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private TracingAdapter tracingAdapter;

    @AroundInvoke
    public Object trace(InvocationContext ic) throws Exception {
        Traced traced = resolveAnnotation(ic);
        String module = traced.module().isEmpty()
            ? ic.getMethod().getDeclaringClass().getSimpleName()
            : traced.module();
        String operation = traced.operation().isEmpty()
            ? ic.getMethod().getName()
            : traced.operation();

        return tracingAdapter.executeInSpan(module, operation, ic::proceed);
    }

    private Traced resolveAnnotation(InvocationContext ic) {
        Traced methodLevel = ic.getMethod().getAnnotation(Traced.class);
        return methodLevel != null ? methodLevel
            : ic.getTarget().getClass().getAnnotation(Traced.class);
    }
}
