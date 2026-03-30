/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.lang.reflect.Field;
import java.util.Map;

import org.demoiselle.jee.crud.entity.AddressModelForTest;
import org.demoiselle.jee.crud.entity.UserModelForTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ReflectionCache}.
 */
class ReflectionCacheTest {

    private ReflectionCache cache;

    @BeforeEach
    void setUp() {
        cache = new ReflectionCache();
    }

    @Test
    void getFieldsShouldReturnAllDeclaredFieldsForClass() {
        Map<String, Field> fields = cache.getFields(UserModelForTest.class);

        assertNotNull(fields);
        assertTrue(fields.containsKey("id"));
        assertTrue(fields.containsKey("name"));
        assertTrue(fields.containsKey("mail"));
        assertTrue(fields.containsKey("age"));
        assertTrue(fields.containsKey("address"));
    }

    @Test
    void getFieldsShouldReturnSameReferenceOnSecondCall() {
        Map<String, Field> first = cache.getFields(UserModelForTest.class);
        Map<String, Field> second = cache.getFields(UserModelForTest.class);

        assertSame(first, second, "Cache should return the same map instance on repeated calls");
    }

    @Test
    void getFieldsShouldCacheDifferentClassesSeparately() {
        Map<String, Field> userFields = cache.getFields(UserModelForTest.class);
        Map<String, Field> addressFields = cache.getFields(AddressModelForTest.class);

        assertNotSame(userFields, addressFields);
        assertTrue(userFields.containsKey("name"));
        assertTrue(addressFields.containsKey("street"));
        assertFalse(addressFields.containsKey("name"));
    }

    @Test
    void getFieldsShouldReturnUnmodifiableMap() {
        Map<String, Field> fields = cache.getFields(UserModelForTest.class);

        assertThrows(UnsupportedOperationException.class, () -> fields.put("fake", null));
    }

    @Test
    void getFieldsShouldIncludeInheritedFields() {
        // ChildClass extends a parent — use a simple inner class to test hierarchy
        Map<String, Field> fields = cache.getFields(ChildEntity.class);

        assertTrue(fields.containsKey("parentField"), "Should include inherited field from parent");
        assertTrue(fields.containsKey("childField"), "Should include own field");
    }

    // Simple hierarchy for testing inherited fields
    static class ParentEntity {
        private String parentField;
    }

    static class ChildEntity extends ParentEntity {
        private String childField;
    }
}
