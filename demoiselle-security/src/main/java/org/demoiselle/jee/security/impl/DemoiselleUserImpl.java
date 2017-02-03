/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.Response;

import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;

/**
 * <p>
 * The demoiselle is used to keep the user logged into request scope and
 * transported to the frontend by jwt
 * </p>
 *
 * @see
 * <a href="https://demoiselle.gitbooks.io/documentacao-jee/content/security.html">Documentation</a>
 *
 * @author SERPRO
 */
@RequestScoped
public class DemoiselleUserImpl implements DemoiselleUser, Cloneable {

    private String identity;
    private String name;
    private List<String> roles;
    private Map<String, String> params;
    private Map<String, List<String>> permissions;

    @PostConstruct
    public void init() {
        this.roles = new ArrayList<>();
        this.permissions = new ConcurrentHashMap<>();
        this.params = new ConcurrentHashMap<>();
    }

    public DemoiselleUserImpl() {
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
        return Collections.unmodifiableList(roles);
    }

    @Override
    public Map<String, List<String>> getPermissions() {
        return Collections.unmodifiableMap(permissions);
    }

    @Override
    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(params);
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
    public void addPermission(String resource, String operation) {
        List<String> operations = permissions.get(resource);
        if (operations != null && !operations.isEmpty()) {
            if (!permissions.get(resource).contains(operation)) {
                permissions.get(resource).add(operation);
            }
        } else {
            List<String> newoperations = new ArrayList<>();
            newoperations.add(operation);
            permissions.putIfAbsent(resource, newoperations);
        }
    }

    @Override
    public void removePermission(String resource, String operation) {
        List<String> operations = permissions.get(resource);
        if (operations != null && !operations.isEmpty()) {
            if (permissions.get(resource).contains(operation)) {
                permissions.get(resource).remove(operation);
            }
            if (operations.isEmpty()) {
                permissions.remove(resource);
            }
        }
    }

    @Override
    public String getParams(String key) {
        return params.get(key);
    }

    @Override
    public void addParam(String key, String value) {
        this.params.putIfAbsent(key, value);
    }

    @Override
    public void removeParam(String key) {
        this.params.remove(key);
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
        final DemoiselleUserImpl other = (DemoiselleUserImpl) obj;
        return Objects.equals(this.identity, other.identity);
    }

    @Override
    public String toString() {
        return "{\"identity\":\"" + identity + "\", \"name\":\"" + name + "\"}";
    }

    @Override
    public DemoiselleUser clone() {
        try {
            return (DemoiselleUser) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new DemoiselleSecurityException("Erro ao clonar", Response.Status.UNAUTHORIZED.getStatusCode(), ex);
        }

    }

}
