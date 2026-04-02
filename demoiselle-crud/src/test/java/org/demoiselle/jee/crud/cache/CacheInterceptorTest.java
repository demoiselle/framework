/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.cache;

import jakarta.interceptor.InvocationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CacheInterceptor} (Task 13.4).
 *
 * Validates: Requirements 7.2, 7.3
 */
class CacheInterceptorTest {

    private CacheInterceptor interceptor;
    private QueryCacheStore cacheStore;

    @BeforeEach
    void setUp() throws Exception {
        interceptor = new CacheInterceptor();
        cacheStore = new QueryCacheStore();

        // Inject cacheStore via reflection (no CDI container in unit tests)
        Field storeField = CacheInterceptor.class.getDeclaredField("cacheStore");
        storeField.setAccessible(true);
        storeField.set(interceptor, cacheStore);
    }

    // --- Cache miss: method executes and result is stored ---

    @Test
    void intercept_cacheMiss_executesMethodAndStoresResult() throws Exception {
        StubInvocationContext ctx = new StubInvocationContext(
                new SampleService(), SampleService.class.getMethod("findAll", String.class),
                new Object[]{"param1"}, "result-data");

        Object result = interceptor.intercept(ctx);

        assertEquals("result-data", result);
        assertTrue(ctx.proceeded, "Method should have been invoked on cache miss");
    }

    // --- Cache hit: method does NOT execute ---

    @Test
    void intercept_cacheHit_returnsFromCacheWithoutExecutingMethod() throws Exception {
        SampleService target = new SampleService();
        Method method = SampleService.class.getMethod("findAll", String.class);
        Object[] params = new Object[]{"param1"};

        // First call — cache miss
        StubInvocationContext ctx1 = new StubInvocationContext(target, method, params, "original-result");
        Object result1 = interceptor.intercept(ctx1);
        assertEquals("original-result", result1);
        assertTrue(ctx1.proceeded);

        // Second call — cache hit
        StubInvocationContext ctx2 = new StubInvocationContext(target, method, params, "should-not-be-returned");
        Object result2 = interceptor.intercept(ctx2);
        assertEquals("original-result", result2, "Should return cached result");
        assertFalse(ctx2.proceeded, "Method should NOT be invoked on cache hit");
    }

    // --- Different parameters produce different cache keys ---

    @Test
    void intercept_differentParams_produceDifferentCacheEntries() throws Exception {
        SampleService target = new SampleService();
        Method method = SampleService.class.getMethod("findAll", String.class);

        StubInvocationContext ctx1 = new StubInvocationContext(target, method, new Object[]{"A"}, "result-A");
        StubInvocationContext ctx2 = new StubInvocationContext(target, method, new Object[]{"B"}, "result-B");

        assertEquals("result-A", interceptor.intercept(ctx1));
        assertEquals("result-B", interceptor.intercept(ctx2));
    }

    // --- Different methods produce different cache keys ---

    @Test
    void intercept_differentMethods_produceDifferentCacheEntries() throws Exception {
        SampleService target = new SampleService();
        Method findAll = SampleService.class.getMethod("findAll", String.class);
        Method findById = SampleService.class.getMethod("findById", String.class);

        StubInvocationContext ctx1 = new StubInvocationContext(target, findAll, new Object[]{"x"}, "all-result");
        StubInvocationContext ctx2 = new StubInvocationContext(target, findById, new Object[]{"x"}, "byId-result");

        assertEquals("all-result", interceptor.intercept(ctx1));
        assertEquals("byId-result", interceptor.intercept(ctx2));
    }

    // --- Exception propagation: method exception is not cached ---

    @Test
    void intercept_whenMethodThrows_exceptionPropagatesAndNothingIsCached() throws Exception {
        SampleService target = new SampleService();
        Method method = SampleService.class.getMethod("findAll", String.class);
        Object[] params = new Object[]{"error"};

        StubInvocationContext ctx = new StubInvocationContext(target, method, params, null) {
            @Override
            public Object proceed() throws Exception {
                proceeded = true;
                throw new RuntimeException("DB error");
            }
        };

        assertThrows(RuntimeException.class, () -> interceptor.intercept(ctx));

        // Verify nothing was cached for this key
        StubInvocationContext ctx2 = new StubInvocationContext(target, method, params, "fresh-result");
        Object result = interceptor.intercept(ctx2);
        assertEquals("fresh-result", result);
        assertTrue(ctx2.proceeded, "Should execute method since nothing was cached");
    }

    // --- TTL is respected from @Cacheable annotation ---

    @Test
    void intercept_usesTtlFromAnnotation() throws Exception {
        SampleService target = new SampleService();
        Method method = SampleService.class.getMethod("findWithShortTtl", String.class);
        Object[] params = new Object[]{"p"};

        StubInvocationContext ctx1 = new StubInvocationContext(target, method, params, "cached-value");
        assertEquals("cached-value", interceptor.intercept(ctx1));

        // Wait for TTL (1 second) to expire
        Thread.sleep(1100);

        StubInvocationContext ctx2 = new StubInvocationContext(target, method, params, "fresh-value");
        Object result = interceptor.intercept(ctx2);
        assertEquals("fresh-value", result, "Should execute method after TTL expires");
        assertTrue(ctx2.proceeded);
    }

    // --- Null parameters handled correctly ---

    @Test
    void intercept_withNullParameters_doesNotThrow() throws Exception {
        SampleService target = new SampleService();
        Method method = SampleService.class.getMethod("findAll", String.class);

        StubInvocationContext ctx = new StubInvocationContext(target, method, new Object[]{null}, "result");
        Object result = interceptor.intercept(ctx);
        assertEquals("result", result);
    }

    // --- Empty parameters handled correctly ---

    @Test
    void intercept_withNoParameters_doesNotThrow() throws Exception {
        SampleService target = new SampleService();
        Method method = SampleService.class.getMethod("findNoArgs");

        StubInvocationContext ctx = new StubInvocationContext(target, method, new Object[]{}, "result");
        Object result = interceptor.intercept(ctx);
        assertEquals("result", result);
    }

    // ========== Test helpers ==========

    /**
     * Sample service class used as the interceptor target.
     */
    static class SampleService {

        @Cacheable
        public String findAll(String filter) {
            return "real-" + filter;
        }

        @Cacheable
        public String findById(String id) {
            return "real-byId-" + id;
        }

        @Cacheable(ttl = 1)
        public String findWithShortTtl(String filter) {
            return "real-short-" + filter;
        }

        @Cacheable
        public String findNoArgs() {
            return "real-no-args";
        }
    }

    /**
     * Minimal stub for {@link InvocationContext} that provides the target,
     * method, parameters, and a configurable return value for proceed().
     */
    static class StubInvocationContext implements InvocationContext {

        private final Object target;
        private final Method method;
        private final Object[] parameters;
        private final Object proceedResult;
        boolean proceeded = false;

        StubInvocationContext(Object target, Method method, Object[] parameters, Object proceedResult) {
            this.target = target;
            this.method = method;
            this.parameters = parameters;
            this.proceedResult = proceedResult;
        }

        @Override
        public Object getTarget() {
            return target;
        }

        @Override
        public Object getTimer() {
            return null;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Constructor<?> getConstructor() {
            return null;
        }

        @Override
        public Object[] getParameters() {
            return parameters;
        }

        @Override
        public void setParameters(Object[] params) {
            // no-op
        }

        @Override
        public Map<String, Object> getContextData() {
            return Map.of();
        }

        @Override
        public Object proceed() throws Exception {
            proceeded = true;
            return proceedResult;
        }
    }
}
