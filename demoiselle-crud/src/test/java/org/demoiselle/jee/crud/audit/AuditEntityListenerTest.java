/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.audit;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.crud.annotation.CreatedAt;
import org.demoiselle.jee.crud.annotation.CreatedBy;
import org.demoiselle.jee.crud.annotation.UpdatedAt;
import org.demoiselle.jee.crud.annotation.UpdatedBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AuditEntityListener} (Task 9.2).
 *
 * Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8
 */
class AuditEntityListenerTest {

    // --- Test entity ---

    static class AuditedEntity {
        @CreatedAt
        private LocalDateTime createdAt;

        @CreatedBy
        private String createdBy;

        @UpdatedAt
        private LocalDateTime updatedAt;

        @UpdatedBy
        private String updatedBy;

        private String name;
    }

    static class PartialAuditEntity {
        @CreatedAt
        private LocalDateTime createdAt;

        @UpdatedBy
        private String updatedBy;

        private String data;
    }

    static class NoAuditEntity {
        private Long id;
        private String name;
    }

    private AuditEntityListener listener;

    @BeforeEach
    void setUp() {
        listener = new AuditEntityListener();
    }

    // --- Helper to inject DemoiselleUser via reflection ---

    private void injectUser(DemoiselleUser user) throws Exception {
        Field userField = AuditEntityListener.class.getDeclaredField("demoiselleUser");
        userField.setAccessible(true);
        userField.set(listener, user);
    }

    // --- Minimal DemoiselleUser stub ---

    private DemoiselleUser stubUser(String identity) {
        return new DemoiselleUser() {
            @Override public String getIdentity() { return identity; }
            @Override public void setIdentity(String id) {}
            @Override public String getName() { return identity; }
            @Override public void setName(String name) {}
            @Override public void addRole(String role) {}
            @Override public void removeRole(String role) {}
            @Override public java.util.List<String> getRoles() { return java.util.List.of(); }
            @Override public java.util.Map<String, java.util.List<String>> getPermissions() { return java.util.Map.of(); }
            @Override public java.util.List<String> getPermissions(String resource) { return java.util.List.of(); }
            @Override public void addPermission(String resource, String operation) {}
            @Override public void removePermission(String resource, String operation) {}
            @Override public java.util.Map<String, String> getParams() { return java.util.Map.of(); }
            @Override public String getParams(String key) { return null; }
            @Override public void addParam(String key, String value) {}
            @Override public void removeParam(String key) {}
            @Override public DemoiselleUser clone() { return this; }
        };
    }

    // --- @PrePersist tests ---

    @Test
    void onPrePersist_fillsCreatedAtAndCreatedBy() throws Exception {
        injectUser(stubUser("admin"));
        AuditedEntity entity = new AuditedEntity();
        LocalDateTime before = LocalDateTime.now();

        listener.onPrePersist(entity);

        assertNotNull(entity.createdAt, "createdAt should be set");
        assertFalse(entity.createdAt.isBefore(before), "createdAt should be >= test start time");
        assertEquals("admin", entity.createdBy, "createdBy should be the user identity");
    }

    @Test
    void onPrePersist_doesNotFillUpdateFields() throws Exception {
        injectUser(stubUser("admin"));
        AuditedEntity entity = new AuditedEntity();

        listener.onPrePersist(entity);

        assertNull(entity.updatedAt, "updatedAt should remain null on persist");
        assertNull(entity.updatedBy, "updatedBy should remain null on persist");
    }

    // --- @PreUpdate tests ---

    @Test
    void onPreUpdate_fillsUpdatedAtAndUpdatedBy() throws Exception {
        injectUser(stubUser("editor"));
        AuditedEntity entity = new AuditedEntity();
        LocalDateTime before = LocalDateTime.now();

        listener.onPreUpdate(entity);

        assertNotNull(entity.updatedAt, "updatedAt should be set");
        assertFalse(entity.updatedAt.isBefore(before), "updatedAt should be >= test start time");
        assertEquals("editor", entity.updatedBy, "updatedBy should be the user identity");
    }

