package br.gov.frameworkdemoiselle.internal.implementation;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * @author SERPRO
 * @see DefaultAuthenticator
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(CoreBundle.class)
public class DefaultAuthenticatorTest {

	private DefaultAuthenticator authenticator;

	@Before
	public void setUp() throws Exception {
		authenticator = new DefaultAuthenticator();

		mockStatic(CoreBundle.class);

		ResourceBundle bundle = new ResourceBundle(ResourceBundle.getBundle("demoiselle-core-bundle"));
		expect(CoreBundle.get()).andReturn(bundle);

		replay(CoreBundle.class);
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

	@Test
	public void testUnAuthenticate() {
		try {
			authenticator.unAuthenticate();
		} catch (Exception e) {
			assertTrue(e instanceof DemoiselleException);
		}
	}

	@Test
	public void testGetUser() {
		try {
			authenticator.getUser();
		} catch (Exception e) {
			assertTrue(e instanceof DemoiselleException);
		}
	}

}
