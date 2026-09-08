/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;

import org.demoiselle.jee.rest.annotation.ApiLifecycle;

/**
 * Response filter that emits standard API deprecation/lifecycle headers for
 * resources annotated with {@link ApiLifecycle}.
 *
 * <p>Emitted headers:</p>
 * <ul>
 *   <li>{@code Deprecation}: {@code "true"} or an IMF-fixdate derived from
 *       {@link ApiLifecycle#since()}.</li>
 *   <li>{@code Sunset}: an IMF-fixdate derived from {@link ApiLifecycle#sunset()}
 *       (RFC 8594).</li>
 *   <li>{@code Link}: {@code rel="deprecation"} and/or
 *       {@code rel="successor-version"} links.</li>
 * </ul>
 *
 * <p>
 * The filter never overrides headers already set by the application and is
 * bound to the {@link ApiLifecycle} name binding so it only runs for annotated
 * resources.
 * </p>
 *
 * @author SERPRO
 */
@Provider
@ApiLifecycle
@Priority(Priorities.DEPRECATION)
public class DeprecationFilter implements ContainerResponseFilter {

    static final String DEPRECATION = "Deprecation";
    static final String SUNSET = "Sunset";
    static final String LINK = "Link";

    /** IMF-fixdate format, e.g. {@code Sun, 06 Nov 1994 08:49:37 GMT}. */
    private static final DateTimeFormatter IMF_FIXDATE =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", java.util.Locale.US)
                    .withZone(ZoneOffset.UTC);

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        ApiLifecycle lifecycle = resolve();
        if (lifecycle == null) {
            return;
        }
        var headers = responseContext.getHeaders();

        if (!headers.containsKey(DEPRECATION)) {
            String since = lifecycle.since();
            String value = (since == null || since.isBlank()) ? "true" : toImfDateOrRaw(since);
            headers.putSingle(DEPRECATION, value);
        }

        if (!lifecycle.sunset().isBlank() && !headers.containsKey(SUNSET)) {
            headers.putSingle(SUNSET, toImfDateOrRaw(lifecycle.sunset()));
        }

        addLink(headers, lifecycle.link(), "deprecation");
        addLink(headers, lifecycle.successor(), "successor-version");
    }

    private ApiLifecycle resolve() {
        if (resourceInfo == null) {
            return null;
        }
        Method method = resourceInfo.getResourceMethod();
        if (method != null && method.isAnnotationPresent(ApiLifecycle.class)) {
            return method.getAnnotation(ApiLifecycle.class);
        }
        Class<?> clazz = resourceInfo.getResourceClass();
        if (clazz != null && clazz.isAnnotationPresent(ApiLifecycle.class)) {
            return clazz.getAnnotation(ApiLifecycle.class);
        }
        return null;
    }

    private static void addLink(jakarta.ws.rs.core.MultivaluedMap<String, Object> headers,
            String uri, String rel) {
        if (uri == null || uri.isBlank()) {
            return;
        }
        String linkValue = "<" + uri + ">; rel=\"" + rel + "\"";
        // Append without clobbering existing Link headers.
        if (!headers.containsKey(LINK)) {
            headers.add(LINK, linkValue);
        } else if (!headers.get(LINK).contains(linkValue)) {
            headers.add(LINK, linkValue);
        }
    }

    /**
     * Converts an ISO-8601 date/date-time to an IMF-fixdate, or returns the raw
     * value if it is not parseable (so callers can supply pre-formatted values).
     */
    private static String toImfDateOrRaw(String value) {
        try {
            OffsetDateTime odt = OffsetDateTime.parse(value);
            return IMF_FIXDATE.format(odt);
        } catch (DateTimeParseException ignored) {
            // Try a date-only value at start of day UTC.
        }
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(value);
            return IMF_FIXDATE.format(date.atStartOfDay(ZoneOffset.UTC));
        } catch (DateTimeParseException ignored) {
            return value;
        }
    }
}
