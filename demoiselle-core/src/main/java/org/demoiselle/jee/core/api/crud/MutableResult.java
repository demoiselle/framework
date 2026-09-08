/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.crud;

import java.util.List;

/**
 * A {@link Result} whose content may be replaced after construction.
 *
 * <p>
 * This interface makes mutability an explicit, opt-in capability. Code that
 * needs to populate a result should depend on {@code MutableResult} rather than
 * calling the deprecated {@link Result#setContent(List)} bridge on a generic
 * {@link Result} reference. Immutable implementations (for example
 * {@code PageResult}) intentionally do
 * <strong>not</strong> implement this interface, so a {@code MutableResult}
 * reference is always safe to mutate — restoring the Liskov Substitution
 * Principle.
 * </p>
 *
 * @param <T> the element type
 * @author SERPRO
 */
public interface MutableResult<T> extends Result<T> {

	/**
	 * Replaces the content of this result.
	 *
	 * <p>
	 * Implementations are encouraged to store a defensive, immutable copy of the
	 * provided list and to normalise a {@code null} argument to an empty list so
	 * that {@link #getContent()} never returns {@code null}.
	 * </p>
	 *
	 * @param content the new content; a {@code null} value should be treated as
	 *                an empty list
	 */
	@Override
	@SuppressWarnings("deprecation")
	void setContent(List<T> content);

}
