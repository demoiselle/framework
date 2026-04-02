/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.observability.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponse.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link HealthCheckProducer}.
 *
 * <p>Tests the liveness and readiness health check producers
 * without a running CDI container.</p>
 */
class HealthCheckProducerTest {

    private HealthCheckProducer producer;

    @BeforeEach
    void setUp() {
        producer = new HealthCheckProducer();
    }

    @Test
    void cdiLivenessCheckReturnsUpStatus() {
        HealthCheck check = producer.cdiLivenessCheck();
        assertNotNull(check, "Liveness check must not be null");

        HealthCheckResponse response = check.call();
        assertNotNull(response, "Response must not be null");
        assertEquals("demoiselle-cdi", response.getName());
        assertEquals(Status.UP, response.getStatus(),
                "Liveness check should report UP when CDI is active");
    }

    @Test
    void configurationReadinessCheckReturnsResponseWithCorrectName() {
        HealthCheck check = producer.configurationReadinessCheck();
        assertNotNull(check, "Readiness check must not be null");

        // Without a CDI container running, the check should report DOWN
        // because CDI.current() will throw IllegalStateException
        HealthCheckResponse response = check.call();
        assertNotNull(response, "Response must not be null");
        assertEquals("demoiselle-configuration", response.getName());
        // Without CDI container, config is not loaded
        assertEquals(Status.DOWN, response.getStatus(),
                "Readiness check should report DOWN when CDI container is not available");
    }

    @Test
    void configurationReadinessCheckIncludesModuleData() {
        HealthCheck check = producer.configurationReadinessCheck();
        HealthCheckResponse response = check.call();

        assertTrue(response.getData().isPresent(), "Response should include data");
        assertEquals("demoiselle-configuration",
                response.getData().get().get("module"),
                "Response data should include module name");
    }

    @Test
    void livenessCheckProducesNewInstanceEachCall() {
        HealthCheck check1 = producer.cdiLivenessCheck();
        HealthCheck check2 = producer.cdiLivenessCheck();
        // Each call produces a new lambda, both should work independently
        assertNotNull(check1.call());
        assertNotNull(check2.call());
    }

    @Test
    void jwtKeysReadinessCheckReturnsUpWhenModuleNotOnClasspath() {
        // demoiselle-security-jwt is NOT on the test classpath for this module
        HealthCheck check = producer.jwtKeysReadinessCheck();
        assertNotNull(check, "JWT keys readiness check must not be null");

        HealthCheckResponse response = check.call();
        assertNotNull(response, "Response must not be null");
        assertEquals("demoiselle-security-jwt-keys", response.getName());
        assertEquals(Status.UP, response.getStatus(),
                "JWT keys check should report UP when module is not on classpath");
    }

    @Test
    void jwtKeysReadinessCheckIncludesModuleDataWhenNotOnClasspath() {
        HealthCheck check = producer.jwtKeysReadinessCheck();
        HealthCheckResponse response = check.call();

        assertTrue(response.getData().isPresent(), "Response should include data");
        assertEquals("demoiselle-security-jwt",
                response.getData().get().get("module"),
                "Response data should include module name");
        assertEquals("module not on classpath",
                response.getData().get().get("reason"),
                "Response data should indicate module is not on classpath");
    }

    @Test
    void isSecurityJwtOnClasspathReturnsFalseWhenModuleAbsent() {
        // In the observability module test classpath, security-jwt is not present
        assertFalse(HealthCheckProducer.isSecurityJwtOnClasspath(),
                "Should return false when demoiselle-security-jwt is not on classpath");
    }
}
