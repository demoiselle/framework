/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.inject.Inject;

import org.demoiselle.jee.script.DynamicManagerCache;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for DynamicManagerCache with instance fields (non-static).
 * <p>
 * Validates:
 * - Each DynamicManagerCache instance has its own independent cache (Req 12.2)
 * - getScriptCache() and getEngineList() return non-null maps (Req 12.2)
 * - Concurrent reads and writes are thread-safe via ConcurrentHashMap (Req 12.4)
 * - Multiple threads can put/get simultaneously without data corruption (Req 12.4)
 * </p>
 *
 * Validates: Requirements 12.2, 12.4
 */
@EnableAutoWeld
@AddBeanClasses({ DynamicManagerCache.class })
class DynamicManagerCacheTest {

    private static final int THREAD_COUNT = 10;
    private static final long TIMEOUT_SECONDS = 10;

    @Inject
    DynamicManagerCache cache;

    // ---- CDI Injection Tests (Req 12.2) ----

    @Test
    @DisplayName("CDI-injected DynamicManagerCache should not be null")
    void cdiInjectedCacheShouldNotBeNull() {
        assertNotNull(cache, "DynamicManagerCache should be injected by CDI");
    }

    @Test
    @DisplayName("getScriptCache() should return non-null map")
    void getScriptCacheShouldReturnNonNullMap() {
        assertNotNull(cache.getScriptCache(), "scriptCache should never be null");
    }

    @Test
    @DisplayName("getEngineList() should return non-null map")
    void getEngineListShouldReturnNonNullMap() {
        assertNotNull(cache.getEngineList(), "engineList should never be null");
    }

    @Test
    @DisplayName("Each DynamicManagerCache instance should have independent caches")
    void eachInstanceShouldHaveIndependentCaches() {
        DynamicManagerCache instance1 = new DynamicManagerCache();
        DynamicManagerCache instance2 = new DynamicManagerCache();

        // Put data in instance1
        instance1.getEngineList().put("engine1", "value1");
        instance1.getScriptCache().put("engine1", new ConcurrentHashMap<>());

        // instance2 should be empty — not sharing static state
        assertTrue(instance2.getEngineList().isEmpty(),
                "Second instance should have empty engineList (no shared static state)");
        assertTrue(instance2.getScriptCache().isEmpty(),
                "Second instance should have empty scriptCache (no shared static state)");

        // Verify instance1 still has its data
        assertEquals("value1", instance1.getEngineList().get("engine1"));
        assertNotNull(instance1.getScriptCache().get("engine1"));
    }

    @Test
    @DisplayName("CDI-injected cache should support basic put/get operations")
    void cdiCacheShouldSupportBasicOperations() {
        cache.getEngineList().put("testEngine", "engineObj");
        cache.getScriptCache().put("testEngine", new ConcurrentHashMap<>());
        cache.getScriptCache().get("testEngine").put("script1", "compiledScript");

        assertEquals("engineObj", cache.getEngineList().get("testEngine"));
        assertEquals("compiledScript", cache.getScriptCache().get("testEngine").get("script1"));

        // Cleanup
        cache.getEngineList().remove("testEngine");
        cache.getScriptCache().remove("testEngine");
    }

    // ---- Thread-Safety / Concurrency Tests (Req 12.4) ----

