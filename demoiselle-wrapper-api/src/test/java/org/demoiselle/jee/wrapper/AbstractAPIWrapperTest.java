/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.wrapper;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
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

    /**
     * Test of get method, of class AbstractAPIWrapper.
     */
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

    /**
     * Test of queryString method, of class AbstractAPIWrapper.
     */
    @Test
    public void testQueryString() {
        System.out.println("queryString");
        MultivaluedMap map = new MultivaluedHashMap();
        map.add("cep", "80520170");
        List<Cep> expResult = new ArrayList<>();
        Cep cep = new Cep();
        cep.setId(785981);
        cep.setLogradouro("Rua Carlos Pioli");
        cep.setCep("80520170");
        cep.setUf("PR");
        cep.setCidade("Curitiba");
        cep.setBairroIni("Bom Retiro");
        cep.setBairroFim(null);
        expResult.add(cep);
        List<Cep> result = instance.queryString(map);
        assertEquals(expResult.toString(), result.toString());
    }

//    /**
//     * Test of path method, of class AbstractAPIWrapper.
//     */
//    @Test
//    public void testPath() {
//        System.out.println("path");
//        String path = "";
//        String expResult = "[{\"id\":563385,\"logradouro\":\"Travessa Boa Esperança\",\"cep\":\"59114174\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563386,\"logradouro\":\"Rua Francisca Ferreira de Souza\",\"cep\":\"59114175\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563387,\"logradouro\":\"Travessa Vila Bela\",\"cep\":\"59114176\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563388,\"logradouro\":\"Travessa Barão de Melgaço\",\"cep\":\"59114178\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563389,\"logradouro\":\"Rua Alvorecer\",\"cep\":\"59114179\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563390,\"logradouro\":\"Rua Professor Manoel Vilar\",\"cep\":\"59114180\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563391,\"logradouro\":\"Rua André Matias\",\"cep\":\"59114181\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563392,\"logradouro\":\"Rua Manoel Maria\",\"cep\":\"59114182\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563393,\"logradouro\":\"Rua Comandante Costa\",\"cep\":\"59114183\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563394,\"logradouro\":\"Rua Pedro Ivo\",\"cep\":\"59114184\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563395,\"logradouro\":\"Avenida Senador Carlos Alberto\",\"cep\":\"59114185\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563396,\"logradouro\":\"Rua Manoel Fernandes\",\"cep\":\"59114186\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563397,\"logradouro\":\"Rua Israel Ramos\",\"cep\":\"59114187\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563398,\"logradouro\":\"Rua Coronel Farias\",\"cep\":\"59114188\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563399,\"logradouro\":\"Rua dos Operários\",\"cep\":\"59114189\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563400,\"logradouro\":\"Rua dos Protetores\",\"cep\":\"59114190\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563401,\"logradouro\":\"Rua Gervásio Mota\",\"cep\":\"59114191\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563402,\"logradouro\":\"Rua da Consolação (Lot B Jesus)\",\"cep\":\"59114192\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563403,\"logradouro\":\"Vila Campo Grande\",\"cep\":\"59114193\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null},{\"id\":563404,\"logradouro\":\"Travessa dos Portugueses\",\"cep\":\"59114194\",\"uf\":\"RN\",\"cidade\":\"Natal\",\"bairroIni\":\"Nossa Senhora da Apresentação\",\"bairroFim\":null}]";
//        List<Cep> result = instance.path(path);
//        assertEquals(expResult, result.toString());
//
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
