/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.crud;

import java.util.List;

/**
 * Read-only view over a collection of results produced by a CRUD operation.
 *
 * <p>
 * Historically this interface exposed both {@code getContent()} and
 * {@code setContent(List)}. Immutable implementations such as
 * {@code PageResult} were forced to override
 * {@code setContent} to throw {@link UnsupportedOperationException}, which broke
 * the Liskov Substitution Principle (LSP): code holding a {@code Result<T>}
 * reference could not safely call {@code setContent} without risking a runtime
 * failure depending on the concrete implementation.
 * </p>
 *
 * <p>
 * As of Demoiselle 4.1, {@code Result} is a <strong>read-only</strong> contract:
 * the only guaranteed operation is {@link #getContent()}. Mutability is now an
 * <em>opt-in</em> capability expressed by {@link MutableResult}. New code that
 * needs to populate a result should depend on {@link MutableResult} instead of
 * calling {@link #setContent(List)} on a generic {@code Result} reference.
 * </p>
 *
 * <h2>Backward compatibility bridge</h2>
 * <p>
 * To preserve source and binary compatibility with existing callers, the
 * previously-abstract {@link #setContent(List)} method is retained as a
 * {@code default} method and marked {@link Deprecated}. Its default behaviour is
 * to throw {@link UnsupportedOperationException} — a read-only result cannot be
 * mutated. Mutable implementations override it via {@link MutableResult}.
 * </p>
 *
 * <p>
 * This design guarantees that a fresh, generic use of {@code Result<T>} never
 * silently mutates an immutable structure: callers that require mutation must
 * declare {@link MutableResult}, whereas immutable results (e.g.
 * {@code PageResult}) simply do not implement the mutating sub-type. Legacy code
 * still compiles, but the deprecation clearly signals the migration path.
 * </p>
 *
 * @param <T> the element type
 * @author SERPRO
 */
public interface Result<T> {

	/**
	 * Returns the content of this result. Implementations must never return
	 * {@code null}; an empty list is used to represent the absence of elements.
	 *
	 * @return the (possibly empty) list of elements; never {@code null}
	 */
	public List<T> getContent();

	/**
	 * Replaces the content of this result.
	 *
	 * @param content the new content
	 * @throws UnsupportedOperationException if this result is read-only
	 * @deprecated Mutation is no longer part of the read-only {@code Result}
	 *             contract. Depend on {@link MutableResult} when you need to set
	 *             the content, or use an immutable factory such as
	 *             an immutable {@code PageResult} factory.
	 *             This default method exists only as a compatibility bridge and
	 *             throws {@link UnsupportedOperationException} unless overridden
	 *             by a {@link MutableResult} implementation.
	 */
	@Deprecated(since = "4.1.0", forRemoval = true)
	default void setContent(List<T> content) {
		throw new UnsupportedOperationException(
				"Result is read-only; implement MutableResult to allow setContent(...)");
	}

}
