package br.gov.frameworkdemoiselle.transaction;

import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.Test;


public class TransactionExceptionTest {

	SQLException cause = new SQLException();
	
	@Test
	public void testTransactionException() {
		TransactionException test = new TransactionException(cause);
		Assert.assertEquals("java.sql.SQLException", test.getCause().toString());
	}
	
}
