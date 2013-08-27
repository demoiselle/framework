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
//package br.gov.frameworkdemoiselle.template;
//
//import static org.easymock.EasyMock.expect;
//import static org.junit.Assert.assertEquals;
//import static org.powermock.api.easymock.PowerMock.replayAll;
//import static org.powermock.api.easymock.PowerMock.verifyAll;
//
//import javax.faces.component.UIViewRoot;
//import javax.faces.context.FacesContext;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.powermock.api.easymock.PowerMock;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;
//
//import br.gov.frameworkdemoiselle.annotation.NextView;
//import br.gov.frameworkdemoiselle.annotation.PreviousView;
//import br.gov.frameworkdemoiselle.message.MessageContext;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ MessageContext.class })
//public class AbstractPageTest {
//
//	private AbstractPageBean abstractPage;
//
//	private FacesContext facesContext;
//
//	private UIViewRoot viewRoot;
//
//	@Before
//	public void before() {
//		facesContext = PowerMock.createMock(FacesContext.class);
//		viewRoot = PowerMock.createMock(UIViewRoot.class);
//		expect(facesContext.getViewRoot()).andReturn(viewRoot).anyTimes();
//		expect(viewRoot.getViewId()).andReturn("viewId").anyTimes();
//	}
//
//	@Test
//	public void testGetCurrentView() {
//		abstractPage = new MyAbstractPage();
//		Whitebox.setInternalState(abstractPage, "facesContext", facesContext);
//
//		replayAll();
//		assertEquals(abstractPage.getCurrentView(), "viewId");
//		verifyAll();
//	}
//
//	@Test
//	public void testGetNextViewOK() {
//		abstractPage = new MyAbstractPage();
//
//		replayAll();
//		assertEquals(abstractPage.getNextView(), "next_view");
//		verifyAll();
//	}
//
//	@Test
//	public void testGetNextViewWhenNoAnnotation() {
//		abstractPage = new MyAbstractPageNoAnnotations();
//
//		replayAll();
//		assertEquals(abstractPage.getNextView(), null);
//		verifyAll();
//	}
//
//	@Test
//	public void testGetNextViewAlreadySet() {
//		abstractPage = new MyAbstractPageNoAnnotations();
//		Whitebox.setInternalState(abstractPage, "nextView", "next");
//
//		replayAll();
//		assertEquals(abstractPage.getNextView(), "next");
//		verifyAll();
//	}
//
//	@Test
//	public void testGetPreviousViewOK() {
//		abstractPage = new MyAbstractPage();
//
//		replayAll();
//		assertEquals(abstractPage.getPreviousView(), "prevs");
//		verifyAll();
//	}
//
//	@Test
//	public void testGetPreviousViewAlreadySet() {
//		abstractPage = new MyAbstractPageNoAnnotations();
//		Whitebox.setInternalState(abstractPage, "previousView", "previous");
//
//		replayAll();
//		assertEquals(abstractPage.getPreviousView(), "previous");
//		verifyAll();
//	}
//
//	@Test
//	public void testGetPreviousViewWhenNoAnnotation() {
//		abstractPage = new MyAbstractPageNoAnnotations();
//
//		replayAll();
//		assertEquals(abstractPage.getPreviousView(), null);
//		verifyAll();
//	}
//
//	@Test
//	public void testOtherTests() {
//		abstractPage = new MyAbstractPageNoAnnotations();
//		assertEquals(null, abstractPage.getTitle());
//	}
//}
//
//@SuppressWarnings("serial")
//@NextView("next_view")
//@PreviousView("prevs")
//class MyAbstractPage extends AbstractPageBean {
//
//}
//
//@SuppressWarnings("serial")
//class MyAbstractPageNoAnnotations extends AbstractPageBean {
//
//}
