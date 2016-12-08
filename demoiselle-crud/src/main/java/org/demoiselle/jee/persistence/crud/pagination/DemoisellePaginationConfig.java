package org.demoiselle.jee.persistence.crud.pagination;

import org.demoiselle.jee.configuration.annotation.Configuration;

@Configuration(prefix = "demoiselle.crud.pagination")
public class DemoisellePaginationConfig {
	
	private Integer defaultPagination = new Integer(50);

	public Integer getDefaultPagination() {
		return defaultPagination;
	}

}
