/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.sort;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.demoiselle.jee.rest.exception.DemoiselleRestExceptionMessage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for structural equality of Java 17 Records.
 *
 * <p><b>Validates: Requirements 1.5, 2.4</b></p>
 */
class RecordStructuralEqualityPropertiesTest {

    /**
     * Property 2 (SortModel): For any two SortModel records created with the
     * same type and field values, equals() returns true and hashCode() is equal.
     */
    @Property(tries = 200)
    void sortModelStructuralEquality(
            @ForAll("crudSortTypes") CrudSort type,
            @ForAll("nonBlankStrings") String field) {

        SortModel a = new SortModel(type, field);
        SortModel b = new SortModel(type, field);

        assertEquals(a, b, "Records with same values must be equal");
        assertEquals(a.hashCode(), b.hashCode(),
                "Records with same values must have same hashCode");
    }

    /**
     * Property 2 (DemoiselleRestExceptionMessage): For any two
     * DemoiselleRestExceptionMessage records created with the same error,
     * errorDescription and errorLink values, equals() returns true and
     * hashCode() is equal.
     */
    @Property(tries = 200)
    void restExceptionMessageStructuralEquality(
            @ForAll("nonNullStrings") String error,
            @ForAll("optionalStrings") String errorDescription,
            @ForAll("optionalStrings") String errorLink) {

        DemoiselleRestExceptionMessage a =
                new DemoiselleRestExceptionMessage(error, errorDescription, errorLink);
        DemoiselleRestExceptionMessage b =
                new DemoiselleRestExceptionMessage(error, errorDescription, errorLink);

        assertEquals(a, b, "Records with same values must be equal");
        assertEquals(a.hashCode(), b.hashCode(),
                "Records with same values must have same hashCode");
    }

    // --- Providers ---

    @Provide
    Arbitrary<CrudSort> crudSortTypes() {
        return Arbitraries.of(CrudSort.values());
    }

    @Provide
    Arbitrary<String> nonBlankStrings() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(50)
                .alpha()
                .numeric();
    }

    @Provide
    Arbitrary<String> nonNullStrings() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(100);
    }

    @Provide
    Arbitrary<String> optionalStrings() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.strings().ofMinLength(0).ofMaxLength(100)
        );
    }
}
