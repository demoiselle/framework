/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.filter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.demoiselle.jee.crud.AbstractDAO;
import org.demoiselle.jee.crud.TreeNodeField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AbstractDAO#resolveFilterOp(String, String, TreeNodeField)}
 * and {@link AbstractDAO#buildPredicate(FilterOp, From, CriteriaBuilder, CriteriaQuery)}.
 *
 * Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 7.8
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResolveFilterOpTest {

    /**
     * Minimal test subclass that exposes the protected methods of AbstractDAO.
     * Only resolveFilterOp() and buildPredicate() are used; JPA/CDI dependencies
     * are not needed for the resolution logic under test.
     */
    static class TestableDAO extends AbstractDAO<Object, Long> {

        @Override
        public EntityManager getEntityManager() {
            return null; // not needed for resolveFilterOp tests
        }

        /** Expose protected resolveFilterOp for testing. */
        public FilterOp testResolveFilterOp(String key, String value,
                TreeNodeField<String, Set<String>> parent) {
            return resolveFilterOp(key, value, parent);
        }

        /** Expose protected buildPredicate for testing. */
        public Predicate testBuildPredicate(FilterOp op, From<?, ?> from,
                CriteriaBuilder cb, CriteriaQuery<?> cq) {
            return buildPredicate(op, from, cb, cq);
        }
    }

    private TestableDAO dao;

    @Mock private From<?, ?> from;
    @Mock private CriteriaBuilder cb;
    @Mock private CriteriaQuery<?> cq;
    @Mock private Predicate predicate;
    @Mock private Path<Object> path;
    @Mock private Expression<String> lowerExpr;

    @BeforeEach
    void setUp() {
        dao = new TestableDAO();
    }

    // ========================================================================
    // resolveFilterOp() — Requirement 7.1: always returns a FilterOp
    // ========================================================================

    @Test
    void resolveFilterOp_plainValue_returnsEquals() {
        FilterOp op = dao.testResolveFilterOp("name", "John", null);
        assertInstanceOf(FilterOp.Equals.class, op);
        assertEquals("name", op.key());
        assertEquals("John", ((FilterOp.Equals) op).value());
    }

    // ========================================================================
    // resolveFilterOp() — Requirement 7.2: null / "null" → IsNull
    // ========================================================================

    @Test
    void resolveFilterOp_nullValue_returnsIsNull() {
        FilterOp op = dao.testResolveFilterOp("status", null, null);
        assertInstanceOf(FilterOp.IsNull.class, op);
        assertEquals("status", op.key());
    }

    @Test
    void resolveFilterOp_stringNull_returnsIsNull() {
        FilterOp op = dao.testResolveFilterOp("status", "null", null);
        assertInstanceOf(FilterOp.IsNull.class, op);
        assertEquals("status", op.key());
    }

    // ========================================================================
    // resolveFilterOp() — Requirement 7.3: wildcard (*) → Like
    // ========================================================================

    @Test
    void resolveFilterOp_wildcardPrefix_returnsLike() {
        FilterOp op = dao.testResolveFilterOp("name", "*John", null);
        assertInstanceOf(FilterOp.Like.class, op);
        assertEquals("name", op.key());
        assertEquals("*John", ((FilterOp.Like) op).pattern());
    }

    @Test
    void resolveFilterOp_wildcardSuffix_returnsLike() {
        FilterOp op = dao.testResolveFilterOp("name", "John*", null);
        assertInstanceOf(FilterOp.Like.class, op);
        assertEquals("John*", ((FilterOp.Like) op).pattern());
    }

    @Test
    void resolveFilterOp_wildcardBoth_returnsLike() {
        FilterOp op = dao.testResolveFilterOp("name", "*John*", null);
        assertInstanceOf(FilterOp.Like.class, op);
        assertEquals("*John*", ((FilterOp.Like) op).pattern());
    }

    // ========================================================================
    // resolveFilterOp() — Requirement 7.4: "true"/"isTrue" → IsTrue
    // ========================================================================

    @Test
    void resolveFilterOp_trueValue_returnsIsTrue() {
        FilterOp op = dao.testResolveFilterOp("active", "true", null);
        assertInstanceOf(FilterOp.IsTrue.class, op);
        assertEquals("active", op.key());
    }

    @Test
    void resolveFilterOp_isTrueValue_returnsIsTrue() {
        FilterOp op = dao.testResolveFilterOp("active", "isTrue", null);
        assertInstanceOf(FilterOp.IsTrue.class, op);
        assertEquals("active", op.key());
    }

    @Test
    void resolveFilterOp_trueCaseInsensitive_returnsIsTrue() {
        FilterOp op = dao.testResolveFilterOp("active", "TRUE", null);
        assertInstanceOf(FilterOp.IsTrue.class, op);
    }

    @Test
    void resolveFilterOp_isTrueCaseInsensitive_returnsIsTrue() {
        FilterOp op = dao.testResolveFilterOp("active", "ISTRUE", null);
        assertInstanceOf(FilterOp.IsTrue.class, op);
    }

    // ========================================================================
    // resolveFilterOp() — Requirement 7.5: "false"/"isFalse" → IsFalse
    // ========================================================================

    @Test
    void resolveFilterOp_falseValue_returnsIsFalse() {
        FilterOp op = dao.testResolveFilterOp("deleted", "false", null);
        assertInstanceOf(FilterOp.IsFalse.class, op);
        assertEquals("deleted", op.key());
    }

    @Test
    void resolveFilterOp_isFalseValue_returnsIsFalse() {
        FilterOp op = dao.testResolveFilterOp("deleted", "isFalse", null);
        assertInstanceOf(FilterOp.IsFalse.class, op);
        assertEquals("deleted", op.key());
    }

    @Test
    void resolveFilterOp_falseCaseInsensitive_returnsIsFalse() {
        FilterOp op = dao.testResolveFilterOp("deleted", "FALSE", null);
        assertInstanceOf(FilterOp.IsFalse.class, op);
    }

    @Test
    void resolveFilterOp_isFalseCaseInsensitive_returnsIsFalse() {
        FilterOp op = dao.testResolveFilterOp("deleted", "ISFALSE", null);
        assertInstanceOf(FilterOp.IsFalse.class, op);
    }

    // ========================================================================
    // resolveFilterOp() — Default case: plain string → Equals
    // ========================================================================

    @Test
    void resolveFilterOp_numericString_returnsEquals() {
        FilterOp op = dao.testResolveFilterOp("age", "25", null);
        assertInstanceOf(FilterOp.Equals.class, op);
        assertEquals("25", ((FilterOp.Equals) op).value());
    }

    @Test
    void resolveFilterOp_emptyString_returnsEquals() {
        // Empty string doesn't match null, wildcard, true, or false patterns
        FilterOp op = dao.testResolveFilterOp("name", "", null);
        assertInstanceOf(FilterOp.Equals.class, op);
        assertEquals("", ((FilterOp.Equals) op).value());
    }

    // ========================================================================
    // buildPredicate() — Requirement 7.8: functional equivalence with original
    // ========================================================================

    @SuppressWarnings("unchecked")
    private void setupMocks() {
        // Use doReturn to bypass generic type checking
        doReturn(path).when(from).get(anyString());
        when(cb.isNull(any())).thenReturn(predicate);
        doReturn(predicate).when(cb).isTrue(any());
        doReturn(predicate).when(cb).isFalse(any());
        // Use Mockito.lenient() + default answer to handle all equal() overloads
        when(cb.equal(any(), (Object) any())).thenReturn(predicate);
        when(cb.equal(any(), (Expression<?>) any())).thenReturn(predicate);
        when(cb.lower(any())).thenReturn(lowerExpr);
        when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
    }

    @Test
    void buildPredicate_isNull_callsCbIsNull() {
        setupMocks();
        var op = new FilterOp.IsNull("status");
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        verify(cb).isNull(path);
    }

    @Test
    void buildPredicate_isTrue_callsCbIsTrue() {
        setupMocks();
        var op = new FilterOp.IsTrue("active");
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        verify(cb).isTrue(any());
    }

    @Test
    void buildPredicate_isFalse_callsCbIsFalse() {
        setupMocks();
        var op = new FilterOp.IsFalse("deleted");
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        verify(cb).isFalse(any());
    }

    @Test
    void buildPredicate_equals_callsCbEqual() {
        setupMocks();
        var op = new FilterOp.Equals("name", "John");
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        verify(cb).equal(path, "John");
    }

    @Test
    void buildPredicate_enumFilter_callsCbEqualWithOrdinal() {
        setupMocks();
        var op = new FilterOp.EnumFilter("status", "ACTIVE", 1);
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        verify(cb).equal(path, 1);
    }

    @Test
    void buildPredicate_uuidFilter_callsCbEqualWithUUID() {
        setupMocks();
        UUID uuid = UUID.randomUUID();
        var op = new FilterOp.UUIDFilter("id", uuid);
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        verify(cb).equal(path, uuid);
    }

    @SuppressWarnings("unchecked")
    @Test
    void buildPredicate_like_callsCbLikeWithTransformedPattern() {
        setupMocks();
        var op = new FilterOp.Like("name", "*John*");
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        // buildLikePredicate converts * to % and applies lower()
        verify(cb).lower(any());
        verify(cb).like(any(Expression.class), eq("%john%"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void buildPredicate_likePrefix_convertsWildcard() {
        setupMocks();
        var op = new FilterOp.Like("name", "*Smith");
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        verify(cb).like(any(Expression.class), eq("%smith"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void buildPredicate_likeSuffix_convertsWildcard() {
        setupMocks();
        var op = new FilterOp.Like("name", "Smith*");
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        verify(cb).like(any(Expression.class), eq("smith%"));
    }

    // ========================================================================
    // Functional equivalence: resolveFilterOp + buildPredicate round-trip
    // ========================================================================

    @Test
    void roundTrip_nullValue_producesIsNullPredicate() {
        setupMocks();
        FilterOp op = dao.testResolveFilterOp("field", null, null);
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        verify(cb).isNull(path);
    }

    @Test
    void roundTrip_stringNull_producesIsNullPredicate() {
        setupMocks();
        FilterOp op = dao.testResolveFilterOp("field", "null", null);
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        verify(cb).isNull(path);
    }

    @Test
    void roundTrip_trueValue_producesIsTruePredicate() {
        setupMocks();
        FilterOp op = dao.testResolveFilterOp("active", "true", null);
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        verify(cb).isTrue(any());
    }

    @Test
    void roundTrip_falseValue_producesIsFalsePredicate() {
        setupMocks();
        FilterOp op = dao.testResolveFilterOp("deleted", "false", null);
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        verify(cb).isFalse(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void roundTrip_wildcardValue_producesLikePredicate() {
        setupMocks();
        FilterOp op = dao.testResolveFilterOp("name", "*test*", null);
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        verify(cb).like(any(Expression.class), eq("%test%"));
    }

    @Test
    void roundTrip_plainValue_producesEqualPredicate() {
        setupMocks();
        FilterOp op = dao.testResolveFilterOp("name", "John", null);
        Predicate result = dao.testBuildPredicate(op, from, cb, cq);

        assertNotNull(result);
        verify(cb).equal(path, "John");
    }
}
