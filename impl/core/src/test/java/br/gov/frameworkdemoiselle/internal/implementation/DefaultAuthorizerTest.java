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
 * @see DefaultAuthorizer
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Beans.class)
public class DefaultAuthorizerTest {

	private DefaultAuthorizer authorizer;

	@Before
	public void setUp() throws Exception {
		authorizer = new DefaultAuthorizer();

		mockStatic(Beans.class);

		expect(Beans.getReference(Locale.class)).andReturn(Locale.getDefault());

		replay(Beans.class);
	}

	@After
	public void tearDown() {
		authorizer = null;
	}

	@Test(expected = DemoiselleException.class)
	public void testHasRole() {
		authorizer.hasRole(null);
	}

	@Test(expected = DemoiselleException.class)
	public void testHasPermission() {
		authorizer.hasPermission(null, null);
	}

}
