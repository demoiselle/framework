/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Specification} functional interface and its default methods.
 *
 * <p><b>Validates: Requirements 3.1, 3.2, 3.3, 3.4</b></p>
 */
class SpecificationTest {

    private Root<Object> root;
    private CriteriaQuery<?> query;
    private CriteriaBuilder cb;
    private Predicate predicateA;
    private Predicate predicateB;
    private Predicate combinedPredicate;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class);
        predicateA = mock(Predicate.class, "predicateA");
        predicateB = mock(Predicate.class, "predicateB");
        combinedPredicate = mock(Predicate.class, "combined");
    }

    @Test
    void toPredicateIsInvokedAsLambda() {
        Specification<Object> spec = (r, q, c) -> predicateA;
        Predicate result = spec.toPredicate(root, query, cb);
        assertSame(predicateA, result);
    }

    @Test
    void andCombinesBothPredicatesWithCriteriaBuilderAnd() {
        Specification<Object> specA = (r, q, c) -> predicateA;
        Specification<Object> specB = (r, q, c) -> predicateB;
        when(cb.and(predicateA, predicateB)).thenReturn(combinedPredicate);

        Specification<Object> combined = specA.and(specB);
        Predicate result = combined.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);
        verify(cb).and(predicateA, predicateB);
    }

    @Test
    void orCombinesBothPredicatesWithCriteriaBuilderOr() {
        Specification<Object> specA = (r, q, c) -> predicateA;
        Specification<Object> specB = (r, q, c) -> predicateB;
        when(cb.or(predicateA, predicateB)).thenReturn(combinedPredicate);

        Specification<Object> combined = specA.or(specB);
        Predicate result = combined.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);
        verify(cb).or(predicateA, predicateB);
    }

    @Test
    void notNegatesPredicateWithCriteriaBuilderNot() {
        Specification<Object> spec = (r, q, c) -> predicateA;
        when(cb.not(predicateA)).thenReturn(combinedPredicate);

        Specification<Object> negated = spec.not();
        Predicate result = negated.toPredicate(root, query, cb);

        assertSame(combinedPredicate, result);
        verify(cb).not(predicateA);
    }

    @Test
    void chainingAndOrNotProducesCorrectPredicateTree() {
        Predicate notPredicate = mock(Predicate.class, "notPredicate");
        Predicate orPredicate = mock(Predicate.class, "orPredicate");

        Specification<Object> specA = (r, q, c) -> predicateA;
        Specification<Object> specB = (r, q, c) -> predicateB;

        when(cb.not(predicateA)).thenReturn(notPredicate);
        when(cb.or(notPredicate, predicateB)).thenReturn(orPredicate);

        // not(A) or B
        Specification<Object> chained = specA.not().or(specB);
        Predicate result = chained.toPredicate(root, query, cb);

        assertSame(orPredicate, result);
        verify(cb).not(predicateA);
        verify(cb).or(notPredicate, predicateB);
    }
}
