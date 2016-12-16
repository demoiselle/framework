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
public interface DemoiselleSecurityJWTMessages {

    @MessageTemplate("{general}")
    String general();

    @MessageTemplate("{expired}")
    String expired();

    @MessageTemplate("{master}")
    String master();

    @MessageTemplate("{slave}")
    String slave();

    @MessageTemplate("{error}")
    String error();

    @MessageTemplate("{choose-type}")
    String chooseType();

    @MessageTemplate("{not-type}")
    String notType();

    @MessageTemplate("{put-key}")
    String putKey();

    @MessageTemplate("{not-jwt}")
    String notJwt();

    @MessageTemplate("{type-server}")
    String typeServer(String text);

    @MessageTemplate("{primary-key}")
    String primaryKey(String text);

    @MessageTemplate("{public-key}")
    String publicKey(String text);

    @MessageTemplate("{age-token}")
    String ageToken(String text);

    @MessageTemplate("{issuer}")
    String issuer(String text);

    @MessageTemplate("{audience}")
    String audience(String text);

}
