package org.demoiselle.jee.security.jwt.impl;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.core.api.security.DemoisellePrincipal;
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
    private DemoisellePrincipal dml;

    @Inject
    private Token token;

    @Inject
    private TokenManager instance;

    @Inject
    private DemoiselleSecurityJWTConfig config;

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
    public void test10() {
        out.println("init - Type null");
        config.setType(null);
        config.setPrivateKey(null);
        config.setPublicKey(null);
        ((TokenManagerImpl) instance).init();
    }

    @Test
    public void test11() {
        out.println("init - Type slave");
        config.setType("teste");
        ((TokenManagerImpl) instance).init();
    }

    @Test
    public void test12() {
        out.println("init - Type slave");
        config.setType("slave");
        ((TokenManagerImpl) instance).init();
    }

    @Test
    public void test13() {
        out.println("init - Type slave + key error");
        config.setPublicKey("-----BEGIN PUBLIC KEY-----AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA-----END PUBLIC KEY-----");
        ((TokenManagerImpl) instance).init();
    }

    @Test
    public void test14() {
        out.println("init - Type slave + key error");
        config.setPublicKey("");
        ((TokenManagerImpl) instance).init();
    }

    @Test
    public void test15() {
        out.println("init - Type slave + key");
        config.setPublicKey("-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA09A11Zaqmp5ZBTOCxgJ8qqtHhb6b+eO9C8gAX3DXFIlfcl0FU7FVwfQtHWuN3KU83c1sSj9wPMuviGvZeSV7oUp2DXML76EsEKf9r+6JNZRdnTCkXZklQSEmeuftSxnMtAwo7k+eIgPpOIrYpMJv5GzVju0zbyucnwbQnDvoGv08pMrbWaGOxcnHXCazsrRTI7UKQ+jvMDB3bsUcII0XS+92ZLQkiMkKH85HSSlm4AFKuUljRh59RlpJrCDc+TUZWQuOC6Li/H9/78tAW8kJIHfASJhkKgkcdGixBJNggp+K+0hMWvxLt5fi1BXvWiy/Ma3QNHtxOCorRa+4NBB+KwIDAQAB-----END PUBLIC KEY-----");
        ((TokenManagerImpl) instance).init();
    }

    @Test
    public void test21() {
        out.println("getUser");
        token.setKey("eyJraWQiOiJkZW1vaXNlbGxlLXNlY3VyaXR5LWp3dCIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJTVE9SRSIsImF1ZCI6IndlYiIsImV4cCI6NjAwMDAxNDk1ODQ4ODE2LCJqdGkiOiJXRHdLYVNkSXdDVl81WVBnWnZtdFBBIiwiaWF0IjoxNDc2ODEwNjA4LCJuYmYiOjE0NzY4MTA1NDgsImlkZW50aXR5IjoiMSIsIm5hbWUiOiJUZXN0ZSIsInJvbGVzIjpbIkFETUlOSVNUUkFUT1IiLCJNQU5BR0VSIl0sInBlcm1pc3Npb25zIjp7IkNhdGVnb3JpYSI6WyJDb25zdWx0YXIiXSwiUHJvZHV0byI6WyJBbHRlcmFyIl19LCJwYXJhbXMiOnt9fQ.VLVu422XNRXGdahQr93YnTt5iKMaKjybP7jifZMQ0tdIPT3-mivXDbTEfMmMEC9DwdaTQqZdwhuPQRDR7rvUQ3MFwHyPzMzKNPWqFyq-SMMEC_pOvnLjJaPgG0pCyZT9-Dl8QqAMWnzsvceL3XjLKsaS6Ov1S5wXxPQk2m0Y1rdjYGRPLgLNNoR5rH91VToM6UxOOvjUwHoqEFFMHxp6saxQVSYtF_Cjhq1Jqk-cQ3YhhZvcPvrjz6fSLhGtDDEy9-w7Yd_HFzBCr9EVhLcSXr23Vrl-ryvpxdOESK9lSizuTiZgpI-TGqo5hydXZ-uy877CPvMaFIgIueE7GpQz3w");
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

    @Test
    public void test22() {
        out.println("validate");
        token.setKey("eyJraWQiOiJkZW1vaXNlbGxlLXNlY3VyaXR5LWp3dCIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJTVE9SRSIsImF1ZCI6IndlYiIsImV4cCI6NjAwMDAxNDk1ODQ4ODE2LCJqdGkiOiJXRHdLYVNkSXdDVl81WVBnWnZtdFBBIiwiaWF0IjoxNDc2ODEwNjA4LCJuYmYiOjE0NzY4MTA1NDgsImlkZW50aXR5IjoiMSIsIm5hbWUiOiJUZXN0ZSIsInJvbGVzIjpbIkFETUlOSVNUUkFUT1IiLCJNQU5BR0VSIl0sInBlcm1pc3Npb25zIjp7IkNhdGVnb3JpYSI6WyJDb25zdWx0YXIiXSwiUHJvZHV0byI6WyJBbHRlcmFyIl19LCJwYXJhbXMiOnt9fQ.VLVu422XNRXGdahQr93YnTt5iKMaKjybP7jifZMQ0tdIPT3-mivXDbTEfMmMEC9DwdaTQqZdwhuPQRDR7rvUQ3MFwHyPzMzKNPWqFyq-SMMEC_pOvnLjJaPgG0pCyZT9-Dl8QqAMWnzsvceL3XjLKsaS6Ov1S5wXxPQk2m0Y1rdjYGRPLgLNNoR5rH91VToM6UxOOvjUwHoqEFFMHxp6saxQVSYtF_Cjhq1Jqk-cQ3YhhZvcPvrjz6fSLhGtDDEy9-w7Yd_HFzBCr9EVhLcSXr23Vrl-ryvpxdOESK9lSizuTiZgpI-TGqo5hydXZ-uy877CPvMaFIgIueE7GpQz3w");
        boolean expResult = true;
        boolean result = instance.validate();
        assertEquals(expResult, result);
    }

    @Test
    public void test23() {
        out.println("getUserError");
        instance.setUser(dml);
        token.setKey("eyJraWQiOiJkZW1vaXNlbGxlLXNlY3VyaXR5LWp3dCIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJTVE9SRSIsImF1ZCI6IndlYiIsImV4cCI6NjAwMDAxNDk1ODQ4MzQ5LCJqdGkiOiJ2MHVfdmJRbDYzS1VFVDF0UV9FMU1nIiwiaWF0IjoxNDc2ODEwMTQxLCJuYmYiOjE0NzY4MTAwODEsImlkZW50aXR5IjoiMSIsIm5hbWUiOiJUZXN0ZSIsInJvbGVzIjpbIkFETUlOSVNUUkFUT1IiLCJNQU5BR0VSIl0sInBlcm1pc3Npb25zIjp7IkNhdGVnb3JpYSI6WyJDb25zdWx0YXIiXSwiUHJvZHV0byI6WyJBbHRlcmFyIl19LCJwYXJhbXMiOnt9fQ.a6t3ALd0AsIfXw3hbXpE91MggNNA12bn9nwwznLpFUgRMR9Jp4Jcp4fMNoONry3i5q83AFQhi6fPrwMISrbxQ9-fVJHmrAMGQbubJb__6A9aiKthfagFhI0PrZIgxYj3AyTb0ia7Fo_aM8Ji9ADktp3kd7t0v0-nWVGLcdt_FXmBumigP6803-23hBTs3lC5ewFxjXeYx4LNZujFKSMJgafUVtOePRp8lRr6x5Cu_HyjvU2W-IQKb5H3L7hlgS5MOTPn8DWryF0FA8Vwdm2AJGhulGb78igmOG5PnslrPaX56jLnMI8g820KZ_K_cVqulyqUA7arbf-JLR62VWhslQ");
        DemoisellePrincipal expResult = dml;
        DemoisellePrincipal result = instance.getUser();
        assertNotEquals(expResult, result);
    }

    @Test
    public void test24() {
        out.println("validateError");
        boolean expResult = false;
        boolean result = instance.validate();
        assertEquals(expResult, result);
    }

}
