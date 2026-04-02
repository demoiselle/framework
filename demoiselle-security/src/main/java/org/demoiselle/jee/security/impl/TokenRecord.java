/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import java.util.Objects;

import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenType;

/**
 * Immutable record implementation of {@link Token}.
 * <p>
 * Equality and hash code are based solely on the {@code key} field,
 * maintaining compatibility with the behaviour of {@link TokenImpl}.
 * </p>
 *
 * @author Demoiselle Framework
 */
public record TokenRecord(String key, TokenType type) implements Token {

    @Override
    public String getKey() { return key; }

    @Override
    public TokenType getType() { return type; }

    @Override
    public void setKey(String key) {
        throw new UnsupportedOperationException("TokenRecord é imutável");
    }

    @Override
    public void setType(TokenType type) {
        throw new UnsupportedOperationException("TokenRecord é imutável");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token other)) return false;
        return Objects.equals(this.key, other.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }
}
