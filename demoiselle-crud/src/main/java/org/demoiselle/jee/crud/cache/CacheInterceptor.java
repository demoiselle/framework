/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.cache;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.Objects;

/**
 * CDI interceptor that caches method results annotated with {@link Cacheable}.
 *
 * <p>On invocation, the interceptor builds a cache key from the configured
 * entity/owner class, method signature, and a deep hash of the parameters. If
 * a valid (non-expired) entry exists in the {@link QueryCacheStore}, it is
 * returned immediately. Otherwise, the method proceeds normally and a
 * non-null result is stored with the configured TTL.</p>
 *
 * <p>Cache key format: {@code ownerClass:methodSignature:parametersHash}</p>
 *
 * <p>Validates: Requirements 7.2, 7.3</p>
 */
@Cacheable
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class CacheInterceptor {

    @Inject
    private QueryCacheStore cacheStore;

    /**
     * Intercepts methods annotated with {@link Cacheable}, checking the cache
     * before proceeding with the actual method invocation.
     *
     * @param ctx the invocation context
     * @return the cached result or the result of the method invocation
     * @throws Exception if the underlying method throws an exception
     */
    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        Cacheable cacheable = resolveCacheable(ctx);
        CacheKey cacheKey = buildCacheKey(ctx, cacheable);

        Object cached = cacheStore.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Object result = ctx.proceed();
        if (result != null) {
            cacheStore.put(cacheKey, result, cacheable.ttl());
        }
        return result;
    }

    private Cacheable resolveCacheable(InvocationContext ctx) {
        Cacheable cacheable = ctx.getMethod().getAnnotation(Cacheable.class);
        if (cacheable == null) {
            cacheable = ctx.getMethod().getDeclaringClass().getAnnotation(Cacheable.class);
        }
        if (cacheable == null && ctx.getTarget() != null) {
            Class<?> type = ctx.getTarget().getClass();
            while (cacheable == null && type != null) {
                cacheable = type.getAnnotation(Cacheable.class);
                type = type.getSuperclass();
            }
        }
        return Objects.requireNonNull(cacheable,
                "CacheInterceptor invoked without @Cacheable binding");
    }

    /**
     * Builds a structured key namespaced by the explicitly configured entity
     * class, or by the method's declaring class for backwards compatibility. The
     * discriminator is the method's generic signature and the parameters are
     * captured with a collision-resistant structured signature.
     */
    private CacheKey buildCacheKey(InvocationContext ctx, Cacheable cacheable) {
        Class<?> owner = cacheable.entityClass() == Void.class
                ? ctx.getMethod().getDeclaringClass()
                : cacheable.entityClass();
        return CacheKey.of(
                owner.getName(),
                ctx.getMethod().toGenericString(),
                ctx.getParameters());
    }
}
