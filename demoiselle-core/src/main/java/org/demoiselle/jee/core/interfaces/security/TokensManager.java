/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.interfaces.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Structure used to handle both authentication and authorizations mechanisms.
 * </p>
 *
 * @author SERPRO
 */
public interface TokensManager extends Serializable {

    public Principal getUser();

    public void setUser(Principal user);

    public boolean validate();

}
