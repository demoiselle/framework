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
package org.demoiselle.jee.rest;

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
@RunWith(CdiTestRunner.class)
public class DemoiselleRestConfigTest {

    @Inject
    private DemoiselleRestConfig instance;

    public DemoiselleRestConfigTest() {
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
     * Test of isGzipEnabled method, of class DemoiselleRestConfig.
     */
    @Test
    public void test11() {
        System.out.println("isGzipEnabled");
        boolean expResult = true;
        boolean result = instance.isGzipEnabled();
        assertEquals(expResult, result);
    }

    /**
     * Test of setGzipEnabled method, of class DemoiselleRestConfig.
     */
    @Test
    public void test12() {
        System.out.println("setGzipEnabled");
        boolean gzipEnabled = false;
        instance.setGzipEnabled(gzipEnabled);
    }

    /**
     * Test of isGzipEnabled method, of class DemoiselleRestConfig.
     */
    @Test
    public void test13() {
        System.out.println("isGzipEnabled");
        boolean expResult = false;
        boolean result = instance.isGzipEnabled();
        assertEquals(expResult, result);
    }
}
