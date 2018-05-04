package org.demoiselle.jee.mock;

import java.io.Serializable;
import java.util.Objects;

public class Cep implements Serializable {

    private Integer id;
    private String logradouro;
    private String cep;
    private String uf;
    private String cidade;
    private String bairroIni;
    private String bairroFim;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getBairroIni() {
        return bairroIni;
    }

    public void setBairroIni(String bairroIni) {
        this.bairroIni = bairroIni;
    }

    public String getBairroFim() {
        return bairroFim;
    }

    public void setBairroFim(String bairroFim) {
        this.bairroFim = bairroFim;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.id);
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
        final Cep other = (Cep) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Cep{" + "id=" + id + ", logradouro=" + logradouro + ", cep=" + cep + ", uf=" + uf + ", cidade=" + cidade + ", bairroIni=" + bairroIni + ", bairroFim=" + bairroFim + '}';
    }

}
