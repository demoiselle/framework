package br.gov.frameworkdemoiselle.internal.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import br.gov.frameworkdemoiselle.internal.implementation.DefaultAuthenticator;
import br.gov.frameworkdemoiselle.internal.implementation.DefaultAuthorizer;
import br.gov.frameworkdemoiselle.security.Authenticator;
import br.gov.frameworkdemoiselle.security.Authorizer;

public class SecurityConfigTest {

	private SecurityConfig config;

	@Before
	public void setUp() throws Exception {
		this.config = new SecurityConfig();
	}

	@Test
	public void testIsEnabled() {
		assertEquals(true, config.isEnabled());
	}

	@Test
	public void testSetEnabled() {
		config.setEnabled(false);
		assertEquals(false, config.isEnabled());
	}

	@Test
	public void testSetAuthenticatorClass() {
		Authenticator authenticator = new DefaultAuthenticator();
		config.setAuthenticatorClass(authenticator.getClass());
		assertEquals("br.gov.frameworkdemoiselle.internal.implementation.DefaultAuthenticator", config
				.getAuthenticatorClass().getName());
	}

	@Test
	public void testSetAuthorizerClass() {
		Authorizer authorizer = new DefaultAuthorizer();
		config.setAuthorizerClass(authorizer.getClass());
		assertEquals("br.gov.frameworkdemoiselle.internal.implementation.DefaultAuthorizer", config
				.getAuthorizerClass().getName());
	}

}
