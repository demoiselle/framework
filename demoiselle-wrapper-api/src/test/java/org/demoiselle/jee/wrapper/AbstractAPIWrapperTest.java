/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.wrapper;

import javax.inject.Inject;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.mock.CEPApiWrapperImpl;
import org.demoiselle.jee.mock.Cep;
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
public class AbstractAPIWrapperTest {

    @Inject
    private CEPApiWrapperImpl instance;

    public AbstractAPIWrapperTest() {
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

//    /**
//     * Test of get method, of class AbstractAPIWrapper.
//     */
    @Test
    public void testGet() {
        System.out.println("get");
        Integer id = 777777;
        Cep expResult = new Cep();
        expResult.setId(id);
        expResult.setLogradouro("Travessa Gilmar Vieira");
        expResult.setCep("79084212");
        expResult.setUf("MS");
        expResult.setCidade("Campo Grande");
        expResult.setBairroIni("Conjunto Aero Rancho");
        expResult.setBairroFim(null);
        Object result = instance.get(id);
        assertEquals(expResult, result);
    }
//
//    /**
//     * Test of patch method, of class AbstractAPIWrapper.
//     */
//    @Test
//    public void testPatch() {
//        System.out.println("patch");
//        Object id = null;
//        Object entity = null;
//        AbstractAPIWrapper instance = new APIWrapperImpl();
//        Object expResult = null;
//        Object result = instance.patch(id, entity);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of put method, of class AbstractAPIWrapper.
//     */
//    @Test
//    public void testPut() {
//        System.out.println("put");
//        Object entity = null;
//        AbstractAPIWrapper instance = new APIWrapperImpl();
//        Object expResult = null;
//        Object result = instance.put(entity);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of delete method, of class AbstractAPIWrapper.
//     */
//    @Test
//    public void testDelete() {
//        System.out.println("delete");
//        Object id = null;
//        AbstractAPIWrapper instance = new APIWrapperImpl();
//        instance.delete(id);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of queryString method, of class AbstractAPIWrapper.
//     */
//    @Test
//    public void testQueryString() {
//        System.out.println("queryString");
//        MultivaluedMap map = null;
//        AbstractAPIWrapper instance = new APIWrapperImpl();
//        List expResult = null;
//        List result = instance.queryString(map);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of path method, of class AbstractAPIWrapper.
//     */
//    @Test
//    public void testPath() {
//        System.out.println("path");
//        String path = "";
//        AbstractAPIWrapper instance = new APIWrapperImpl();
//        List expResult = null;
//        List result = instance.path(path);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of params method, of class AbstractAPIWrapper.
//     */
//    @Test
//    public void testParams() {
//        System.out.println("params");
//        String[] params = null;
//        AbstractAPIWrapper instance = new APIWrapperImpl();
//        List expResult = null;
//        List result = instance.params(params);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
