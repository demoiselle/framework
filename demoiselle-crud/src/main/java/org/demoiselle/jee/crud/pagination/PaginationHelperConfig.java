/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import org.demoiselle.jee.configuration.annotation.Configuration;

/**
 * Class responsible for hold configuration about Pagination feature.
 * 
 * @author SERPRO
 */ 
@Configuration(prefix = "demoiselle.crud.pagination")
public class PaginationHelperConfig {
	
    private Boolean isGlobalEnabled = Boolean.TRUE;
	private Integer defaultPagination = new Integer(20);

	public Integer getDefaultPagination() {
		return defaultPagination;
	}

    public Boolean getIsGlobalEnabled() {
        return isGlobalEnabled;
    }

}
