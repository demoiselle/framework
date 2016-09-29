/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.core.interfaces.security;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 *
 * @author 70744416353
 */
public interface DemoisellePrincipal extends Principal {

    public String getId();

    public void setId(String id);

    public void setName(String name);

    public void setRoles(List<String> roles);

    public List<String> getRoles();

    public Map<String, String> getPermissions();

    public void setPermissions(Map<String, String> permissions);

    public DemoisellePrincipal clone();
}
