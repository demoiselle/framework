/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import java.util.Objects;

import javax.enterprise.context.RequestScoped;

import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenType;

/**
 * <p>
 * Object loaded to each request containing the token sent in http header
 * </p>
 *
 * @see
 * <a href="https://demoiselle.gitbooks.io/documentacao-jee/content/security.html">Documentation</a>
 *
 * @author SERPRO
 */
@RequestScoped
public class TokenImpl implements Token {

    private String key;
    private TokenType type;

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public void setType(TokenType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.key);
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
        final TokenImpl other = (TokenImpl) obj;
        return Objects.equals(this.key, other.key);
    }

    @Override
    public String toString() {
        return "{" + "\"key\":\"" + key + "\", \"type\":\"" + type.toString() + "\"}";
    }

}
