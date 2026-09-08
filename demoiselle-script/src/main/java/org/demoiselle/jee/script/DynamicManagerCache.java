/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Dynamic Manager Cache — application-scoped, shared store for compiled scripts
 * and loaded JSR-223 engines, with a {@link ReadWriteLock} <em>per engine</em>.
 *
 * <p>
 * Prior versions relied on {@code synchronized} methods on the request-scoped
 * {@code DynamicManager}, which serialised unrelated engines and did not protect
 * the shared application-scoped cache against concurrent access from multiple
 * {@code DynamicManager} instances. Locking now lives here, next to the state it
 * guards, and is scoped to a single engine so operations on different engines
 * proceed concurrently.
 * </p>
 *
 * <p>
 * The maps themselves are {@link ConcurrentHashMap} instances so structural
 * reads (e.g. {@code get}) are always safe. The per-engine locks provide
 * <em>atomicity</em> for compound operations (load, remove, unload, clear, eval)
 * that would otherwise interleave. Read operations (eval, get) take the read
 * lock; mutating operations (load, remove, unload, clear) take the write lock.
 * </p>
 *
 * @author SERPRO
 */
@ApplicationScoped
public class DynamicManagerCache implements Serializable {

    private static final long serialVersionUID = 2305168056315491913L;

    private final Map<String, ConcurrentHashMap<String, Object>> scriptCache = new ConcurrentHashMap<>();
    private final Map<String, Object> engineList = new ConcurrentHashMap<>();
    private final Map<String, ReadWriteLock> engineLocks = new ConcurrentHashMap<>();

    public Map<String, ConcurrentHashMap<String, Object>> getScriptCache() {
        return scriptCache;
    }

    public Map<String, Object> getEngineList() {
        return engineList;
    }

    /**
     * Returns the {@link ReadWriteLock} guarding the given engine, creating it
     * atomically on first use. The lock exists independently of whether the
     * engine is currently loaded, so load/unload cycles never race on lock
     * creation.
     *
     * @param engineName the engine name (must not be {@code null})
     * @return the per-engine lock; never {@code null}
     */
    public ReadWriteLock lockFor(String engineName) {
        return engineLocks.computeIfAbsent(engineName, k -> new ReentrantReadWriteLock());
    }

    /**
     * Executes {@code action} while holding the write lock for {@code engineName}.
     *
     * @param engineName the engine name
     * @param action     the mutating action
     * @param <R>        the return type
     * @return the value produced by {@code action}
     */
    public <R> R withWriteLock(String engineName, Supplier<R> action) {
        ReadWriteLock lock = lockFor(engineName);
        lock.writeLock().lock();
        try {
            return action.get();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Executes {@code action} while holding the write lock for {@code engineName}.
     *
     * @param engineName the engine name
     * @param action     the mutating action
     */
    public void withWriteLock(String engineName, Runnable action) {
        withWriteLock(engineName, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Executes {@code action} while holding the read lock for {@code engineName}.
     * The read lock allows concurrent readers while excluding writers, ensuring
     * an in-progress read (e.g. {@code eval}) is never torn down by a concurrent
     * {@code unload}/{@code clear}.
     *
     * @param engineName the engine name
     * @param action     the read action
     * @param <R>        the return type
     * @return the value produced by {@code action}
     */
    public <R> R withReadLock(String engineName, Supplier<R> action) {
        ReadWriteLock lock = lockFor(engineName);
        lock.readLock().lock();
        try {
            return action.get();
        } finally {
            lock.readLock().unlock();
        }
    }
}
