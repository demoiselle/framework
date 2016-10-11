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
     * @return
     */
    public List<String> getRoles();

    /**
     *
     * @return
     */
    public Map<String, String> getPermissions();

    /**
     *
     * @param permissions
     */
    public void setPermissions(Map<String, String> permissions);

    /**
     *
     * @return
     */
    public DemoisellePrincipal clone();
}
