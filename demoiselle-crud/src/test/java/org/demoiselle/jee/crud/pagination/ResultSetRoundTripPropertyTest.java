/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.List;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

// Feature: core-api-enhancements, Property 1: Round-trip de conteúdo tipado no Result<T>

/**
 * Property-based test for typed round-trip on {@link ResultSet}.
 *
 * <p>For any typed {@code List<T>}, setting it as content via {@code setContent()}
 * and retrieving it via {@code getContent()} must return an equal list.</p>
 *
 * <p><b>Validates: Requirements 1.2, 1.5</b></p>
 */
class ResultSetRoundTripPropertyTest {

    @Property(tries = 150)
    void roundTripWithStrings(@ForAll List<@From("nonNullStrings") String> elements) {
        ResultSet<String> rs = new ResultSet<>();
        rs.setContent(elements);

        List<String> retrieved = rs.getContent();

        assertEquals(elements, retrieved,
                "getContent() must equal the list passed to setContent()");
    }

    @Property(tries = 150)
    void roundTripWithIntegers(@ForAll List<Integer> elements) {
        ResultSet<Integer> rs = new ResultSet<>();
        rs.setContent(elements);

        List<Integer> retrieved = rs.getContent();

        assertEquals(elements, retrieved,
                "getContent() must equal the list passed to setContent()");
    }

    @Provide
    Arbitrary<String> nonNullStrings() {
        return Arbitraries.strings().ofMinLength(0).ofMaxLength(100);
    }
}
