/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.filter;

import java.util.List;
import java.util.UUID;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for {@link FilterOp} null-safety of key().
 *
 * <p><b>Validates: Requirement 6.2</b></p>
 */
class FilterOpNullSafetyPropertyTest {

    /**
     * Property 6: For any instance of FilterOp (all 7 variants),
     * key() never returns null.
     *
     * <p>We use {@code Arbitraries.oneOf()} to generate all 7 variants
     * with valid parameters and verify that key() is always non-null.</p>
     */
    @Property(tries = 200)
    void keyNeverReturnsNullForAnyFilterOpVariant(
            @ForAll("anyFilterOp") FilterOp op) {
        assertNotNull(op.key(), "key() must never return null for " + op.getClass().getSimpleName());
    }

    @Provide
    Arbitrary<FilterOp> anyFilterOp() {
        return Arbitraries.oneOf(
                equalsOps(),
                likeOps(),
                isNullOps(),
                isTrueOps(),
                isFalseOps(),
                enumFilterOps(),
                uuidFilterOps(),
                greaterThanOps(),
                lessThanOps(),
                greaterThanOrEqualOps(),
                lessThanOrEqualOps(),
                betweenOps(),
                inOps()
        );
    }

    private Arbitrary<FilterOp> equalsOps() {
        return Combinators.combine(nonNullKeys(), nonNullStrings())
                .as(FilterOp.Equals::new);
    }

    private Arbitrary<FilterOp> likeOps() {
        return Combinators.combine(nonNullKeys(), likePatterns())
                .as(FilterOp.Like::new);
    }

    private Arbitrary<FilterOp> isNullOps() {
        return nonNullKeys().map(FilterOp.IsNull::new);
    }

    private Arbitrary<FilterOp> isTrueOps() {
        return nonNullKeys().map(FilterOp.IsTrue::new);
    }

    private Arbitrary<FilterOp> isFalseOps() {
        return nonNullKeys().map(FilterOp.IsFalse::new);
    }

    private Arbitrary<FilterOp> enumFilterOps() {
        return Combinators.combine(nonNullKeys(), nonNullStrings(), validOrdinals())
                .as(FilterOp.EnumFilter::new);
    }

    private Arbitrary<FilterOp> uuidFilterOps() {
        return Combinators.combine(nonNullKeys(), validUUIDs())
                .as(FilterOp.UUIDFilter::new);
    }

    private Arbitrary<FilterOp> greaterThanOps() {
        return Combinators.combine(nonNullKeys(), nonNullStrings())
                .as(FilterOp.GreaterThan::new);
    }

    private Arbitrary<FilterOp> lessThanOps() {
        return Combinators.combine(nonNullKeys(), nonNullStrings())
                .as(FilterOp.LessThan::new);
    }

    private Arbitrary<FilterOp> greaterThanOrEqualOps() {
        return Combinators.combine(nonNullKeys(), nonNullStrings())
                .as(FilterOp.GreaterThanOrEqual::new);
    }

    private Arbitrary<FilterOp> lessThanOrEqualOps() {
        return Combinators.combine(nonNullKeys(), nonNullStrings())
                .as(FilterOp.LessThanOrEqual::new);
    }

    private Arbitrary<FilterOp> betweenOps() {
        return Combinators.combine(nonNullKeys(), nonNullStrings(), nonNullStrings())
                .as(FilterOp.Between::new);
    }

    private Arbitrary<FilterOp> inOps() {
        return Combinators.combine(nonNullKeys(), nonNullStrings().list().ofMinSize(1).ofMaxSize(10))
                .as(FilterOp.In::new);
    }

    private Arbitrary<String> nonNullKeys() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30);
    }

    private Arbitrary<String> nonNullStrings() {
        return Arbitraries.strings().ofMinLength(0).ofMaxLength(50);
    }

    private Arbitrary<String> likePatterns() {
        return Arbitraries.strings().ofMinLength(1).ofMaxLength(30)
                .map(s -> "*" + s + "*");
    }

    private Arbitrary<Integer> validOrdinals() {
        return Arbitraries.integers().between(0, 100);
    }

    private Arbitrary<UUID> validUUIDs() {
        return Combinators.combine(
                Arbitraries.longs(), Arbitraries.longs()
        ).as((most, least) -> new UUID(most, least));
    }
}
