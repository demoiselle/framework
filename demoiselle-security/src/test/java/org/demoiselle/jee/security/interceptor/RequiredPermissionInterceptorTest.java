/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.interceptor;

import static org.junit.Assert.assertEquals;

import javax.interceptor.InvocationContext;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;


/**
 *
 * @author SERPRO
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
