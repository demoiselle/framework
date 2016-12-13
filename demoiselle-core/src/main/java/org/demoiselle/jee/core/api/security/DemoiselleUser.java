/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.security;

import java.security.Principal;
import java.util.List;
import java.util.Map;

//TODO JAVADOC
/**
 *
 * @author SERPRO
 */
public interface DemoiselleUser extends Principal, Cloneable {

    /**
     * @return Identity user
     */
    public String getIdentity();

    /**
     * @param id Identity user
     */
    public void setIdentity(String id);

    /**
     * @param name Principal name
     */
    public void setName(String name);

    /**
     * @param role Role name
     */
    public void addRole(String role);

    /**
     * @param role Role name
     */
    public void removeRole(String role);

    /**
     * @return List of roles
     */
    public List<String> getRoles();

    /**
     *
     * @return List of permissions
     */
    public Map<String, List<String>> getPermissions();

    /**
     * @param resource Resource name
     * @return List of permissions
     */
    public List<String> getPermissions(String resource);

    /**
     *
     * @param resource Resource name
     * @param operation Operation name
     */
    public void addPermission(String resource, String operation);

    /**
     *
     * @param resource Resource name
     * @param operation Operation name
     */
    public void removePermission(String resource, String operation);

    /**
     *
     * @return Params
     */
    public Map<String, List<String>> getParams();

    /**
     *
     * @param key Key parameter
     * @return List of parameter
     */
    public List<String> getParams(String key);

    /**
     *
     * @param key Key parameter
     * @param value Value parameter
     */
    public void addParam(String key, String value);

    /**
     *
     * @param key Key parameter
     * @param value Value parameter
     */
    public void removeParam(String key, String value);


    /**
     *
     * @return Principal clone
     */
    public DemoiselleUser clone();
}
