package br.gov.frameworkdemoiselle.internal.implementation;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.util.Beans;

/**
 * @author SERPRO
 * @see DefaultAuthenticator
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Beans.class)
public class DefaultAuthenticatorTest {

	private DefaultAuthenticator authenticator;

	@Before
	public void setUp() throws Exception {
		authenticator = new DefaultAuthenticator();

		mockStatic(Beans.class);

		expect(Beans.getReference(Locale.class)).andReturn(Locale.getDefault());

		replay(Beans.class);
	}

	@After
	public void tearDown() {
		authenticator = null;
	}

	@Test
	public void testAuthenticate() {
		try {
			authenticator.authenticate();
		} catch (Exception e) {
			assertTrue(e instanceof DemoiselleException);
		}
	}

	@Test(expected = DemoiselleException.class)
	public void testUnAuthenticate() {
		authenticator.unAuthenticate();
	}

	@Test(expected = DemoiselleException.class)
	public void testGetUser() {
		authenticator.getUser();
	}

}
