package br.gov.frameworkdemoiselle.internal.implementation;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
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
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.annotation.Redirect;

@RunWith(PowerMockRunner.class)
public class RedirectExceptionHandlerTest {

	private RedirectExceptionHandler handler;

	private ExceptionQueuedEventContext eventContext;

	private Collection<ExceptionQueuedEvent> events;
	
	@SuppressWarnings("serial")
	@Redirect
	class AnnotatedException extends RuntimeException {
	}

	@Before
	public void setUp() {

		ExceptionHandler jsfExceptionHandler = createMock(ExceptionHandler.class);
		ExceptionQueuedEvent event = createMock(ExceptionQueuedEvent.class);
		eventContext = createMock(ExceptionQueuedEventContext.class);
		handler = new RedirectExceptionHandler(jsfExceptionHandler);
		events = new ArrayList<ExceptionQueuedEvent>();

		expect(event.getSource()).andReturn(eventContext);
		events.add(event);
		expect(handler.getUnhandledExceptionQueuedEvents()).andReturn(events).times(2);

	}
	
	@Test
	public void testHandleAnAnnotatedException() {

		AnnotatedException exception = new AnnotatedException();
		
		expect(eventContext.getException()).andReturn(exception);
		
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
