/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import org.demoiselle.jee.core.interfaces.security.DemoisellePrincipal;

/**
 *
 * @author 70744416353
 */
@RequestScoped
public class DemoisellePrincipalImpl implements DemoisellePrincipal, Cloneable {

    private String id;
    private String name;
    private List<String> roles;
    private Map<String, String> permissions;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<String> getRoles() {
        return roles;
    }

    @Override
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public Map<String, String> getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(Map<String, String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DemoisellePrincipalImpl other = (DemoisellePrincipalImpl) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{" + "\"id\"=" + id + ", \"name\"=" + name + ", \"roles\"=" + roles + ", \"permissions\"=" + permissions + '}';
    }

    @Override
    public DemoisellePrincipal clone() {
        try {
            return (DemoisellePrincipal) super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(DemoisellePrincipalImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
