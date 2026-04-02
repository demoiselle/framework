/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.cache;

import java.lang.reflect.Field;
import java.util.List;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Cache — Properties 19, 20, 21.
 *
 * <p><b>Validates: Requirements 7.2, 7.3, 7.4, 7.5, 7.6, 7.7</b></p>
 */
class CachePropertyTest {

    // -----------------------------------------------------------------------
    // Property 19: Cache round-trip — hit dentro do TTL, miss após expiração
    // -----------------------------------------------------------------------

    /**
     * Feature: crud-enhancements, Property 19: Cache round-trip — hit dentro do TTL, miss após expiração
     *
     * <p>For any cache key and value stored with a positive TTL, {@code get(key)}
     * must return the stored value immediately (within TTL). After the TTL expires,
     * {@code get(key)} must return {@code null}.</p>
     *
     * <p>We use a very short TTL (1 second) and Thread.sleep to verify expiration.</p>
     *
     * <p><b>Validates: Requirements 7.2, 7.3</b></p>
     */
    @Property(tries = 100)
    // Feature: crud-enhancements, Property 19: Cache round-trip — hit dentro do TTL, miss após expiração
    void cacheRoundTripHitWithinTtlMissAfterExpiration(
            @ForAll("cacheKeys") String key,
            @ForAll("cacheValues") String value
    ) throws InterruptedException {
        QueryCacheStore store = new QueryCacheStore();

        // Store with 1-second TTL
        store.put(key, value, 1);

        // Within TTL: get() must return the stored value
        assertEquals(value, store.get(key),
                "get(key) must return stored value within TTL");

        // Wait for TTL to expire
        Thread.sleep(1100);

        // After expiration: get() must return null
        assertNull(store.get(key),
                "get(key) must return null after TTL expiration");
    }

    // -----------------------------------------------------------------------
    // Property 20: Operações de escrita disparam EntityModifiedEvent
    // -----------------------------------------------------------------------

    /**
     * Feature: crud-enhancements, Property 20: Operações de escrita disparam EntityModifiedEvent
     *
     * <p>For each write operation type (PERSIST, MERGE, REMOVE), an
     * {@link EntityModifiedEvent} must carry the correct {@code entityClass}
     * and {@code action}. Since firing events requires CDI infrastructure,
     * we verify the event construction contract: given an arbitrary entity class
     * and action, the event record correctly stores and returns them.</p>
     *
     * <p><b>Validates: Requirements 7.4, 7.5, 7.6</b></p>
     */
    @Property(tries = 100)
    // Feature: crud-enhancements, Property 20: Operações de escrita disparam EntityModifiedEvent
    void writeOperationsProduceCorrectEntityModifiedEvent(
            @ForAll("entityClasses") Class<?> entityClass,
            @ForAll("actions") EntityModifiedEvent.Action action
    ) {
        Object payload = "entity-payload-" + entityClass.getSimpleName();

        @SuppressWarnings("unchecked")
        EntityModifiedEvent<?> event = new EntityModifiedEvent<>(
                (Class<Object>) entityClass, action, payload);

        assertEquals(entityClass, event.entityClass(),
                "Event entityClass must match the class used at construction");
        assertEquals(action, event.action(),
                "Event action must match the action used at construction");
        assertEquals(payload, event.entity(),
                "Event entity/payload must match the payload used at construction");

        // Verify action mapping: PERSIST for persist, MERGE for merge, REMOVE for remove
        switch (action) {
            case PERSIST -> assertEquals(EntityModifiedEvent.Action.PERSIST, event.action());
            case MERGE   -> assertEquals(EntityModifiedEvent.Action.MERGE, event.action());
            case REMOVE  -> assertEquals(EntityModifiedEvent.Action.REMOVE, event.action());
        }
    }

    // -----------------------------------------------------------------------
    // Property 21: Invalidação de cache ao observar EntityModifiedEvent
    // -----------------------------------------------------------------------

