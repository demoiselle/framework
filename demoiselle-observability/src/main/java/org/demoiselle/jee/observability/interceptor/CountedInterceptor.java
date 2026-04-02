package org.demoiselle.jee.observability.interceptor;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.demoiselle.jee.observability.annotation.Counted;
import org.demoiselle.jee.observability.metrics.MetricsAdapter;

import java.io.Serializable;

@Counted
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class CountedInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MetricsAdapter metricsAdapter;

    @AroundInvoke
    public Object count(InvocationContext ic) throws Exception {
        Counted counted = resolveAnnotation(ic);
        String name = counted.value().isEmpty()
            ? ic.getMethod().getDeclaringClass().getSimpleName() + "." + ic.getMethod().getName()
            : counted.value();

        metricsAdapter.increment(name);
        return ic.proceed();
    }

    private Counted resolveAnnotation(InvocationContext ic) {
        Counted methodLevel = ic.getMethod().getAnnotation(Counted.class);
        return methodLevel != null ? methodLevel
            : ic.getTarget().getClass().getAnnotation(Counted.class);
    }
}
