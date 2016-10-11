package org.demoiselle.jee.security.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import org.demoiselle.jee.core.api.security.DemoisellePrincipal;

/**
 *
 * @author 70744416353
 */
@RequestScoped
public class DemoisellePrincipalImpl implements DemoisellePrincipal, Cloneable {

    private String identity;
    private String name;
    private List<String> roles;
    private Map<String, String> permissions;

    /**
     *
     * @return
     */
    @Override
    public String getIdentity() {
        return identity;
    }

    /**
     *
     * @param id
     */
    @Override
    public void setIdentity(String id) {
        this.identity = id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    @Override
    public List<String> getRoles() {
        return roles;
    }

    /**
     *
     * @param roles
     */
    @Override
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    /**
     *
     * @return
     */
    @Override
    public Map<String, String> getPermissions() {
        return permissions;
    }

    /**
     *
     * @param permissions
     */
    @Override
    public void setPermissions(Map<String, String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.identity);
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
        return Objects.equals(this.identity, other.identity);
    }

    @Override
    public String toString() {
        return "{identity:\"" + identity + "\", name:\"" + name + "\", roles:" + roles + ", permissions:" + permissions + '}';
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
