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
     * @return
     */
    @MessageTemplate("{version}")
	String version();

    /**
     *
     * @return
     */
    @MessageTemplate("{engine-on}")
	String engineOn();

    /**
     *
     * @return
     */
    @MessageTemplate("{resource-not-found}")
	String resourceNotFound();

    /**
     *
     * @param key
     * @return
     */
    @MessageTemplate("{key-not-found}")
	String keyNotFound(String key);

    /**
     *
     * @param interfaceString
     * @param implementations
     * @return
     */
    @MessageTemplate("{ambiguous-strategy-resolution}")
	String ambiguousStrategyResolution(String interfaceString, String implementations);

    /**
     *
     * @param implementation
     * @return
     */
    @MessageTemplate("{ambiguous-bean-resolution}")
	String ambiguousBeanResolution(String implementation);

    /**
     *
     * @param bean
     * @return
     */
    @MessageTemplate("{bean-not-found}")
	String beanNotFound(String bean);

    /**
     *
     * @param object
     * @param scope
     * @return
     */
    @MessageTemplate("{store-not-found}")
	String storeNotFound(String object, String scope);

    /**
     *
     * @param clazz
     * @param ex
     * @return
     */
    @MessageTemplate("{more-than-one-exceptionhandler-defined-for-same-class}")
	String moreThanOneExceptionhandlerDefinedForSameClass(String clazz, String ex);

    /**
     *
     * @param ex
     * @return
     */
    @MessageTemplate("{handling-exception}")
	String handlingException(String ex);

    /**
     *
     * @return
     */
    @MessageTemplate("{taking-off}")
	String takingOff();

    /**
     *
     * @return
     */
    @MessageTemplate("{engine-off}")
	String engineOff();

    /**
     *
     * @param util
     * @return
     */
    @MessageTemplate("{setting-up-bean-manager}")
	String settingUpBeanManagerException(String util);

    /**
     *
     * @return
     */
    @MessageTemplate("{processing-fail}")
	String processingFail();

}