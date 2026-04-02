/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.cache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * CDI observer that listens for {@link EntityModifiedEvent} and invalidates
 * all cached query results associated with the modified entity's class.
 *
 * <p>This ensures that any cached queries for a given entity type are evicted
 * whenever a persist, merge, or remove operation occurs on that type.</p>
 *
 * <p>Validates: Requirements 7.7</p>
 */
@ApplicationScoped
public class CacheInvalidationListener {

    @Inject
    private QueryCacheStore cacheStore;

    /**
     * Observes entity modification events and invalidates all cache entries
     * associated with the entity class of the event.
     *
     * @param event the entity modification event
     */
    public void onEntityModified(@Observes EntityModifiedEvent<?> event) {
        cacheStore.invalidateByEntityClass(event.entityClass());
    }
}
