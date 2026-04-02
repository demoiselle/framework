/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.observability.health;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponse.Status;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.spi.HealthCheckResponseProvider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Minimal {@link HealthCheckResponseProvider} for unit testing without a
 * MicroProfile Health runtime (e.g., SmallRye Health).
 */
public class TestHealthCheckResponseProvider implements HealthCheckResponseProvider {

    @Override
    public HealthCheckResponseBuilder createResponseBuilder() {
        return new TestResponseBuilder();
    }

    private static final class TestResponseBuilder extends HealthCheckResponseBuilder {

        private String name;
        private Status status = Status.DOWN;
        private final Map<String, Object> data = new LinkedHashMap<>();

        @Override
        public HealthCheckResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public HealthCheckResponseBuilder withData(String key, String value) {
            data.put(key, value);
            return this;
        }

        @Override
        public HealthCheckResponseBuilder withData(String key, long value) {
            data.put(key, value);
            return this;
        }

        @Override
        public HealthCheckResponseBuilder withData(String key, boolean value) {
            data.put(key, value);
            return this;
        }

        @Override
        public HealthCheckResponseBuilder up() {
            this.status = Status.UP;
            return this;
        }

        @Override
        public HealthCheckResponseBuilder down() {
            this.status = Status.DOWN;
            return this;
        }

        @Override
        public HealthCheckResponseBuilder status(boolean up) {
            this.status = up ? Status.UP : Status.DOWN;
            return this;
        }

        @Override
        public HealthCheckResponse build() {
            return new TestHealthCheckResponse(name, status,
                    data.isEmpty() ? null : new LinkedHashMap<>(data));
        }
    }

    private static final class TestHealthCheckResponse extends HealthCheckResponse {

        private final String name;
        private final Status status;
        private final Map<String, Object> data;

        TestHealthCheckResponse(String name, Status status, Map<String, Object> data) {
            this.name = name;
            this.status = status;
            this.data = data;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        @Override
        public Optional<Map<String, Object>> getData() {
            return Optional.ofNullable(data);
        }
    }
}
