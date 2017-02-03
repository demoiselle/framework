/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.core.api.security.DemoiselleUser;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokenManager;
import org.demoiselle.jee.core.api.security.TokenType;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
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
public class TokenManagerImplSlaveTest {

    @Inject
    private DemoiselleUser dml;

    @Inject
    private Token token;

    @Inject
    private TokenManager instance;

    public TokenManagerImplSlaveTest() {
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

    @Test
    public void test21() {
        out.println("getUser");
        token.setKey("eyJraWQiOiJkZW1vaXNlbGxlLXNlY3VyaXR5LWp3dCIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJTVE9SRSIsImV4cCI6MTAwMTQ4MjQ5NTI3MCwiYXVkIjoid2ViIiwianRpIjoiTmxvU0NFUnktd2xXdVhtaGZhVi1IUSIsImlhdCI6MTQ4MjQ5NTI3MSwibmJmIjoxNDgyNDk1MjExLCJpZGVudGl0eSI6IjEiLCJuYW1lIjoiVGVzdGUiLCJyb2xlcyI6WyJBRE1JTklTVFJBVE9SIiwiTUFOQUdFUiJdLCJwZXJtaXNzaW9ucyI6eyJDYXRlZ29yaWEiOlsiQ29uc3VsdGFyIiwiQWx0ZXJhciIsIkluY2x1aXIiXSwiUHJvZHV0byI6WyJBbHRlcmFyIiwiRXhjbHVpciJdfSwicGFyYW1zIjp7ImZvbmUiOiI0MTM1OTM4MDAwIiwiZW5kZXJlY28iOiJydWEgY2FybG9zIHBpb2xpLCAxMzMiLCJlbWFpbCI6InVzZXJAZGVtb2lzZWxsZS5vcmcifX0.EV8L1OEFVMsuCgVSz3gyM2mJIEHczhHBvxSjTFGslHGKItlFtM32BUrzbzA9QECzSUkk-ITnUEtmm-ERTH529clymKX1-LGcboPSQlNAHv4SNRD5i8eJxjlCz_cMSTIdSZRSYOSJZHJHYf0kWEvo1vTthLGWcH_D--b9K_WYDR9hrVmljof46Dd4THXv5_VY9RJlYVHJ1bpIl69f0UDtVzDqfxNSTsBCm6tZXS40f9dh_qjEWATZeMJmjd_t2ZRzXDSLHHbJpLnNOGd2yOdp9H4tmGCxViguRa4Jck6C7cpMM6QIFB7ta67XzS4nl0NTqY64rNseKcyQS-TdAbPxAA");
        token.setType(TokenType.JWT);
        dml.setName("Teste");
        dml.setIdentity("1");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Produto", "Excluir");
        dml.addPermission("Categoria", "Consultar");
        dml.addPermission("Categoria", "Alterar");
        dml.addPermission("Categoria", "Incluir");
        dml.addPermission("Produto", "Alterar");
        dml.addParam("email", "user@demoiselle.org");
        dml.addParam("endereco", "rua carlos pioli, 133");
        dml.addParam("fone", "4135938000");
        DemoiselleUser expResult = dml;
        DemoiselleUser result = instance.getUser();
        assertEquals(expResult, result);
    }

    @Test
    public void test22() {
        out.println("validate");
        token.setKey("eyJraWQiOiJkZW1vaXNlbGxlLXNlY3VyaXR5LWp3dCIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJTVE9SRSIsImV4cCI6MTAwMTQ4MjQ5NTI3MCwiYXVkIjoid2ViIiwianRpIjoiTmxvU0NFUnktd2xXdVhtaGZhVi1IUSIsImlhdCI6MTQ4MjQ5NTI3MSwibmJmIjoxNDgyNDk1MjExLCJpZGVudGl0eSI6IjEiLCJuYW1lIjoiVGVzdGUiLCJyb2xlcyI6WyJBRE1JTklTVFJBVE9SIiwiTUFOQUdFUiJdLCJwZXJtaXNzaW9ucyI6eyJDYXRlZ29yaWEiOlsiQ29uc3VsdGFyIiwiQWx0ZXJhciIsIkluY2x1aXIiXSwiUHJvZHV0byI6WyJBbHRlcmFyIiwiRXhjbHVpciJdfSwicGFyYW1zIjp7ImZvbmUiOiI0MTM1OTM4MDAwIiwiZW5kZXJlY28iOiJydWEgY2FybG9zIHBpb2xpLCAxMzMiLCJlbWFpbCI6InVzZXJAZGVtb2lzZWxsZS5vcmcifX0.EV8L1OEFVMsuCgVSz3gyM2mJIEHczhHBvxSjTFGslHGKItlFtM32BUrzbzA9QECzSUkk-ITnUEtmm-ERTH529clymKX1-LGcboPSQlNAHv4SNRD5i8eJxjlCz_cMSTIdSZRSYOSJZHJHYf0kWEvo1vTthLGWcH_D--b9K_WYDR9hrVmljof46Dd4THXv5_VY9RJlYVHJ1bpIl69f0UDtVzDqfxNSTsBCm6tZXS40f9dh_qjEWATZeMJmjd_t2ZRzXDSLHHbJpLnNOGd2yOdp9H4tmGCxViguRa4Jck6C7cpMM6QIFB7ta67XzS4nl0NTqY64rNseKcyQS-TdAbPxAA");
        token.setType(TokenType.JWT);
        boolean expResult = true;
        boolean result = instance.validate();
        assertEquals(expResult, result);
    }

    @Test(expected = DemoiselleSecurityException.class)
    public void test23() {
        out.println("getUserError");
        instance.setUser(dml);
        token.setKey("eyJraWQiOiJkZW1vaXNlbGxlLXNlY3VyaXR5LWp3dCIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJTVE9SRSIsImV4cCI6MTAwMTQ4MjQ5NTI3MCwiYXVkIjoid2ViIiwianRpIjoiTmxvU0NFUnktd2xXdVhtaGZhVi1IUSIsImlhdCI6MTQ4MjQ5NTI3MSwibmJmIjoxNDgyNDk1MjExLCJpZGVudGl0eSI6IjEiLCJuYW1lIjoiVGVzdGUiLCJyb2xlcyI6WyJBRE1JTklTVFJBVE9SIiwiTUFOQUdFUiJdLCJwZXJtaXNzaW9ucyI6eyJDYXRlZ29yaWEiOlsiQ29uc3VsdGFyIiwiQWx0ZXJhciIsIkluY2x1aXIiXSwiUHJvZHV0byI6WyJBbHRlcmFyIiwiRXhjbHVpciJdfSwicGFyYW1zIjp7ImZvbmUiOiI0MTM1OTM4MDAwIiwiZW5kZXJlY28iOiJydWEgY2FybG9zIHBpb2xpLCAxMzMiLCJlbWFpbCI6InVzZXJAZGVtb2lzZWxsZS5vcmcifX0.EV8L1OEFVMsuCgVSz3gyM2mJIEHczhHBvxSjTFGslHGKItlFtM32BUrzbzA9QECzSUkk-ITnUEtmm-ERTH529clymKX1-LGcboPSQlNAHv4SNRD5i8eJxjlCz_cMSTIdSZRSYOSJZHJHYf0kWEvo1vTthLGWcH_D--b9K_WYDR9hrVmljof46Dd4THXv5_VY9RJlYVHJ1bpIl69f0UDtVzDqfxNSTsBCm6tZXS40f9dh_qjEWATZeMJmjd_t2ZRzXDSLHHbJpLnNOGd2yOdp9H4tmGCxViguRa4Jck6C7cpMM6QIFB7ta67XzS4nl0NTqY64rNseKcyQS-TdAbPxaa");
        token.setType(TokenType.JWT);
        DemoiselleUser expResult = dml;
        DemoiselleUser result = instance.getUser();
        assertNotEquals(expResult, result);
    }

    @Test
    public void test24() {
        out.println("validateError");
        token.setType(TokenType.JWT);
        boolean expResult = false;
        boolean result = instance.validate();
        assertEquals(expResult, result);
    }

}
