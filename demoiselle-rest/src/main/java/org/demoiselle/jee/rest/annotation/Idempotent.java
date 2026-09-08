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
 * Marks a JAX-RS resource method (or resource class) as <em>idempotent</em> with
 * respect to the {@code Idempotency-Key} request header.
 *
 * <p>
 * When present, {@code IdempotencyFilter} records the first successful response
 * for a given fingerprint and <strong>replays</strong> it for subsequent
 * requests carrying the same {@code Idempotency-Key}. A request that arrives
 * while an identical request is still in progress receives a
 * {@code 409 Conflict}.
 * </p>
 *
 * <p>
 * The fingerprint is derived from the authenticated principal, HTTP method,
 * request path and a hash of the request payload. This annotation is a
 * {@link NameBinding}, so it can target a method or an entire resource class.
 * </p>
 *
 * @author SERPRO
 */
@NameBinding
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Idempotent {

    /**
     * Time-to-live, in seconds, for a stored idempotency record. Defaults to
     * {@code 86400} (24 hours).
     *
     * @return the TTL in seconds
     */
    long ttlSeconds() default 86_400L;

    /**
     * Whether the request payload participates in the fingerprint. When
     * {@code true} (default), the same key with a different body is treated as a
     * conflict rather than a replay.
     *
     * @return {@code true} to include the payload hash
     */
    boolean includePayload() default true;
}
