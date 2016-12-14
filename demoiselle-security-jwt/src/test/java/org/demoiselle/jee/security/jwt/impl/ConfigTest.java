/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.jwt.impl;

import static java.lang.Long.valueOf;
import static java.lang.System.out;
import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
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
public class ConfigTest {

    @Inject
    private DemoiselleSecurityJWTConfig instance;

    public ConfigTest() {
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
    public void testGetType() {
        out.println("getType");
        assertEquals("master", instance.getType());
    }

    @Test
    public void testGetPrivateKey() {
        out.println("getPrivateKey");
        String expResult = "-----BEGIN PRIVATE KEY-----MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDT0DXVlqqanlkFM4LGAnyqq0eFvpv5470LyABfcNcUiV9yXQVTsVXB9C0da43cpTzdzWxKP3A8y6+Ia9l5JXuhSnYNcwvvoSwQp/2v7ok1lF2dMKRdmSVBISZ65+1LGcy0DCjuT54iA+k4itikwm/kbNWO7TNvK5yfBtCcO+ga/TykyttZoY7FycdcJrOytFMjtQpD6O8wMHduxRwgjRdL73ZktCSIyQofzkdJKWbgAUq5SWNGHn1GWkmsINz5NRlZC44LouL8f3/vy0BbyQkgd8BImGQqCRx0aLEEk2CCn4r7SExa/Eu3l+LUFe9aLL8xrdA0e3E4KitFr7g0EH4rAgMBAAECggEABhUilqGfAJWvhMC37qu/nL8SbLrOi9yIX0A9EoCRDJvtS8F0F7Ut+0Xhzch66G0uVEhD5dXwiS5oOgiu1BXJeRZEUZqOKzF7rHbGiDjXY9yA27S745w0P6yOCFWEsPVqtXjr6/wJVHy8Q81o70JOKEcf0tzo7zZXZxGxB+uIfM3ffGNixX/tOHOERSViBvJsBp7sPZOPdgFQy1yQzYHDHob3V7BoG3bL/6ZXBIKMovBcKPoZuHDSyU1w3UbkFpPf8NbElPeMVoPxSwT1e3gfkKwBdp3bXucUnWBJE/aEkpUuIPTHiI4oGJDapmybLx+UACqp3eid48IDC25qXIq/8QKBgQD2yuECo9mKvP6CAZTl9LUUqYqwf4HLqgzOcCzDdigJaF08jLkI+9c7kMhq5C3R+VpRXCMJVHQSOEyUaOEj/m4QrF2JmfGcLxhqnDm8kZ84nX5anBxASCVcyiM2A37fpXmohczzu+Re8VFbFIMC1qJa5r/7/whmc5UL2jBVbQ7kPwKBgQDbtz6RlZWWnlJh1Dk6PuNczhmjbNRctrsVc2RcvIuZrw3BPGbeFanJVDa9Q9n2owxMs6BoSgIfrf0XGnl71yTSKtCliMyTgxg27g8iVlLA5YPaTIsazScCzLMOJmfzP2RcDBMVg+42Zu2tHhOoRAyNIpM7PQBDk1rLbpBH/HL7FQKBgGwvSW3317BK4yKogNZBbHPvUn3Gl2ZpWA3S/Lx+elSNbHnTknWOuK5C7Kh2+GMYdPA/fJhlbjBif6d7Rl6Z9TPX63Ubh9+YgZKSg3jXOT3/RFmCH5xKRB6l+cN+yspNZsRqSwr5bcX08V4E4t2Gq0s/5h8YkF0hA9BbSF7aXPHPAoGBAJ8m7TunjuO7axFSGOIIC8l9wTSP8IP4GSxAmcJTEQwRsXT3u8vDBWnAhqYyMABnutEUjGz+rusjrOC/XKBIB3P1b414ujdgDno7ltrYjLkNh6TpLRoM4OU2Qb1ONJ4OnTPPy0MafcMKa7+qubJ5GF5jXSLb3QUWB/6z5+88/kzBAoGAWzcBjuAYmRa+Q9GpGRyml1SgS0foaKwycN9Az0IGaNmN+hvKBgoJtUvY5V/sDdNAbLz+1Hpc0s3TJpghmHTVxUrC0VKqhEyXpHIUN2IOjiqoiQbB1stW0GS3N9U4akQfYQrR7u+IcUizfs6OQTmTR3Xp6LryES/rLn0vwZKZIvo=-----END PRIVATE KEY-----";
        String result = instance.getPrivateKey();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetPublicKey() {
        out.println("getPublicKey");
        String expResult = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA09A11Zaqmp5ZBTOCxgJ8qqtHhb6b+eO9C8gAX3DXFIlfcl0FU7FVwfQtHWuN3KU83c1sSj9wPMuviGvZeSV7oUp2DXML76EsEKf9r+6JNZRdnTCkXZklQSEmeuftSxnMtAwo7k+eIgPpOIrYpMJv5GzVju0zbyucnwbQnDvoGv08pMrbWaGOxcnHXCazsrRTI7UKQ+jvMDB3bsUcII0XS+92ZLQkiMkKH85HSSlm4AFKuUljRh59RlpJrCDc+TUZWQuOC6Li/H9/78tAW8kJIHfASJhkKgkcdGixBJNggp+K+0hMWvxLt5fi1BXvWiy/Ma3QNHtxOCorRa+4NBB+KwIDAQAB-----END PUBLIC KEY-----";
        String result = instance.getPublicKey();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetTimetoLiveMilliseconds() {
        out.println("getTimetoLiveMilliseconds");
        Long expResult = valueOf("9999999999");
        Long result = instance.getTimetoLiveMilliseconds();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetIssuer() {
        out.println("getIssuer");
        String expResult = "STORE";
        String result = instance.getIssuer();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetAudience() {
        out.println("getAudience()");
        String expResult = "web";
        String result = instance.getAudience();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetAlgorithmIdentifiers() {
        out.println("getAlgorithmIdentifiers()");
        String expResult = "RS256";
        String result = instance.getAlgorithmIdentifiers();
        assertEquals(expResult, result);
    }

}
