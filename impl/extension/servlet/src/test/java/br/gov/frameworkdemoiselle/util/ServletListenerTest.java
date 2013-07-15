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
//package br.gov.frameworkdemoiselle.util;
//
//import static org.easymock.EasyMock.createMock;
//import static org.easymock.EasyMock.expect;
//import static org.powermock.api.easymock.PowerMock.mockStatic;
//import static org.powermock.api.easymock.PowerMock.replayAll;
//import static org.powermock.api.easymock.PowerMock.verifyAll;
//
//import javax.enterprise.inject.spi.BeanManager;
//import javax.servlet.ServletContextEvent;
//
//import org.easymock.EasyMock;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.powermock.api.easymock.PowerMock;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//import br.gov.frameworkdemoiselle.internal.bootstrap.ShutdownBootstrap;
//import br.gov.frameworkdemoiselle.lifecycle.AfterStartupProccess;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(Beans.class)
//public class ServletListenerTest {
//
//	private ServletListener listener;
//
//	@Test
//	public void testContextInitialized() {
//		ServletContextEvent event = createMock(ServletContextEvent.class);
//		BeanManager beanManager = PowerMock.createMock(BeanManager.class);
//
//		mockStatic(Beans.class);
//		expect(Beans.getBeanManager()).andReturn(beanManager);
//		beanManager.fireEvent(EasyMock.anyObject(AfterStartupProccess.class));
//		PowerMock.expectLastCall().times(1);
//
//		replayAll();
//
//		listener = new ServletListener();
//		listener.contextInitialized(event);
//
//		verifyAll();
//	}
//
//	@Test
//	public void testContextDestroyed() {
//		ServletContextEvent event = createMock(ServletContextEvent.class);
//		BeanManager beanManager = PowerMock.createMock(BeanManager.class);
//
//		mockStatic(Beans.class);
//		expect(Beans.getBeanManager()).andReturn(beanManager);
//		beanManager.fireEvent(EasyMock.anyObject(ShutdownBootstrap.class));
//		PowerMock.expectLastCall().times(1);
//
//		replayAll();
//
//		listener = new ServletListener();
//		listener.contextDestroyed(event);
//
//		verifyAll();
//	}
//
//}
