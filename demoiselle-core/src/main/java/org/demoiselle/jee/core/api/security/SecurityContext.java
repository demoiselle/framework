/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.security;


/**
 * Security context is the main object of demoiselle security, in it there are 
 * interactions that guarantee the use of the module, 
 * the JWT and Token submodules are connected to it.
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
    DemoiselleUser getUser();

    /**
     * Get User
     * @param issuer Issuer
     * @param audience Audience
     * @return Principal Principal
     */
    DemoiselleUser getUser(String issuer, String audience);

    /**
     *
     * @param loggedUser Principal
     */
    void setUser(DemoiselleUser loggedUser);

    /**
     * Set User
     * @param loggedUser Principal
     * @param issuer Issuer
     * @param audience Audience
     */
    void setUser(DemoiselleUser loggedUser, String issuer, String audience);

    /**
     * 
     * @param loggedUser Principal
     */
    void removeUser(DemoiselleUser loggedUser);

    /**
     * Checks if the user has at least one of the given roles.
     *
     * @param roles Role names to check
     * @return {@code true} if the user has at least one role, {@code false} otherwise
     */
    default boolean hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0) return false;
        for (String role : roles) {
            if (hasRole(role)) return true;
        }
        return false;
    }

    /**
     * Checks if the user has all of the given roles.
     *
     * @param roles Role names to check
     * @return {@code true} if the user has all roles, {@code false} otherwise
     */
    default boolean hasAllRoles(String... roles) {
        if (roles == null || roles.length == 0) return false;
        for (String role : roles) {
            if (!hasRole(role)) return false;
        }
        return true;
    }

}
