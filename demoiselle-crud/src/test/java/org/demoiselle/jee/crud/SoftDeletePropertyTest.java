/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import net.jqwik.api.*;

import org.demoiselle.jee.crud.annotation.SoftDeletable;
import org.demoiselle.jee.crud.softdelete.SoftDeleteMeta;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Soft Delete behavior — Properties 1-3.
 *
 * <p>Uses Mockito to mock JPA infrastructure (EntityManager, CriteriaBuilder,
 * CriteriaUpdate, CriteriaQuery, Root, etc.) and jqwik to generate arbitrary
 * soft delete field types. The key verifications are:</p>
 * <ul>
 *   <li>Property 1: remove() executes CriteriaUpdate (not EntityManager.remove()) and sets the appropriate value</li>
 *   <li>Property 2: find/find(id)/count apply the softDeletePredicate</li>
 *   <li>Property 3: findIncludingDeleted does NOT apply the softDeletePredicate</li>
 * </ul>
 *
 * <p><b>Validates: Requirements 1.1, 1.2, 1.3, 1.4, 1.6, 1.9</b></p>
 */
class SoftDeletePropertyTest {

    // -----------------------------------------------------------------------
    // Test entities for each supported soft delete type
    // -----------------------------------------------------------------------

    @SoftDeletable(field = "deletedAt")
    static class LocalDateTimeEntity {
        private Long id;
        private String name;
        private LocalDateTime deletedAt;
    }

    @SoftDeletable(field = "deleted", type = Boolean.class)
    static class BooleanEntity {
        private Long id;
        private Boolean deleted;
    }

    @SoftDeletable(field = "deletedInstant", type = Instant.class)
    static class InstantEntity {
        private Long id;
        private Instant deletedInstant;
    }

    // -----------------------------------------------------------------------
    // Concrete DAO subclasses
    // -----------------------------------------------------------------------

    static class LocalDateTimeDAO extends AbstractDAO<LocalDateTimeEntity, Long> {
        private final EntityManager em;
        LocalDateTimeDAO(EntityManager em) { this.em = em; }
        @Override protected EntityManager getEntityManager() { return em; }
    }

    static class BooleanDAO extends AbstractDAO<BooleanEntity, Long> {
        private final EntityManager em;
        BooleanDAO(EntityManager em) { this.em = em; }
        @Override protected EntityManager getEntityManager() { return em; }
    }

    static class InstantDAO extends AbstractDAO<InstantEntity, Long> {
        private final EntityManager em;
        InstantDAO(EntityManager em) { this.em = em; }
        @Override protected EntityManager getEntityManager() { return em; }
    }

    // -----------------------------------------------------------------------
    // Enum representing supported soft delete types for jqwik generation
    // -----------------------------------------------------------------------

    enum SoftDeleteType {
        LOCAL_DATE_TIME(LocalDateTime.class),
        BOOLEAN(Boolean.class),
        INSTANT(Instant.class);

        final Class<?> fieldType;
        SoftDeleteType(Class<?> fieldType) { this.fieldType = fieldType; }
    }

    // -----------------------------------------------------------------------
    // Helper: inject DemoiselleRequestContext via reflection
    // -----------------------------------------------------------------------

