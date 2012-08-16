///*
// * Demoiselle Framework
// * Copyright (C) 2010 SERPRO
// * ----------------------------------------------------------------------------
// * This file is part of Demoiselle Framework.
// * 
// * Demoiselle Framework is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public License version 3
// * as published by the Free Software Foundation.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU Lesser General Public License version 3
// * along with this program; if not,  see <http://www.gnu.org/licenses/>
// * or write to the Free Software Foundation, Inc., 51 Franklin Street,
// * Fifth Floor, Boston, MA  02110-1301, USA.
// * ----------------------------------------------------------------------------
// * Este arquivo é parte do Framework Demoiselle.
// * 
// * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
// * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
// * do Software Livre (FSF).
// * 
// * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
// * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
// * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
// * para maiores detalhes.
// * 
// * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
// * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
// * ou escreva para a Fundação do Software Livre (FSF) Inc.,
// * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
// */
//package br.gov.frameworkdemoiselle.internal.implementation;
//
//import static junit.framework.Assert.assertFalse;
//import static junit.framework.Assert.assertTrue;
//import static org.easymock.EasyMock.expect;
//import static org.powermock.api.easymock.PowerMock.createMock;
//import static org.powermock.api.easymock.PowerMock.expectLastCall;
//import static org.powermock.api.easymock.PowerMock.mockStatic;
//import static org.powermock.api.easymock.PowerMock.replayAll;
//import static org.powermock.api.easymock.PowerMock.verifyAll;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
//import javax.faces.context.ExceptionHandler;
//import javax.faces.context.FacesContext;
//import javax.faces.event.ExceptionQueuedEvent;
//import javax.faces.event.ExceptionQueuedEventContext;
//import javax.faces.event.PhaseId;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.powermock.api.easymock.PowerMock;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import br.gov.frameworkdemoiselle.security.AuthorizationException;
//import br.gov.frameworkdemoiselle.util.Faces;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ FacesContext.class,  CoreBundle.class, Faces.class })
//public class AuthorizationExceptionHandlerTest {
//
//	private AuthorizationExceptionHandler handler;
//
//	private ExceptionQueuedEventContext eventContext;
//
//	private Collection<ExceptionQueuedEvent> events;
//	
//	private FacesContext facesContext;
//
//	@Before
//	public void setUp() {
//		
//		mockStatic(FacesContext.class);
//
//		events = new ArrayList<ExceptionQueuedEvent>();
//		ExceptionHandler jsfExceptionHandler = createMock(ExceptionHandler.class);
//		handler = new AuthorizationExceptionHandler(jsfExceptionHandler);
//		eventContext = createMock(ExceptionQueuedEventContext.class);
//		ExceptionQueuedEvent event = createMock(ExceptionQueuedEvent.class);
//		facesContext = PowerMock.createMock(FacesContext.class);
//
//		expect(event.getSource()).andReturn(eventContext);
//		events.add(event);
//		expect(handler.getUnhandledExceptionQueuedEvents()).andReturn(events).times(2);
//		expect(FacesContext.getCurrentInstance()).andReturn(facesContext).anyTimes();
//
//
//	}
//
//	@Test
//	public void testHandleAnAuthorizationExceptionNotOnRenderResponse() {
//
//		mockStatic(Faces.class);
//		
////		ResourceBundle bundle = new ResourceBundle(ResourceBundle.getBundle("demoiselle-core-bundle"));
//		
//		AuthorizationException exception = new AuthorizationException("");
//		PhaseId phaseId = PowerMock.createMock(PhaseId.class);
//
//		expect(eventContext.getException()).andReturn(exception);
//		expect(facesContext.getCurrentPhaseId()).andReturn(phaseId);
//		
//		Faces.addMessage(exception);
//		expectLastCall();
//
//		replayAll();
//
//		handler.handle();
//
//		assertTrue(events.isEmpty());
//
//		verifyAll();
//
//	}
//	
//	@Test
//	public void testHandleAnAuthorizationExceptionOnRenderResponse() {
//
////		ResourceBundle bundle = new ResourceBundle(ResourceBundle.getBundle("demoiselle-core-bundle"));
//		
//		AuthorizationException exception = new AuthorizationException("");
//		PhaseId phaseId = PhaseId.RENDER_RESPONSE;
//
//		expect(eventContext.getException()).andReturn(exception);
//		expect(facesContext.getCurrentPhaseId()).andReturn(phaseId);
//		
//		handler.getWrapped().handle();
//		expectLastCall();
//
//		replayAll();
//
//		handler.handle();
//
//		assertFalse(events.isEmpty());
//
//		verifyAll();
//
//	}
//	
//	@Test
//	public void testHandleAnyException() {
//		
//		Exception exception = new Exception();
//		PhaseId phaseId = PowerMock.createMock(PhaseId.class);
//
//		expect(eventContext.getException()).andReturn(exception);
//		expect(facesContext.getCurrentPhaseId()).andReturn(phaseId);
//
//		handler.getWrapped().handle();
//		expectLastCall();
//
//		replayAll();
//
//		handler.handle();
//
//		assertFalse(events.isEmpty());
//
//		verifyAll();
//		
//	}
//
//}
