/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.crud;

import java.util.List;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Crud default method {@code count()}.
 *
 * <p><b>Validates: Requirements 2.2, 2.7</b></p>
 */
class CrudCountPropertyTest {

    // -----------------------------------------------------------------------
    // Property 3: Consistência de count() com find()
    // -----------------------------------------------------------------------

    /**
     * Feature: core-api-enhancements, Property 3: Consistência de count() com find()
     *
     * <p>For any Crud implementation, the result of {@code count()} must equal
     * the size of the list returned by {@code find().getContent()}.
     * When {@code find()} returns null, {@code count()} must return 0L.</p>
     *
     * <p><b>Validates: Requirements 2.2, 2.7</b></p>
     */
    @Property(tries = 100)
    // Feature: core-api-enhancements, Property 3: Consistência de count() com find()
    void countIsConsistentWithFindContent(
            @ForAll @Size(max = 200) List<String> elements,
            @ForAll boolean findReturnsNull
    ) {
        StubCrud<String> crud = new StubCrud<>();

        if (findReturnsNull) {
            crud.setFindAllResult(null);
        } else {
            crud.setFindAllContent(elements);
        }

        long countResult = crud.count();

        if (findReturnsNull) {
            assertEquals(0L, countResult,
                    "When find() returns null, count() must return 0L");
        } else {
            assertEquals(elements.size(), countResult,
                    "count() must equal find().getContent().size()");
        }
    }
}
