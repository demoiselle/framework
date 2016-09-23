/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interfaces;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;
import java.util.Set;
import org.demoiselle.jee.security.exception.AuthorizationException;
import org.demoiselle.jee.security.exception.NotLoggedInException;

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
     * @return the user logged in a specific authenticated session. If there is
     * no active session {@code null} is returned.
     */
    Principal getUser();

    void setUser(Principal principal);

    String getToken();

    void setToken(String token);

    void setRoles(Set<String> roles);

    void setPermission(Map<String, String> permissions);

    Set<String> getResources(String operation);

    Set<String> getOperations(String resources);

}
