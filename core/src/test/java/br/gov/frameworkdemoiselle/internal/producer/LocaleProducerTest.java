package br.gov.frameworkdemoiselle.internal.producer;

import java.util.Locale;

import junit.framework.Assert;

import org.junit.Test;

public class LocaleProducerTest {
	
	@Test
	public void testCreate() {
		
		Locale locale = (new LocaleProducer()).create();
		
		Assert.assertNotNull(locale);
		
	}

}
