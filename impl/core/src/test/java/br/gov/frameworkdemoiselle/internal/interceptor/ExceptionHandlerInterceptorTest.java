/*
 * Demoiselle Framework
 * Copyright (C) 2010 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.internal.interceptor;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import javax.interceptor.InvocationContext;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.exception.ExceptionHandler;
import br.gov.frameworkdemoiselle.internal.bootstrap.CoreBootstrap;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CoreBootstrap.class)
public class ExceptionHandlerInterceptorTest {

	private ExceptionHandlerInterceptor interceptor;

	private InvocationContext context;

	private Logger logger;

	private ResourceBundle bundle;

	class TestException extends DemoiselleException {

		private static final long serialVersionUID = 1L;

		public TestException(String message) {
			super(message);
		}
	}
	

	class ClassWithMethodsAnnotatedWithExceptionHandler {

		int times = 0;

		@ExceptionHandler
		public void methodWithExceptionHandlerAnotation(DemoiselleException cause) {
			times++;
		}

		@ExceptionHandler
		public void methodWithExceptionHandlerAnotationAndGenericException(Exception cause) {
			times++;
		}

	}

	class ClassWithoutMethodsAnnotatedWithExceptionHandler {

		public void methodWithoutExceptionHandlerAnotation(DemoiselleException cause) {
		}
	}

	class ClassWithMethodsAnnotatedWithExceptionHandlerAndThrowException {

		int times = 0;

		@ExceptionHandler
		public void methodWithExceptionHandlerAnotation(DemoiselleException cause) {
			times++;
			throw new RuntimeException();
		}
	}

	class ClassWithMethodWithoutParameterAnnotatedWithExceptionHandler {

		@ExceptionHandler
		public void methodWithExceptionHandlerAnotation() {
		}

	}

	class ClassWithExceptionHandlerMethodThatRethrowException {

		int times = 0;

		@ExceptionHandler
		public void methodThatRethrowException(TestException cause) {
			times++;
			throw cause;
		}

	}

	@Before
	public void setUp() throws Exception {
		this.logger = PowerMock.createMock(Logger.class);
		this.bundle = new ResourceBundle(ResourceBundle.getBundle("demoiselle-core-bundle"));
		this.logger.info(EasyMock.anyObject(String.class));
		PowerMock.expectLastCall().anyTimes();
		replay(this.logger);
		this.interceptor = new ExceptionHandlerInterceptor(this.logger, this.bundle);
		this.context = PowerMock.createMock(InvocationContext.class);
		mockStatic(CoreBootstrap.class);
	}

	@Test
	public void manageSuccessfully() throws Throwable {
		expect(this.context.proceed()).andReturn(null);
		replay();
		assertEquals(null, this.interceptor.manage(this.context));
		verify();
	}	

	@Test
	public void manageWithClassThatDoesNotContainHandleMethod() throws Exception {
		ClassWithoutMethodsAnnotatedWithExceptionHandler classWithoutException = new ClassWithoutMethodsAnnotatedWithExceptionHandler();
		expect(this.context.getTarget()).andReturn(classWithoutException);
		expect(this.context.proceed()).andThrow(new DemoiselleException(""));
		expect(CoreBootstrap.isAnnotatedType(ClassWithoutMethodsAnnotatedWithExceptionHandler.class)).andReturn(true);
		replayAll(this.context, ClassWithoutMethodsAnnotatedWithExceptionHandler.class);

		try {
			this.interceptor.manage(this.context);
			fail();
		} catch (DemoiselleException e) {
			assertTrue(true);
		}

		verifyAll();
	}

	@Test
	public void manageWithClassThatContainsHandleMethod() throws Exception {
		ClassWithMethodsAnnotatedWithExceptionHandler classWithException = new ClassWithMethodsAnnotatedWithExceptionHandler();
		expect(this.context.getTarget()).andReturn(classWithException).anyTimes();
		expect(this.context.proceed()).andThrow(new DemoiselleException(""));
		expect(CoreBootstrap.isAnnotatedType(ClassWithMethodsAnnotatedWithExceptionHandler.class)).andReturn(true);
		replayAll(this.context, CoreBootstrap.class);

		assertNull(this.interceptor.manage(this.context));
		assertEquals(1, classWithException.times);
		verifyAll();
	}	

	@Test
	public void manageWithClassThatContainsHandleMethodWithDiferentException() throws Exception {
		ClassWithMethodsAnnotatedWithExceptionHandler classWithException = new ClassWithMethodsAnnotatedWithExceptionHandler();
		expect(this.context.getTarget()).andReturn(classWithException).anyTimes();
		expect(this.context.proceed()).andThrow(new TestException(""));
		replay(this.context);
		expect(CoreBootstrap.isAnnotatedType(ClassWithMethodsAnnotatedWithExceptionHandler.class)).andReturn(true);
		replayAll(this.context, CoreBootstrap.class);

		assertNull(this.interceptor.manage(this.context));
		assertEquals(1, classWithException.times);	

		verify();
	}

	@Test
	public void manageWithClassThatContainsHandleMethodThatThrowsAnotherException() throws Exception {
		ClassWithMethodsAnnotatedWithExceptionHandlerAndThrowException classWithException = new ClassWithMethodsAnnotatedWithExceptionHandlerAndThrowException();
		expect(this.context.getTarget()).andReturn(classWithException).anyTimes();
		expect(this.context.proceed()).andThrow(new DemoiselleException(""));
		expect(CoreBootstrap.isAnnotatedType(ClassWithMethodsAnnotatedWithExceptionHandlerAndThrowException.class))
				.andReturn(true);
		replayAll(this.context, CoreBootstrap.class);

		try {
			this.interceptor.manage(this.context);
			fail();
		} catch (RuntimeException e) {
			assertEquals(1, classWithException.times);
		}

		verifyAll();
	}

	@Test
	public void manageWithClassThatContainsHandleMethodsAndIsInvokedTwice() throws Exception {
		ClassWithMethodsAnnotatedWithExceptionHandler classWithException = new ClassWithMethodsAnnotatedWithExceptionHandler();
		expect(this.context.getTarget()).andReturn(classWithException).anyTimes();
		expect(this.context.proceed()).andThrow(new DemoiselleException(""));
		expect(CoreBootstrap.isAnnotatedType(ClassWithMethodsAnnotatedWithExceptionHandler.class)).andReturn(true)
				.anyTimes();
		replayAll(this.context, CoreBootstrap.class);

		assertNull(this.interceptor.manage(this.context));
		assertEquals(1, classWithException.times);

		this.context = PowerMock.createMock(InvocationContext.class);
		expect(this.context.getTarget()).andReturn(classWithException).anyTimes();
		expect(this.context.proceed()).andThrow(new Exception(""));
		replayAll(this.context, CoreBootstrap.class);

		assertNull(this.interceptor.manage(this.context));
		assertEquals(2, classWithException.times);
		verifyAll();

	}

	@Test
	public void manageWithClassThatContainsHandleMethodWithoutParameter() throws Exception {
		ClassWithMethodWithoutParameterAnnotatedWithExceptionHandler classWithException = new ClassWithMethodWithoutParameterAnnotatedWithExceptionHandler();
		expect(this.context.getTarget()).andReturn(classWithException).anyTimes();
		expect(this.context.proceed()).andThrow(new DemoiselleException(""));
		expect(CoreBootstrap.isAnnotatedType(ClassWithMethodWithoutParameterAnnotatedWithExceptionHandler.class))
				.andReturn(true);
		replayAll(this.context, CoreBootstrap.class);

		try {
			this.interceptor.manage(this.context);
			fail();
		} catch (DemoiselleException e) {
			assertTrue(true);
		}

		verifyAll();
	}

	@Test
	public void manageHandlerMethodThatRethrowExpectedException() throws Exception {
		ClassWithExceptionHandlerMethodThatRethrowException testClass = new ClassWithExceptionHandlerMethodThatRethrowException();
		expect(this.context.getTarget()).andReturn(testClass).anyTimes();
		expect(this.context.proceed()).andThrow(new TestException(""));
		expect(CoreBootstrap.isAnnotatedType(ClassWithExceptionHandlerMethodThatRethrowException.class))
				.andReturn(true);
		replayAll(this.context, CoreBootstrap.class);

		try {
			this.interceptor.manage(this.context);
			fail();
		} catch (TestException e) {
			assertEquals(1, testClass.times);
		}

		verifyAll();
	}

	/**
	 * Tests an exception handler when the class that contains the method is a proxy. This is the case when using
	 * injection.
	 * 
	 * @throws Exception
	 */
	@Test
	public void manageHandlerMethodInsideProxyClass() throws Exception {
		// creates a proxy class
		ClassWithExceptionHandlerMethodThatRethrowException testClass = PowerMock
				.createNicePartialMockForAllMethodsExcept(ClassWithExceptionHandlerMethodThatRethrowException.class,
						"methodThatRethrowException");
		expect(this.context.getTarget()).andReturn(testClass).anyTimes();
		expect(this.context.proceed()).andThrow(new TestException(""));
		expect(CoreBootstrap.isAnnotatedType(testClass.getClass())).andReturn(false);

		this.logger = PowerMock.createMock(Logger.class);
		this.logger.info(EasyMock.anyObject(String.class));
		this.logger.debug(EasyMock.anyObject(String.class));
		replayAll(testClass, this.context, CoreBootstrap.class, logger);

		this.interceptor = new ExceptionHandlerInterceptor(this.logger, this.bundle);

		try {
			this.interceptor.manage(this.context);
			fail();
		} catch (TestException e) {
			assertEquals(1, testClass.times);
		}
	}
	
	@Test
	public void manageWithClassThatContainsParentExceptionHandleMethod() throws Exception {
		ClassWithMethodsAnnotatedWithExceptionHandler classWithException = new ClassWithMethodsAnnotatedWithExceptionHandler();
		expect(this.context.getTarget()).andReturn(classWithException).anyTimes();
		expect(this.context.proceed()).andThrow(new TestException(""));
		expect(CoreBootstrap.isAnnotatedType(ClassWithMethodsAnnotatedWithExceptionHandler.class)).andReturn(true);
		replayAll(this.context, CoreBootstrap.class);

		assertNull(this.interceptor.manage(this.context));
		assertEquals(1, classWithException.times);
		verifyAll();
	}
	
	@Test
	public void manageWithClassThatDoesNotContainsParentExceptionHandleMethod() throws Exception {
		ClassWithoutMethodsAnnotatedWithExceptionHandler classWithException = new ClassWithoutMethodsAnnotatedWithExceptionHandler();
		expect(this.context.getTarget()).andReturn(classWithException).anyTimes();
		expect(this.context.proceed()).andThrow(new TestException(""));
		expect(CoreBootstrap.isAnnotatedType(ClassWithoutMethodsAnnotatedWithExceptionHandler.class)).andReturn(true);
		replayAll(this.context, CoreBootstrap.class);

		try {
			this.interceptor.manage(this.context);
			fail();
		} catch (TestException e) {
			assertTrue(true);
		}

		verifyAll();
	}
	
	@Test
	public void manageWithClassThatContainsOnlySubExceptionHandleMethod() throws Exception {
		ClassWithExceptionHandlerMethodThatRethrowException classWithException = new ClassWithExceptionHandlerMethodThatRethrowException();
		expect(this.context.getTarget()).andReturn(classWithException).anyTimes();
		expect(this.context.proceed()).andThrow(new DemoiselleException(""));
		expect(CoreBootstrap.isAnnotatedType(ClassWithExceptionHandlerMethodThatRethrowException.class)).andReturn(true);
		replayAll(this.context, CoreBootstrap.class);

		try {
			this.interceptor.manage(this.context);
			fail();
		} catch (DemoiselleException e) {
			assertTrue(true);
		}

		verifyAll();
	}

}
