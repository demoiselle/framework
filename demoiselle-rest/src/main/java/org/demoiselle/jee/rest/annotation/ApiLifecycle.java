/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.ws.rs.NameBinding;

/**
 * Declares the lifecycle/deprecation status of a JAX-RS resource method or class
 * so the framework can emit standard deprecation signalling headers.
 *
 * <p>
 * When applied, {@code DeprecationFilter} adds:
 * </p>
 * <ul>
 *   <li>{@code Deprecation} — per <a href="https://datatracker.ietf.org/doc/html/draft-ietf-httpapi-deprecation-header">the
 *       Deprecation HTTP header draft</a>: {@code "true"} or an IMF-fixdate when
 *       {@link #since()} is provided.</li>
 *   <li>{@code Sunset} — per <a href="https://www.rfc-editor.org/rfc/rfc8594">RFC 8594</a>:
 *       the date after which the resource may become unavailable
 *       (from {@link #sunset()}).</li>
 *   <li>{@code Link} — a {@code rel="deprecation"} (and, when a successor is
 *       given, {@code rel="successor-version"}) link pointing at documentation or
 *       the replacement resource.</li>
 * </ul>
 *
 * @author SERPRO
 */
@NameBinding
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface ApiLifecycle {

    /**
     * The deprecation date as an ISO-8601 date or date-time (e.g.
     * {@code 2025-06-01} or {@code 2025-06-01T00:00:00Z}). When blank, the
     * {@code Deprecation} header is emitted as {@code "true"}.
     *
     * @return the deprecation instant, or empty
     */
    String since() default "";

    /**
     * The sunset date as an ISO-8601 date or date-time, after which the resource
     * may be removed. When blank, no {@code Sunset} header is emitted.
     *
     * @return the sunset date, or empty
     */
    String sunset() default "";

    /**
     * A documentation or successor URI. When present it is emitted as a
     * {@code Link} with {@code rel="deprecation"}.
     *
     * @return the documentation link, or empty
     */
    String link() default "";

    /**
     * A successor-version URI. When present it is emitted as a {@code Link} with
     * {@code rel="successor-version"}.
     *
     * @return the successor URI, or empty
     */
    String successor() default "";
}
