package exception;

import javax.inject.Inject;

import static junit.framework.Assert.fail;
import static junit.framework.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;

@RunWith(Arquillian.class)
public class OneExceptionTest {

	@Inject
	private OneException oneException;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(OneExceptionTest.class);
		return deployment;
	}

	@Test
	public void testExceptionWithHandler() {
		oneException.throwExceptionWithHandler();
		assertEquals(true, oneException.isExceptionHandler());
	}

	@Test
	public void testExceptionWithoutHandler() {
		try {
			oneException.throwExceptionWithoutHandler();
			fail();
		} catch (Exception cause) {
			assertEquals(ArithmeticException.class, cause.getClass());
		}
	}
	
	@Test
	public void testExceptionWithMultiHandler() {
		oneException.throwExceptionIllegalArgument();
		assertEquals(false, oneException.isExceptionHandlerIllegalArgument1());
		assertEquals(true, oneException.isExceptionHandlerIllegalArgument2());
		assertEquals(false, oneException.isExceptionHandlerIllegalArgument3());
	}	
}
