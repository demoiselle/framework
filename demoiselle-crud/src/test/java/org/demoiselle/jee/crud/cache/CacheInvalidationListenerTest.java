/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CacheInvalidationListener} (Task 13.5).
 *
 * Validates: Requirements 7.7
 */
class CacheInvalidationListenerTest {

    private CacheInvalidationListener listener;
    private QueryCacheStore cacheStore;

    @BeforeEach
    void setUp() throws Exception {
        cacheStore = new QueryCacheStore();
        listener = new CacheInvalidationListener();

        // Inject cacheStore via reflection (no CDI container in unit tests)
        Field field = CacheInvalidationListener.class.getDeclaredField("cacheStore");
        field.setAccessible(true);
        field.set(listener, cacheStore);
    }

    @Test
    void onEntityModified_invalidatesCacheEntriesForEntityClass() {
        String userPrefix = User.class.getName();
        cacheStore.put(userPrefix + ":findAll:111", "users-list", 300);
        cacheStore.put(userPrefix + ":findById:222", "single-user", 300);

        EntityModifiedEvent<User> event = new EntityModifiedEvent<>(
                User.class, EntityModifiedEvent.Action.PERSIST, new User());

        listener.onEntityModified(event);

        assertNull(cacheStore.get(userPrefix + ":findAll:111"));
        assertNull(cacheStore.get(userPrefix + ":findById:222"));
    }

    @Test
    void onEntityModified_doesNotInvalidateOtherEntityClasses() {
        String userPrefix = User.class.getName();
        String orderPrefix = Order.class.getName();

        cacheStore.put(userPrefix + ":findAll:111", "users", 300);
        cacheStore.put(orderPrefix + ":findAll:222", "orders", 300);

        EntityModifiedEvent<User> event = new EntityModifiedEvent<>(
                User.class, EntityModifiedEvent.Action.MERGE, new User());

        listener.onEntityModified(event);

        assertNull(cacheStore.get(userPrefix + ":findAll:111"));
        assertEquals("orders", cacheStore.get(orderPrefix + ":findAll:222"));
    }

    @Test
    void onEntityModified_handlesRemoveAction() {
        String userPrefix = User.class.getName();
        cacheStore.put(userPrefix + ":findAll:100", "cached-data", 300);

        EntityModifiedEvent<User> event = new EntityModifiedEvent<>(
                User.class, EntityModifiedEvent.Action.REMOVE, 1L);

        listener.onEntityModified(event);

        assertNull(cacheStore.get(userPrefix + ":findAll:100"));
    }

    @Test
    void onEntityModified_handlesEmptyCache() {
        EntityModifiedEvent<User> event = new EntityModifiedEvent<>(
                User.class, EntityModifiedEvent.Action.PERSIST, new User());

        assertDoesNotThrow(() -> listener.onEntityModified(event));
    }

    @Test
    void onEntityModified_invalidatesAllActionsForSameEntityClass() {
        String userPrefix = User.class.getName();
        cacheStore.put(userPrefix + ":findAll:1", "data1", 300);
        cacheStore.put(userPrefix + ":count:2", "data2", 300);
        cacheStore.put(userPrefix + ":findById:3", "data3", 300);

        listener.onEntityModified(new EntityModifiedEvent<>(
                User.class, EntityModifiedEvent.Action.PERSIST, new User()));

        assertNull(cacheStore.get(userPrefix + ":findAll:1"));
        assertNull(cacheStore.get(userPrefix + ":count:2"));
        assertNull(cacheStore.get(userPrefix + ":findById:3"));
    }

    // --- Dummy entity classes ---

    private static class User {}
    private static class Order {}
}
