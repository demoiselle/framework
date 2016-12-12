/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.persistence.crud;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
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
public class DemoiselleCrudConfigTest {

    @Inject
    private DemoiselleCrudConfig instance;

    public DemoiselleCrudConfigTest() {
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
     * Test of getAcceptRange method, of class DemoiselleCrudConfig.
     */
    @Test
    public void testGetAcceptRange() {
        System.out.println("getAcceptRange");
        int expResult = 20;
        int result = instance.getAcceptRange();
        assertEquals(expResult, result);
    }

    /**
     * Test of setAcceptRange method, of class DemoiselleCrudConfig.
     */
    @Test
    public void testSetAcceptRange() {
        System.out.println("setAcceptRange");
        int acceptRange = 100;
        instance.setAcceptRange(100);
        int result = instance.getAcceptRange();
        assertEquals(acceptRange, result);
    }

    /**
     * Test of isPatternsEnabled method, of class DemoiselleCrudConfig.
     */
    @Test
    public void testIsPatternsEnabled() {
        System.out.println("isPatternsEnabled");
        boolean expResult = true;
        boolean result = instance.isPatternsEnabled();
        assertEquals(expResult, result);
    }

    /**
     * Test of setPatternsEnabled method, of class DemoiselleCrudConfig.
     */
    @Test
    public void testSetPatternsEnabled() {
        System.out.println("setPatternsEnabled");
        boolean patternsEnabled = false;
        instance.setPatternsEnabled(false);
        boolean result = instance.isPatternsEnabled();
        assertEquals(patternsEnabled, result);
    }

}
