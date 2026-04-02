package org.demoiselle.jee.crud.cache;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method for query result caching with automatic invalidation via CDI events.
 *
 * <p>When a method annotated with {@code @Cacheable} is invoked, the {@code CacheInterceptor}
 * checks for a cached result keyed by the entity class and method parameters. If a valid
 * (non-expired) entry exists, it is returned directly. Otherwise, the method executes normally
 * and its result is stored in the cache with the configured TTL.</p>
 *
 * <p>Cache entries are automatically invalidated when an {@link EntityModifiedEvent} is observed
 * for the corresponding entity class.</p>
 *
 * <p>Validates: Requirements 7.1</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@InterceptorBinding
public @interface Cacheable {

    /**
     * Time-to-live in seconds for cached results. Defaults to 300 (5 minutes).
     *
     * @return the TTL in seconds
     */
    @Nonbinding
    long ttl() default 300;
}
