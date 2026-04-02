/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.crud;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for Crud default method {@code exists(id)}.
 *
 * <p><b>Validates: Requirement 2.1</b></p>
 */
class CrudExistsPropertyTest {

    // -----------------------------------------------------------------------
    // Property 2: Consistência de exists() com find(id)
    // -----------------------------------------------------------------------

    /**
     * Feature: core-api-enhancements, Property 2: Consistência de exists() com find(id)
     *
     * <p>For any Crud implementation and any id, the result of {@code exists(id)}
     * must equal {@code find(id) != null}. That is, exists returns true if and
     * only if find returns a non-null object.</p>
     *
     * <p><b>Validates: Requirement 2.1</b></p>
     */
    @Property(tries = 100)
    // Feature: core-api-enhancements, Property 2: Consistência de exists() com find(id)
    void existsIsConsistentWithFindById(
            @ForAll String id,
            @ForAll boolean entityPresent
    ) {
        StubCrud<String> crud = new StubCrud<>();

        if (entityPresent) {
            crud.putEntity(id, "entity-" + id);
        }
        // If entityPresent is false, the entity is not in the map, so find(id) returns null

        boolean existsResult = crud.exists(id);
        String findResult = crud.find(id);

        assertEquals(findResult != null, existsResult,
                "exists(id) must equal (find(id) != null)");
    }
}
