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
 * Messages used to inform user about Field feature
 * 
 * @author SERPRO
 */
@MessageBundle
public interface FieldHelperMessage {
    
    @MessageTemplate("{field-request-malformed}")
    String fieldRequestMalFormed(String fieldName, String field);
    
}
