/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author SERPRO
 */
@RunWith(CdiTestRunner.class)
@TestControl(startScopes = RequestScoped.class)
public class TokenImplTest {

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Inject
    private Token instance;

    public TokenImplTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test11() {
        String key = "123456789";
        instance.setKey(key);
    }

    @Test
    public void test12() {
        String expResult = "123456789";
        String result = instance.getKey();
        assertEquals(expResult, result);
    }

    @Test
    public void test13() {
        String type = "Teste";
        instance.setType(TokenType.JWT);
    }

    @Test
    public void test14() {
        TokenType expResult = TokenType.JWT;
        TokenType result = instance.getType();
        assertEquals(expResult, result);
    }

    @Test
    public void test15() {
        int expResult = 0;
        int result = instance.hashCode();
        assertNotEquals(expResult, result);
    }

    @Test
    public void test16() {
        Object obj = null;
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
    }

    @Test
    public void test17() {
        String expResult = "{\"key\":\"123456789\", \"type\":\"JWT\"}";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
