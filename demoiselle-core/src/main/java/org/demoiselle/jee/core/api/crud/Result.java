/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.crud;

import java.util.List;

/**
 * 
 * @author SERPRO
 *
 */
public interface Result {

	public List<?> getContent();
	public void setContent(List<?> content);

}
