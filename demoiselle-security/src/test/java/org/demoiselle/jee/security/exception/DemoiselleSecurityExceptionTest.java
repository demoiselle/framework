/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.exception;

import java.util.HashMap;
import javax.inject.Inject;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author 70744416353
 */
public class DemoiselleSecurityExceptionTest {

    private DemoiselleSecurityException instance;

    public DemoiselleSecurityExceptionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        instance = new DemoiselleSecurityException("Teste");
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getStatusCode method, of class DemoiselleSecurityException.
     */
    @Test
    public void testGetStatusCode() {
        System.out.println("getStatusCode");
        int expResult = 401;
        int result = instance.getStatusCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of addMessage method, of class DemoiselleSecurityException.
     */
    @Test
    public void testAddMessage() {
        System.out.println("addMessage");
        instance = new DemoiselleSecurityException("Teste", 500);
        String field = "Teste";
        String msg = "Teste";
        instance.addMessage(field, msg);
    }

    /**
     * Test of getMessages method, of class DemoiselleSecurityException.
     */
    @Test
    public void testGetMessages() {
        System.out.println("getMessages");
        HashMap<String, String> expResult = new HashMap<>();
        HashMap<String, String> result = instance.getMessages();
        assertEquals(expResult, result);
    }

}
