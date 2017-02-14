/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

/**
 * 
 * Messages used to inform user about CRUD feature
 * 
 * @author SERPRO 
 */
@MessageBundle
public interface CrudMessage {
    
    @MessageTemplate("{method-find-not-implemented}")
    String methodFindNotImplemented();
    
    @MessageTemplate("{field-request-does-not-exists-on-search-field}")
    String fieldRequestDoesNotExistsOnSearchField(String field);
    
    @MessageTemplate("{field-request-does-not-exists-on-object}")
    String fieldRequestDoesNotExistsOnObject(String field, String className);

}
