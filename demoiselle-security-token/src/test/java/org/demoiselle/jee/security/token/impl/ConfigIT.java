/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.token.impl;

import javax.inject.Inject;
import org.demoiselle.jee.configuration.ConfigurationBootstrap;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author 70744416353
 */
@RunWith(Arquillian.class)
public class ConfigIT {

    @Inject
    private Config config;

    public ConfigIT() {
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

    @Deployment
    public static Archive<?> createDeployment() {

        WebArchive war = ShrinkWrap.create(WebArchive.class, "teste.war");
        war.addPackage("org.demoiselle.jee.security.token.impl");

        war.addPackage("org.demoiselle.jee.core.exception");
        war.addPackage("org.demoiselle.jee.rest.exception");
        war.addPackage("org.demoiselle.jee.security.exception");

        war.addPackage("org.demoiselle.jee.configuration");
        war.addAsResource("demoiselle-security-token.properties");

        war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return war;
    }

    /**
     * Test of getType method, of class Config.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        assertNotNull(config);
        Assert.assertEquals("master", config.getType());
    }

}
