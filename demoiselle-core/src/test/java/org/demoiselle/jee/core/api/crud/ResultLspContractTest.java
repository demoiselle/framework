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
 * Verifies the Liskov-Substitution-safe contract of {@link Result} /
 * {@link MutableResult}.
 *
 * <ul>
 *   <li>A read-only {@link Result} that does not implement {@link MutableResult}
 *       throws {@link UnsupportedOperationException} from the deprecated
 *       {@code setContent} bridge.</li>
 *   <li>A {@link MutableResult} reference can always be mutated safely.</li>
 *   <li>An immutable result is never assignable to {@link MutableResult}, so
 *       generic mutable code cannot receive one at compile time.</li>
 * </ul>
 */
class ResultLspContractTest {

    /** A minimal read-only result relying entirely on the default bridge. */
    private static final class ReadOnly<T> implements Result<T> {
        private final List<T> content;
        ReadOnly(List<T> content) { this.content = content; }
        @Override public List<T> getContent() { return content; }
    }

    @Test
    void readOnlyResultSetContentThrows() {
        Result<String> r = new ReadOnly<>(List.of("a"));
        assertThrows(UnsupportedOperationException.class, () -> r.setContent(List.of("b")),
                "Read-only Result must throw from the deprecated setContent bridge");
        assertEquals(List.of("a"), r.getContent());
    }

    @Test
    void mutableResultCanBeMutatedSafely() {
        MutableResult<String> r = new StubResult<>(List.of("a"));
        r.setContent(List.of("x", "y"));
        assertEquals(List.of("x", "y"), r.getContent());
    }

    @Test
    void mutableResultIsAlsoAReadOnlyResult() {
        MutableResult<String> mutable = new StubResult<>(List.of("a"));
        Result<String> asReadOnly = mutable; // upcast always safe
        assertEquals(List.of("a"), asReadOnly.getContent());
    }

    @Test
    void immutableResultIsNotAssignableToMutableResult() {
        // A read-only implementation must not be a MutableResult, guaranteeing
        // that a MutableResult reference is always safe to mutate (LSP).
        Result<String> readOnly = new ReadOnly<>(List.of("a"));
        assertFalse(readOnly instanceof MutableResult,
                "Immutable results must not implement MutableResult");
    }
}
