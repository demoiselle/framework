/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.hashcash.execution;

import org.demoiselle.jee.security.hashcash.execution.HashCash;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author SERPRO
 */
public class HashCashTest {

    public HashCashTest() {
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
     * Test of mintCash method, of class HashCash.
     */
    @Test
    public void testMintCash_String_int() throws Exception {
        //TODO Teste do componente
        System.out.println("mintCash");
        String resource = "Demoiselle";
        String value = resource;
        int version = 1;
        Date data = new Date(170126153655l);
        assertEquals(resource, value);
    }

}
