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
import java.util.Arrays;

/**
 * CDI interceptor that caches method results annotated with {@link Cacheable}.
 *
 * <p>On invocation, the interceptor builds a cache key from the target class name,
 * method name, and a hash of the parameters. If a valid (non-expired) entry exists
 * in the {@link QueryCacheStore}, it is returned immediately (cache hit). Otherwise,
 * the method proceeds normally and its result is stored in the cache with the
 * configured TTL (cache miss).</p>
 *
 * <p>Cache key format: {@code entityClass:methodName:hashOfParameters}</p>
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
        Cacheable cacheable = ctx.getMethod().getAnnotation(Cacheable.class);
        String cacheKey = buildCacheKey(ctx);

        Object cached = cacheStore.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Object result = ctx.proceed();
        cacheStore.put(cacheKey, result, cacheable.ttl());
        return result;
    }

    /**
     * Builds a cache key from the target's superclass name (to get the actual
     * bean class rather than the CDI proxy), the method name, and a hash of
     * the method parameters.
     *
     * @param ctx the invocation context
     * @return the composite cache key
     */
    private String buildCacheKey(InvocationContext ctx) {
        return ctx.getTarget().getClass().getSuperclass().getName() + ":"
             + ctx.getMethod().getName() + ":"
             + Arrays.hashCode(ctx.getParameters());
    }
}
