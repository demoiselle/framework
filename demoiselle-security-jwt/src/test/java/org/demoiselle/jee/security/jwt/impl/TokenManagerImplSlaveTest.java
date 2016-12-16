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

    private static String localtoken;

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
        token.setKey("eyJraWQiOiJkZW1vaXNlbGxlLXNlY3VyaXR5LWp3dCIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJTVE9SRSIsImV4cCI6MTAwMTQ4MTgzMjUxMiwiYXVkIjoid2ViIiwianRpIjoiUE9DV3FZWXRZalZvdGpLNTRpUFhtUSIsImlhdCI6MTQ4MTgzMjUxMywibmJmIjoxNDgxODMyNDUzLCJpZGVudGl0eSI6IjEiLCJuYW1lIjoiVGVzdGUiLCJyb2xlcyI6WyJBRE1JTklTVFJBVE9SIiwiTUFOQUdFUiJdLCJwZXJtaXNzaW9ucyI6eyJDYXRlZ29yaWEiOlsiQ29uc3VsdGFyIiwiQWx0ZXJhciIsIkluY2x1aXIiXSwiUHJvZHV0byI6WyJBbHRlcmFyIiwiRXhjbHVpciJdfSwicGFyYW1zIjp7fX0.XBwV_aP53npwx5VthpmYKJqLY_YjodEK4kHCq-CZOXWNSyw9e77M55TKLY1p_lI43qsbZ0H9U7YSsExkl8tRdXOYs_YjttASc_F9iaXG165CC4sy2hW5wfUEYHH15-1w5ZYsBMNT5ZmJgPDg2-YYU_sf8tKxvDMrczq7bc_GfbdwRw6ahsAWKtlBMhGq-GDESlWXcMOb5q42o-3oTGbr9VuhHqiZKa1bxolvUJuIBEuD0TvJ_M-8v6hUYX4N-M2nMCOnpg3x9fIeZXJCD0RPkfjLy_U9LX-HPka7WazTwY1TRT5KZRQS0ciGDDUpY1XouiZW0LkVDxXjgzcAxpRZSg");
        dml.setName("Teste");
        dml.setIdentity("1");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Categoria", "Consultar");
        DemoiselleUser expResult = dml;
        DemoiselleUser result = instance.getUser();
        assertEquals(expResult, result);
    }
//
//    @Test
//    public void test22() {
//        out.println("validate");
//        token.setKey("eyJraWQiOiJkZW1vaXNlbGxlLXNlY3VyaXR5LWp3dCIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJTVE9SRSIsImV4cCI6MTAwMTQ4MTgzMjUxMiwiYXVkIjoid2ViIiwianRpIjoiUE9DV3FZWXRZalZvdGpLNTRpUFhtUSIsImlhdCI6MTQ4MTgzMjUxMywibmJmIjoxNDgxODMyNDUzLCJpZGVudGl0eSI6IjEiLCJuYW1lIjoiVGVzdGUiLCJyb2xlcyI6WyJBRE1JTklTVFJBVE9SIiwiTUFOQUdFUiJdLCJwZXJtaXNzaW9ucyI6eyJDYXRlZ29yaWEiOlsiQ29uc3VsdGFyIiwiQWx0ZXJhciIsIkluY2x1aXIiXSwiUHJvZHV0byI6WyJBbHRlcmFyIiwiRXhjbHVpciJdfSwicGFyYW1zIjp7fX0.XBwV_aP53npwx5VthpmYKJqLY_YjodEK4kHCq-CZOXWNSyw9e77M55TKLY1p_lI43qsbZ0H9U7YSsExkl8tRdXOYs_YjttASc_F9iaXG165CC4sy2hW5wfUEYHH15-1w5ZYsBMNT5ZmJgPDg2-YYU_sf8tKxvDMrczq7bc_GfbdwRw6ahsAWKtlBMhGq-GDESlWXcMOb5q42o-3oTGbr9VuhHqiZKa1bxolvUJuIBEuD0TvJ_M-8v6hUYX4N-M2nMCOnpg3x9fIeZXJCD0RPkfjLy_U9LX-HPka7WazTwY1TRT5KZRQS0ciGDDUpY1XouiZW0LkVDxXjgzcAxpRZSg");
//        boolean expResult = true;
//        boolean result = instance.validate();
//        assertEquals(expResult, result);
//    }
//
//    @Test
//    public void test23() {
//        out.println("getUserError");
//        instance.setUser(dml);
//        token.setKey("eyJraWQiOiJkZW1vaXNlbGxlLXNlY3VyaXR5LWp3dCIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJTVE9SRSIsImV4cCI6MTAwMTQ4MTgzMjUxMiwiYXVkIjoid2ViIiwianRpIjoiUE9DV3FZWXRZalZvdGpLNTRpUFhtUSIsImlhdCI6MTQ4MTgzMjUxMywibmJmIjoxNDgxODMyNDUzLCJpZGVudGl0eSI6IjEiLCJuYW1lIjoiVGVzdGUiLCJyb2xlcyI6WyJBRE1JTklTVFJBVE9SIiwiTUFOQUdFUiJdLCJwZXJtaXNzaW9ucyI6eyJDYXRlZ29yaWEiOlsiQ29uc3VsdGFyIiwiQWx0ZXJhciIsIkluY2x1aXIiXSwiUHJvZHV0byI6WyJBbHRlcmFyIiwiRXhjbHVpciJdfSwicGFyYW1zIjp7fX0.XBwV_aP53npwx5VthpmYKJqLY_YjodEK4kHCq-CZOXWNSyw9e77M55TKLY1p_lI43qsbZ0H9U7YSsExkl8tRdXOYs_YjttASc_F9iaXG165CC4sy2hW5wfUEYHH15-1w5ZYsBMNT5ZmJgPDg2-YYU_sf8tKxvDMrczq7bc_GfbdwRw6ahsAWKtlBMhGq-GDESlWXcMOb5q42o-3oTGbr9VuhHqiZKa1bxolvUJuIBEuD0TvJ_M-8v6hUYX4N-M2nMCOnpg3x9fIeZXJCD0RPkfjLy_U9LX-HPka7WazTwY1TRT5KZRQS0ciGDDUpY1XouiZW0LkVDxXjgzcAxpRZSG");
//        DemoiselleUser expResult = dml;
//        DemoiselleUser result = instance.getUser();
//        assertNotEquals(expResult, result);
//    }
//
//    @Test
//    public void test24() {
//        out.println("validateError");
//        boolean expResult = false;
//        boolean result = instance.validate();
//        assertEquals(expResult, result);
//    }

}
