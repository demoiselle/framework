
package org.demoiselle.jee.security.interceptor;

import javax.interceptor.InvocationContext;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author 70744416353
 */
//@RunWith(CdiTestRunner.class)
public class RequiredPermissionInterceptorTest {


    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }
    
    public RequiredPermissionInterceptorTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of manage method, of class RequiredPermissionInterceptor.
     */
    //@Test
    public void testManage() throws Exception {
        InvocationContext ic = null;
        RequiredPermissionInterceptor instance = new RequiredPermissionInterceptor();
        Object expResult = null;
        Object result = instance.manage(ic);
        assertEquals(expResult, result);
    }

}
