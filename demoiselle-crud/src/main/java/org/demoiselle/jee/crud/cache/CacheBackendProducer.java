/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.cache;

import java.util.ServiceLoader;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Selects the active {@link CacheBackend} for the application.
 *
 * <p>Selection order:</p>
 * <ol>
 *   <li>A distributed/shared backend discovered through
 *       {@link ServiceLoader} (any registered {@link CacheBackend} whose name is
 *       not {@code local}). This is the adapter extension point — a Redis or
 *       Hazelcast adapter registered in
 *       {@code META-INF/services/org.demoiselle.jee.crud.cache.CacheBackend}
 *       is picked up automatically.</li>
 *   <li>Otherwise the JDK-only {@link LocalBoundedCacheBackend}.</li>
 * </ol>
 *
 * <p>The selection can be pinned to a specific backend name with the system
 * property {@code demoiselle.crud.cache.backend}.</p>
 *
 * @author SERPRO
 */
@ApplicationScoped
public class CacheBackendProducer {

    private static final Logger LOGGER = Logger.getLogger(CacheBackendProducer.class.getName());

    /** System property to pin the backend by {@link CacheBackend#name()}. */
    public static final String BACKEND_PROPERTY = "demoiselle.crud.cache.backend";

    private volatile CacheBackend backend;

    /**
     * @return the singleton {@link CacheBackend} for the application
     */
    @Produces
    @ApplicationScoped
    public CacheBackend cacheBackend() {
        return resolve();
    }

    private CacheBackend resolve() {
        CacheBackend resolved = backend;
        if (resolved != null) {
            return resolved;
        }
        synchronized (this) {
            if (backend == null) {
                backend = discover();
            }
            return backend;
        }
    }

    private CacheBackend discover() {
        String pinned = System.getProperty(BACKEND_PROPERTY);
        CacheBackend local = null;
        CacheBackend distributed = null;

        for (CacheBackend candidate : ServiceLoader.load(CacheBackend.class,
                Thread.currentThread().getContextClassLoader() != null
                        ? Thread.currentThread().getContextClassLoader()
                        : CacheBackendProducer.class.getClassLoader())) {
            if (pinned != null && pinned.equalsIgnoreCase(candidate.name())) {
                LOGGER.info(() -> "Using pinned CRUD cache backend: " + candidate.name());
                return candidate;
            }
            if ("local".equalsIgnoreCase(candidate.name())) {
                local = candidate;
            } else if (distributed == null) {
                distributed = candidate;
            }
        }

        if (distributed != null) {
            final CacheBackend chosen = distributed;
            LOGGER.info(() -> "Using distributed CRUD cache backend: " + chosen.name());
            return distributed;
        }
        if (local != null) {
            return local;
        }
        return new LocalBoundedCacheBackend();
    }
}
