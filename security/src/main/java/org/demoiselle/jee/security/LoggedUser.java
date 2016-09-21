/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author 70744416353
 */
@RequestScoped
public class LoggedUser {

    private String id;
    private String username;
    private String email;
    private Map<String, String> premissions;
    private List<String> roles;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, String> getPremissions() {
        return premissions;
    }

    public void setPremissions(Map<String, String> premissions) {
        this.premissions = premissions;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

}