    private static void injectDrc(AbstractDAO<?, ?> dao, DemoiselleRequestContext drc) {
        try {
            Field drcField = AbstractDAO.class.getDeclaredField("drc");
            drcField.setAccessible(true);
            drcField.set(dao, drc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject DRC", e);
        }
    }

    private static DemoiselleRequestContext mockDrc() {
        DemoiselleRequestContext drc = mock(DemoiselleRequestContext.class);
        when(drc.getFilters()).thenReturn(null);
        when(drc.isPaginationEnabled()).thenReturn(false);
        when(drc.getSorts()).thenReturn(Collections.emptyList());
        return drc;
    }

    // -----------------------------------------------------------------------
    // Property 1: Soft delete marca registro em vez de remover fisicamente
    // **Validates: Requirements 1.1, 1.6**
    // -----------------------------------------------------------------------

    /**
     * For any supported soft delete type (LocalDateTime, Boolean, Instant) and
     * any positive ID, remove(id) must execute a CriteriaUpdate (not
     * EntityManager.remove()) and set the soft delete field with an appropriate
     * non-null value matching the configured type.
     */
    @Property(tries = 50)
    void removeExecutesCriteriaUpdateForAnySupportedType(
            @ForAll("softDeleteTypes") SoftDeleteType type,
            @ForAll("positiveIds") Long id) {

        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        CriteriaUpdate<Object> update = mock(CriteriaUpdate.class);
        @SuppressWarnings("unchecked")
        Root<Object> root = mock(Root.class);
        Query query = mock(Query.class);
        Map<String, Path<Object>> pathMocks = new HashMap<>();

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createCriteriaUpdate(any())).thenReturn(update);
        when(update.from(any(Class.class))).thenReturn(root);
        when(root.get(anyString())).thenAnswer(inv -> {
            String fieldName = inv.getArgument(0);
            return pathMocks.computeIfAbsent(fieldName, k -> {
                @SuppressWarnings("unchecked")
                Path<Object> p = mock(Path.class, "path<" + k + ">");
                return p;
            });
        });
        when(cb.equal(any(), (Object) any())).thenReturn(mock(Predicate.class));
        when(update.where(any(Predicate.class))).thenReturn(update);
        when(em.createQuery(update)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        // Create DAO and invoke remove
        switch (type) {
            case LOCAL_DATE_TIME -> {
                LocalDateTimeDAO dao = new LocalDateTimeDAO(em);
                dao.remove(id);
                Path<Object> fieldPath = pathMocks.get("deletedAt");
                assertNotNull(fieldPath, "deletedAt path must have been accessed");
                verify(update).set(eq(fieldPath), any(LocalDateTime.class));
            }
            case BOOLEAN -> {
                BooleanDAO dao = new BooleanDAO(em);
                dao.remove(id);
                Path<Object> fieldPath = pathMocks.get("deleted");
                assertNotNull(fieldPath, "deleted path must have been accessed");
                verify(update).set(fieldPath, Boolean.TRUE);
            }
            case INSTANT -> {
                InstantDAO dao = new InstantDAO(em);
                dao.remove(id);
                Path<Object> fieldPath = pathMocks.get("deletedInstant");
                assertNotNull(fieldPath, "deletedInstant path must have been accessed");
                verify(update).set(eq(fieldPath), any(Instant.class));
            }
        }

        // Common assertions: CriteriaUpdate was used, EntityManager.remove() was NOT
        verify(em, never()).remove(any());
        verify(em).createQuery(update);
        verify(query).executeUpdate();
    }

    /**
     * For any supported type, the SoftDeleteMeta resolved by the DAO must
     * correctly reflect the field name and type from the @SoftDeletable annotation.
     */
    @Property(tries = 30)
    void softDeleteMetaMatchesAnnotationForAnyType(
            @ForAll("softDeleteTypes") SoftDeleteType type) {

        EntityManager em = mock(EntityManager.class);

        switch (type) {
            case LOCAL_DATE_TIME -> {
                LocalDateTimeDAO dao = new LocalDateTimeDAO(em);
                SoftDeleteMeta meta = dao.getSoftDeleteMeta();
                assertNotNull(meta);
                assertEquals("deletedAt", meta.fieldName());
                assertEquals(LocalDateTime.class, meta.fieldType());
                assertFalse(meta.isBoolean());
            }
            case BOOLEAN -> {
                BooleanDAO dao = new BooleanDAO(em);
                SoftDeleteMeta meta = dao.getSoftDeleteMeta();
                assertNotNull(meta);
                assertEquals("deleted", meta.fieldName());
                assertEquals(Boolean.class, meta.fieldType());
                assertTrue(meta.isBoolean());
            }
            case INSTANT -> {
                InstantDAO dao = new InstantDAO(em);
                SoftDeleteMeta meta = dao.getSoftDeleteMeta();
                assertNotNull(meta);
                assertEquals("deletedInstant", meta.fieldName());
                assertEquals(Instant.class, meta.fieldType());
                assertFalse(meta.isBoolean());
            }
        }
    }

    // -----------------------------------------------------------------------
    // Property 2: Consultas excluem registros soft-deleted
    // **Validates: Requirements 1.2, 1.3, 1.9**
    // -----------------------------------------------------------------------

    /**
     * For any supported soft delete type, the softDeletePredicate must be applied
     * when calling find(id). For temporal types it produces cb.isNull(field),
     * for Boolean it produces cb.or(cb.isFalse(field), cb.isNull(field)).
     */
    @Property(tries = 50)
    void findByIdAppliesSoftDeletePredicateForAnyType(
            @ForAll("softDeleteTypes") SoftDeleteType type,
            @ForAll("positiveIds") Long id) {

        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        CriteriaQuery<Object> cq = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<Object> root = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Object> typedQuery = mock(TypedQuery.class);

        Predicate idPredicate = mock(Predicate.class, "idPred");
        Predicate softDeletePred = mock(Predicate.class, "softDeletePred");
        Predicate isFalsePred = mock(Predicate.class, "isFalsePred");
        Predicate isNullPred = mock(Predicate.class, "isNullPred");
        // Mock as Path<Boolean> so it satisfies both root.get() return type and cb.isFalse() param
        @SuppressWarnings("unchecked")
        Path<Boolean> softDeletePath = mock(Path.class, "softDeletePath");
        @SuppressWarnings("unchecked")
        Path<Object> idPath = mock(Path.class, "idPath");

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(any(Class.class))).thenReturn(cq);
        when(cq.from(any(Class.class))).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);
        when(cq.where(any(Predicate[].class))).thenReturn(cq);
        when(root.get(anyString())).thenAnswer(inv -> {
            String field = inv.getArgument(0);
            if ("id".equals(field)) return idPath;
            return softDeletePath;
        });
        when(cb.equal(any(), (Object) any())).thenReturn(idPredicate);
        when(cb.isNull(softDeletePath)).thenReturn(isNullPred);
        when(cb.isFalse(softDeletePath)).thenReturn(isFalsePred);
        when(cb.or(isFalsePred, isNullPred)).thenReturn(softDeletePred);
        when(em.createQuery(cq)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        switch (type) {
            case LOCAL_DATE_TIME -> {
                LocalDateTimeDAO dao = new LocalDateTimeDAO(em);
                dao.find(id);
                verify(cb).isNull(softDeletePath);
            }
            case BOOLEAN -> {
                BooleanDAO dao = new BooleanDAO(em);
                dao.find(id);
                verify(cb).isFalse(softDeletePath);
                verify(cb).isNull(softDeletePath);
                verify(cb).or(isFalsePred, isNullPred);
            }
            case INSTANT -> {
                InstantDAO dao = new InstantDAO(em);
                dao.find(id);
                verify(cb).isNull(softDeletePath);
            }
        }

        // In all cases, a CriteriaQuery was used (not EntityManager.find directly)
        verify(em).createQuery(cq);
        verify(em, never()).find(any(Class.class), any());
    }

