/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.pagination;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

@MessageBundle
// TODO merge
public interface DemoisellePaginationMessage {
	
	@MessageTemplate("{invalid-range-parameters}")
	String invalidRangeParameters();

	@MessageTemplate("{default-pagination-number-exceed}")
	String defaultPaginationNumberExceed(Integer defaultPaginationNumber);

}
		