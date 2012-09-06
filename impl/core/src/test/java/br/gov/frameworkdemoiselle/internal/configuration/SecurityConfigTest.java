package br.gov.frameworkdemoiselle.internal.configuration;

import org.junit.Ignore;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import br.gov.frameworkdemoiselle.security.SecurityConfig;


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

}
