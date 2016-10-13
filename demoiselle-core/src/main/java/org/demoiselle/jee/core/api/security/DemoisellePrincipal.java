/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.core.api.security;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 *
 * @author 70744416353
 */
public interface DemoisellePrincipal extends Principal {

    /**
     *
     * @return
     */
    public String getIdentity();

    /**
     *
     * @param id
     */
    public void setIdentity(String id);

    /**
     *
     * @param name
     */
    public void setName(String name);

    /**
     *
     * @param roles
     */
    public void setRoles(List<String> roles);

    /**
     *
     * @param role
     */
    public void addRole(String role);

    /**
     *
     * @return
     */
    public List<String> getRoles();

    /**
     *
     * @return
     */
    public Map<String, List<String>> getPermissions();

    /**
     *
     * @param resource
     * @return
     */
    public List<String> getPermissions(String resource);

    /**
     *
     * @param resource
     * @param operetion
     */
    public void addPermission(String resource, String operetion);

    /**
     *
     * @param permissions
     */
    public void setPermissions(Map<String, List<String>> permissions);

    /**
     *
     * @return
     */
    public Map<String, List<String>> getParams();

    /**
     *
     * @param key
     * @return
     */
    public List<String> getParams(String key);

    /**
     *
     * @param key
     * @param value
     */
    public void addParam(String key, String value);

    /**
     *
     * @param params
     */
    public void setParams(Map<String, List<String>> params);

    /**
     *
     * @return
     */
    public DemoisellePrincipal clone();
}
