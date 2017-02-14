/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import java.util.LinkedList;
import java.util.List;

import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.AbstractDAO;

/**
 * This classes implements {@link org.demoiselle.jee.core.api.crud.Result} to hold the results came from {@link AbstractDAO}
 * 
 * @author SERPRO
 */
public class ResultSet implements Result{
	
	private List<?> content = new LinkedList<>();
	
	@Override
	public List<?> getContent() {
        return content;
    }

    @Override
	public void setContent(List<?> content) {
		this.content = (List<?>) content;
	}

}