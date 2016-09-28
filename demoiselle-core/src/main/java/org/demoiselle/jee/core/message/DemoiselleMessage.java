/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.message;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

@MessageBundle
public interface DemoiselleMessage {

	@MessageTemplate("{version}")
	String version();

	@MessageTemplate("{engine-on}")
	String engineOn();

	@MessageTemplate("{resource-not-found}")
	String resourceNotFound();

	@MessageTemplate("{key-not-found}")
	String keyNotFound(String key);

	@MessageTemplate("{ambiguous-strategy-resolution}")
	String ambiguousStrategyResolution(String interfaceString, String implementations);

	@MessageTemplate("{ambiguous-bean-resolution}")
	String ambiguousBeanResolution(String implementation);

	@MessageTemplate("{bean-not-found}")
	String beanNotFound(String bean);

	@MessageTemplate("{store-not-found}")
	String storeNotFound(String object, String scope);

	@MessageTemplate("{more-than-one-exceptionhandler-defined-for-same-class}")
	String moreThanOneExceptionhandlerDefinedForSameClass(String clazz, String ex);

	@MessageTemplate("{handling-exception}")
	String handlingException(String ex);

	@MessageTemplate("{taking-off}")
	String takingOff();

	@MessageTemplate("{engine-off}")
	String engineOff(String ex);

	@MessageTemplate("{setting-up-bean-manager}")
	String settingUpBeanManagerException(String util);

	@MessageTemplate("{processing-fail}")
	String processingFail();

}