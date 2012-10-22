package br.gov.frameworkdemoiselle.internal.configuration;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ExceptionHandlerConfigTest {

	private ExceptionHandlerConfig config;

	@Before
	public void setUP() throws Exception {
		this.config = new ExceptionHandlerConfig();
	}

	@Test
	public void testGetExceptionPage() {
		assertEquals("/application_error", config.getExceptionPage());
	}

	@Test
	public void testIsHandleApplicationException() {
		assertEquals(true, config.isHandleApplicationException());
	}
	
}
