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
 * @author SERPRO
 */
@MessageBundle
public interface DemoiselleSecurityMessages {

    @MessageTemplate("{access-checking-permission}")
    String accessCheckingPermission(String operacao, String recurso);

    @MessageTemplate("{access-denied}")
    String accessDenied(String usuario, String operacao, String recurso);

    @MessageTemplate("{user-not-authenticated}")
    String userNotAuthenticated();

    @MessageTemplate("{invalid-credentials}")
    String invalidCredentials();

    @MessageTemplate("{does-not-have-role}")
    String doesNotHaveRole(String role);

    @MessageTemplate("{does-not-have-permission}")
    String doesNotHavePermission(String operacao, String recurso);

}
