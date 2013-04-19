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
public class CustomExceptionTest {
	
	@Inject
	private CustomExceptionHandler exception;
	
	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(CustomExceptionTest.class);
		return deployment;
	}
	
	@Test
	public void testCustomExceptionHandler() {
		exception.throwExceptionWithMessage();
		assertEquals(true, exception.isExceptionHandler());
	}
}
