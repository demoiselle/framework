/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;

import org.demoiselle.jee.rest.exception.DemoiselleRestExceptionMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author SERPRO
 */
class DemoiselleSecurityExceptionTest {

    private DemoiselleSecurityException instance;

    @BeforeEach
    void setUp() {
        instance = new DemoiselleSecurityException("Teste");
    }

    /**
     * Test of getStatusCode method, of class DemoiselleSecurityException.
     */
    @Test
    void testGetStatusCode() {
        int expResult = 500;
        int result = instance.getStatusCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of addMessage method, of class DemoiselleSecurityException.
     */
    @Test
    void testAddMessage() {
        instance = new DemoiselleSecurityException("Teste", 500);
        String field = "Teste";
        String msg = "Teste";
        instance.addMessage(field, msg);
    }

    /**
     * Test of getMessages method, of class DemoiselleSecurityException.
     */
    @Test
    void testGetMessages() {
        HashSet<DemoiselleRestExceptionMessage> expResult = new HashSet<>();
        HashSet<DemoiselleRestExceptionMessage> result = instance.getMessages();
        assertEquals(expResult, result);
    }

}