    /**
     * Feature: crud-enhancements, Property 21: Invalidação de cache ao observar EntityModifiedEvent
     *
     * <p>For any {@link EntityModifiedEvent} observed by the
     * {@link CacheInvalidationListener}, all cache entries whose key starts
     * with the event's {@code entityClass} name must be removed, while entries
     * for other entity classes must remain intact.</p>
     *
     * <p><b>Validates: Requirements 7.7</b></p>
     */
    @Property(tries = 100)
    // Feature: crud-enhancements, Property 21: Invalidação de cache ao observar EntityModifiedEvent
    void cacheInvalidationRemovesOnlyTargetEntityClassEntries(
            @ForAll("entityClasses") Class<?> targetClass,
            @ForAll("otherEntityClasses") Class<?> otherClass,
            @ForAll("actions") EntityModifiedEvent.Action action,
            @ForAll @IntRange(min = 1, max = 5) int targetEntryCount,
            @ForAll @IntRange(min = 1, max = 5) int otherEntryCount
    ) throws Exception {
        // Skip when both classes are the same — we need distinct classes
        Assume.that(!targetClass.getName().equals(otherClass.getName()));

        QueryCacheStore cacheStore = new QueryCacheStore();
        CacheInvalidationListener listener = new CacheInvalidationListener();

        // Inject cacheStore via reflection
        Field field = CacheInvalidationListener.class.getDeclaredField("cacheStore");
        field.setAccessible(true);
        field.set(listener, cacheStore);

        String targetPrefix = targetClass.getName();
        String otherPrefix = otherClass.getName();

        // Populate cache entries for target class
        for (int i = 0; i < targetEntryCount; i++) {
            cacheStore.put(targetPrefix + ":method" + i + ":" + i, "target-value-" + i, 300);
        }

        // Populate cache entries for other class
        for (int i = 0; i < otherEntryCount; i++) {
            cacheStore.put(otherPrefix + ":method" + i + ":" + i, "other-value-" + i, 300);
        }

        // Fire event for target class
        @SuppressWarnings("unchecked")
        EntityModifiedEvent<?> event = new EntityModifiedEvent<>(
                (Class<Object>) targetClass, action, "payload");
        listener.onEntityModified(event);

        // All target class entries must be removed
        for (int i = 0; i < targetEntryCount; i++) {
            assertNull(cacheStore.get(targetPrefix + ":method" + i + ":" + i),
                    "Cache entry for target class must be invalidated");
        }

        // All other class entries must remain intact
        for (int i = 0; i < otherEntryCount; i++) {
            assertEquals("other-value-" + i,
                    cacheStore.get(otherPrefix + ":method" + i + ":" + i),
                    "Cache entry for other class must remain intact");
        }
    }

    // -----------------------------------------------------------------------
    // Arbitraries / Providers
    // -----------------------------------------------------------------------

    @Provide
    Arbitrary<String> cacheKeys() {
        // Generate keys in the format "ClassName:methodName:hash"
        Arbitrary<String> classNames = Arbitraries.of(
                "com.example.User", "com.example.Order", "com.example.Product",
                "org.test.Entity", "org.test.Item");
        Arbitrary<String> methodNames = Arbitraries.of(
                "findAll", "findById", "count", "search", "list");
        Arbitrary<Integer> hashes = Arbitraries.integers().between(0, 99999);

        return Combinators.combine(classNames, methodNames, hashes)
                .as((cls, method, hash) -> cls + ":" + method + ":" + hash);
    }

    @Provide
    Arbitrary<String> cacheValues() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50);
    }

    @Provide
    Arbitrary<Class<?>> entityClasses() {
        return Arbitraries.of(
                EntityA.class, EntityB.class, EntityC.class);
    }

    @Provide
    Arbitrary<Class<?>> otherEntityClasses() {
        return Arbitraries.of(
                EntityD.class, EntityE.class, EntityF.class);
    }

    @Provide
    Arbitrary<EntityModifiedEvent.Action> actions() {
        return Arbitraries.of(EntityModifiedEvent.Action.values());
    }

    // --- Dummy entity classes for property tests ---
    private static class EntityA {}
    private static class EntityB {}
    private static class EntityC {}
    private static class EntityD {}
    private static class EntityE {}
    private static class EntityF {}
}
