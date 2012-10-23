package br.gov.frameworkdemoiselle.internal.configuration;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;


public class TransactionConfigTest {

	TransactionConfig transactionConfig;
	
	@Before
	public void setUp() {
		transactionConfig = new TransactionConfig();
	}
	
	@Test
	public void testGetTransactionClass() {
		Assert.assertNull(transactionConfig.getTransactionClass());
	}
	
}
