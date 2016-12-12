/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.lifecycle;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

/**
 * 
 * Represents the Messages used by Lifecycle feature
 * 
 * @author SERPRO
 *
 */
@MessageBundle
public interface LifecycleMessage {
	
	@MessageTemplate("{executing-method}")
	String executingMethod(String method);
	
}
