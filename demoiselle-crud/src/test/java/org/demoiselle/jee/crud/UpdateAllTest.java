/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.demoiselle.jee.crud.entity.UserModelForTest;
import org.demoiselle.jee.crud.specification.Specification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AbstractDAO.updateAll(Specification, Map) (Task 11.4).
 *
 * Validates: Requirements 4.3
 */
class UpdateAllTest {

    static class TestDAO extends AbstractDAO<UserModelForTest, Long> {
        private final EntityManager em;

        TestDAO(EntityManager em) { this.em = em; }

        @Override
        protected EntityManager getEntityManager() { return em; }
    }

    private EntityManager em;
    private CriteriaBuilder cb;
    private TestDAO dao;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        em = mock(EntityManager.class);
        cb = mock(CriteriaBuilder.class);
        when(em.getCriteriaBuilder()).thenReturn(cb);
        dao = new TestDAO(em);
    }

    @SuppressWarnings("unchecked")
    @Test
    void updateAll_appliesUpdatesAndSpecPredicate() {
        CriteriaUpdate<UserModelForTest> cu = mock(CriteriaUpdate.class);
        Root<UserModelForTest> root = mock(Root.class);
        Path<Object> namePath = mock(Path.class);
        Path<Object> agePath = mock(Path.class);
        Predicate predicate = mock(Predicate.class);
        jakarta.persistence.Query query = mock(jakarta.persistence.Query.class);

        when(cb.createCriteriaUpdate(UserModelForTest.class)).thenReturn(cu);
        when(cu.from(UserModelForTest.class)).thenReturn(root);
        when(root.get("name")).thenReturn(namePath);
        when(root.get("age")).thenReturn(agePath);
        when(em.createQuery(cu)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(5);

        Specification<UserModelForTest> spec = (r, q, c) -> predicate;

        Map<String, Object> updates = new LinkedHashMap<>();
        updates.put("name", "Updated");
        updates.put("age", 30);

        int result = dao.updateAll(spec, updates);

        assertEquals(5, result);
        verify(cu).set(namePath, "Updated");
        verify(cu).set(agePath, 30);
        verify(cu).where(predicate);
        verify(em).createQuery(cu);
        verify(query).executeUpdate();
    }

    @SuppressWarnings("unchecked")
    @Test
    void updateAll_nullSpec_noWhereClause() {
        CriteriaUpdate<UserModelForTest> cu = mock(CriteriaUpdate.class);
        Root<UserModelForTest> root = mock(Root.class);
        Path<Object> namePath = mock(Path.class);
        jakarta.persistence.Query query = mock(jakarta.persistence.Query.class);

        when(cb.createCriteriaUpdate(UserModelForTest.class)).thenReturn(cu);
        when(cu.from(UserModelForTest.class)).thenReturn(root);
        when(root.get("name")).thenReturn(namePath);
        when(em.createQuery(cu)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(10);

        Map<String, Object> updates = Map.of("name", "AllUpdated");

        int result = dao.updateAll(null, updates);

        assertEquals(10, result);
        verify(cu).set(namePath, "AllUpdated");
        verify(cu, never()).where(any(Predicate.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void updateAll_emptyUpdatesMap_executesWithNoSets() {
        CriteriaUpdate<UserModelForTest> cu = mock(CriteriaUpdate.class);
        Root<UserModelForTest> root = mock(Root.class);
        jakarta.persistence.Query query = mock(jakarta.persistence.Query.class);

        when(cb.createCriteriaUpdate(UserModelForTest.class)).thenReturn(cu);
        when(cu.from(UserModelForTest.class)).thenReturn(root);
        when(em.createQuery(cu)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(0);

        int result = dao.updateAll(null, Map.of());

        assertEquals(0, result);
        verify(root, never()).get(anyString());
    }

    @SuppressWarnings("unchecked")
    @Test
    void updateAll_returnsCountFromExecuteUpdate() {
        CriteriaUpdate<UserModelForTest> cu = mock(CriteriaUpdate.class);
        Root<UserModelForTest> root = mock(Root.class);
        Path<Object> mailPath = mock(Path.class);
        jakarta.persistence.Query query = mock(jakarta.persistence.Query.class);

        when(cb.createCriteriaUpdate(UserModelForTest.class)).thenReturn(cu);
        when(cu.from(UserModelForTest.class)).thenReturn(root);
        when(root.get("mail")).thenReturn(mailPath);
        when(em.createQuery(cu)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(42);

        Specification<UserModelForTest> spec = (r, q, c) -> mock(Predicate.class);

        int result = dao.updateAll(spec, Map.of("mail", "new@test.com"));

        assertEquals(42, result);
    }
}
