package br.gov.frameworkdemoiselle.internal.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

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

}
