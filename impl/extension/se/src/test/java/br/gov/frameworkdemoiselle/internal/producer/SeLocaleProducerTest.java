package br.gov.frameworkdemoiselle.internal.producer;

import java.util.Locale;

import junit.framework.Assert;

import org.junit.Test;

public class SeLocaleProducerTest {
	
	@Test
	public void testCreate() {
		
		Locale locale = (new SeLocaleProducer()).create();
		
		Assert.assertNotNull(locale);
		
	}

}
