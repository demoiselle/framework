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
public class RequiredRoleInterceptorTest {

    public RequiredRoleInterceptorTest() {
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
     * Test of manage method, of class RequiredRoleInterceptor.
     */
//    @Test
    public void testManage() throws Exception {
        System.out.println("manage");
        InvocationContext ic = null;
        RequiredRoleInterceptor instance = new RequiredRoleInterceptor();
        Object expResult = null;
        Object result = instance.manage(ic);
        assertEquals(expResult, result);
    }

}