    @Test
    void onPreUpdate_doesNotFillCreationFields() throws Exception {
        injectUser(stubUser("editor"));
        AuditedEntity entity = new AuditedEntity();

        listener.onPreUpdate(entity);

        assertNull(entity.createdAt, "createdAt should remain null on update");
        assertNull(entity.createdBy, "createdBy should remain null on update");
    }

    @Test
    void onPreUpdate_doesNotAlterExistingCreationFields() throws Exception {
        injectUser(stubUser("editor"));
        AuditedEntity entity = new AuditedEntity();
        LocalDateTime originalCreatedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        entity.createdAt = originalCreatedAt;
        entity.createdBy = "original-user";

        listener.onPreUpdate(entity);

        assertEquals(originalCreatedAt, entity.createdAt, "createdAt should not be altered on update");
        assertEquals("original-user", entity.createdBy, "createdBy should not be altered on update");
    }

    // --- Fallback to "system" ---

    @Test
    void onPrePersist_fallsBackToSystem_whenUserIsNull() {
        // demoiselleUser is null (no injection)
        AuditedEntity entity = new AuditedEntity();

        listener.onPrePersist(entity);

        assertNotNull(entity.createdAt, "createdAt should still be set");
        assertEquals("system", entity.createdBy, "createdBy should fallback to 'system'");
    }

    @Test
    void onPrePersist_fallsBackToSystem_whenIdentityIsNull() throws Exception {
        injectUser(stubUser(null));
        AuditedEntity entity = new AuditedEntity();

        listener.onPrePersist(entity);

        assertEquals("system", entity.createdBy, "createdBy should fallback to 'system' when identity is null");
    }

    @Test
    void onPreUpdate_fallsBackToSystem_whenUserIsNull() {
        AuditedEntity entity = new AuditedEntity();

        listener.onPreUpdate(entity);

        assertNotNull(entity.updatedAt, "updatedAt should still be set");
        assertEquals("system", entity.updatedBy, "updatedBy should fallback to 'system'");
    }

    // --- Partial annotation tests ---

    @Test
    void onPrePersist_worksWithPartialAnnotations() throws Exception {
        injectUser(stubUser("admin"));
        PartialAuditEntity entity = new PartialAuditEntity();

        listener.onPrePersist(entity);

        assertNotNull(entity.createdAt, "createdAt should be set");
        assertNull(entity.updatedBy, "updatedBy should remain null on persist");
    }

    @Test
    void onPreUpdate_worksWithPartialAnnotations() throws Exception {
        injectUser(stubUser("admin"));
        PartialAuditEntity entity = new PartialAuditEntity();

        listener.onPreUpdate(entity);

        assertNull(entity.createdAt, "createdAt should not be touched on update");
        assertEquals("admin", entity.updatedBy, "updatedBy should be set on update");
    }

    // --- Entity without audit annotations ---

    @Test
    void onPrePersist_noOpForEntityWithoutAnnotations() throws Exception {
        injectUser(stubUser("admin"));
        NoAuditEntity entity = new NoAuditEntity();
        entity.name = "test";

        listener.onPrePersist(entity);

        assertEquals("test", entity.name, "Non-annotated fields should not be modified");
    }

    @Test
    void onPreUpdate_noOpForEntityWithoutAnnotations() throws Exception {
        injectUser(stubUser("admin"));
        NoAuditEntity entity = new NoAuditEntity();
        entity.name = "test";

        listener.onPreUpdate(entity);

        assertEquals("test", entity.name, "Non-annotated fields should not be modified");
    }

    // --- resolveUser tests ---

    @Test
    void resolveUser_returnsIdentity_whenAvailable() throws Exception {
        injectUser(stubUser("john.doe"));
        assertEquals("john.doe", listener.resolveUser());
    }

    @Test
    void resolveUser_returnsSystem_whenUserIsNull() {
        assertEquals("system", listener.resolveUser());
    }

    @Test
    void resolveUser_returnsSystem_whenIdentityIsNull() throws Exception {
        injectUser(stubUser(null));
        assertEquals("system", listener.resolveUser());
    }
}
