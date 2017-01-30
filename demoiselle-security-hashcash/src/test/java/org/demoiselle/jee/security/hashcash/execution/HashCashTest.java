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
 * @author 70744416353
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
        System.out.println("mintCash");
        String resource = "Demoiselle";
        int value = 21;
        int version = 1;
        Date data = new Date(170126153655l);
        HashCash expResult = HashCash.mintCash(resource, data, value, version);
        HashCash novo = new HashCash(expResult.toString());
        System.out.println(novo.toString());
        assertEquals(expResult, novo);
    }

    /**
     * Test of mintCash method, of class HashCash.
     */
    @Test
    public void testMintCash_3args_1() throws Exception {
        System.out.println("mintCash");
        String resource = "";
        int value = 21;
        int version = 1;
        HashCash expResult = new HashCash("1:21:170127:demoiselle::xo0v+a96R9XirGjQ:0000000QrY7");
        //HashCash result = HashCash.mintCash(resource, value, version);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
//
//    /**
//     * Test of mintCash method, of class HashCash.
//     */
//    @Test
//    public void testMintCash_3args_2() throws Exception {
//        System.out.println("mintCash");
//        String resource = "";
//        Date date = null;
//        int value = 0;
//        HashCash expResult = null;
//        HashCash result = HashCash.mintCash(resource, date, value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of mintCash method, of class HashCash.
//     */
//    @Test
//    public void testMintCash_4args_1() throws Exception {
//        System.out.println("mintCash");
//        String resource = "";
//        Date date = null;
//        int value = 0;
//        int version = 0;
//        HashCash expResult = null;
//        HashCash result = HashCash.mintCash(resource, date, value, version);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of mintCash method, of class HashCash.
//     */
//    @Test
//    public void testMintCash_3args_3() throws Exception {
//        System.out.println("mintCash");
//        String resource = "";
//        Map<String, List<String>> extensions = null;
//        int value = 0;
//        HashCash expResult = null;
//        HashCash result = HashCash.mintCash(resource, extensions, value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of mintCash method, of class HashCash.
//     */
//    @Test
//    public void testMintCash_4args_2() throws Exception {
//        System.out.println("mintCash");
//        String resource = "";
//        Map<String, List<String>> extensions = null;
//        int value = 0;
//        int version = 0;
//        HashCash expResult = null;
//        HashCash result = HashCash.mintCash(resource, extensions, value, version);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of mintCash method, of class HashCash.
//     */
//    @Test
//    public void testMintCash_4args_3() throws Exception {
//        System.out.println("mintCash");
//        String resource = "";
//        Map<String, List<String>> extensions = null;
//        Date date = null;
//        int value = 0;
//        HashCash expResult = null;
//        HashCash result = HashCash.mintCash(resource, extensions, date, value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of mintCash method, of class HashCash.
//     */
//    @Test
//    public void testMintCash_5args() throws Exception {
//        System.out.println("mintCash");
//        String resource = "";
//        Map<String, List<String>> extensions = null;
//        Date date = null;
//        int value = 0;
//        int version = 0;
//        HashCash expResult = null;
//        HashCash result = HashCash.mintCash(resource, extensions, date, value, version);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of equals method, of class HashCash.
//     */
//    @Test
//    public void testEquals() {
//        System.out.println("equals");
//        Object obj = null;
//        HashCash instance = null;
//        boolean expResult = false;
//        boolean result = instance.equals(obj);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toString method, of class HashCash.
//     */
//    @Test
//    public void testToString() {
//        System.out.println("toString");
//        HashCash instance = null;
//        String expResult = "";
//        String result = instance.toString();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getExtensions method, of class HashCash.
//     */
//    @Test
//    public void testGetExtensions() {
//        System.out.println("getExtensions");
//        HashCash instance = null;
//        Map<String, List<String>> expResult = null;
//        Map<String, List<String>> result = instance.getExtensions();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getResource method, of class HashCash.
//     */
//    @Test
//    public void testGetResource() {
//        System.out.println("getResource");
//        HashCash instance = null;
//        String expResult = "";
//        String result = instance.getResource();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDate method, of class HashCash.
//     */
//    @Test
//    public void testGetDate() {
//        System.out.println("getDate");
//        HashCash instance = null;
//        Date expResult = null;
//        Date result = instance.getDate();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getComputedBits method, of class HashCash.
//     */
//    @Test
//    public void testGetComputedBits() {
//        System.out.println("getComputedBits");
//        HashCash instance = null;
//        int expResult = 0;
//        int result = instance.getComputedBits();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getClaimedBits method, of class HashCash.
//     */
//    @Test
//    public void testGetClaimedBits() {
//        System.out.println("getClaimedBits");
//        HashCash instance = null;
//        int expResult = 0;
//        int result = instance.getClaimedBits();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getVersion method, of class HashCash.
//     */
//    @Test
//    public void testGetVersion() {
//        System.out.println("getVersion");
//        HashCash instance = null;
//        int expResult = 0;
//        int result = instance.getVersion();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of estimateTime method, of class HashCash.
//     */
//    @Test
//    public void testEstimateTime() throws Exception {
//        System.out.println("estimateTime");
//        int value = 0;
//        long expResult = 0L;
//        long result = HashCash.estimateTime(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of estimateValue method, of class HashCash.
//     */
//    @Test
//    public void testEstimateValue() throws Exception {
//        System.out.println("estimateValue");
//        int secs = 0;
//        int expResult = 0;
//        int result = HashCash.estimateValue(secs);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of compareTo method, of class HashCash.
//     */
//    @Test
//    public void testCompareTo() {
//        System.out.println("compareTo");
//        HashCash other = null;
//        HashCash instance = null;
//        int expResult = 0;
//        int result = instance.compareTo(other);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
