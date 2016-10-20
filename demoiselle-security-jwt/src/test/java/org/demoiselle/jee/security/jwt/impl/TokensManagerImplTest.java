package org.demoiselle.jee.security.jwt.impl;

import static java.lang.System.out;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.core.api.security.DemoisellePrincipal;
import org.demoiselle.jee.core.api.security.Token;
import org.demoiselle.jee.core.api.security.TokensManager;
import org.demoiselle.jee.security.exception.DemoiselleSecurityException;
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
public class TokensManagerImplTest {

    @Inject
    private DemoisellePrincipal dml;

    @Inject
    private Token token;

    private static String localtoken;

    @Inject
    private TokensManager instance;

    @Inject
    private DemoiselleSecurityJWTConfig config;

    /**
     *
     */
    public TokensManagerImplTest() {
    }

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
    }

    /**
     *
     */
    @After
    public void tearDown() {
    }

    @Test
    public void test10() {
        out.println("init - error");
        config.setPrivateKey("-----BEGIN PRIVATE KEY-----AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=-----END PRIVATE KEY-----");
        ((TokensManagerImpl) instance).init();
    }

    @Test
    public void test11() {
        out.println("init");
        config.setPrivateKey("-----BEGIN PRIVATE KEY-----MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDT0DXVlqqanlkFM4LGAnyqq0eFvpv5470LyABfcNcUiV9yXQVTsVXB9C0da43cpTzdzWxKP3A8y6+Ia9l5JXuhSnYNcwvvoSwQp/2v7ok1lF2dMKRdmSVBISZ65+1LGcy0DCjuT54iA+k4itikwm/kbNWO7TNvK5yfBtCcO+ga/TykyttZoY7FycdcJrOytFMjtQpD6O8wMHduxRwgjRdL73ZktCSIyQofzkdJKWbgAUq5SWNGHn1GWkmsINz5NRlZC44LouL8f3/vy0BbyQkgd8BImGQqCRx0aLEEk2CCn4r7SExa/Eu3l+LUFe9aLL8xrdA0e3E4KitFr7g0EH4rAgMBAAECggEABhUilqGfAJWvhMC37qu/nL8SbLrOi9yIX0A9EoCRDJvtS8F0F7Ut+0Xhzch66G0uVEhD5dXwiS5oOgiu1BXJeRZEUZqOKzF7rHbGiDjXY9yA27S745w0P6yOCFWEsPVqtXjr6/wJVHy8Q81o70JOKEcf0tzo7zZXZxGxB+uIfM3ffGNixX/tOHOERSViBvJsBp7sPZOPdgFQy1yQzYHDHob3V7BoG3bL/6ZXBIKMovBcKPoZuHDSyU1w3UbkFpPf8NbElPeMVoPxSwT1e3gfkKwBdp3bXucUnWBJE/aEkpUuIPTHiI4oGJDapmybLx+UACqp3eid48IDC25qXIq/8QKBgQD2yuECo9mKvP6CAZTl9LUUqYqwf4HLqgzOcCzDdigJaF08jLkI+9c7kMhq5C3R+VpRXCMJVHQSOEyUaOEj/m4QrF2JmfGcLxhqnDm8kZ84nX5anBxASCVcyiM2A37fpXmohczzu+Re8VFbFIMC1qJa5r/7/whmc5UL2jBVbQ7kPwKBgQDbtz6RlZWWnlJh1Dk6PuNczhmjbNRctrsVc2RcvIuZrw3BPGbeFanJVDa9Q9n2owxMs6BoSgIfrf0XGnl71yTSKtCliMyTgxg27g8iVlLA5YPaTIsazScCzLMOJmfzP2RcDBMVg+42Zu2tHhOoRAyNIpM7PQBDk1rLbpBH/HL7FQKBgGwvSW3317BK4yKogNZBbHPvUn3Gl2ZpWA3S/Lx+elSNbHnTknWOuK5C7Kh2+GMYdPA/fJhlbjBif6d7Rl6Z9TPX63Ubh9+YgZKSg3jXOT3/RFmCH5xKRB6l+cN+yspNZsRqSwr5bcX08V4E4t2Gq0s/5h8YkF0hA9BbSF7aXPHPAoGBAJ8m7TunjuO7axFSGOIIC8l9wTSP8IP4GSxAmcJTEQwRsXT3u8vDBWnAhqYyMABnutEUjGz+rusjrOC/XKBIB3P1b414ujdgDno7ltrYjLkNh6TpLRoM4OU2Qb1ONJ4OnTPPy0MafcMKa7+qubJ5GF5jXSLb3QUWB/6z5+88/kzBAoGAWzcBjuAYmRa+Q9GpGRyml1SgS0foaKwycN9Az0IGaNmN+hvKBgoJtUvY5V/sDdNAbLz+1Hpc0s3TJpghmHTVxUrC0VKqhEyXpHIUN2IOjiqoiQbB1stW0GS3N9U4akQfYQrR7u+IcUizfs6OQTmTR3Xp6LryES/rLn0vwZKZIvo=-----END PRIVATE KEY-----");
        ((TokensManagerImpl) instance).init();
    }

    /**
     * Test of setUser method, of class TokensManagerImpl.
     */
    @Test
    public void test20() {
        out.println("setUser");
        token.setKey("");
        dml.setName("Teste");
        dml.setIdentity("1");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Categoria", "Consultar");
        instance.setUser(dml);
        localtoken = token.getKey();
        assertNotEquals("", token.getKey());
    }

    /**
     * Test of getUser method, of class TokensManagerImpl.
     */
    @Test
    public void test21() {
        out.println("getUser");
        token.setKey(localtoken);
        dml.setName("Teste");
        dml.setIdentity("1");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Categoria", "Consultar");
        DemoisellePrincipal expResult = dml;
        DemoisellePrincipal result = instance.getUser();
        assertEquals(expResult, result);
    }

    /**
     * Test of validate method, of class TokensManagerImpl.
     */
    @Test
    public void test22() {
        out.println("validate");
        token.setKey(localtoken);
        boolean expResult = true;
        boolean result = instance.validate();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUser method, of class TokensManagerImpl.
     */
    @Test
    public void test23() {
        out.println("getUserError");
        token.setKey(localtoken);
        dml.setName("Teste2");
        dml.setIdentity("2");
        dml.addRole("ADMINISTRATOR");
        dml.addRole("MANAGER");
        dml.addPermission("Produto", "Alterar");
        dml.addPermission("Categoria", "Consultar");
        String expResult = dml.getIdentity();
        String result = instance.getUser().getIdentity();
        assertNotEquals(expResult, result);
    }

    /**
     * Test of validate method, of class TokensManagerImpl.
     */
    @Test
    public void test24() {
        out.println("validateError");
        token.setKey("");
        boolean expResult = false;
        boolean result = instance.validate();
        assertEquals(expResult, result);
    }

}
