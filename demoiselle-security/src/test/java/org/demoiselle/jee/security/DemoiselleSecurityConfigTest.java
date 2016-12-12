
package org.demoiselle.jee.security;

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
public class DemoiselleSecurityConfigTest {


    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }
    @Inject
    private DemoiselleSecurityConfig instance;
    public DemoiselleSecurityConfigTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of isCorsEnabled method, of class DemoiselleSecurityConfig.
     */
    @Test
    public void testIsCorsEnabled() {
        boolean expResult = true;
        boolean result = instance.isCorsEnabled();
        assertEquals(expResult, result);
    }

    /**
     * Test of setCorsEnabled method, of class DemoiselleSecurityConfig.
     */
    @Test
    public void testSetCorsEnabled() {
        boolean corsEnabled = false;
        instance.setCorsEnabled(corsEnabled);
        assertEquals(instance.isCorsEnabled(), corsEnabled);
    }

}
