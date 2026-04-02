package org.demoiselle.jee.observability;

import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;

import org.demoiselle.jee.observability.metrics.MetricsAdapter;
import org.demoiselle.jee.observability.metrics.NoopMetricsAdapter;
import org.demoiselle.jee.observability.tracing.NoopTracingAdapter;
import org.demoiselle.jee.observability.tracing.TracingAdapter;

/**
 * CDI Extension that detects the presence of MicroProfile Metrics, MicroProfile Health
 * and OpenTelemetry on the classpath and conditionally registers the appropriate adapter beans.
 * <p>
 * When an optional API is not available, a noop adapter is registered so that interceptors
 * degrade gracefully without errors. An informational log message is emitted for each
 * unavailable API.
 * <p>
 * Registered via {@code META-INF/services/jakarta.enterprise.inject.spi.Extension}.
 */
public class ObservabilityExtension implements Extension {

    private static final Logger LOG = Logger.getLogger(ObservabilityExtension.class.getName());

    private boolean metricsAvailable;
    private boolean healthAvailable;
    private boolean otelAvailable;

    /**
     * Observes {@link AfterBeanDiscovery} to detect optional APIs on the classpath
     * and register the appropriate CDI beans.
     *
     * @param abd the AfterBeanDiscovery event
     * @param bm  the BeanManager
     */
    void detectApis(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        metricsAvailable = isClassAvailable("org.eclipse.microprofile.metrics.MetricRegistry", cl);
        healthAvailable = isClassAvailable("org.eclipse.microprofile.health.HealthCheck", cl);
        otelAvailable = isClassAvailable("io.opentelemetry.api.trace.Tracer", cl);

        registerMetricsAdapter(abd);
        registerTracingAdapter(abd);
        registerHealthCheckProducer(abd);
    }

    private void registerMetricsAdapter(AfterBeanDiscovery abd) {
        if (metricsAvailable) {
            LOG.info("MicroProfile Metrics detected — registering MicroProfileMetricsAdapter");
            try {
                Class<?> adapterClass = Class.forName(
                        "org.demoiselle.jee.observability.metrics.MicroProfileMetricsAdapter",
                        true,
                        Thread.currentThread().getContextClassLoader());
                abd.addBean()
                        .beanClass(adapterClass)
                        .types(MetricsAdapter.class, adapterClass, Object.class)
                        .scope(ApplicationScoped.class)
                        .createWith(ctx -> {
                            try {
                                return adapterClass.getDeclaredConstructor().newInstance();
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to create MicroProfileMetricsAdapter", e);
                            }
                        });
            } catch (ClassNotFoundException e) {
                LOG.warning("MicroProfileMetricsAdapter class not found — falling back to NoopMetricsAdapter");
                registerNoopMetrics(abd);
            }
        } else {
            LOG.info("MicroProfile Metrics não disponível — métricas desativadas");
            registerNoopMetrics(abd);
        }
    }

    private void registerNoopMetrics(AfterBeanDiscovery abd) {
        abd.addBean()
                .beanClass(NoopMetricsAdapter.class)
                .types(MetricsAdapter.class, NoopMetricsAdapter.class, Object.class)
                .scope(ApplicationScoped.class)
                .createWith(ctx -> new NoopMetricsAdapter());
    }

    private void registerTracingAdapter(AfterBeanDiscovery abd) {
        if (otelAvailable) {
            LOG.info("OpenTelemetry detected — registering OpenTelemetryTracingAdapter");
            try {
                Class<?> adapterClass = Class.forName(
                        "org.demoiselle.jee.observability.tracing.OpenTelemetryTracingAdapter",
                        true,
                        Thread.currentThread().getContextClassLoader());
                abd.addBean()
                        .beanClass(adapterClass)
                        .types(TracingAdapter.class, adapterClass, Object.class)
                        .scope(ApplicationScoped.class)
                        .createWith(ctx -> {
                            try {
                                return adapterClass.getDeclaredConstructor().newInstance();
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to create OpenTelemetryTracingAdapter", e);
                            }
                        });
            } catch (ClassNotFoundException e) {
                LOG.warning("OpenTelemetryTracingAdapter class not found — falling back to NoopTracingAdapter");
                registerNoopTracing(abd);
            }
        } else {
            LOG.info("OpenTelemetry não disponível — tracing desativado");
            registerNoopTracing(abd);
        }
    }

    private void registerNoopTracing(AfterBeanDiscovery abd) {
        abd.addBean()
                .beanClass(NoopTracingAdapter.class)
                .types(TracingAdapter.class, NoopTracingAdapter.class, Object.class)
                .scope(ApplicationScoped.class)
                .createWith(ctx -> new NoopTracingAdapter());
    }

    private void registerHealthCheckProducer(AfterBeanDiscovery abd) {
        if (healthAvailable) {
            LOG.info("MicroProfile Health detected — registering HealthCheckProducer");
            try {
                Class<?> producerClass = Class.forName(
                        "org.demoiselle.jee.observability.health.HealthCheckProducer",
                        true,
                        Thread.currentThread().getContextClassLoader());
                abd.addBean()
                        .beanClass(producerClass)
                        .types(producerClass, Object.class)
                        .scope(ApplicationScoped.class)
                        .createWith(ctx -> {
                            try {
                                return producerClass.getDeclaredConstructor().newInstance();
                            } catch (Exception e) {
                                throw new RuntimeException("Failed to create HealthCheckProducer", e);
                            }
                        });
            } catch (ClassNotFoundException e) {
                LOG.warning("HealthCheckProducer class not found — health checks will not be registered");
            }
        } else {
            LOG.info("MicroProfile Health não disponível — health checks desativados");
        }
    }

    private boolean isClassAvailable(String className, ClassLoader cl) {
        try {
            Class.forName(className, false, cl);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * @return {@code true} if MicroProfile Metrics API is available on the classpath
     */
    public boolean isMetricsAvailable() {
        return metricsAvailable;
    }

    /**
     * @return {@code true} if MicroProfile Health API is available on the classpath
     */
    public boolean isHealthAvailable() {
        return healthAvailable;
    }

    /**
     * @return {@code true} if OpenTelemetry API is available on the classpath
     */
    public boolean isOtelAvailable() {
        return otelAvailable;
    }
}
