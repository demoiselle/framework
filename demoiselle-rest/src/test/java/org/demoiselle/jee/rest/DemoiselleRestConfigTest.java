
package org.demoiselle.jee.rest;

import javax.inject.Inject;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author 70744416353
 */
@RunWith(CdiTestRunner.class)
public class DemoiselleRestConfigTest {


    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }
    @Inject
    private DemoiselleRestConfig instance;
    public DemoiselleRestConfigTest() {
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
        boolean expResult = true;
        boolean result = instance.isGzipEnabled();
        assertEquals(expResult, result);
    }

    /**
     * Test of setGzipEnabled method, of class DemoiselleRestConfig.
     */
    @Test
    public void test12() {
        boolean gzipEnabled = false;
        instance.setGzipEnabled(gzipEnabled);
    }

    /**
     * Test of isGzipEnabled method, of class DemoiselleRestConfig.
     */
    @Test
    public void test13() {
        boolean expResult = false;
        boolean result = instance.isGzipEnabled();
        assertEquals(expResult, result);
    }
}
