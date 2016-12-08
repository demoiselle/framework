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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author 70744416353
 */
@RunWith(CdiTestRunner.class)
@TestControl(startScopes = RequestScoped.class)
public class TokenImplTest {

    @Inject
    private Token instance;

    public TokenImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of setKey method, of class TokenImpl.
     */
    @Test
    public void test11() {
        System.out.println("setKey");
        String key = "123456789";
        instance.setKey(key);
    }

    /**
     * Test of getKey method, of class TokenImpl.
     */
    @Test
    public void test12() {
        System.out.println("getKey");
        String expResult = "123456789";
        String result = instance.getKey();
        assertEquals(expResult, result);
    }

    /**
     * Test of setType method, of class TokenImpl.
     */
    @Test
    public void test13() {
        System.out.println("setType");
        String type = "Teste";
        instance.setType(type);
    }

    /**
     * Test of getType method, of class TokenImpl.
     */
    @Test
    public void test14() {
        System.out.println("getType");
        String expResult = "Teste";
        String result = instance.getType();
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class TokenImpl.
     */
    @Test
    public void test15() {
        System.out.println("hashCode");
        int expResult = 0;
        int result = instance.hashCode();
        assertNotEquals(expResult, result);
    }

    /**
     * Test of equals method, of class TokenImpl.
     */
    @Test
    public void test16() {
        System.out.println("equals");
        Object obj = null;
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class TokenImpl.
     */
    @Test
    public void test17() {
        System.out.println("toString");
        String expResult = "Token{\"key\"=123456789, \"type\"=Teste}";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

}
