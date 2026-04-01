/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for defensive copy behavior in {@link DemoiselleUserImpl}.
 *
 * Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5
 */
class DemoiselleUserImplDefensiveCopyTest {

    private DemoiselleUserImpl user;

    @BeforeEach
    void setUp() {
        user = new DemoiselleUserImpl();
        user.init(); // simulate @PostConstruct
    }

    // --- Requirement 10.1: getRoles() returns defensive copy via List.copyOf() ---

    @Test
    void getRolesReturnsIndependentCopy() {
        user.addRole("admin");
        user.addRole("editor");

        List<String> snapshot = user.getRoles();

        // mutate internal state after snapshot
        user.addRole("viewer");

        assertEquals(2, snapshot.size(), "snapshot must not reflect later additions");
        assertFalse(snapshot.contains("viewer"));
    }

    @Test
    void getRolesReturnedListIsUnmodifiable() {
        user.addRole("admin");

        List<String> roles = user.getRoles();

        assertThrows(UnsupportedOperationException.class, () -> roles.add("hacker"));
    }

    // --- Requirement 10.2: getPermissions() returns deep copy ---

    @Test
    void getPermissionsReturnsDeepCopyIndependentOfInternalMap() {
        user.addPermission("resource1", "read");
        user.addPermission("resource1", "write");

        Map<String, List<String>> snapshot = user.getPermissions();

        // mutate internal state after snapshot
        user.addPermission("resource1", "delete");
        user.addPermission("resource2", "read");

        // snapshot should still have only original data
        assertEquals(1, snapshot.size(), "snapshot must not reflect new resource keys");
        assertEquals(2, snapshot.get("resource1").size(),
                "snapshot values must not reflect later additions");
        assertFalse(snapshot.get("resource1").contains("delete"));
    }

    @Test
    void getPermissionsValueListsAreUnmodifiable() {
        user.addPermission("res", "op1");

        Map<String, List<String>> perms = user.getPermissions();

        assertThrows(UnsupportedOperationException.class,
                () -> perms.get("res").add("hacker-op"));
    }

    @Test
    void getPermissionsMapIsUnmodifiable() {
        user.addPermission("res", "op1");

        Map<String, List<String>> perms = user.getPermissions();

        assertThrows(UnsupportedOperationException.class,
                () -> perms.put("evil", List.of("x")));
    }

    // --- Requirement 10.3: getParams() returns defensive copy via Map.copyOf() ---

    @Test
    void getParamsReturnsIndependentCopy() {
        user.addParam("token", "abc123");

        Map<String, String> snapshot = user.getParams();

        // mutate internal state after snapshot
        user.addParam("session", "xyz");

        assertEquals(1, snapshot.size(), "snapshot must not reflect later additions");
        assertFalse(snapshot.containsKey("session"));
    }

    @Test
    void getParamsReturnedMapIsUnmodifiable() {
        user.addParam("key", "value");

        Map<String, String> params = user.getParams();

        assertThrows(UnsupportedOperationException.class,
                () -> params.put("evil", "value"));
    }

    // --- Requirement 10.4: internal mutation after getRoles() doesn't affect snapshot ---

    @Test
    void removeRoleDoesNotAffectPreviousSnapshot() {
        user.addRole("admin");
        user.addRole("editor");

        List<String> snapshot = user.getRoles();

        user.removeRole("admin");

        assertEquals(2, snapshot.size(), "snapshot must not reflect removals");
        assertTrue(snapshot.contains("admin"));
    }

    // --- Requirement 10.5: addRole(null) is rejected ---

    @Test
    void addRoleNullThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> user.addRole(null));
    }
}
