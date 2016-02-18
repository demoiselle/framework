/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ${package}.cover;

import ${package}.entity.User;

/**
 *
 * @author gladson
 */
public class UserCover {

    private Long id;
    private String nome;
    private String perfil;

    public UserCover(User user) {
        this.id = user.getId();
        this.nome = user.getName();
        this.perfil = user.getPerfil();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

}
