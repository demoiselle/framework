package org.demoiselle.jee.security.jwt.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import org.demoiselle.jee.core.api.security.DemoisellePrincipal;
import org.demoiselle.jee.security.impl.DemoisellePrincipalImpl;

/**
 *
 * @author 70744416353
 */
@Default
@ApplicationScoped
public class DemoisellePrincipalMock implements DemoisellePrincipal, Cloneable {

    private String identity;
    private String name;
    private List<String> roles;
    private Map<String, List<String>> permissions;
    private Map<String, List<String>> params;

    /**
     *
     */
    public DemoisellePrincipalMock() {
        this.roles = new ArrayList<>();
        this.permissions = new HashMap<>();
        this.params = new HashMap<>();
    }

    @Override
    public String getIdentity() {
        return identity;
    }

    @Override
    public void setIdentity(String identity) {
        this.identity = identity;
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
    public Map<String, List<String>> getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(Map<String, List<String>> permissions) {
        this.permissions = permissions;
    }

    @Override
    public Map<String, List<String>> getParams() {
        return params;
    }

    @Override
    public void setParams(Map<String, List<String>> params) {
        this.params = params;
    }

    @Override
    public void addRole(String role) {
        this.roles.add(role);
    }

    @Override
    public List<String> getPermissions(String resource) {
        return permissions.get(resource);
    }

    @Override
    public void addPermission(String resource, String operetion) {
        List<String> operations = permissions.get(resource);
        if (operations != null && !operations.isEmpty()) {
            permissions.get(resource).add(operetion);
        } else {
            List<String> newoperations = new ArrayList<>();
            newoperations.add(operetion);
            permissions.put(resource, newoperations);
        }
    }

    @Override
    public List<String> getParams(String key) {
        return params.get(key);
    }

    @Override
    public void addParam(String key, String value) {
        List<String> paramss = params.get(key);
        if (paramss != null && !paramss.isEmpty()) {
            params.get(key).add(value);
        } else {
            List<String> newparamss = new ArrayList<>();
            newparamss.add(value);
            params.put(key, newparamss);
        }
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
        final DemoisellePrincipalMock other = (DemoisellePrincipalMock) obj;
        return Objects.equals(this.identity, other.identity);
    }

    @Override
    public String toString() {
        return "{identity:\"" + identity + "\", name:\"" + name + "\"}";
    }

    @Override
    public DemoisellePrincipal clone() {
        try {
            return (DemoisellePrincipal) super.clone();
        } catch (CloneNotSupportedException ex) {
            getLogger(DemoisellePrincipalImpl.class.getName()).log(SEVERE, null, ex);
        }
        return null;
    }

}
