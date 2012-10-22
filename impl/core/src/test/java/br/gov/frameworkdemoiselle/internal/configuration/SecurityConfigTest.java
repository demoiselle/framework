package br.gov.frameworkdemoiselle.internal.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import br.gov.frameworkdemoiselle.security.Authenticator;
import br.gov.frameworkdemoiselle.security.Authorizer;
import br.gov.frameworkdemoiselle.security.User;

public class SecurityConfigTest {

	private SecurityConfig config;

	@Before
	public void setUp() throws Exception {
		this.config = new SecurityConfigImpl();
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
		Authenticator authenticator = new TestAuthenticator();
		config.setAuthenticatorClass(authenticator.getClass());
		assertEquals("br.gov.frameworkdemoiselle.internal.configuration.TestAuthenticator", config
				.getAuthenticatorClass().getName());
	}
	
	@Test
	public void testSetAuthorizerClass() {
		Authorizer authorizer = new TestAuthorizer();
		config.setAuthorizerClass(authorizer.getClass());
		assertEquals("br.gov.frameworkdemoiselle.internal.configuration.TestAuthorizer", config
				.getAuthorizerClass().getName());
	}

}

class TestAuthenticator implements Authenticator {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean authenticate() {
		return false;
	}

	@Override
	public void unAuthenticate() {
	}

	@Override
	public User getUser() {
		return null;
	}
}

class TestAuthorizer implements Authorizer{

	@Override
	public boolean hasRole(String role) {
		return false;
	}

	@Override
	public boolean hasPermission(String resource, String operation) {
		return false;
	}
} 