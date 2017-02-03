/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest;

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
 * @author SERPRO
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
     * Test of isErrorDetails method, of class DemoiselleRestConfig.
     */
    @Test
    public void test1() {
        
        boolean expResult = instance.isShowErrorDetails();
        assertEquals(expResult, true);
    } 
    
    /**
     * Test of setshowErrorDetails method, of class DemoiselleRestConfig.
     */
    @Test
    public void test2() {        
    	instance.setShowErrorDetails(false);
        boolean expResult = instance.isShowErrorDetails();
        
        assertEquals(expResult, false);
    }
}
