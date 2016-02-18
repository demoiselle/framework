package ${package}.entity;

import java.io.Serializable;
import java.security.Principal;
import javax.enterprise.context.RequestScoped;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author 70744416353
 */
@Entity
@RequestScoped
public class User implements Principal, Serializable {

    private static final long serialVersionUID = 5625711959333905292L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Size(min = 1)
    @Column(name = "nome")
    private String name;
    private String email;
    private String fone;
    private String senha;
    private String perfil;

    @Transient
    private String token;

    @Transient
    private String ip;

    /**
     *
     * @return
     */
    public Long getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    /**
     *
     * @param Name
     */
    public void setName(String Name) {
        this.name = Name;
    }

    /**
     *
     * @return
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    public String getFone() {
        return fone;
    }

    public void setFone(String fone) {
        this.fone = fone;
    }

    /**
     *
     * @return
     */
    public String getToken() {
        return token;
    }

    /**
     *
     * @param token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     *
     * @return
     */
    public String getIp() {
        return ip;
    }

    /**
     *
     * @param ip
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getPerfil() {
        return perfil.toUpperCase();
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil.toUpperCase();
    }

}