    /**
     * For any supported soft delete type, count() must apply the soft delete
     * predicate to exclude soft-deleted records from the count.
     */
    @Property(tries = 50)
    void countAppliesSoftDeletePredicateForAnyType(
            @ForAll("softDeleteTypes") SoftDeleteType type) {

        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<Object> root = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Long> typedQuery = mock(TypedQuery.class);
        @SuppressWarnings("unchecked")
        Expression<Long> countExpr = mock(Expression.class);

        Predicate isNullPred = mock(Predicate.class, "isNullPred");
        Predicate isFalsePred = mock(Predicate.class, "isFalsePred");
        Predicate orPred = mock(Predicate.class, "orPred");
        // Mock as Path<Boolean> for type compatibility
        @SuppressWarnings("unchecked")
        Path<Boolean> softDeletePath = mock(Path.class, "softDeletePath");

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Long.class)).thenReturn(countQuery);
        when(countQuery.from(any(Class.class))).thenReturn(root);
        when(cb.count(root)).thenReturn(countExpr);
        when(countQuery.select(countExpr)).thenReturn(countQuery);
        when(countQuery.where(any(Predicate[].class))).thenReturn(countQuery);
        doReturn(softDeletePath).when(root).get(anyString());
        when(cb.isNull(softDeletePath)).thenReturn(isNullPred);
        when(cb.isFalse(softDeletePath)).thenReturn(isFalsePred);
        when(cb.or(isFalsePred, isNullPred)).thenReturn(orPred);
        when(em.createQuery(countQuery)).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(5L);

        switch (type) {
            case LOCAL_DATE_TIME -> {
                LocalDateTimeDAO dao = new LocalDateTimeDAO(em);
                injectDrc(dao, mockDrc());
                Long result = dao.count();
                verify(cb).isNull(softDeletePath);
                assertEquals(5L, result);
            }
            case BOOLEAN -> {
                BooleanDAO dao = new BooleanDAO(em);
                injectDrc(dao, mockDrc());
                Long result = dao.count();
                verify(cb).isFalse(softDeletePath);
                verify(cb).isNull(softDeletePath);
                verify(cb).or(isFalsePred, isNullPred);
                assertEquals(5L, result);
            }
            case INSTANT -> {
                InstantDAO dao = new InstantDAO(em);
                injectDrc(dao, mockDrc());
                Long result = dao.count();
                verify(cb).isNull(softDeletePath);
                assertEquals(5L, result);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Property 3: findIncludingDeleted retorna todos os registros
    // **Validates: Requirements 1.4**
    // -----------------------------------------------------------------------

    /**
     * For any supported soft delete type, findIncludingDeleted() must NOT apply
     * the soft delete predicate. It should return all records regardless of
     * deletion status. We verify that cb.isNull/cb.isFalse are never called.
     */
    @Property(tries = 50)
    void findIncludingDeletedDoesNotApplySoftDeletePredicate(
            @ForAll("softDeleteTypes") SoftDeleteType type) {

        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        CriteriaQuery<Object> cq = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<Object> root = mock(Root.class);
        @SuppressWarnings("unchecked")
        TypedQuery<Object> typedQuery = mock(TypedQuery.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(any(Class.class))).thenReturn(cq);
        when(cq.from(any(Class.class))).thenReturn(root);
        when(cq.select(root)).thenReturn(cq);
        when(cq.where(any(Predicate[].class))).thenReturn(cq);
        when(em.createQuery(cq)).thenReturn(typedQuery);

        List<Object> allRecords = List.of("record1", "record2", "record3");
        when(typedQuery.getResultList()).thenReturn(allRecords);

        switch (type) {
            case LOCAL_DATE_TIME -> {
                LocalDateTimeDAO dao = new LocalDateTimeDAO(em);
                injectDrc(dao, mockDrc());
                var result = dao.findIncludingDeleted();
                assertNotNull(result);
                assertEquals(3, result.getContent().size());
            }
            case BOOLEAN -> {
                BooleanDAO dao = new BooleanDAO(em);
                injectDrc(dao, mockDrc());
                var result = dao.findIncludingDeleted();
                assertNotNull(result);
                assertEquals(3, result.getContent().size());
            }
            case INSTANT -> {
                InstantDAO dao = new InstantDAO(em);
                injectDrc(dao, mockDrc());
                var result = dao.findIncludingDeleted();
                assertNotNull(result);
                assertEquals(3, result.getContent().size());
            }
        }

        // Verify soft delete predicate was NOT applied
        verify(cb, never()).isNull(any());
        verify(cb, never()).isFalse(any());
    }

    // -----------------------------------------------------------------------
    // Providers
    // -----------------------------------------------------------------------

    @Provide
    Arbitrary<SoftDeleteType> softDeleteTypes() {
        return Arbitraries.of(SoftDeleteType.values());
    }

    @Provide
    Arbitrary<Long> positiveIds() {
        return Arbitraries.longs().between(1L, 10_000L);
    }
}
