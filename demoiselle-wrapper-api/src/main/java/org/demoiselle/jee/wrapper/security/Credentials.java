package org.demoiselle.jee.wrapper.security;

import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author PauloGladson
 */
public class Credentials implements Serializable {

    private static final Logger LOG = getLogger(Credentials.class.getName());

    private String name;
    private String username;
    private String password;

    /**
     *
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public boolean validate() {
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.username);
        return hash;
    }

    /**
     *
     * @param obj
     * @return
     */
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
        final Credentials other = (Credentials) obj;
        return Objects.equals(this.username, other.username);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "Credentials{" + "username=" + username + ", password=" + password + '}';
    }

}
