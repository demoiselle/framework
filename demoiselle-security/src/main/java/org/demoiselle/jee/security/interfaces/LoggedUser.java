/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interfaces;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;

/**
 * <p>
 * Structure used to handle both authentication and authorizations mechanisms.
 * </p>
 *
 * @author SERPRO
 */
@RequestScoped
public interface LoggedUser extends Principal, Serializable {

    public String getId();

    public void setId(String id);

    public Map<String, String> getPermissions();

    public void setPermissions(Map<String, String> premissions);

    public List<String> getRoles();

    public void setRoles(List<String> roles);

}
