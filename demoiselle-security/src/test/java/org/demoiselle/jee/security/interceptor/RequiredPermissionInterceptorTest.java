/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.interceptor;

import javax.interceptor.InvocationContext;
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
public class RequiredPermissionInterceptorTest {

    public RequiredPermissionInterceptorTest() {
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
     * Test of manage method, of class RequiredPermissionInterceptor.
     */
//    @Test
    public void testManage() throws Exception {
        System.out.println("manage");
        InvocationContext ic = null;
        RequiredPermissionInterceptor instance = new RequiredPermissionInterceptor();
        Object expResult = null;
        Object result = instance.manage(ic);
        assertEquals(expResult, result);
    }

}
