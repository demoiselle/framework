/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.demoiselle.jee.crud.entity.UserModelForTest;
import org.demoiselle.jee.crud.pagination.PaginationHelperConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EntityGraph support in {@link AbstractDAO#find()}.
 *
 * Validates: Requirements 14.1, 14.2, 14.3
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EntityGraphFindTest {

    /**
     * Concrete subclass with default getEntityGraph() returning null.
     */
    static class DefaultGraphDAO extends AbstractDAO<UserModelForTest, Long> {
        private final EntityManager em;

        DefaultGraphDAO(EntityManager em) {
            this.em = em;
        }

        @Override
        protected EntityManager getEntityManager() {
            return em;
        }
    }

    /**
     * Concrete subclass that overrides getEntityGraph() to return a provided EntityGraph.
     */
    static class CustomGraphDAO extends AbstractDAO<UserModelForTest, Long> {
        private final EntityManager em;
        private final EntityGraph<UserModelForTest> entityGraph;

        CustomGraphDAO(EntityManager em, EntityGraph<UserModelForTest> entityGraph) {
            this.em = em;
            this.entityGraph = entityGraph;
        }

        @Override
        protected EntityManager getEntityManager() {
            return em;
        }

        @Override
        protected EntityGraph<UserModelForTest> getEntityGraph() {
            return entityGraph;
        }
    }

    @Mock private EntityManager entityManager;
    @Mock private CriteriaBuilder criteriaBuilder;
    @SuppressWarnings("rawtypes")
    @Mock private CriteriaQuery criteriaQuery;
    @SuppressWarnings("rawtypes")
    @Mock private Root root;
    @SuppressWarnings("rawtypes")
    @Mock private TypedQuery typedQuery;
    @Mock private DemoiselleRequestContext drc;
    @Mock private PaginationHelperConfig paginationConfig;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(UserModelForTest.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(UserModelForTest.class)).thenReturn(root);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        when(drc.isPaginationEnabled()).thenReturn(false);
        when(drc.getFilters()).thenReturn(null);
        when(drc.getSorts()).thenReturn(List.of());
    }

    /**
     * Injects mock DemoiselleRequestContext and PaginationHelperConfig into the DAO
     * via reflection, since they are @Inject fields in AbstractDAO.
     */
    private void injectMocks(AbstractDAO<?, ?> dao) throws Exception {
        Field drcField = AbstractDAO.class.getDeclaredField("drc");
        drcField.setAccessible(true);
        drcField.set(dao, drc);

        Field configField = AbstractDAO.class.getDeclaredField("paginationConfig");
        configField.setAccessible(true);
        configField.set(dao, paginationConfig);
    }

    // ========================================================================
    // Requirement 14.2: find() works normally when getEntityGraph() returns null
    // ========================================================================

    /**
     * When getEntityGraph() returns null (default behavior), the TypedQuery
     * should NOT have setHint() called with the fetchgraph hint.
     */
    @Test
    void find_withNullEntityGraph_doesNotSetHint() throws Exception {
        DefaultGraphDAO dao = new DefaultGraphDAO(entityManager);
        injectMocks(dao);

        dao.find();

        verify(typedQuery, never()).setHint(eq("jakarta.persistence.fetchgraph"), any());
    }

    // ========================================================================
    // Requirement 14.1: hint is applied when EntityGraph is provided
    // ========================================================================

    /**
     * When a subclass overrides getEntityGraph() to return a non-null EntityGraph,
     * find() should call query.setHint("jakarta.persistence.fetchgraph", graph).
     */
    @SuppressWarnings("unchecked")
    @Test
    void find_withEntityGraph_setsHint() throws Exception {
        EntityGraph<UserModelForTest> mockGraph = mock(EntityGraph.class);
        CustomGraphDAO dao = new CustomGraphDAO(entityManager, mockGraph);
        injectMocks(dao);

        dao.find();

        verify(typedQuery).setHint("jakarta.persistence.fetchgraph", mockGraph);
    }

    // ========================================================================
    // Requirement 14.3: EntityGraph preserves existing pagination/sort/filter
    // ========================================================================

    /**
     * When EntityGraph is applied and pagination is enabled, both the hint
     * and pagination parameters should be set on the query.
     */
    @SuppressWarnings("unchecked")
    @Test
    void find_withEntityGraphAndPagination_setsHintAndPagination() throws Exception {
        EntityGraph<UserModelForTest> mockGraph = mock(EntityGraph.class);
        CustomGraphDAO dao = new CustomGraphDAO(entityManager, mockGraph);
        injectMocks(dao);

        // Enable pagination — limit and offset must both be non-null for getMaxResult()
        when(drc.isPaginationEnabled()).thenReturn(true);
        when(drc.getOffset()).thenReturn(0);
        when(drc.getLimit()).thenReturn(19);
        when(drc.getCount()).thenReturn(10L);
        when(paginationConfig.getDefaultPagination()).thenReturn(20);

        // Mock count query
        CriteriaQuery<Long> countQuery = mock(CriteriaQuery.class);
        when(criteriaBuilder.createQuery(Long.class)).thenReturn(countQuery);
        Root<UserModelForTest> countRoot = mock(Root.class);
        when(countQuery.from(UserModelForTest.class)).thenReturn(countRoot);
        when(countQuery.select(any())).thenReturn(countQuery);
        TypedQuery<Long> countTypedQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(countQuery)).thenReturn(countTypedQuery);
        when(countTypedQuery.getSingleResult()).thenReturn(5L);

        dao.find();

        // Verify EntityGraph hint was applied
        verify(typedQuery).setHint("jakarta.persistence.fetchgraph", mockGraph);
        // Verify pagination was also applied
        verify(typedQuery).setFirstResult(0);
        verify(typedQuery).setMaxResults(20);
    }
}
