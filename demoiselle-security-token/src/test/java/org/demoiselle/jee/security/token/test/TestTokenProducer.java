/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.token.test;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.security.impl.TokenImpl;

/**
 * Test-only CDI producer that provides a mutable {@link Token} for unit tests.
 */
public class TestTokenProducer {

    @Produces
    @RequestScoped
    @SuppressWarnings("deprecation")
    public Token produceToken() {
        return new TokenImpl();
    }
}
