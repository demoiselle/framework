/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import org.demoiselle.jee.crud.batch.BatchConfig;
import org.demoiselle.jee.crud.entity.UserModelForTest;
import org.demoiselle.jee.crud.specification.Specification;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Batch Operations — Properties 11-13.
 *
 * <p>Uses jqwik to generate arbitrary list sizes and Mockito to mock
 * EntityManager and BatchConfig. Verifications:</p>
 * <ul>
 *   <li>Property 11: persistAll returns list of same size, calls em.persist() for each entity</li>
 *   <li>Property 12: removeAll returns list size, calls remove() for each ID</li>
 *   <li>Property 13: updateAll returns count from executeUpdate()</li>
 * </ul>
 */
class BatchOperationsPropertyTest {

    // -----------------------------------------------------------------------
    // Concrete DAO subclass for testing
    // -----------------------------------------------------------------------

    static class TestDAO extends AbstractDAO<UserModelForTest, Long> {
        private final EntityManager em;

        TestDAO(EntityManager em) { this.em = em; }

        @Override
        protected EntityManager getEntityManager() { return em; }
    }

    // -----------------------------------------------------------------------
    // Helper: inject BatchConfig via reflection
    // -----------------------------------------------------------------------

    private static void injectBatchConfig(AbstractDAO<?, ?> dao, BatchConfig config) {
        try {
            Field batchField = AbstractDAO.class.getDeclaredField("batchConfig");
            batchField.setAccessible(true);
            batchField.set(dao, config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject BatchConfig", e);
        }
    }

    private static BatchConfig createBatchConfig(int size) {
        try {
            BatchConfig config = new BatchConfig();
            Field sizeField = BatchConfig.class.getDeclaredField("size");
            sizeField.setAccessible(true);
            sizeField.setInt(config, size);
            return config;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create BatchConfig", e);
        }
    }

    // -----------------------------------------------------------------------
    // Property 11: persistAll persiste todas as entidades
    // **Validates: Requirements 4.1**
    // -----------------------------------------------------------------------

    /**
     * For any list of entities (size 0-100), persistAll returns a list of the
     * same size and calls em.persist() for each entity.
     */
    @Property(tries = 100)
    void persistAllReturnsListOfSameSizeAndPersistsEach(
            @ForAll @IntRange(min = 0, max = 100) int listSize,
            @ForAll @IntRange(min = 1, max = 50) int batchSize) {

        EntityManager em = mock(EntityManager.class);
        TestDAO dao = new TestDAO(em);
        injectBatchConfig(dao, createBatchConfig(batchSize));

        List<UserModelForTest> entities = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            UserModelForTest u = new UserModelForTest();
            u.setName("user" + i);
            entities.add(u);
        }

        List<UserModelForTest> result = dao.persistAll(entities);

        // Property: returned list has same size as input
        assertEquals(listSize, result.size(),
                "persistAll must return a list of the same size as the input");

        // Property: em.persist() called for each entity
        for (UserModelForTest entity : entities) {
            verify(em).persist(entity);
        }

        // Property: all returned entities are the same objects as input
        for (int i = 0; i < listSize; i++) {
            assertSame(entities.get(i), result.get(i),
                    "Returned entity at index " + i + " must be the same object");
        }
    }

    // -----------------------------------------------------------------------
    // Property 12: removeAll remove e retorna contagem correta
    // **Validates: Requirements 4.2**
    // -----------------------------------------------------------------------

    /**
     * For any list of IDs (size 0-100), removeAll returns the list size and
     * calls remove() (via EntityManager) for each ID. Since UserModelForTest
     * has no @SoftDeletable, remove() delegates to em.remove(em.find(...)).
     */
    @Property(tries = 100)
    void removeAllReturnsListSizeAndRemovesEach(
            @ForAll @IntRange(min = 0, max = 100) int listSize,
            @ForAll @IntRange(min = 1, max = 50) int batchSize) {

        EntityManager em = mock(EntityManager.class);
        TestDAO dao = new TestDAO(em);
        injectBatchConfig(dao, createBatchConfig(batchSize));

        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            long id = i + 1L;
            ids.add(id);
            // Mock em.find() to return a non-null entity for each ID
            UserModelForTest entity = new UserModelForTest();
            entity.setId(id);
            when(em.find(UserModelForTest.class, id)).thenReturn(entity);
        }

