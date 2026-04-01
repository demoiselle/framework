/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.sort;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for {@link SortModel} blank field rejection.
 *
 * <p><b>Validates: Requirements 1.4</b></p>
 */
class SortModelPropertiesTest {

    /**
     * Property 1: For any string composed of whitespace characters,
     * {@code new SortModel(type, blankField)} must throw IllegalArgumentException.
     */
    @Property(tries = 200)
    void blankFieldMustBeRejected(
            @ForAll("crudSortTypes") CrudSort type,
            @ForAll("blankStrings") String blankField) {

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new SortModel(type, blankField));

        assertNotNull(ex.getMessage());
    }

    @Provide
    Arbitrary<CrudSort> crudSortTypes() {
        return Arbitraries.of(CrudSort.values());
    }

    @Provide
    Arbitrary<String> blankStrings() {
        // Whitespace characters: space, tab, newline, carriage return, form feed
        Arbitrary<Character> whitespaceChars = Arbitraries.of(' ', '\t', '\n', '\r', '\f');

        Arbitrary<String> whitespaceStrings = whitespaceChars.list()
                .ofMinSize(1)
                .ofMaxSize(20)
                .map(chars -> {
                    StringBuilder sb = new StringBuilder(chars.size());
                    for (char c : chars) {
                        sb.append(c);
                    }
                    return sb.toString();
                });

        // Also include the empty string ""
        return Arbitraries.oneOf(
                Arbitraries.just(""),
                whitespaceStrings
        );
    }
}
