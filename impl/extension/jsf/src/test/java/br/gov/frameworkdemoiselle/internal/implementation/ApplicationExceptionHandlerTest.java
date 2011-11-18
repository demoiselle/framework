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
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.faces.event.PhaseId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.exception.ApplicationException;
import br.gov.frameworkdemoiselle.internal.configuration.ExceptionHandlerConfig;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.Faces;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class, FacesContext.class, Faces.class })
public class ApplicationExceptionHandlerTest {

	private ApplicationExceptionHandler handler;

	private ExceptionQueuedEventContext eventContext;

	private ExceptionHandlerConfig config;

	private FacesContext facesContext;

	private Collection<ExceptionQueuedEvent> events;

	@SuppressWarnings("serial")
	@ApplicationException
	class AnnotatedAppException extends RuntimeException {
	}

	@SuppressWarnings("serial")
	class SomeException extends RuntimeException {
	}

	@Before
	public void setUp() {

		mockStatic(Beans.class);
		mockStatic(FacesContext.class);

		events = new ArrayList<ExceptionQueuedEvent>();
		ExceptionHandler jsfExceptionHandler = createMock(ExceptionHandler.class);
		handler = new ApplicationExceptionHandler(jsfExceptionHandler);
		eventContext = PowerMock.createMock(ExceptionQueuedEventContext.class);
		ExceptionQueuedEvent event = PowerMock.createMock(ExceptionQueuedEvent.class);
		config = PowerMock.createMock(ExceptionHandlerConfig.class);
		facesContext = PowerMock.createMock(FacesContext.class);

		expect(event.getSource()).andReturn(eventContext);
		expect(Beans.getReference(ExceptionHandlerConfig.class)).andReturn(config);
		expect(FacesContext.getCurrentInstance()).andReturn(facesContext).anyTimes();
		expect(handler.getUnhandledExceptionQueuedEvents()).andReturn(events).times(2);

		events.add(event);

	}

	@Test
	public void testHandleAnApplicationExceptionNotOnRenderResponse() {

		mockStatic(Faces.class);

		AnnotatedAppException exception = new AnnotatedAppException();
		PhaseId phaseId = PowerMock.createMock(PhaseId.class);

		expect(eventContext.getException()).andReturn(exception);
		expect(facesContext.getCurrentPhaseId()).andReturn(phaseId);
		expect(config.isHandleApplicationException()).andReturn(true);

		Faces.addMessage(exception);
		expectLastCall();

		replayAll();

		handler.handle();

		assertTrue(events.isEmpty());

		verifyAll();

	}

	@Test
	public void testHandleAnApplicationExceptionOnRenderResponse() {

		AnnotatedAppException exception = new AnnotatedAppException();
//		PhaseId phaseId = PhaseId.RENDER_RESPONSE;

		expect(eventContext.getException()).andReturn(exception);
//		expect(facesContext.getCurrentPhaseId()).andReturn(phaseId);
		expect(config.isHandleApplicationException()).andReturn(false);

		handler.getWrapped().handle();
		expectLastCall();

		replayAll();

		handler.handle();

		assertFalse(events.isEmpty());

		verifyAll();

	}

	@Test
	public void testHandleAnyException() {

		SomeException exception = new SomeException();
//		PhaseId phaseId = PowerMock.createMock(PhaseId.class);

		expect(eventContext.getException()).andReturn(exception);
//		expect(facesContext.getCurrentPhaseId()).andReturn(phaseId);
		expect(config.isHandleApplicationException()).andReturn(true);

		handler.getWrapped().handle();
		expectLastCall();

		replayAll();

		handler.handle();

		assertFalse(events.isEmpty());

		verifyAll();

	}

	@Test
	public void testDoNotHandleApplicationExceptions() {

		AnnotatedAppException exception = new AnnotatedAppException();

		expect(eventContext.getException()).andReturn(exception);
		expect(config.isHandleApplicationException()).andReturn(false);

		handler.getWrapped().handle();
		expectLastCall();

		replayAll();

		handler.handle();

		assertFalse(events.isEmpty());

		verifyAll();

	}

}