        int removed = dao.removeAll(ids);

        // Property: return value equals list size
        assertEquals(listSize, removed,
                "removeAll must return the number of IDs in the list");

        // Property: em.remove() called for each ID (no @SoftDeletable → physical remove)
        for (Long id : ids) {
            verify(em).find(UserModelForTest.class, id);
            // em.remove() is called with the entity returned by em.find()
        }
        verify(em, times(listSize)).remove(any(UserModelForTest.class));
    }

    // -----------------------------------------------------------------------
    // Property 13: updateAll atualiza registros que satisfazem Specification
    // **Validates: Requirements 4.3**
    // -----------------------------------------------------------------------

    /**
     * For any update map (1-5 fields) and specification, updateAll returns the
     * count from executeUpdate(). Verifies that each field in the map is set
     * on the CriteriaUpdate and the spec predicate is applied as WHERE clause.
     */
    @SuppressWarnings("unchecked")
    @Property(tries = 100)
    void updateAllReturnsCountAndAppliesUpdatesAndSpec(
            @ForAll @IntRange(min = 1, max = 5) int fieldCount,
            @ForAll @IntRange(min = 0, max = 1000) int expectedCount) {

        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaUpdate<UserModelForTest> cu = mock(CriteriaUpdate.class);
        Root<UserModelForTest> root = mock(Root.class);
        Query query = mock(Query.class);
        Predicate specPredicate = mock(Predicate.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createCriteriaUpdate(UserModelForTest.class)).thenReturn(cu);
        when(cu.from(UserModelForTest.class)).thenReturn(root);
        when(em.createQuery(cu)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(expectedCount);

        // Build update map with fieldCount entries
        String[] fieldNames = {"name", "mail", "age", "id", "address"};
        Map<String, Object> updates = new LinkedHashMap<>();
        Map<String, Path<Object>> pathMocks = new HashMap<>();
        for (int i = 0; i < fieldCount; i++) {
            String fieldName = fieldNames[i];
            Path<Object> path = mock(Path.class, "path<" + fieldName + ">");
            pathMocks.put(fieldName, path);
            when(root.get(fieldName)).thenReturn(path);
            updates.put(fieldName, "value" + i);
        }

        Specification<UserModelForTest> spec = (r, q, c) -> specPredicate;

        int result = dao(em).updateAll(spec, updates);

        // Property: return value equals executeUpdate() result
        assertEquals(expectedCount, result,
                "updateAll must return the count from executeUpdate()");

        // Property: each field in the map is set on the CriteriaUpdate
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            Path<Object> path = pathMocks.get(entry.getKey());
            verify(cu).set(path, entry.getValue());
        }

        // Property: spec predicate is applied as WHERE clause
        verify(cu).where(specPredicate);
    }

    /**
     * For any update map with null specification, updateAll does not apply
     * a WHERE clause but still returns the count from executeUpdate().
     */
    @SuppressWarnings("unchecked")
    @Property(tries = 50)
    void updateAllWithNullSpecDoesNotApplyWhere(
            @ForAll @IntRange(min = 0, max = 500) int expectedCount) {

        EntityManager em = mock(EntityManager.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaUpdate<UserModelForTest> cu = mock(CriteriaUpdate.class);
        Root<UserModelForTest> root = mock(Root.class);
        Query query = mock(Query.class);
        Path<Object> namePath = mock(Path.class);

        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createCriteriaUpdate(UserModelForTest.class)).thenReturn(cu);
        when(cu.from(UserModelForTest.class)).thenReturn(root);
        when(root.get("name")).thenReturn(namePath);
        when(em.createQuery(cu)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(expectedCount);

        Map<String, Object> updates = Map.of("name", "updated");

        int result = dao(em).updateAll(null, updates);

        // Property: return value equals executeUpdate() result
        assertEquals(expectedCount, result);

        // Property: no WHERE clause applied when spec is null
        verify(cu, never()).where(any(Predicate.class));

        // Property: field is still set
        verify(cu).set(namePath, "updated");
    }

    // -----------------------------------------------------------------------
    // Helper: create DAO instance
    // -----------------------------------------------------------------------

    private TestDAO dao(EntityManager em) {
        return new TestDAO(em);
    }
}
