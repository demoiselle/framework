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

import net.jqwik.api.*;

import org.demoiselle.jee.crud.entity.CountryModelForTest;
import org.demoiselle.jee.crud.entity.MergeHalfEntityForTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Property-based test for {@link AbstractDAO#mergeHalf(Object, Object)} CriteriaUpdate
 * field exclusion logic.
 *
 * <p><b>Validates: Requirements 13.2, 13.3, 13.4</b></p>
 *
 * <p>Property 14: For any JPA entity, mergeHalf() includes only fields with non-null value,
 * without @Column(updatable=false), and always includes @ManyToOne if non-null.</p>
 */
class MergeHalfCriteriaUpdatePropertyTest {

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

    /**
     * Property 14: For any arbitrary combination of field values on MergeHalfEntityForTest,
     * mergeHalf() includes in CriteriaUpdate.set() ONLY fields that satisfy ALL of:
     * (a) have non-null value,
     * (b) are NOT annotated with @Column(updatable=false) (unless they have @ManyToOne),
     * (c) fields with @ManyToOne are always included if non-null,
     * (d) fields without @Column or @ManyToOne are NOT included.
     *
     * <p><b>Validates: Requirements 13.2, 13.3, 13.4</b></p>
     */
    @Property(tries = 200)
    void mergeHalfIncludesOnlyCorrectFields(
            @ForAll("entityCombinations") MergeHalfEntityForTest entity) {

        // --- Set up mocks ---
        EntityManager entityManager = mock(EntityManager.class);
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        CriteriaUpdate<MergeHalfEntityForTest> criteriaUpdate = mock(CriteriaUpdate.class);
        @SuppressWarnings("unchecked")
        Root<MergeHalfEntityForTest> root = mock(Root.class);
        Query query = mock(Query.class);

        Map<String, Path<Object>> pathMocks = new HashMap<>();

        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createCriteriaUpdate(MergeHalfEntityForTest.class)).thenReturn(criteriaUpdate);
        when(criteriaUpdate.from(MergeHalfEntityForTest.class)).thenReturn(root);

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

        MergeHalfDAO dao = new MergeHalfDAO(entityManager);

        // --- Execute ---
        dao.mergeHalf(1L, entity);

        // --- Verify field inclusion/exclusion rules ---

        // "name" — @Column(updatable=true): should be set IFF non-null
        verifyFieldBehavior(criteriaUpdate, pathMocks, "name", entity.getName());

        // "age" — @Column(updatable=true): should be set IFF non-null
        verifyFieldBehavior(criteriaUpdate, pathMocks, "age", entity.getAge());

        // "country" — @ManyToOne: should be set IFF non-null
        verifyFieldBehavior(criteriaUpdate, pathMocks, "country", entity.getCountry());

        // "createdBy" — @Column(updatable=false): should NEVER be set
        Path<Object> createdByPath = pathMocks.get("createdBy");
        if (createdByPath != null) {
            verify(criteriaUpdate, never()).set(eq(createdByPath), (Object) any());
        }

        // "unmapped" — no @Column, no @ManyToOne: should NEVER be set
        Path<Object> unmappedPath = pathMocks.get("unmapped");
        if (unmappedPath != null) {
            verify(criteriaUpdate, never()).set(eq(unmappedPath), (Object) any());
        }

        // "id" — @Id only, no @Column, no @ManyToOne: should NEVER be set
        Path<Object> idPath = pathMocks.get("id");
        if (idPath != null) {
            // id path may be accessed for the WHERE clause, but should never be in set()
            verify(criteriaUpdate, never()).set(eq(idPath), (Object) any());
        }
    }

    @SuppressWarnings("unchecked")
    private void verifyFieldBehavior(CriteriaUpdate<MergeHalfEntityForTest> criteriaUpdate,
                                     Map<String, Path<Object>> pathMocks,
                                     String fieldName, Object fieldValue) {
        if (fieldValue != null) {
            // Field has non-null value and is eligible — must be set
            Path<Object> path = pathMocks.get(fieldName);
            assertNotNull(path,
                    fieldName + " has non-null value (" + fieldValue + ") but root.get() was never called for it");
            verify(criteriaUpdate).set(path, fieldValue);
        } else {
            // Field is null — must NOT be set
            Path<Object> path = pathMocks.get(fieldName);
            if (path != null) {
                verify(criteriaUpdate, never()).set(eq(path), (Object) any());
            }
        }
    }

    /**
     * Generates arbitrary MergeHalfEntityForTest instances with random combinations
     * of null and non-null field values to exercise all code paths in mergeHalf().
     */
    @Provide
    Arbitrary<MergeHalfEntityForTest> entityCombinations() {
        Arbitrary<String> nullableNames = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20)
                .injectNull(0.4);
        Arbitrary<String> nullableCreatedBy = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20)
                .injectNull(0.4);
        Arbitrary<Integer> nullableAges = Arbitraries.integers().between(1, 120)
                .map(i -> (Integer) i)
                .injectNull(0.4);
        Arbitrary<CountryModelForTest> nullableCountries = Arbitraries.of(
                createCountry(1L, "Brazil"),
                createCountry(2L, "Argentina"),
                createCountry(3L, "Chile")
        ).injectNull(0.4);
        Arbitrary<String> nullableUnmapped = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10)
                .injectNull(0.4);

        return Combinators.combine(nullableNames, nullableCreatedBy, nullableAges, nullableCountries, nullableUnmapped)
                .as((name, createdBy, age, country, unmapped) -> {
                    MergeHalfEntityForTest entity = new MergeHalfEntityForTest();
                    entity.setName(name);
                    entity.setCreatedBy(createdBy);
                    entity.setAge(age);
                    entity.setCountry(country);
                    entity.setUnmapped(unmapped);
                    return entity;
                });
    }

    private static CountryModelForTest createCountry(Long id, String name) {
        CountryModelForTest c = new CountryModelForTest();
        c.setId(id);
        c.setName(name);
        return c;
    }
}
