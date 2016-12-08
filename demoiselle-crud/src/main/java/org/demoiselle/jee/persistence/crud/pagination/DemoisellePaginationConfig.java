/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.crud.pagination;

import org.demoiselle.jee.configuration.annotation.Configuration;

@Configuration(prefix = "demoiselle.crud.pagination")
public class DemoisellePaginationConfig {
	
	private Integer defaultPagination = new Integer(50);

	public Integer getDefaultPagination() {
		return defaultPagination;
	}

}
