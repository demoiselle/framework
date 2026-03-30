/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenType;
import org.jboss.weld.junit5.auto.ActivateScopes;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

/**
 *
 * @author SERPRO
 */
@EnableAutoWeld
@ActivateScopes(RequestScoped.class)
@AddBeanClasses(TokenImpl.class)
class TokenImplTest {

    @Inject
    private Token instance;

    @Test
    void test11() {
        String key = "123456789";
        instance.setKey(key);
    }

    @Test
    void test12() {
        instance.setKey("123456789");
        String expResult = "123456789";
        String result = instance.getKey();
        assertEquals(expResult, result);
    }

    @Test
    void test13() {
        instance.setType(TokenType.JWT);
    }

    @Test
    void test14() {
        instance.setType(TokenType.JWT);
        TokenType expResult = TokenType.JWT;
        TokenType result = instance.getType();
        assertEquals(expResult, result);
    }

    @Test
    void test15() {
        instance.setKey("123456789");
        int result = instance.hashCode();
        assertNotEquals(0, result);
    }

    @Test
    void test16() {
        Object obj = null;
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
    }

    @Test
    void test17() {
        instance.setKey("123456789");
        instance.setType(TokenType.JWT);
        String expResult = "{\"key\":\"123456789\", \"type\":\"JWT\"}";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
