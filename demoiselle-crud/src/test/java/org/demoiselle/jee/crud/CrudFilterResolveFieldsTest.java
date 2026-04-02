/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.demoiselle.jee.crud.entity.AddressModelForTest;
import org.demoiselle.jee.crud.entity.CountryModelForTest;
import org.demoiselle.jee.crud.entity.UserModelForTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the recursive field projection in {@link CrudFilter#resolveFields}.
 */
class CrudFilterResolveFieldsTest {

    private CrudFilter crudFilter;
    private ReflectionCache reflectionCache;

    @BeforeEach
    void setUp() {
        reflectionCache = new ReflectionCache();
        crudFilter = new CrudFilter(null, null, null, null, null, null, null, reflectionCache, null);
    }

    private UserModelForTest buildUser() {
        CountryModelForTest country = new CountryModelForTest();
        country.setId(1L);
        country.setName("Brazil");

        AddressModelForTest address = new AddressModelForTest();
        address.setId(10L);
        address.setAddress("Rua A");
        address.setStreet("Main St");
        address.setCountry(country);

        UserModelForTest user = new UserModelForTest();
        user.setId(100L);
        user.setName("Alice");
        user.setMail("alice@example.com");
        user.setAge(30);
        user.setAddress(address);
        return user;
    }

    @Test
    void resolveFieldsShouldExtractFirstLevelLeafFields() {
        UserModelForTest user = buildUser();

        // fields=name,mail
        TreeNodeField<String, Set<String>> root = new TreeNodeField<>("root", new HashSet<>());
        root.addChild("name", new HashSet<>());
        root.addChild("mail", new HashSet<>());

        Map<String, Object> result = crudFilter.resolveFields(user, root, UserModelForTest.class, 10);

        assertEquals(2, result.size());
        assertEquals("Alice", result.get("name"));
        assertEquals("alice@example.com", result.get("mail"));
        assertFalse(result.containsKey("id"));
        assertFalse(result.containsKey("age"));
    }

    @Test
    void resolveFieldsShouldExtractSecondLevelNestedFields() {
        UserModelForTest user = buildUser();

        // fields=address(street)
        TreeNodeField<String, Set<String>> root = new TreeNodeField<>("root", new HashSet<>());
        TreeNodeField<String, Set<String>> addressNode = root.addChild("address", new HashSet<>());
        addressNode.addChild("street", new HashSet<>());

        Map<String, Object> result = crudFilter.resolveFields(user, root, UserModelForTest.class, 10);

        assertEquals(1, result.size());
        assertTrue(result.get("address") instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> addressMap = (Map<String, Object>) result.get("address");
        assertEquals("Main St", addressMap.get("street"));
    }

    @Test
    void resolveFieldsShouldExtractThirdLevelNestedFields() {
        UserModelForTest user = buildUser();

        // fields=address(country(name))
        TreeNodeField<String, Set<String>> root = new TreeNodeField<>("root", new HashSet<>());
        TreeNodeField<String, Set<String>> addressNode = root.addChild("address", new HashSet<>());
        TreeNodeField<String, Set<String>> countryNode = addressNode.addChild("country", new HashSet<>());
        countryNode.addChild("name", new HashSet<>());

        Map<String, Object> result = crudFilter.resolveFields(user, root, UserModelForTest.class, 10);

        assertTrue(result.containsKey("address"));
        @SuppressWarnings("unchecked")
        Map<String, Object> addressMap = (Map<String, Object>) result.get("address");
        assertTrue(addressMap.containsKey("country"));
        @SuppressWarnings("unchecked")
        Map<String, Object> countryMap = (Map<String, Object>) addressMap.get("country");
        assertEquals("Brazil", countryMap.get("name"));
    }

    @Test
    void resolveFieldsShouldHandleNullNestedObject() {
        UserModelForTest user = new UserModelForTest();
        user.setId(1L);
        user.setName("Bob");
        // address is null

        // fields=name,address(street)
        TreeNodeField<String, Set<String>> root = new TreeNodeField<>("root", new HashSet<>());
        root.addChild("name", new HashSet<>());
        TreeNodeField<String, Set<String>> addressNode = root.addChild("address", new HashSet<>());
        addressNode.addChild("street", new HashSet<>());

        Map<String, Object> result = crudFilter.resolveFields(user, root, UserModelForTest.class, 10);

        assertEquals("Bob", result.get("name"));
        assertFalse(result.containsKey("address"), "Null nested objects should be omitted");
    }

    @Test
    void resolveFieldsShouldRespectMaxDepthLimit() {
        UserModelForTest user = buildUser();

        // fields=address(country(name)) but maxDepth=1
        TreeNodeField<String, Set<String>> root = new TreeNodeField<>("root", new HashSet<>());
        TreeNodeField<String, Set<String>> addressNode = root.addChild("address", new HashSet<>());
        TreeNodeField<String, Set<String>> countryNode = addressNode.addChild("country", new HashSet<>());
        countryNode.addChild("name", new HashSet<>());

        // maxDepth=1: can resolve address (depth 1) but not country inside address (would need depth 2)
        Map<String, Object> result = crudFilter.resolveFields(user, root, UserModelForTest.class, 1);

        assertTrue(result.containsKey("address"));
        @SuppressWarnings("unchecked")
        Map<String, Object> addressMap = (Map<String, Object>) result.get("address");
        // country is an intermediate node at depth 2, but maxDepth=1 means we only had 1 level of recursion
        // After first recursion (address), maxDepth becomes 0, so country (intermediate) is skipped
        assertFalse(addressMap.containsKey("country"), "Should not recurse beyond maxDepth");
    }

    @Test
    void resolveFieldsShouldHandleMixedLeafAndNestedFields() {
        UserModelForTest user = buildUser();

        // fields=name,address(street,country(name))
        TreeNodeField<String, Set<String>> root = new TreeNodeField<>("root", new HashSet<>());
        root.addChild("name", new HashSet<>());
        TreeNodeField<String, Set<String>> addressNode = root.addChild("address", new HashSet<>());
        addressNode.addChild("street", new HashSet<>());
        TreeNodeField<String, Set<String>> countryNode = addressNode.addChild("country", new HashSet<>());
        countryNode.addChild("name", new HashSet<>());

        Map<String, Object> result = crudFilter.resolveFields(user, root, UserModelForTest.class, 10);

        assertEquals("Alice", result.get("name"));
        @SuppressWarnings("unchecked")
        Map<String, Object> addressMap = (Map<String, Object>) result.get("address");
        assertEquals("Main St", addressMap.get("street"));
        @SuppressWarnings("unchecked")
        Map<String, Object> countryMap = (Map<String, Object>) addressMap.get("country");
        assertEquals("Brazil", countryMap.get("name"));
    }

    @Test
    void resolveFieldsShouldReturnEmptyMapForNonExistentField() {
        UserModelForTest user = buildUser();

        // fields=nonExistent
        TreeNodeField<String, Set<String>> root = new TreeNodeField<>("root", new HashSet<>());
        root.addChild("nonExistent", new HashSet<>());

        Map<String, Object> result = crudFilter.resolveFields(user, root, UserModelForTest.class, 10);

        assertTrue(result.isEmpty(), "Non-existent fields should be silently skipped");
    }

    @Test
    void resolveFieldsShouldReturnEmptyMapWhenNoChildrenRequested() {
        UserModelForTest user = buildUser();

        TreeNodeField<String, Set<String>> root = new TreeNodeField<>("root", new HashSet<>());
        // No children added

        Map<String, Object> result = crudFilter.resolveFields(user, root, UserModelForTest.class, 10);

        assertTrue(result.isEmpty());
    }
}
