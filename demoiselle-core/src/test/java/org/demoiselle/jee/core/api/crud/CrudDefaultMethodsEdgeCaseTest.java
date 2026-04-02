/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.crud;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for edge cases in Crud default methods when {@code find()} returns null.
 *
 * <p><b>Validates: Requirements 2.6, 2.7</b></p>
 */
class CrudDefaultMethodsEdgeCaseTest {

    // Feature: core-api-enhancements — Edge cases for count() and findAll()

    @Test
    void countReturnsZeroWhenFindReturnsNull() {
        StubCrud<String> crud = new StubCrud<>();
        crud.setFindAllResult(null);

        assertEquals(0L, crud.count(),
                "count() must return 0L when find() returns null");
    }

    @Test
    void findAllReturnsEmptyListWhenFindReturnsNull() {
        StubCrud<String> crud = new StubCrud<>();
        crud.setFindAllResult(null);

        List<String> result = crud.findAll();

        assertNotNull(result, "findAll() must never return null");
        assertTrue(result.isEmpty(),
                "findAll() must return an empty list when find() returns null");
    }

    @Test
    void countReturnsZeroWhenResultContentIsNull() {
        StubCrud<String> crud = new StubCrud<>();
        crud.setFindAllResult(new StubResult<>(null));

        assertEquals(0L, crud.count(),
                "count() must return 0L when find().getContent() returns null");
    }

    @Test
    void findAllReturnsEmptyListWhenResultContentIsNull() {
        StubCrud<String> crud = new StubCrud<>();
        crud.setFindAllResult(new StubResult<>(null));

        List<String> result = crud.findAll();

        assertNotNull(result, "findAll() must never return null");
        assertTrue(result.isEmpty(),
                "findAll() must return an empty list when find().getContent() returns null");
    }

    @Test
    void existsReturnsFalseWhenEntityNotFound() {
        StubCrud<String> crud = new StubCrud<>();

        assertFalse(crud.exists("nonexistent"),
                "exists() must return false when find(id) returns null");
    }

    @Test
    void existsReturnsTrueWhenEntityFound() {
        StubCrud<String> crud = new StubCrud<>();
        crud.putEntity("key1", "value1");

        assertTrue(crud.exists("key1"),
                "exists() must return true when find(id) returns non-null");
    }

    @Test
    void countReturnsCorrectSizeForNonEmptyResult() {
        StubCrud<String> crud = new StubCrud<>();
        crud.setFindAllContent(List.of("a", "b", "c"));

        assertEquals(3L, crud.count(),
                "count() must return the size of find().getContent()");
    }

    @Test
    void findAllReturnsContentForNonEmptyResult() {
        StubCrud<String> crud = new StubCrud<>();
        List<String> expected = List.of("a", "b", "c");
        crud.setFindAllContent(expected);

        assertEquals(expected, crud.findAll(),
                "findAll() must return find().getContent()");
    }

    @Test
    void countReturnsZeroForEmptyResult() {
        StubCrud<String> crud = new StubCrud<>();
        crud.setFindAllContent(List.of());

        assertEquals(0L, crud.count(),
                "count() must return 0 for empty content list");
    }

    @Test
    void findAllReturnsEmptyListForEmptyResult() {
        StubCrud<String> crud = new StubCrud<>();
        crud.setFindAllContent(List.of());

        assertTrue(crud.findAll().isEmpty(),
                "findAll() must return empty list for empty content");
    }
}
