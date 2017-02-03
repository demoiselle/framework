/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.field;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

/**
 * @author SERPRO
 *
 */
@MessageBundle
public interface FieldHelperMessage {

    @MessageTemplate("{field-request-does-not-exists-on-search-annotation-field}")
    String fieldRequestDoesNotExistsOnSearchField(String field);
    
    @MessageTemplate("{field-request-does-not-exists-on-object}")
    String fieldRequestDoesNotExistsOnObject(String field, String className);
    
}
