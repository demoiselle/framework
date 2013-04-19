package exception;

import javax.inject.Inject;

import static junit.framework.Assert.fail;
import static junit.framework.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.frameworkdemoiselle.DemoiselleException;

import test.Tests;

@RunWith(Arquillian.class)
public class MultiExceptionTest {

	@Inject
	private MultiException multiException;
	
	@Inject
	private MultiExceptionOneHandler multiExceptionOneHandler;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(MultiExceptionTest.class);
		return deployment;
	}

	@Test
	public void testNullPointerExceptionHandler() {
		multiException.throwNullPointerException();
		assertEquals(true, multiException.isNullPointerExceptionHandler());
	}
	
	@Test
	public void testArithmeticExceptionHandler() {
		multiException.throwArithmeticException();
		assertEquals(true, multiException.isArithmeticExceptionHandler());
	}
	
	@Test
	public void testMultiExceptionHandler() {
		multiException.throwTwoException();
		assertEquals(true, multiException.isNullPointerExceptionHandler());
		assertEquals(true, multiException.isArithmeticExceptionHandler());
	}
	
	@Test
	public void testExceptionHandlerWithTwoException() {
		try {
			multiExceptionOneHandler.throwIllegalPathException();
			fail();
		} catch (Exception e) {
			assertEquals(DemoiselleException.class, e.getClass());
		}
	}
}
