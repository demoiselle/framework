/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link QueryCacheStore} (Task 13.3).
 *
 * Validates: Requirements 7.8
 */
class QueryCacheStoreTest {

    private QueryCacheStore store;

    @BeforeEach
    void setUp() {
        store = new QueryCacheStore();
    }

    // --- get / put round-trip ---

    @Test
    void get_returnsNull_whenKeyDoesNotExist() {
        assertNull(store.get("nonexistent:key"));
    }

    @Test
    void put_and_get_returnsStoredValue() {
        store.put("com.example.Entity:findAll:123", "result-data", 300);
        assertEquals("result-data", store.get("com.example.Entity:findAll:123"));
    }

    @Test
    void put_overwritesExistingEntry() {
        store.put("key1", "value1", 300);
        store.put("key1", "value2", 300);
        assertEquals("value2", store.get("key1"));
    }

    // --- Lazy expiration ---

    @Test
    void get_returnsNull_afterTtlExpires() throws InterruptedException {
        store.put("expiring:key", "data", 1); // 1 second TTL
        // Value should be available immediately
        assertEquals("data", store.get("expiring:key"));

        // Wait for expiration
        Thread.sleep(1100);

        assertNull(store.get("expiring:key"), "Should return null after TTL expires");
    }

    @Test
    void get_returnsValue_withinTtl() {
        store.put("valid:key", "data", 60); // 60 seconds TTL
        assertNotNull(store.get("valid:key"));
    }

    @Test
    void get_removesExpiredEntry_onAccess() throws InterruptedException {
        store.put("lazy:key", "data", 1);
        Thread.sleep(1100);

        // First access triggers lazy removal
        assertNull(store.get("lazy:key"));
        // Second access also returns null (entry was removed)
        assertNull(store.get("lazy:key"));
    }

    // --- invalidateByEntityClass ---

    @Test
    void invalidateByEntityClass_removesMatchingEntries() {
        String userPrefix = User.class.getName();
        String orderPrefix = Order.class.getName();

        store.put(userPrefix + ":findAll:111", "users", 300);
        store.put(userPrefix + ":findById:222", "user1", 300);
        store.put(orderPrefix + ":findAll:333", "orders", 300);

        store.invalidateByEntityClass(User.class);

        assertNull(store.get(userPrefix + ":findAll:111"));
        assertNull(store.get(userPrefix + ":findById:222"));
        assertEquals("orders", store.get(orderPrefix + ":findAll:333"));
    }

    @Test
    void invalidateByEntityClass_noOpWhenNoMatchingEntries() {
        String orderPrefix = Order.class.getName();
        store.put(orderPrefix + ":findAll:333", "orders", 300);

        store.invalidateByEntityClass(User.class);

        assertEquals("orders", store.get(orderPrefix + ":findAll:333"));
    }

    @Test
    void invalidateByEntityClass_doesNotRemovePartialPrefixMatch() {
        String userRolePrefix = UserRole.class.getName();
        String userPrefix = User.class.getName();

        store.put(userRolePrefix + ":findAll:111", "roles", 300);
        store.put(userPrefix + ":findAll:222", "users", 300);

        store.invalidateByEntityClass(User.class);

        // UserRole entries should remain because prefix includes colon separator
        assertEquals("roles", store.get(userRolePrefix + ":findAll:111"));
        assertNull(store.get(userPrefix + ":findAll:222"));
    }

    @Test
    void invalidateByEntityClass_handlesEmptyCache() {
        // Should not throw
        assertDoesNotThrow(() -> store.invalidateByEntityClass(User.class));
    }

    // --- Edge cases ---

    @Test
    void put_withZeroTtl_expiresImmediately() throws InterruptedException {
        store.put("zero:ttl", "data", 0);
        // With 0 TTL, expiresAt = currentTimeMillis, so it may or may not be expired
        // immediately depending on timing. After a small delay it should be expired.
        Thread.sleep(10);
        assertNull(store.get("zero:ttl"));
    }

    @Test
    void put_acceptsNullValue() {
        store.put("null:value", null, 300);
        // get returns null for the value, which is indistinguishable from "not found"
        // This is acceptable behavior — callers should avoid caching null
        assertNull(store.get("null:value"));
    }

    @Test
    void get_and_put_workWithVariousValueTypes() {
        store.put("int:key", 42, 300);
        store.put("list:key", java.util.List.of("a", "b"), 300);

        assertEquals(42, store.get("int:key"));
        assertEquals(java.util.List.of("a", "b"), store.get("list:key"));
    }

    // --- Dummy classes for invalidateByEntityClass key matching ---

    private static class User {}
    private static class UserRole {}
    private static class Order {}
}
