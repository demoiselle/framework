package org.demoiselle.jee.observability.health;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.CDI;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;

/**
 * CDI producer for MicroProfile Health checks.
 * <p>
 * Produces a liveness check that verifies the CDI container is active,
 * a readiness check that verifies the configuration module has been loaded,
 * and a readiness check that verifies JWT cryptographic keys are available
 * when {@code demoiselle-security-jwt} is on the classpath.
 * <p>
 * This producer is registered conditionally by {@code ObservabilityExtension}
 * only when MicroProfile Health is available on the classpath.
 * Does not carry a CDI scope annotation to avoid ambiguity (WELD-001409)
 * with the synthetic bean registered by the extension.
 */
public class HealthCheckProducer {

    /**
     * Produces a liveness health check named "demoiselle-cdi".
     * If this code executes, the CDI container is active — so the check always returns UP.
     *
     * @return a {@link HealthCheck} that reports CDI container liveness
     */
    @Produces
    @Liveness
    public HealthCheck cdiLivenessCheck() {
        return () -> HealthCheckResponse.named("demoiselle-cdi")
                .status(true)
                .build();
    }

    /**
     * Produces a readiness health check named "demoiselle-configuration".
     * Verifies that the Demoiselle configuration module has been loaded
     * by checking whether its CDI bean is resolvable in the container.
     *
     * @return a {@link HealthCheck} that reports configuration readiness
     */
    @Produces
    @Readiness
    public HealthCheck configurationReadinessCheck() {
        return () -> {
            boolean configLoaded = isConfigurationLoaded();
            return HealthCheckResponse.named("demoiselle-configuration")
                    .status(configLoaded)
                    .withData("module", "demoiselle-configuration")
                    .build();
        };
    }

    /**
     * Produces a readiness health check named "demoiselle-security-jwt-keys".
     * When {@code demoiselle-security-jwt} is on the classpath, verifies that
     * the JWT cryptographic keys (public key at minimum) are available.
     * When the module is not on the classpath, reports UP with a note.
     *
     * @return a {@link HealthCheck} that reports JWT key readiness
     */
    @Produces
    @Readiness
    public HealthCheck jwtKeysReadinessCheck() {
        return () -> {
            if (!isSecurityJwtOnClasspath()) {
                return HealthCheckResponse.named("demoiselle-security-jwt-keys")
                        .status(true)
                        .withData("module", "demoiselle-security-jwt")
                        .withData("reason", "module not on classpath")
                        .build();
            }
            return checkJwtKeysAvailability();
        };
    }

    /**
     * Checks whether the Demoiselle configuration module is available and loaded.
     * Uses {@code Class.forName} to avoid a hard compile-time dependency on
     * {@code demoiselle-configuration}, then verifies the bean is resolvable via CDI.
     */
    private boolean isConfigurationLoaded() {
        try {
            Class<?> loaderClass = Class.forName(
                    "org.demoiselle.jee.configuration.ConfigurationLoader",
                    false,
                    Thread.currentThread().getContextClassLoader());
            // If the class is on the classpath, check that CDI can resolve it
            return !CDI.current().select(loaderClass).isUnsatisfied();
        } catch (ClassNotFoundException e) {
            // Configuration module not on classpath — report as not ready
            return false;
        } catch (IllegalStateException e) {
            // CDI container not running
            return false;
        }
    }

    /**
     * Checks whether {@code demoiselle-security-jwt} is on the classpath
     * by probing for the {@code DemoiselleSecurityJWTConfig} class.
     */
    static boolean isSecurityJwtOnClasspath() {
        try {
            Class.forName(
                    "org.demoiselle.jee.security.jwt.impl.DemoiselleSecurityJWTConfig",
                    false,
                    Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Verifies JWT key availability by resolving the config bean via CDI
     * and checking that at least the public key is configured.
     * Uses reflection to avoid a compile-time dependency on {@code demoiselle-security-jwt}.
     */
    private HealthCheckResponse checkJwtKeysAvailability() {
        try {
            Class<?> configClass = Class.forName(
                    "org.demoiselle.jee.security.jwt.impl.DemoiselleSecurityJWTConfig",
                    false,
                    Thread.currentThread().getContextClassLoader());

            Object configBean = CDI.current().select(configClass).get();

            Method getPublicKey = configClass.getMethod("getPublicKey");
            Method getType = configClass.getMethod("getType");

            Object publicKey = getPublicKey.invoke(configBean);
            Object type = getType.invoke(configBean);

            boolean hasPublicKey = publicKey != null && !publicKey.toString().trim().isEmpty();
            boolean hasType = type != null && !type.toString().trim().isEmpty();
            boolean keysAvailable = hasType && hasPublicKey;

            return HealthCheckResponse.named("demoiselle-security-jwt-keys")
                    .status(keysAvailable)
                    .withData("module", "demoiselle-security-jwt")
                    .withData("type", hasType ? type.toString() : "not configured")
                    .withData("publicKeyConfigured", hasPublicKey)
                    .build();

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to verify JWT key availability", e);
            return HealthCheckResponse.named("demoiselle-security-jwt-keys")
                    .status(false)
                    .withData("module", "demoiselle-security-jwt")
                    .withData("error", e.getMessage() != null ? e.getMessage() : e.getClass().getName())
                    .build();
        }
    }

    private static final Logger LOG = Logger.getLogger(HealthCheckProducer.class.getName());
}
