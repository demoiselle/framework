/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.security;

/**
 *
 * @author SERPRO
 */
public interface SecurityContext {

    /**
     *
     * @return If the Principal is logged
     */
    boolean isLoggedIn();

    /**
     *
     * @param resource Resource name
     * @param operation Operation name
     * @return If the Principal has permission
     */
    boolean hasPermission(String resource, String operation);

    /**
     *
     * @param role Role name
     * @return If the Principal has permission
     */
    boolean hasRole(String role);

    /**
     *
     * @return Principal
     */
    DemoisellePrincipal getUser();

    /**
     *
     * @param loggedUser Principal
     */
    void setUser(DemoisellePrincipal loggedUser);

    /**
     *
     * @param loggedUser Principal
     */
    void removeUser(DemoisellePrincipal loggedUser);

}
