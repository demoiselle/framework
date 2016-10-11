/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.message;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

/**
 *
 * @author 70744416353
 */
@MessageBundle
public interface DemoiselleSecurityMessages {

    /**
     *
     * @param operacao
     * @param recurso
     * @return
     */
    @MessageTemplate("{access-checking-permission}")
    String accessCheckingPermission(String operacao, String recurso);

    /**
     *
     * @param usuario
     * @param operacao
     * @param recurso
     * @return
     */
    @MessageTemplate("{access-denied}")
    String accessDenied(String usuario, String operacao, String recurso);

    /**
     *
     * @return
     */
    @MessageTemplate("{user-not-authenticated}")
    String userNotAuthenticated();

    /**
     *
     * @return
     */
    @MessageTemplate("{invalid-credentials}")
    String invalidCredentials();

    /**
     *
     * @param role
     * @return
     */
    @MessageTemplate("{does-not-have-role}")
    String doesNotHaveRole(String role);

    /**
     *
     * @param operacao
     * @param recurso
     * @return
     */
    @MessageTemplate("{does-not-have-permission}")
    String doesNotHavePermission(String operacao, String recurso);

}
