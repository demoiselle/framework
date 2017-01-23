/*
  * Demoiselle Framework
  *
  * License: GNU Lesser General Public License (LGPL), version 3 or later.
  * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.filter;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

/**
 * @author SERPRO
 *
 */
@MessageBundle
public interface FilterHelperMessage {
    
    @MessageTemplate("{filter-field-request-not-exists-on-search-field}")
    String filterFieldRequestNotExistsOnSearchField(String field);

}
