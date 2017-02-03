/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.message;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

/**
 * Message class intended to be used by all framework.
 * 
 * @author SERPRO
 */
@MessageBundle
public interface DemoiselleMessage {

	/**
	 *
	 * @return start message
	 */
	@MessageTemplate("{start-message}")
	String startMessage();

	/**
	 *
	 * @return framework name
	 */
	@MessageTemplate("{framework-name}")
	String frameworkName();

	/**
	 *
	 * @return framework version
	 */
	@MessageTemplate("{version}")
	String version();

	/**
	 * 
	 * @return engine status
	 */
	@MessageTemplate("{engine-on}")
	String engineOn();

	/**
	 *
	 * @return message for resource not found
	 */
	@MessageTemplate("{resource-not-found}")
	String resourceNotFound();

	/**
	 *
	 * @param key search key
	 * @return message for key not found
	 */
	@MessageTemplate("{key-not-found}")
	String keyNotFound(String key);

	/**
	 *
	 * @param interfaceString interface name
	 * @param implementation implementations name
	 * @return message for ambiguous
	 */
	@MessageTemplate("{ambiguous-strategy-resolution}")
	String ambiguousStrategyResolution(String interfaceString, String implementation);

	/**
	 *
	 * @param implementation implementations name
	 * @return message for ambiguous
	 */
	@MessageTemplate("{ambiguous-bean-resolution}")
	String ambiguousBeanResolution(String implementation);

	/**
	 *
	 * @param bean bean name
	 * @return message for bean not found
	 */
	@MessageTemplate("{bean-not-found}")
	String beanNotFound(String bean);

	/**
	 *
	 * @param clazz class name
	 * @param ex exception handler name
	 * @return message for more than one exception handler defined for same class
	 */
	@MessageTemplate("{more-than-one-exceptionhandler-defined-for-same-class}")
	String moreThanOneExceptionhandlerDefinedForSameClass(String clazz, String ex);

	/**
	 *
	 * @param ex  Exception name           
	 * @return message for handling Exception
	 */
	@MessageTemplate("{handling-exception}")
	String handlingException(String ex);

	/**
	 *
	 * @return message for taking Off
	 */
	@MessageTemplate("{taking-off}")
	String takingOff();

	/**
	 *
	 * @return message for engine Off
	 */
	@MessageTemplate("{engine-off}")
	String engineOff();

	/**
	 *
	 * @param setting            
	 * @return message for setting up bean manager exception
	 */
	@MessageTemplate("{setting-up-bean-manager}")
	String settingUpBeanManagerException(String setting);

	/**
	 *
	 * @return message for processing fail
	 */
	@MessageTemplate("{processing-fail}")
	String processingFail();

	/**
	 *
	 * @param method
	 * @return message for executing-method
	 */
	@MessageTemplate("{executing-method}")
	String executingMethod(String method);

	/**
	 * 
	 * @param method
	 * @return message for startup method
	 */
	@MessageTemplate("{add-startup-method}")
	String addStartupMethod(String method);

	/**
	 * 
	 * @param method
	 * @return message for shutdown method
	 */
	@MessageTemplate("{add-shutdown-method}")
	String addShutdownMethod(String method);

}