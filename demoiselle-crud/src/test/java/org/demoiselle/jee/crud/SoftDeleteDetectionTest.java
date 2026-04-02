/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import jakarta.persistence.EntityManager;

import org.demoiselle.jee.crud.annotation.SoftDeletable;
import org.demoiselle.jee.crud.exception.DemoiselleCrudException;
import org.demoiselle.jee.crud.entity.UserModelForTest;
import org.demoiselle.jee.crud.softdelete.SoftDeleteMeta;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for soft delete detection in AbstractDAO constructor (Task 7.3).
 *
 * Validates: Requirement 1.8
 */
class SoftDeleteDetectionTest {

    // --- Test entities ---

    @SoftDeletable(field = "deletedAt")
    static class SoftDeletableEntity {
        private Long id;
        private String name;
        private LocalDateTime deletedAt;
    }

    @SoftDeletable(field = "deleted", type = Boolean.class)
    static class SoftDeletableBooleanEntity {
        private Long id;
        private Boolean deleted;
    }

    @SoftDeletable(field = "deletedInstant", type = Instant.class)
    static class SoftDeletableInstantEntity {
        private Long id;
        private Instant deletedInstant;
    }

    @SoftDeletable(field = "nonExistentField")
    static class SoftDeletableInvalidFieldEntity {
        private Long id;
        private String name;
    }

    @SoftDeletable(field = "name", type = String.class)
    static class SoftDeletableUnsupportedTypeEntity {
        private Long id;
        private String name;
    }

    // Entity without @SoftDeletable (uses UserModelForTest from existing tests)

    // --- Concrete DAO subclasses for each entity ---

    static class SoftDeletableDAO extends AbstractDAO<SoftDeletableEntity, Long> {
        @Override protected EntityManager getEntityManager() { return null; }
    }

    static class SoftDeletableBooleanDAO extends AbstractDAO<SoftDeletableBooleanEntity, Long> {
        @Override protected EntityManager getEntityManager() { return null; }
    }

    static class SoftDeletableInstantDAO extends AbstractDAO<SoftDeletableInstantEntity, Long> {
        @Override protected EntityManager getEntityManager() { return null; }
    }

    static class NonSoftDeletableDAO extends AbstractDAO<UserModelForTest, Long> {
        @Override protected EntityManager getEntityManager() { return null; }
    }

    static class InvalidFieldDAO extends AbstractDAO<SoftDeletableInvalidFieldEntity, Long> {
        @Override protected EntityManager getEntityManager() { return null; }
    }

    static class UnsupportedTypeDAO extends AbstractDAO<SoftDeletableUnsupportedTypeEntity, Long> {
        @Override protected EntityManager getEntityManager() { return null; }
    }

    // --- Tests ---

    @Test
    void constructor_withSoftDeletableLocalDateTime_resolvesMeta() {
        SoftDeletableDAO dao = new SoftDeletableDAO();
        SoftDeleteMeta meta = dao.getSoftDeleteMeta();

        assertNotNull(meta);
        assertEquals("deletedAt", meta.fieldName());
        assertEquals(LocalDateTime.class, meta.fieldType());
        assertFalse(meta.isBoolean());
    }

    @Test
    void constructor_withSoftDeletableBoolean_resolvesMeta() {
        SoftDeletableBooleanDAO dao = new SoftDeletableBooleanDAO();
        SoftDeleteMeta meta = dao.getSoftDeleteMeta();

        assertNotNull(meta);
        assertEquals("deleted", meta.fieldName());
        assertEquals(Boolean.class, meta.fieldType());
        assertTrue(meta.isBoolean());
    }

    @Test
    void constructor_withSoftDeletableInstant_resolvesMeta() {
        SoftDeletableInstantDAO dao = new SoftDeletableInstantDAO();
        SoftDeleteMeta meta = dao.getSoftDeleteMeta();

        assertNotNull(meta);
        assertEquals("deletedInstant", meta.fieldName());
        assertEquals(Instant.class, meta.fieldType());
        assertFalse(meta.isBoolean());
    }

    @Test
    void constructor_withoutSoftDeletable_returnsNullMeta() {
        NonSoftDeletableDAO dao = new NonSoftDeletableDAO();
        assertNull(dao.getSoftDeleteMeta());
    }

    @Test
    void constructor_withInvalidField_throwsDemoiselleCrudException() {
        DemoiselleCrudException ex = assertThrows(DemoiselleCrudException.class,
                InvalidFieldDAO::new);
        assertTrue(ex.getMessage().contains("nonExistentField"));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void constructor_withUnsupportedType_throwsDemoiselleCrudException() {
        DemoiselleCrudException ex = assertThrows(DemoiselleCrudException.class,
                UnsupportedTypeDAO::new);
        assertTrue(ex.getMessage().contains("Unsupported"));
        assertTrue(ex.getMessage().contains("String"));
    }
}
