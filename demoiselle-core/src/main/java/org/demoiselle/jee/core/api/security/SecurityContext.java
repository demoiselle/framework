/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.security;

/**
 *
 * @author 70744416353
 */
public interface SecurityContext {

    /**
	 *
	 * @return
	 */
	boolean isLoggedIn();

    /**
     *
     * @param resource
     * @param operation
     * @return
     */
    boolean hasPermission(String resource, String operation);

    /**
     *
     * @param role
     * @return
     */
    boolean hasRole(String role);

    /**
     *
     * @return
     */
    DemoisellePrincipal getUser();

    /**
     *
     * @param loggedUser
     */
    void setUser(DemoisellePrincipal loggedUser);

}
