/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.message;

import org.apache.deltaspike.core.api.message.MessageBundle;
import org.apache.deltaspike.core.api.message.MessageTemplate;

@MessageBundle
public interface DemoiselleSecurityMessages {

    @MessageTemplate("{access-checking-permission}")
    String accessCheckingPermission(String usuario, String operacao, String recurso);

    @MessageTemplate("{access-checking-role}")
    String accessCheckingRole(String usuario, String role);

    @MessageTemplate("{access-allowed}")
    String accessAllowed(String usuario, String operacao, String recurso);

    @MessageTemplate("{access-denied}")
    String accessDenied(String usuario, String operacao, String recurso);

    @MessageTemplate("{user-not-authenticated}")
    String userNotAuthenticated();

    @MessageTemplate("{invalid-credentials}")
    String invalidCredentials();

    @MessageTemplate("{does-not-have-role}")
    String doesNotHaveRole(String usuario, String role);

    @MessageTemplate("{does-not-have-permission}")
    String doesNotHavePermission(String usuario, String operacao, String recurso);

}
