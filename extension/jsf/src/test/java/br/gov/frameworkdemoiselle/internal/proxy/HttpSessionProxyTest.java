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
package br.gov.frameworkdemoiselle.internal.proxy;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@SuppressWarnings("deprecation")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ HttpSession.class, ServletContext.class, Enumeration.class, HttpSessionContext.class })
public class HttpSessionProxyTest {

	private HttpSessionProxy proxy;

	private ServletContext servletContext;

	private Enumeration<?> enumeration;

	private HttpSession session;

	private HttpSessionContext sessionContext;
	
	@Before
	public void before() {
		session = PowerMock.createMock(HttpSession.class);
		servletContext = PowerMock.createMock(ServletContext.class);
		enumeration = PowerMock.createMock(Enumeration.class);
		sessionContext = PowerMock.createMock(HttpSessionContext.class);

		expect(session.getValueNames()).andReturn(new String[] {"abcdef"});
		expect(session.getValue("value")).andReturn("value");
		expect(session.getSessionContext()).andReturn(sessionContext);
		expect(session.getCreationTime()).andReturn(10L);
		expect(session.getId()).andReturn("ID");
		expect(session.getLastAccessedTime()).andReturn(1L);
		expect(session.getServletContext()).andReturn(servletContext);
		expect(session.getMaxInactiveInterval()).andReturn(2);
		expect(session.getAttribute("attribute")).andReturn("attribute-1");
		expect(session.getAttributeNames()).andReturn(enumeration);
		expect(session.isNew()).andReturn(true);

		session.removeValue("removeValue");
		session.putValue("put", "it");
		session.invalidate();
		session.removeAttribute("remove");
		session.setAttribute("name", "object");
		session.setMaxInactiveInterval(1);

		replay(session);

		proxy = new HttpSessionProxy(session);
	}

	@Test
	public void testDelegation() {
		assertEquals(sessionContext, proxy.getSessionContext());
		assertEquals("value", proxy.getValue("value"));
		assertEquals("abcdef", proxy.getValueNames()[0]);
		assertEquals(10L, proxy.getCreationTime());
		assertEquals("ID", proxy.getId());
		assertEquals(1L, proxy.getLastAccessedTime());
		assertEquals(servletContext, proxy.getServletContext());
		assertEquals(2, proxy.getMaxInactiveInterval());
		assertEquals("attribute-1", proxy.getAttribute("attribute"));
		assertEquals(enumeration, proxy.getAttributeNames());
		assertEquals(true, proxy.isNew());
		
		proxy.removeValue("removeValue");
		proxy.putValue("put","it");
		proxy.invalidate();
		proxy.removeAttribute("remove");
		proxy.setAttribute("name", "object");
		proxy.setMaxInactiveInterval(1);

		verify(session);
	}
}
