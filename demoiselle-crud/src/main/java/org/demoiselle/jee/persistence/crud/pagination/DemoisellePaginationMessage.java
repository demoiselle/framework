package org.demoiselle.jee.persistence.crud.pagination;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

@MessageBundle
public interface DemoisellePaginationMessage {
	
	@MessageTemplate("{invalid-range-parameters}")
	String invalidRangeParameters();

	@MessageTemplate("{default-pagination-number-exceed}")
	String defaultPaginationNumberExceed(Integer defaultPaginationNumber);

}
