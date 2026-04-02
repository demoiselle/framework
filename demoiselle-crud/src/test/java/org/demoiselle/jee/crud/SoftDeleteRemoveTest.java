/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.demoiselle.jee.crud.annotation.SoftDeletable;
import org.demoiselle.jee.crud.entity.UserModelForTest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for soft delete behavior in AbstractDAO.remove() (Task 7.4).
 *
 * Validates: Requirements 1.1, 1.7
 */
class SoftDeleteRemoveTest {

    // --- Test entities ---

    @SoftDeletable(field = "deletedAt")
    static class SoftDeleteLocalDateTimeEntity {
        private Long id;
        private String name;
        private LocalDateTime deletedAt;
    }

    @SoftDeletable(field = "deleted", type = Boolean.class)
    static class SoftDeleteBooleanEntity {
        private Long id;
        private Boolean deleted;
    }

    @SoftDeletable(field = "deletedInstant", type = Instant.class)
    static class SoftDeleteInstantEntity {
        private Long id;
        private Instant deletedInstant;
    }

    // --- Concrete DAO subclasses ---

    static class LocalDateTimeDAO extends AbstractDAO<SoftDeleteLocalDateTimeEntity, Long> {
        private final EntityManager em;
        LocalDateTimeDAO(EntityManager em) { this.em = em; }
        @Override protected EntityManager getEntityManager() { return em; }
    }

    static class BooleanDAO extends AbstractDAO<SoftDeleteBooleanEntity, Long> {
        private final EntityManager em;
        BooleanDAO(EntityManager em) { this.em = em; }
        @Override protected EntityManager getEntityManager() { return em; }
    }

    static class InstantDAO extends AbstractDAO<SoftDeleteInstantEntity, Long> {
        private final EntityManager em;
        InstantDAO(EntityManager em) { this.em = em; }
        @Override protected EntityManager getEntityManager() { return em; }
    }

    static class NonSoftDeleteDAO extends AbstractDAO<UserModelForTest, Long> {
        private final EntityManager em;
        NonSoftDeleteDAO(EntityManager em) { this.em = em; }
        @Override protected EntityManager getEntityManager() { return em; }
    }

    // --- Helper to set up CriteriaUpdate mocks ---

    @SuppressWarnings("unchecked")
    private <T> MockContext<T> setupCriteriaUpdateMocks(EntityManager em, Class<T> entityClass) {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaUpdate<T> update = mock(CriteriaUpdate.class);
        Root<T> root = mock(Root.class);
        Query query = mock(Query.class);
        Map<String, Path<Object>> pathMocks = new HashMap<>();

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createCriteriaUpdate(entityClass)).thenReturn(update);
        when(update.from(entityClass)).thenReturn(root);
        when(root.get(anyString())).thenAnswer(invocation -> {
            String fieldName = invocation.getArgument(0);
            return pathMocks.computeIfAbsent(fieldName, k -> {
                Path<Object> p = mock(Path.class, "path<" + k + ">");
                return p;
            });
        });
        when(cb.equal(any(), (Object) any())).thenReturn(mock(Predicate.class));
        when(update.where(any(Predicate.class))).thenReturn(update);
        when(em.createQuery(update)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        return new MockContext<>(cb, update, root, query, pathMocks);
    }

    record MockContext<T>(
            CriteriaBuilder cb,
            CriteriaUpdate<T> update,
            Root<T> root,
            Query query,
            Map<String, Path<Object>> pathMocks
    ) {}

    // --- Tests ---

    @Test
    void remove_withSoftDeletableLocalDateTime_executesCriteriaUpdate() {
        EntityManager em = mock(EntityManager.class);
        MockContext<SoftDeleteLocalDateTimeEntity> ctx = setupCriteriaUpdateMocks(em, SoftDeleteLocalDateTimeEntity.class);

        LocalDateTimeDAO dao = new LocalDateTimeDAO(em);
        dao.remove(42L);

        // Should execute CriteriaUpdate, NOT EntityManager.remove()
        verify(em, never()).remove(any());
        verify(em).createQuery(ctx.update());
        verify(ctx.query()).executeUpdate();

        // Should set the deletedAt field with a LocalDateTime value
        Path<Object> deletedAtPath = ctx.pathMocks().get("deletedAt");
        verify(ctx.update()).set(eq(deletedAtPath), any(LocalDateTime.class));
    }

    @Test
    void remove_withSoftDeletableBoolean_setsTrueValue() {
        EntityManager em = mock(EntityManager.class);
        MockContext<SoftDeleteBooleanEntity> ctx = setupCriteriaUpdateMocks(em, SoftDeleteBooleanEntity.class);

        BooleanDAO dao = new BooleanDAO(em);
        dao.remove(10L);

        verify(em, never()).remove(any());
        verify(em).createQuery(ctx.update());
        verify(ctx.query()).executeUpdate();

        // Should set the deleted field to Boolean.TRUE
        Path<Object> deletedPath = ctx.pathMocks().get("deleted");
        verify(ctx.update()).set(deletedPath, Boolean.TRUE);
    }

    @Test
    void remove_withSoftDeletableInstant_executesCriteriaUpdate() {
        EntityManager em = mock(EntityManager.class);
        MockContext<SoftDeleteInstantEntity> ctx = setupCriteriaUpdateMocks(em, SoftDeleteInstantEntity.class);

        InstantDAO dao = new InstantDAO(em);
        dao.remove(7L);

        verify(em, never()).remove(any());
        verify(em).createQuery(ctx.update());
        verify(ctx.query()).executeUpdate();

        // Should set the deletedInstant field with an Instant value
        Path<Object> deletedInstantPath = ctx.pathMocks().get("deletedInstant");
        verify(ctx.update()).set(eq(deletedInstantPath), any(Instant.class));
    }

    @Test
    void remove_withoutSoftDeletable_callsEntityManagerRemove() {
        EntityManager em = mock(EntityManager.class);
        UserModelForTest entity = new UserModelForTest();
        when(em.find(UserModelForTest.class, 5L)).thenReturn(entity);

        NonSoftDeleteDAO dao = new NonSoftDeleteDAO(em);
        dao.remove(5L);

        // Should use traditional EntityManager.remove()
        verify(em).find(UserModelForTest.class, 5L);
        verify(em).remove(entity);
        // Should NOT create any CriteriaUpdate
        verify(em, never()).getCriteriaBuilder();
    }
}
