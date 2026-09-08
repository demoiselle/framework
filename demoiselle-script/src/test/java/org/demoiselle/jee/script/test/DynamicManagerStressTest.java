/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script.test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.demoiselle.jee.script.DynamicManager;
import org.demoiselle.jee.script.DynamicManagerCache;
import org.demoiselle.jee.script.exception.DemoiselleScriptException;
import org.demoiselle.jee.script.message.DemoiselleScriptMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Concurrency stress tests for {@link DynamicManager}.
 *
 * <p>
 * The manager is {@code @RequestScoped} in production, so a CDI client proxy
 * cannot be shared across worker threads. To exercise the <em>shared,
 * application-scoped</em> {@link DynamicManagerCache} locking directly, this test
 * wires a plain {@code DynamicManager} instance around a single shared cache
 * (as would happen when multiple request-scoped managers concurrently touch the
 * same application-scoped cache).
 * </p>
 *
 * <p>
 * It hammers {@code loadScript}, {@code eval}, {@code removeScript},
 * {@code getScript}, {@code clearCache} and {@code unloadEngine} from many
 * threads on the same engine and asserts that no unexpected exception (in
 * particular no {@link NullPointerException}) escapes and there is no deadlock.
 * The only tolerated exception is {@link DemoiselleScriptException}, a legitimate
 * outcome of racing against an unload/clear.
 * </p>
 *
 * <p>Validates Requirement (1): per-engine ReadWriteLock; atomic
 * eval/remove/unload/clear/load; no NPE/corruption under stress.</p>
 */
class DynamicManagerStressTest {

    private static final String ENGINE = "groovy";
    private static final String SOURCE = "def a = X; X = (X ?: 0) + 1";
    private static final long TIMEOUT_SECONDS = 30;

    private DynamicManager dm;
    private DynamicManagerCache sharedCache;

    @BeforeEach
    void setUp() throws Exception {
        sharedCache = new DynamicManagerCache();
        dm = new DynamicManager();
        inject(dm, "cache", sharedCache);
        inject(dm, "bundle", new DemoiselleScriptMessage() {
            @Override public String cannotLoadEngine(String engineName) { return "cannot-load:" + engineName; }
            @Override public String engineNotLoaded() { return "engine-not-loaded"; }
            @Override public String engineNotCompilable() { return "engine-not-compilable"; }
            @Override public String scriptNotLoaded(String scriptName) { return "script-not-loaded:" + scriptName; }
        });
    }

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static boolean tolerated(Throwable t) {
        // A DemoiselleScriptException is a valid outcome when a concurrent
        // unload/clear removed the engine/script. NPE or any other error is a bug.
        return t instanceof DemoiselleScriptException;
    }

    @Test
    @DisplayName("Concurrent load/eval/remove/clear/unload never throws NPE or corrupts the cache")
    void stressSameEngineNoNpeNoCorruption() throws Exception {
        final int workers = 16;
        final int iterations = 300;

        dm.loadEngine(ENGINE);

        CyclicBarrier barrier = new CyclicBarrier(workers);
        CountDownLatch done = new CountDownLatch(workers);
        AtomicReference<Throwable> fatal = new AtomicReference<>();
        ExecutorService pool = Executors.newFixedThreadPool(workers);

        try {
            for (int w = 0; w < workers; w++) {
                final int id = w;
                pool.submit(() -> {
                    try {
                        barrier.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        for (int i = 0; i < iterations && fatal.get() == null; i++) {
                            String scriptName = "s-" + (i % 8);
                            int op = (id + i) % 6;
                            try {
                                switch (op) {
                                    case 0 -> dm.loadScript(ENGINE, scriptName, SOURCE);
                                    case 1 -> {
                                        Bindings b = new SimpleBindings();
                                        b.put("X", 0);
                                        dm.eval(ENGINE, scriptName, b);
                                    }
                                    case 2 -> dm.removeScript(ENGINE, scriptName);
                                    case 3 -> dm.getScript(ENGINE, scriptName);
                                    case 4 -> dm.clearCache(ENGINE);
                                    case 5 -> {
                                        dm.unloadEngine(ENGINE);
                                        dm.loadEngine(ENGINE); // reload to keep the workload going
                                    }
                                    default -> { /* unreachable */ }
                                }
                            } catch (Throwable t) {
                                if (!tolerated(t)) {
                                    fatal.compareAndSet(null, t);
                                }
                            }
                        }
                    } catch (Throwable t) {
                        fatal.compareAndSet(null, t);
                    } finally {
                        done.countDown();
                    }
                });
            }

            assertTrue(done.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "All workers should finish within the timeout (no deadlock)");
            assertNull(fatal.get(),
                    () -> "No NPE/corruption expected under stress, but got: " + fatal.get());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @DisplayName("Steady load/eval/remove cycle stays consistent under load")
    void stressLoadEvalRemoveCycle() throws Exception {
        final int workers = 8;
        final int iterations = 400;

        dm.loadEngine(ENGINE);

        CyclicBarrier barrier = new CyclicBarrier(workers);
        CountDownLatch done = new CountDownLatch(workers);
        AtomicReference<Throwable> fatal = new AtomicReference<>();
        ExecutorService pool = Executors.newFixedThreadPool(workers);

        try {
            for (int w = 0; w < workers; w++) {
                pool.submit(() -> {
                    try {
                        barrier.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        for (int i = 0; i < iterations && fatal.get() == null; i++) {
                            String scriptName = "m-" + (i % 4);
                            try {
                                dm.loadScript(ENGINE, scriptName, SOURCE);
                                Bindings b = new SimpleBindings();
                                b.put("X", 0);
                                dm.eval(ENGINE, scriptName, b);
                                dm.removeScript(ENGINE, scriptName);
                            } catch (Throwable t) {
                                if (!tolerated(t)) {
                                    fatal.compareAndSet(null, t);
                                }
                            }
                        }
                    } catch (Throwable t) {
                        fatal.compareAndSet(null, t);
                    } finally {
                        done.countDown();
                    }
                });
            }

            assertTrue(done.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                    "All workers should finish within the timeout (no deadlock)");
            assertNull(fatal.get(),
                    () -> "No NPE/corruption expected under stress, but got: " + fatal.get());
        } finally {
            pool.shutdownNow();
        }
    }
}
