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
public interface SecurityContext extends Serializable {

    /**
     * Checks if a specific user is logged in.
     *
     * @return {@code true} if the user is logged in
     */
    boolean isLoggedIn();

    /**
     * @throws NotLoggedInException if there is no user logged in a specific
     * session
     */
    void checkLoggedIn();

    /**
     * Checks if the logged user has permission to execute an specific operation
     * on a specific resource.
     *
     * @param resource resource to be checked
     * @param operation operation to be checked
     * @return {@code true} if the user has the permission
     * @throws AuthorizationException When the permission checking fails, this
     * exception is thrown.
     * @throws NotLoggedInException if there is no user logged in a specific
     * session.
     */
    boolean hasPermission(String resource, String operation);

    /**
     * Checks if the logged user has an specific role
     *
     * @param role role to be checked
     * @return {@code true} if the user has the role
     * @throws AuthorizationException When the permission checking fails, this
     * exception is thrown.
     * @throws NotLoggedInException if there is no user logged in a specific
     * session.
     */
    boolean hasRole(String role);

    /**
     * Return the user logged in the session.
     *
     * @param token
     * @return the user logged in a specific authenticated session. If there is
     * no active session {@code null} is returned.
     */
    DemoisellePrincipal getUser();

    void setUser(DemoisellePrincipal loggedUser);
    
}
