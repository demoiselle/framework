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
public class ExceptionInheritanceTest {
	
	@Inject
	private ExceptionInheritance exceptionInheritance;
	
	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(ExceptionInheritanceTest.class);
		return deployment;
	}
	
	@Test
	public void testExceptionInheritanceSuperClass() {
		exceptionInheritance.throwNullPointerException();
		assertEquals(true, exceptionInheritance.isHandlerSuperClass());
	}

	@Test
	public void testExceptionInheritanceClass() {
		exceptionInheritance.throwArithmeticException();
		assertEquals(false, exceptionInheritance.isHandlerSuperClass());
		assertEquals(true, exceptionInheritance.isHandlerClass());
	}	
}
