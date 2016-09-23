/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interfaces;

import java.io.Serializable;

import org.demoiselle.jee.security.LoggedUser;
import org.demoiselle.jee.security.Token;

/**
 * <p>
 * Structure used to handle both authentication and authorizations mechanisms.
 * </p>
 *
 * @author SERPRO
 */
public interface TokensManager extends Serializable {

    public LoggedUser getUser(Token token);

    public String create(LoggedUser user);

}
