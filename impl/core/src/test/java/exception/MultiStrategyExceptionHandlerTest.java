package exception;

import static junit.framework.Assert.assertEquals;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;

@RunWith(Arquillian.class)
public class MultiStrategyExceptionHandlerTest {

	@Inject
	private MultiStrategyExceptionHandler handlerTest;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(OneExceptionTest.class);
		return deployment;
	}

	@Test
	public void testExceptionMultiStrategyTryAndHandler() {
		handlerTest.exceptionMultiStrategyTryAndHandler();
		assertEquals(true, handlerTest.isExceptionTryCacth());
		assertEquals(true, handlerTest.isExceptionHandler());
	}
	
	@Test
	public void testExceptionMultiStrategyHandlerInTry() {
		handlerTest.exceptionMultiStrategyHandlerInTry();
		assertEquals(true, handlerTest.isExceptionTryCacth());
		assertEquals(true, handlerTest.isExceptionHandler());
	}
	
	@Test
	public void testExceptionMultiStrategyHandlerAndTry() {
		handlerTest.exceptionMultiStrategyHandlerAndTry();
		assertEquals(true, handlerTest.isExceptionTryCacth());
		assertEquals(true, handlerTest.isExceptionHandler());
	}	
	
	@Test
	public void testSameExceptionTwoStrategyHandler() {
		handlerTest.exceptionTwoHandler();
		assertEquals(true, handlerTest.isExceptionTryCacth());
		assertEquals(false, handlerTest.isExceptionHandler());
	}
	
	@Test
	public void testExceptionOneStrategyHandler() {
		handlerTest.exceptionHandler();
		assertEquals(false, handlerTest.isExceptionTryCacth());
		assertEquals(true, handlerTest.isExceptionHandler());
	}		
}
