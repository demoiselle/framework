/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.lang.reflect.Field;
import java.util.Map;

import net.jqwik.api.*;

import org.demoiselle.jee.crud.ReflectionCache;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: jee-migration-v4, Property 10: Idempotência do cache de reflection
 *
 * Validates: Requirements 11.1, 11.2
 *
 * For any class, invoking getFields() twice on ReflectionCache should return
 * the same reference (cache hit). The cache is thread-safe and the result
 * of the second call must be identical to the first.
 */
class ReflectionCachePropertyTest {

    // Sample classes to feed the property test
    static class Alpha {
        private int x;
        private String y;
    }

    static class Beta extends Alpha {
        private double z;
        private boolean flag;
    }

    static class Gamma {
        private String name;
        private long id;
        private Object ref;
    }

    static class Delta extends Gamma {
        private float score;
    }

    static class Epsilon {
        private String a;
        private String b;
        private String c;
        private String d;
        private String e;
    }

    @Provide
    Arbitrary<Class<?>> entityClasses() {
        return Arbitraries.of(
                Alpha.class, Beta.class, Gamma.class, Delta.class, Epsilon.class,
                String.class, Integer.class, Object.class
        );
    }

    /**
     * P10: For any class, two consecutive calls to getFields() must return
     * the exact same object reference (cache hit).
     */
    @Property(tries = 100)
    @Tag("Feature_jee-migration-v4_Property-10_reflection-cache-idempotent")
    void getFieldsShouldReturnSameReferenceOnSecondCall(
            @ForAll("entityClasses") Class<?> clazz) {

        ReflectionCache cache = new ReflectionCache();

        Map<String, Field> first = cache.getFields(clazz);
        Map<String, Field> second = cache.getFields(clazz);

        // Same reference means cache hit
        assertSame(first, second,
                "getFields() should return the same reference on second call for " + clazz.getName());

        // Content must also be equal
        assertEquals(first, second,
                "getFields() results should be equal for " + clazz.getName());
    }

    /**
     * P10 (supplementary): Cache results should be consistent across
     * interleaved calls for different classes.
     */
    @Property(tries = 100)
    @Tag("Feature_jee-migration-v4_Property-10_reflection-cache-idempotent")
    void cacheIsConsistentAcrossMultipleClasses(
            @ForAll("entityClasses") Class<?> clazz1,
            @ForAll("entityClasses") Class<?> clazz2) {

        ReflectionCache cache = new ReflectionCache();

        Map<String, Field> first1 = cache.getFields(clazz1);
        Map<String, Field> first2 = cache.getFields(clazz2);
        Map<String, Field> second1 = cache.getFields(clazz1);
        Map<String, Field> second2 = cache.getFields(clazz2);

        assertSame(first1, second1,
                "Cache should return same reference for " + clazz1.getName());
        assertSame(first2, second2,
                "Cache should return same reference for " + clazz2.getName());
    }
}
