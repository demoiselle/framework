/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.ArrayList;
import java.util.List;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for {@link ResultSet} defensive copy independence.
 *
 * <p><b>Validates: Requirements 3.1, 3.4</b></p>
 */
class ResultSetDefensiveCopyPropertyTest {

    /**
     * Property 4: For any list passed to setContent(), modifications to the
     * original list do not affect getContent().
     *
     * <p>We generate an arbitrary list of strings, pass a mutable copy to
     * setContent(), then mutate the original (add, remove, clear) and verify
     * that getContent() returns the snapshot taken at setContent() time.</p>
     */
    @Property(tries = 200)
    void modifyingOriginalListDoesNotAffectGetContent(
            @ForAll List<@From("nonNullStrings") String> elements) {

        // Create a mutable copy to pass to setContent
        List<String> mutableList = new ArrayList<>(elements);
        List<String> snapshot = List.copyOf(mutableList);

        ResultSet rs = new ResultSet();
        rs.setContent(mutableList);

        // Verify initial content matches
        assertEquals(snapshot, rs.getContent());

        // Mutate the original list: add an element
        mutableList.add("EXTRA_ELEMENT");
        assertEquals(snapshot, rs.getContent(),
                "Adding to original list must not affect getContent()");

        // Mutate the original list: clear it
        mutableList.clear();
        assertEquals(snapshot, rs.getContent(),
                "Clearing original list must not affect getContent()");
    }

    @Provide
    Arbitrary<String> nonNullStrings() {
        return Arbitraries.strings().ofMinLength(0).ofMaxLength(50);
    }
}
