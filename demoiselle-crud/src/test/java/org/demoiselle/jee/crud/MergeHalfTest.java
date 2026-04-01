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
import jakarta.persistence.criteria.Root;

import org.demoiselle.jee.crud.entity.CountryModelForTest;
import org.demoiselle.jee.crud.entity.MergeHalfEntityForTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AbstractDAO#mergeHalf(Object, Object)} with CriteriaUpdate.
 *
 * Validates: Requirements 13.1, 13.2, 13.3, 13.4, 13.5
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MergeHalfTest {

    /**
     * Concrete subclass of AbstractDAO parameterized with MergeHalfEntityForTest.
     */
    static class MergeHalfDAO extends AbstractDAO<MergeHalfEntityForTest, Long> {
        private final EntityManager em;

        MergeHalfDAO(EntityManager em) {
            this.em = em;
        }

        @Override
        protected EntityManager getEntityManager() {
            return em;
        }
    }

    @Mock private EntityManager entityManager;
    @Mock private CriteriaBuilder criteriaBuilder;
    @SuppressWarnings("rawtypes")
    @Mock private CriteriaUpdate criteriaUpdate;
    @SuppressWarnings("rawtypes")
    @Mock private Root root;
    @Mock private Query query;

    /** Maps field names to distinct Path mocks so we can verify which fields were set. */
    private final Map<String, Path<Object>> pathMocks = new HashMap<>();

    private MergeHalfDAO dao;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        pathMocks.clear();

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createCriteriaUpdate(MergeHalfEntityForTest.class)).thenReturn(criteriaUpdate);
        when(criteriaUpdate.from(MergeHalfEntityForTest.class)).thenReturn(root);

        // Return a distinct Path mock for each field name so we can verify per-field
        when(root.get(anyString())).thenAnswer(invocation -> {
            String fieldName = invocation.getArgument(0);
            return pathMocks.computeIfAbsent(fieldName, k -> {
                @SuppressWarnings("unchecked")
                Path<Object> p = mock(Path.class, "path<" + k + ">");
                return p;
            });
        });

        when(criteriaBuilder.equal(any(), (Object) any()))
                .thenReturn(mock(jakarta.persistence.criteria.Predicate.class));
        when(criteriaUpdate.where(any(jakarta.persistence.criteria.Predicate.class)))
                .thenReturn(criteriaUpdate);
        when(entityManager.createQuery(criteriaUpdate)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        dao = new MergeHalfDAO(entityManager);
    }

    private Path<Object> pathFor(String fieldName) {
        return pathMocks.get(fieldName);
    }

    // ========================================================================
    // Requirement 13.1: CriteriaUpdate is used (type-safe, not JPQL)
    // Requirement 13.2: Null fields are ignored (partial update)
    // ========================================================================

    /**
     * When only some fields have non-null values, mergeHalf() should only
     * call set() for those fields, ignoring null ones.
     */
    @SuppressWarnings("unchecked")
    @Test
    void mergeHalf_partialUpdate_ignoresNullFields() {
        MergeHalfEntityForTest entity = new MergeHalfEntityForTest();
        entity.setName("Updated Name");
        // age is null — should be ignored
        // country is null — should be ignored

        dao.mergeHalf(1L, entity);

        // "name" should be set
        verify(criteriaUpdate).set(pathFor("name"), (Object) "Updated Name");
        // "age" path should never appear in a set() call
        assertNull(pathFor("age"), "age is null and should not have been accessed for set()");
        verify(entityManager).createQuery(criteriaUpdate);
        verify(query).executeUpdate();
    }

    // ========================================================================
    // Requirement 13.3: @Column(updatable=false) fields are excluded
    // ========================================================================

    /**
     * Fields annotated with @Column(updatable=false) must never appear in the
     * CriteriaUpdate SET clause, even when they have non-null values.
     */
    @SuppressWarnings("unchecked")
    @Test
    void mergeHalf_excludesNonUpdatableColumns() {
        MergeHalfEntityForTest entity = new MergeHalfEntityForTest();
        entity.setName("Updated Name");
        entity.setCreatedBy("admin"); // @Column(updatable=false) — must be excluded

        dao.mergeHalf(1L, entity);

        // "name" should be set
        verify(criteriaUpdate).set(pathFor("name"), (Object) "Updated Name");
        // "createdBy" should NOT appear in any set() call
        Path<Object> createdByPath = pathFor("createdBy");
        if (createdByPath != null) {
            verify(criteriaUpdate, never()).set(eq(createdByPath), any());
        }
    }

    // ========================================================================
    // Requirement 13.4: @ManyToOne fields are included
    // ========================================================================

    /**
     * Fields annotated with @ManyToOne should be included in the CriteriaUpdate
     * regardless of @Column annotation, as long as the value is non-null.
     */
    @SuppressWarnings("unchecked")
    @Test
    void mergeHalf_includesManyToOneFields() {
        MergeHalfEntityForTest entity = new MergeHalfEntityForTest();
        CountryModelForTest country = new CountryModelForTest();
        country.setId(10L);
        country.setName("Brazil");
        entity.setCountry(country);

        dao.mergeHalf(1L, entity);

        // "country" has @ManyToOne and non-null value — must be included
        verify(criteriaUpdate).set(pathFor("country"), (Object) country);
        verify(entityManager).createQuery(criteriaUpdate);
        verify(query).executeUpdate();
    }

    // ========================================================================
    // Requirement 13.5: No query executed when all fields are null
    // ========================================================================

    /**
     * When all updatable fields have null values, mergeHalf() should not
     * execute any query (no executeUpdate call).
     */
    @Test
    void mergeHalf_allFieldsNull_noQueryExecuted() {
        MergeHalfEntityForTest entity = new MergeHalfEntityForTest();
        // All fields are null by default

        MergeHalfEntityForTest result = dao.mergeHalf(1L, entity);

        // No query should be created or executed
        verify(entityManager, never()).createQuery(criteriaUpdate);
        verify(query, never()).executeUpdate();
        // Should still return the entity
        assertSame(entity, result);
    }

    // ========================================================================
    // Additional: unmapped fields (no @Column, no @ManyToOne) are excluded
    // ========================================================================

    /**
     * Fields without @Column or @ManyToOne annotations should be excluded
     * from the CriteriaUpdate, even when they have non-null values.
     */
    @SuppressWarnings("unchecked")
    @Test
    void mergeHalf_excludesUnmappedFields() {
        MergeHalfEntityForTest entity = new MergeHalfEntityForTest();
        entity.setName("Updated Name");
        entity.setUnmapped("should be ignored"); // no @Column, no @ManyToOne

        dao.mergeHalf(1L, entity);

        // "name" should be set
        verify(criteriaUpdate).set(pathFor("name"), (Object) "Updated Name");
        // "unmapped" should NOT appear in any set() call
        Path<Object> unmappedPath = pathFor("unmapped");
        if (unmappedPath != null) {
            verify(criteriaUpdate, never()).set(eq(unmappedPath), any());
        }
    }

    // ========================================================================
    // Requirement 13.1: Multiple fields updated together
    // ========================================================================

    /**
     * When multiple updatable fields have non-null values, all of them
     * should appear in the CriteriaUpdate SET clause.
     */
    @SuppressWarnings("unchecked")
    @Test
    void mergeHalf_multipleFieldsUpdated() {
        MergeHalfEntityForTest entity = new MergeHalfEntityForTest();
        entity.setName("New Name");
        entity.setAge(30);
        CountryModelForTest country = new CountryModelForTest();
        country.setId(5L);
        entity.setCountry(country);

        dao.mergeHalf(1L, entity);

        // All three should be set
        verify(criteriaUpdate).set(pathFor("name"), (Object) "New Name");
        verify(criteriaUpdate).set(pathFor("age"), (Object) 30);
        verify(criteriaUpdate).set(pathFor("country"), (Object) country);
        verify(entityManager).createQuery(criteriaUpdate);
        verify(query).executeUpdate();
    }

    /**
     * mergeHalf() should return the same entity instance that was passed in.
     */
    @Test
    void mergeHalf_returnsEntity() {
        MergeHalfEntityForTest entity = new MergeHalfEntityForTest();
        entity.setName("Test");

        MergeHalfEntityForTest result = dao.mergeHalf(1L, entity);

        assertSame(entity, result);
    }
}
