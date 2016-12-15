/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.exception;

import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 * @author SERPRO
 */
public class DemoiselleSecurityExceptionTest {

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }
    private DemoiselleSecurityException instance;

    public DemoiselleSecurityExceptionTest() {
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
        int expResult = 500;
        int result = instance.getStatusCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of addMessage method, of class DemoiselleSecurityException.
     */
    @Test
    public void testAddMessage() {
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
        HashMap<String, String> expResult = new HashMap<>();
        HashMap<String, String> result = instance.getMessages();
        assertEquals(expResult, result);
    }

}
