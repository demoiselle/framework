/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.List;

import org.demoiselle.jee.core.api.crud.MutableResult;
import org.demoiselle.jee.crud.AbstractDAO;

/**
 * Mutable {@link org.demoiselle.jee.core.api.crud.Result} implementation used to
 * hold the results produced by {@link AbstractDAO}.
 *
 * <p>
 * Because population happens after construction, this class implements
 * {@link MutableResult}. Callers that only read the result should depend on the
 * read-only {@link org.demoiselle.jee.core.api.crud.Result} super-type.
 * </p>
 *
 * @author SERPRO
 */
public class ResultSet<T> implements MutableResult<T> {

	private List<T> content = List.of();

	@Override
	public List<T> getContent() {
		return content;
	}

	@Override
	public void setContent(List<T> content) {
		this.content = content == null ? List.of() : List.copyOf(content);
	}

}
