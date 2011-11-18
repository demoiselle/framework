package br.gov.frameworkdemoiselle.internal.implementation;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.ArrayList;
import java.util.Collection;

import javax.faces.context.ExceptionHandler;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.security.NotLoggedInException;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class, CoreBundle.class })
public class AuthenticationExceptionHandlerTest {

	private AuthenticationExceptionHandler handler;

	private ExceptionQueuedEventContext eventContext;

	private Collection<ExceptionQueuedEvent> events;

	@Before
	public void setUp() {

		mockStatic(Beans.class);

		events = new ArrayList<ExceptionQueuedEvent>();
		ExceptionHandler jsfExceptionHandler = createMock(ExceptionHandler.class);
		handler = new AuthenticationExceptionHandler(jsfExceptionHandler);
		eventContext = createMock(ExceptionQueuedEventContext.class);
		ExceptionQueuedEvent event = createMock(ExceptionQueuedEvent.class);
		
		expect(event.getSource()).andReturn(eventContext);
		expect(handler.getUnhandledExceptionQueuedEvents()).andReturn(events).times(2);

		events.add(event);
		
	}

	@Test
	public void testHandleNotLoggedInException() {

		NotLoggedInException exception = new NotLoggedInException("");
		
		SecurityObserver observer = createMock(SecurityObserver.class);
		expect(Beans.getReference(SecurityObserver.class)).andReturn(observer);
		expect(eventContext.getException()).andReturn(exception);
		
		observer.redirectToLoginPage();
		expectLastCall();

		replayAll();

		handler.handle();

		assertTrue(events.isEmpty());

		verifyAll();

	}
	
	@Test
	public void testHandleAnyException() {

		Exception exception = new Exception();

		expect(eventContext.getException()).andReturn(exception);

		handler.getWrapped().handle();
		expectLastCall();
		
		replayAll();

		handler.handle();

		assertFalse(events.isEmpty());

		verifyAll();

	}

}
