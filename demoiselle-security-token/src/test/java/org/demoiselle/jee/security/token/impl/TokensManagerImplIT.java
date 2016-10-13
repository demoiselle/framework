package org.demoiselle.jee.security.token.impl;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.demoiselle.jee.core.api.security.DemoisellePrincipal;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import static org.jboss.shrinkwrap.api.ShrinkWrap.create;
import static org.jboss.shrinkwrap.api.asset.EmptyAsset.INSTANCE;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author 70744416353
 */
@RunWith(Arquillian.class)
public class TokensManagerImplIT {

    @Inject
    private DemoisellePrincipal dml;
    @Inject
    private TokensManagerImpl tokensManagerImpl;

    /**
     *
     */
    @BeforeClass
    public static void setUpClass() {
    }

    /**
     *
     */
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     *
     */
    @Before
    public void setUp() {
        assertNotNull(dml);
        assertNotNull(tokensManagerImpl);
    }

    /**
     *
     */
    @After
    public void tearDown() {
        assertNotNull(dml);
        assertNotNull(tokensManagerImpl);
    }

    /**
     *
     * @return
     */
    @Deployment
    public static Archive<?> createDeployment() {

        WebArchive war = create(WebArchive.class, "teste.war");
        war.addPackage("org.demoiselle.jee.security.impl");
        war.addPackage("org.demoiselle.jee.security.token.impl");
        war.addPackage("org.demoiselle.jee.core.interfaces.security");

        war.addPackage("org.demoiselle.jee.core.exception");
        war.addPackage("org.demoiselle.jee.rest.exception");
        war.addPackage("org.demoiselle.jee.security.exception");

        war.addPackage("org.demoiselle.jee.configuration");
        war.addPackage("org.apache.commons.configuration2");
//        war.addPackage("org.apache.commons.configuration2.event");
//        war.addPackage("org.apache.commons.configuration2.io");

        war.addAsResource("demoiselle-security-token.properties");
        war.addAsWebInfResource(INSTANCE, "beans.xml");

        return war;
    }

    /**
     * Test of getUser method, of class TokensManagerImpl.
     */
    @Test
    @InSequence(0)
    public void testSetUser() {
        out.println("setUser");
        dml.setName("Teste");
        dml.setIdentity("" + currentTimeMillis());
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Produto", "Incluir");
        dml.addPermission("Categoria", "Consultar");
        dml.addParam("Param1", "Value1");
        dml.addParam("Param1", "Value2");
        tokensManagerImpl.setUser(dml);
    }

    /**
     *
     */
    @Test
    @InSequence(1)
    public void testValidate() {
        out.println("validate");
        assertTrue(tokensManagerImpl.validate());
    }

    /**
     * Test of setUser method, of class TokensManagerImpl.
     */
    @Test
    @InSequence(2)
    public void testGetUser() {
        out.println("getUser");
        tokensManagerImpl.getUser().toString();
    }

}
