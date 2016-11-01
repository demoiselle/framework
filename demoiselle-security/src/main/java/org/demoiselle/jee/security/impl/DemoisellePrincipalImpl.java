package org.demoiselle.jee.security.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
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
    private Map<String, List<String>> permissions;
    private Map<String, List<String>> params;

    public DemoisellePrincipalImpl() {
        this.roles = new ArrayList<>();
        this.permissions = new ConcurrentHashMap<>();
        this.params = new ConcurrentHashMap<>();
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
        if (!this.roles.contains(role)) {
            this.roles.add(role);
        }
    }

    @Override
    public void removeRole(String role) {
        this.roles.remove(role);
    }

    @Override
    public List<String> getPermissions(String resource) {
        return permissions.get(resource);
    }

    @Override
    public void addPermission(String resource, String operetion) {
        List<String> operations = permissions.get(resource);
        if (operations != null && !operations.isEmpty()) {
            if (!permissions.get(resource).contains(operetion)) {
                permissions.get(resource).add(operetion);
            }
        } else {
            List<String> newoperations = new ArrayList<>();
            newoperations.add(operetion);
            permissions.put(resource, newoperations);
        }
    }

    @Override
    public void removePermission(String resource, String operetion) {
        List<String> operations = permissions.get(resource);
        if (operations != null && !operations.isEmpty()) {
            if (permissions.get(resource).contains(operetion)) {
                permissions.get(resource).remove(operetion);
            }
            if (operations.isEmpty()) {
                permissions.remove(resource);
            }
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
            if (!params.get(key).contains(value)) {
                params.get(key).add(value);
            }
        } else {
            List<String> newparamss = new ArrayList<>();
            newparamss.add(value);
            params.put(key, newparamss);
        }
    }

    @Override
    public void removeParam(String key, String value) {
        List<String> paramss = params.get(key);
        if (paramss != null && !paramss.isEmpty()) {
            if (params.get(key).contains(value)) {
                params.get(key).remove(value);
            }
            if (paramss.isEmpty()) {
                params.remove(key);
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + Objects.hashCode(this.identity);
        hash = 19 * hash + Objects.hashCode(this.name);
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
        return "{\"identity\":\"" + identity + "\", \"name\":\"" + name + "\"}";
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
