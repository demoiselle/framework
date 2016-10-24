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
 *
 * @author 70744416353
 */
@MessageBundle
public interface DemoiselleMessage {

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

    //TODO verificar "store?"
    /**
     *
     * @param object object name
     * @param scope scope
     * @return message for story not found
     */    
    @MessageTemplate("{store-not-found}")
	String storeNotFound(String object, String scope);

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
     * @param ex Exception name
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
     * @param setting Setting
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

}