    @Test
    @DisplayName("Concurrent writes to engineList should not lose data")
    void concurrentWritesToEngineListShouldNotLoseData() throws Exception {
        DynamicManagerCache localCache = new DynamicManagerCache();
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
        AtomicReference<Throwable> firstError = new AtomicReference<>();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        barrier.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        localCache.getEngineList().put("engine-" + index, "value-" + index);
                    } catch (Throwable t) {
                        firstError.compareAndSet(null, t);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            assertTrue(doneLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "All threads should complete within timeout");
            assertNull(firstError.get(), "No errors should occur during concurrent writes");

            assertEquals(THREAD_COUNT, localCache.getEngineList().size(),
                    "All entries should be present after concurrent writes");

            for (int i = 0; i < THREAD_COUNT; i++) {
                assertEquals("value-" + i, localCache.getEngineList().get("engine-" + i),
                        "Each entry should have correct value");
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Concurrent writes to scriptCache should not lose data")
    void concurrentWritesToScriptCacheShouldNotLoseData() throws Exception {
        DynamicManagerCache localCache = new DynamicManagerCache();
        CyclicBarrier barrier = new CyclicBarrier(THREAD_COUNT);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
        AtomicReference<Throwable> firstError = new AtomicReference<>();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        barrier.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        ConcurrentHashMap<String, Object> scripts = new ConcurrentHashMap<>();
                        scripts.put("script-" + index, "compiled-" + index);
                        localCache.getScriptCache().put("engine-" + index, scripts);
                    } catch (Throwable t) {
                        firstError.compareAndSet(null, t);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            assertTrue(doneLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "All threads should complete within timeout");
            assertNull(firstError.get(), "No errors should occur during concurrent writes");

            assertEquals(THREAD_COUNT, localCache.getScriptCache().size(),
                    "All engine entries should be present");

            for (int i = 0; i < THREAD_COUNT; i++) {
                assertNotNull(localCache.getScriptCache().get("engine-" + i),
                        "Engine entry should exist");
                assertEquals("compiled-" + i,
                        localCache.getScriptCache().get("engine-" + i).get("script-" + i),
                        "Script entry should have correct value");
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Concurrent reads and writes should not corrupt data")
    void concurrentReadsAndWritesShouldNotCorruptData() throws Exception {
        DynamicManagerCache localCache = new DynamicManagerCache();

        // Pre-populate with some data
        for (int i = 0; i < 5; i++) {
            localCache.getEngineList().put("pre-engine-" + i, "pre-value-" + i);
        }

        int writerCount = 5;
        int readerCount = 5;
        int totalThreads = writerCount + readerCount;
        CyclicBarrier barrier = new CyclicBarrier(totalThreads);
        CountDownLatch doneLatch = new CountDownLatch(totalThreads);
        AtomicReference<Throwable> firstError = new AtomicReference<>();
        AtomicInteger readSuccessCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        try {
            // Writers — add new entries
            for (int i = 0; i < writerCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        barrier.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        localCache.getEngineList().put("new-engine-" + index, "new-value-" + index);
                    } catch (Throwable t) {
                        firstError.compareAndSet(null, t);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            // Readers — read pre-existing entries
            for (int i = 0; i < readerCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        barrier.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        Object value = localCache.getEngineList().get("pre-engine-" + index);
                        if (("pre-value-" + index).equals(value)) {
                            readSuccessCount.incrementAndGet();
                        }
                    } catch (Throwable t) {
                        firstError.compareAndSet(null, t);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            assertTrue(doneLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "All threads should complete within timeout");
            assertNull(firstError.get(), "No errors should occur during concurrent read/write");

            assertEquals(readerCount, readSuccessCount.get(),
                    "All readers should see correct pre-existing values");

            for (int i = 0; i < writerCount; i++) {
                assertEquals("new-value-" + i, localCache.getEngineList().get("new-engine-" + i),
                        "All written values should be present");
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Multiple threads putting/getting from same scriptCache engine should not corrupt data")
    void concurrentAccessToSameScriptCacheEngineShouldBeThreadSafe() throws Exception {
        DynamicManagerCache localCache = new DynamicManagerCache();
        ConcurrentHashMap<String, Object> sharedScripts = new ConcurrentHashMap<>();
        localCache.getScriptCache().put("shared-engine", sharedScripts);

        int totalThreads = THREAD_COUNT;
        CyclicBarrier barrier = new CyclicBarrier(totalThreads);
        CountDownLatch doneLatch = new CountDownLatch(totalThreads);
        AtomicReference<Throwable> firstError = new AtomicReference<>();

        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        try {
            for (int i = 0; i < totalThreads; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        barrier.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        // Each thread writes its own script and reads it back
                        String scriptKey = "script-" + index;
                        String scriptValue = "compiled-" + index;
                        localCache.getScriptCache().get("shared-engine").put(scriptKey, scriptValue);

                        // Read back own entry
                        Object readBack = localCache.getScriptCache().get("shared-engine").get(scriptKey);
                        assertEquals(scriptValue, readBack,
                                "Thread should read back its own written value");
                    } catch (Throwable t) {
                        firstError.compareAndSet(null, t);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            assertTrue(doneLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "All threads should complete within timeout");
            assertNull(firstError.get(), "No errors should occur during concurrent access to same engine");

            assertEquals(totalThreads, localCache.getScriptCache().get("shared-engine").size(),
                    "All script entries should be present in the shared engine cache");
        } finally {
            executor.shutdownNow();
        }
    }
}
