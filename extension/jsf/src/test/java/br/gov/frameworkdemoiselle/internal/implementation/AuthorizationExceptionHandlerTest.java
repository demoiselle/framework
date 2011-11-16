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

import br.gov.frameworkdemoiselle.security.AuthorizationException;
import br.gov.frameworkdemoiselle.util.Faces;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class,  CoreBundle.class, Faces.class })
public class AuthorizationExceptionHandlerTest {

	private AuthorizationExceptionHandler handler;

	private ExceptionQueuedEventContext eventContext;

	private Collection<ExceptionQueuedEvent> events;
	
	private FacesContext facesContext;

	@Before
	public void setUp() {
		
		mockStatic(FacesContext.class);

		events = new ArrayList<ExceptionQueuedEvent>();
		ExceptionHandler jsfExceptionHandler = createMock(ExceptionHandler.class);
		handler = new AuthorizationExceptionHandler(jsfExceptionHandler);
		eventContext = createMock(ExceptionQueuedEventContext.class);
		ExceptionQueuedEvent event = createMock(ExceptionQueuedEvent.class);
		facesContext = PowerMock.createMock(FacesContext.class);

		expect(event.getSource()).andReturn(eventContext);
		events.add(event);
		expect(handler.getUnhandledExceptionQueuedEvents()).andReturn(events).times(2);
		expect(FacesContext.getCurrentInstance()).andReturn(facesContext).anyTimes();


	}

	@Test
	public void testHandleAnAuthorizationExceptionNotOnRenderResponse() {

		mockStatic(Faces.class);
		
//		ResourceBundle bundle = new ResourceBundle(ResourceBundle.getBundle("demoiselle-core-bundle"));
		
		AuthorizationException exception = new AuthorizationException("");
		PhaseId phaseId = PowerMock.createMock(PhaseId.class);

		expect(eventContext.getException()).andReturn(exception);
		expect(facesContext.getCurrentPhaseId()).andReturn(phaseId);
		
		Faces.addMessage(exception);
		expectLastCall();

		replayAll();

		handler.handle();

		assertTrue(events.isEmpty());

		verifyAll();

	}
	
	@Test
	public void testHandleAnAuthorizationExceptionOnRenderResponse() {

//		ResourceBundle bundle = new ResourceBundle(ResourceBundle.getBundle("demoiselle-core-bundle"));
		
		AuthorizationException exception = new AuthorizationException("");
		PhaseId phaseId = PhaseId.RENDER_RESPONSE;

		expect(eventContext.getException()).andReturn(exception);
		expect(facesContext.getCurrentPhaseId()).andReturn(phaseId);
		
		handler.getWrapped().handle();
		expectLastCall();

		replayAll();

		handler.handle();

		assertFalse(events.isEmpty());

		verifyAll();

	}
	
	@Test
	public void testHandleAnyException() {
		
		Exception exception = new Exception();
		PhaseId phaseId = PowerMock.createMock(PhaseId.class);

		expect(eventContext.getException()).andReturn(exception);
		expect(facesContext.getCurrentPhaseId()).andReturn(phaseId);

		handler.getWrapped().handle();
		expectLastCall();

		replayAll();

		handler.handle();

		assertFalse(events.isEmpty());

		verifyAll();
		
	}

}
