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
//import static org.powermock.api.easymock.PowerMock.replayAll;
//import static org.powermock.api.easymock.PowerMock.verifyAll;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
//import javax.faces.context.ExceptionHandler;
//import javax.faces.event.ExceptionQueuedEvent;
//import javax.faces.event.ExceptionQueuedEventContext;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import br.gov.frameworkdemoiselle.annotation.Redirect;
//
//@RunWith(PowerMockRunner.class)
//public class RedirectExceptionHandlerTest {
//
//	private RedirectExceptionHandler handler;
//
//	private ExceptionQueuedEventContext eventContext;
//
//	private Collection<ExceptionQueuedEvent> events;
//
//	@SuppressWarnings("serial")
//	@Redirect
//	class AnnotatedException extends RuntimeException {
//	}
//
//	@Before
//	public void setUp() {
//
//		ExceptionHandler jsfExceptionHandler = createMock(ExceptionHandler.class);
//		ExceptionQueuedEvent event = createMock(ExceptionQueuedEvent.class);
//		eventContext = createMock(ExceptionQueuedEventContext.class);
//		handler = new RedirectExceptionHandler(jsfExceptionHandler);
//		events = new ArrayList<ExceptionQueuedEvent>();
//
//		expect(event.getSource()).andReturn(eventContext);
//		events.add(event);
//		expect(handler.getUnhandledExceptionQueuedEvents()).andReturn(events).times(2);
//
//	}
//
//	@Test
//	public void testHandleAnAnnotatedException() {
//
//		AnnotatedException exception = new AnnotatedException();
//
//		expect(eventContext.getException()).andReturn(exception);
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
//	public void testHandleAnyException() {
//
//		Exception exception = new Exception();
//
//		expect(eventContext.getException()).andReturn(exception);
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
