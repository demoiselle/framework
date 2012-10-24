package br.gov.frameworkdemoiselle.internal.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


public class JsfSecurityConfigTest {

	private JsfSecurityConfig config;
	
	@Before
	public void setUp() throws Exception {
		this.config = new JsfSecurityConfig();
	}
	
	@Test
	public void testGetLoginPage() {
		assertEquals("/login", config.getLoginPage());
	}
	
	@Test
	public void testGetRedirectAfterLogin() {
		assertEquals("/index", config.getRedirectAfterLogin());
	}
	
	@Test
	public void testGetRedirectAfterLogout() {
		assertEquals("/login", config.getRedirectAfterLogout());
	}
	
	@Test
	public void testIsRedirectEnabled() {
		assertEquals(true, config.isRedirectEnabled());
	}
	
}
