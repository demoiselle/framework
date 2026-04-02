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
 * Property-based test for Crud default method {@code findAll()}.
 *
 * <p><b>Validates: Requirements 2.3, 2.6</b></p>
 */
class CrudFindAllPropertyTest {

    // -----------------------------------------------------------------------
    // Property 4: Consistência de findAll() com find()
    // -----------------------------------------------------------------------

    /**
     * Feature: core-api-enhancements, Property 4: Consistência de findAll() com find()
     *
     * <p>For any Crud implementation, the result of {@code findAll()} must equal
     * the list returned by {@code find().getContent()}.
     * When {@code find()} returns null, {@code findAll()} must return an empty list.</p>
     *
     * <p><b>Validates: Requirements 2.3, 2.6</b></p>
     */
    @Property(tries = 100)
    // Feature: core-api-enhancements, Property 4: Consistência de findAll() com find()
    void findAllIsConsistentWithFindContent(
            @ForAll @Size(max = 200) List<String> elements,
            @ForAll boolean findReturnsNull
    ) {
        StubCrud<String> crud = new StubCrud<>();

        if (findReturnsNull) {
            crud.setFindAllResult(null);
        } else {
            crud.setFindAllContent(elements);
        }

        List<String> findAllResult = crud.findAll();

        if (findReturnsNull) {
            assertEquals(List.of(), findAllResult,
                    "When find() returns null, findAll() must return an empty list");
        } else {
            assertEquals(elements, findAllResult,
                    "findAll() must equal find().getContent()");
        }
    }
}
