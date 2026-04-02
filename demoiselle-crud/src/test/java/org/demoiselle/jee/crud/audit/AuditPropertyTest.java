/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.audit;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.jqwik.api.*;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.crud.annotation.CreatedAt;
import org.demoiselle.jee.crud.annotation.CreatedBy;
import org.demoiselle.jee.crud.annotation.UpdatedAt;
import org.demoiselle.jee.crud.annotation.UpdatedBy;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Audit EntityListener — Properties 4-5.
 *
 * <p>Uses jqwik to generate arbitrary user identities and verifies that
 * {@link AuditEntityListener} correctly populates audit fields during
 * persist and update lifecycle callbacks.</p>
 *
 * <ul>
 *   <li>Property 4: onPrePersist fills @CreatedAt/@CreatedBy, leaves @UpdatedAt/@UpdatedBy null</li>
 *   <li>Property 5: onPreUpdate fills @UpdatedAt/@UpdatedBy, leaves @CreatedAt/@CreatedBy unchanged</li>
 * </ul>
 */
class AuditPropertyTest {

    // --- Test entity with all four audit annotations ---

    static class AuditedEntity {
        @CreatedAt
        private LocalDateTime createdAt;

        @CreatedBy
        private String createdBy;

        @UpdatedAt
        private LocalDateTime updatedAt;

        @UpdatedBy
        private String updatedBy;
    }

    // --- Helper: inject DemoiselleUser via reflection ---

    private static void injectUser(AuditEntityListener listener, DemoiselleUser user) throws Exception {
        Field userField = AuditEntityListener.class.getDeclaredField("demoiselleUser");
        userField.setAccessible(true);
        userField.set(listener, user);
    }

    // --- Minimal DemoiselleUser stub ---

    private static DemoiselleUser stubUser(String identity) {
        return new DemoiselleUser() {
            @Override public String getIdentity() { return identity; }
            @Override public void setIdentity(String id) {}
            @Override public String getName() { return identity; }
            @Override public void setName(String name) {}
            @Override public void addRole(String role) {}
            @Override public void removeRole(String role) {}
            @Override public List<String> getRoles() { return List.of(); }
            @Override public Map<String, List<String>> getPermissions() { return Map.of(); }
            @Override public List<String> getPermissions(String resource) { return List.of(); }
            @Override public void addPermission(String resource, String operation) {}
            @Override public void removePermission(String resource, String operation) {}
            @Override public Map<String, String> getParams() { return Map.of(); }
            @Override public String getParams(String key) { return null; }
            @Override public void addParam(String key, String value) {}
            @Override public void removeParam(String key) {}
            @Override public DemoiselleUser clone() { return this; }
        };
    }

    // -----------------------------------------------------------------------
    // Property 4: Persist preenche apenas campos de criação
    // **Validates: Requirements 2.1, 2.3, 2.6**
    // -----------------------------------------------------------------------

    /**
     * For any user identity, onPrePersist fills @CreatedAt with a non-null
     * LocalDateTime and @CreatedBy with the user identity, while @UpdatedAt
     * and @UpdatedBy remain null.
     */
    @Property(tries = 100)
    void onPrePersistFillsOnlyCreationFields(
            @ForAll("userIdentities") String userIdentity) throws Exception {

        AuditEntityListener listener = new AuditEntityListener();
        injectUser(listener, stubUser(userIdentity));

        AuditedEntity entity = new AuditedEntity();
        LocalDateTime before = LocalDateTime.now();

        listener.onPrePersist(entity);

        // @CreatedAt must be filled with a non-null LocalDateTime
        assertNotNull(entity.createdAt, "createdAt must be non-null after persist");
        assertFalse(entity.createdAt.isBefore(before),
                "createdAt must be >= time before persist call");

        // @CreatedBy must be the user identity
        assertEquals(userIdentity, entity.createdBy,
                "createdBy must equal the user identity");

        // @UpdatedAt and @UpdatedBy must remain null
        assertNull(entity.updatedAt, "updatedAt must remain null on persist");
        assertNull(entity.updatedBy, "updatedBy must remain null on persist");
    }

    // -----------------------------------------------------------------------
    // Property 5: Merge preenche apenas campos de atualização
    // **Validates: Requirements 2.2, 2.4, 2.7**
    // -----------------------------------------------------------------------

    /**
     * For any user identity and any pre-existing creation values, onPreUpdate
     * fills @UpdatedAt with a non-null LocalDateTime and @UpdatedBy with the
     * user identity, while @CreatedAt and @CreatedBy retain their original values.
     */
    @Property(tries = 100)
    void onPreUpdateFillsOnlyUpdateFields(
            @ForAll("userIdentities") String userIdentity,
            @ForAll("preExistingCreatedBy") String originalCreatedBy,
            @ForAll("pastDateTimes") LocalDateTime originalCreatedAt) throws Exception {

        AuditEntityListener listener = new AuditEntityListener();
        injectUser(listener, stubUser(userIdentity));

        AuditedEntity entity = new AuditedEntity();
        entity.createdAt = originalCreatedAt;
        entity.createdBy = originalCreatedBy;

        LocalDateTime before = LocalDateTime.now();

        listener.onPreUpdate(entity);

        // @UpdatedAt must be filled with a non-null LocalDateTime
        assertNotNull(entity.updatedAt, "updatedAt must be non-null after update");
        assertFalse(entity.updatedAt.isBefore(before),
                "updatedAt must be >= time before update call");

        // @UpdatedBy must be the user identity
        assertEquals(userIdentity, entity.updatedBy,
                "updatedBy must equal the user identity");

        // @CreatedAt and @CreatedBy must retain their original values
        assertEquals(originalCreatedAt, entity.createdAt,
                "createdAt must remain unchanged after update");
        assertEquals(originalCreatedBy, entity.createdBy,
                "createdBy must remain unchanged after update");
    }

    // -----------------------------------------------------------------------
    // Providers
    // -----------------------------------------------------------------------

    @Provide
    Arbitrary<String> userIdentities() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(50)
                .map(s -> s.toLowerCase());
    }

    @Provide
    Arbitrary<String> preExistingCreatedBy() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(30);
    }

    @Provide
    Arbitrary<LocalDateTime> pastDateTimes() {
        return Arbitraries.longs()
                .between(1_000_000_000L, 1_700_000_000L)
                .map(epoch -> LocalDateTime.of(
                        2020 + (int) (epoch % 4),
                        1 + (int) (Math.abs(epoch / 100) % 12),
                        1 + (int) (Math.abs(epoch / 10000) % 28),
                        (int) (Math.abs(epoch / 1000000) % 24),
                        (int) (Math.abs(epoch / 100000000) % 60)
                ));
    }
}
