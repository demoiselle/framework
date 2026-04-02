/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.List;

import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.AbstractDAO;

/**
 * This classes implements {@link org.demoiselle.jee.core.api.crud.Result} to hold the results came from {@link AbstractDAO}
 * 
 * @author SERPRO
 */
public class ResultSet<T> implements Result<T>{
	
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