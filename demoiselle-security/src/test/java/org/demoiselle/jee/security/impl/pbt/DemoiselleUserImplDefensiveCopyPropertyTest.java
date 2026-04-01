/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl.pbt;

import java.util.List;
import java.util.Map;

import net.jqwik.api.*;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.Size;

import org.demoiselle.jee.security.impl.DemoiselleUserImpl;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for defensive copy independence in {@link DemoiselleUserImpl}.
 *
 * <p><b>Validates: Requirements 10.1, 10.2, 10.3, 10.4</b></p>
 *
 * <p>Property 5: For any DemoiselleUserImpl with roles/permissions/params,
 * returned collections are independent of internal ones — subsequent internal
 * mutations do not affect previously obtained copies.</p>
 */
class DemoiselleUserImplDefensiveCopyPropertyTest {

    private DemoiselleUserImpl createUser() {
        DemoiselleUserImpl user = new DemoiselleUserImpl();
        user.init(); // simulate @PostConstruct
        return user;
    }

    @Provide
    Arbitrary<String> nonNullRoles() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30);
    }

    @Provide
    Arbitrary<String> extraRoles() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30);
    }

    @Provide
    Arbitrary<String> paramKeys() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> paramValues() {
        return Arbitraries.strings().ofMinLength(0).ofMaxLength(50);
    }

    /**
     * Property 5 (roles): For any set of roles added to a DemoiselleUserImpl,
     * a snapshot via getRoles() is not affected by subsequent addRole() calls.
     *
     * <p><b>Validates: Requirements 10.1, 10.4</b></p>
     */
    @Property(tries = 200)
    void rolesSnapshotIsIndependentOfSubsequentMutations(
            @ForAll @Size(min = 1, max = 10) List<@From("nonNullRoles") String> initialRoles,
            @ForAll @Size(min = 1, max = 5) List<@From("extraRoles") String> extraRoles) {

        DemoiselleUserImpl user = createUser();

        // Add initial roles
        for (String role : initialRoles) {
            user.addRole(role);
        }

        // Take snapshot
        List<String> snapshot = user.getRoles();
        List<String> snapshotCopy = List.copyOf(snapshot);

        // Mutate internal state by adding more roles
        for (String role : extraRoles) {
            user.addRole(role);
        }

        // Snapshot must remain unchanged
        assertEquals(snapshotCopy, snapshot,
                "getRoles() snapshot must not be affected by subsequent addRole() calls");
    }

    /**
     * Property 5 (permissions): For any set of permissions added to a DemoiselleUserImpl,
     * a snapshot via getPermissions() is not affected by subsequent addPermission() calls.
     *
     * <p><b>Validates: Requirements 10.2</b></p>
     */
    @Property(tries = 200)
    void permissionsSnapshotIsIndependentOfSubsequentMutations(
            @ForAll @Size(min = 1, max = 5) List<@From("nonNullRoles") String> resources,
            @ForAll @Size(min = 1, max = 5) List<@From("nonNullRoles") String> operations) {

        DemoiselleUserImpl user = createUser();

        // Add initial permissions
        for (String resource : resources) {
            for (String operation : operations) {
                user.addPermission(resource, operation);
            }
        }

        // Take snapshot
        Map<String, List<String>> snapshot = user.getPermissions();
        // Deep copy the snapshot for comparison
        Map<String, List<String>> snapshotCopy = snapshot.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> List.copyOf(e.getValue())
                ));

        // Mutate internal state by adding more permissions
        user.addPermission("newResource", "newOperation");
        for (String resource : resources) {
            user.addPermission(resource, "extraOp");
        }

        // Snapshot must remain unchanged
        assertEquals(snapshotCopy, snapshot,
                "getPermissions() snapshot must not be affected by subsequent addPermission() calls");
    }

    /**
     * Property 5 (params): For any set of params added to a DemoiselleUserImpl,
     * a snapshot via getParams() is not affected by subsequent addParam() calls.
     *
     * <p><b>Validates: Requirements 10.3</b></p>
     */
    @Property(tries = 200)
    void paramsSnapshotIsIndependentOfSubsequentMutations(
            @ForAll @Size(min = 1, max = 10) List<@From("paramKeys") String> keys,
            @ForAll @Size(min = 1, max = 10) List<@From("paramValues") String> values) {

        DemoiselleUserImpl user = createUser();

        // Add initial params (pair keys with values, cycling if needed)
        for (int i = 0; i < keys.size(); i++) {
            user.addParam(keys.get(i), values.get(i % values.size()));
        }

        // Take snapshot
        Map<String, String> snapshot = user.getParams();
        Map<String, String> snapshotCopy = Map.copyOf(snapshot);

        // Mutate internal state by adding more params
        user.addParam("extraKey", "extraValue");

        // Snapshot must remain unchanged
        assertEquals(snapshotCopy, snapshot,
                "getParams() snapshot must not be affected by subsequent addParam() calls");
    }
}
