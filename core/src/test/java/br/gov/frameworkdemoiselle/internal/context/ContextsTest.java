/*
 * Demoiselle Framework Copyright (C) 2010 SERPRO
 * ---------------------------------------------------------------------------- This file is part of Demoiselle
 * Framework. Demoiselle Framework is free software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License version 3 as published by the Free Software Foundation. This program is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a
 * copy of the GNU Lesser General Public License version 3 along with this program; if not, see
 * <http://www.gnu.org/licenses/> or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301, USA. ---------------------------------------------------------------------------- Este arquivo
 * é parte do Framework Demoiselle. O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação do Software Livre (FSF). Este
 * programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA GARANTIA; sem uma garantia implícita de
 * ADEQUAÇÃO a qualquer MERCADO ou APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português para
 * maiores detalhes. Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título "LICENCA.txt", junto com esse
 * programa. Se não, acesse <http://www.gnu.org/licenses/> ou escreva para a Fundação do Software Livre (FSF) Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.internal.context;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.AfterBeanDiscovery;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.gov.frameworkdemoiselle.annotation.ViewScoped;

public class ContextsTest {

	private AfterBeanDiscovery event;

	@BeforeClass
	public static void setUpClass() throws Exception {
		// TODO AfterBeanDiscovery event = ??? mock ???
	}

	@Before
	public void setUp() throws Exception {
		Contexts.clear();
	}

	@Test
	public void testRemovingInexistentContext() {
		Contexts.remove(new ThreadLocalContext(SessionScoped.class));
	}

	@Test
	public void testRemovingLastInactiveContext() {
		ThreadLocalContext context1 = new ThreadLocalContext(RequestScoped.class);
		ThreadLocalContext context2 = new ThreadLocalContext(RequestScoped.class);
		ThreadLocalContext context3 = new ThreadLocalContext(RequestScoped.class);

		Contexts.add(context1, event);
		Contexts.add(context2, event);
		Contexts.add(context3, event);
		Contexts.remove(context3);
		assertFalse(Contexts.getInactiveContexts().contains(context3));
	}

	@Test
	public void testRemovingActiveContextAndActivatingInactiveContext() {
		ThreadLocalContext context1 = new ThreadLocalContext(SessionScoped.class);
		ThreadLocalContext context2 = new ThreadLocalContext(SessionScoped.class);
		ThreadLocalContext context3 = new ThreadLocalContext(SessionScoped.class);

		Contexts.add(context1, event);
		Contexts.add(context2, event);
		Contexts.add(context3, event);
		assertTrue(context1.isActive());
		assertFalse(context2.isActive());
		assertFalse(context3.isActive());

		Contexts.remove(context1);
		assertTrue(context2.isActive());
		assertFalse(context3.isActive());

		Contexts.remove(context2);
		assertTrue(context3.isActive());
	}

	@Test
	public void testRemovingActiveContext() {
		ThreadLocalContext context = new ThreadLocalContext(SessionScoped.class);

		Contexts.add(context, event);
		Contexts.remove(context);
		assertEquals(0, Contexts.getActiveContexts().size());
	}

	@Test
	public void testRemovingInactiveContext() {
		ThreadLocalContext context = new ThreadLocalContext(SessionScoped.class);

		Contexts.add(new ThreadLocalContext(SessionScoped.class), event);
		Contexts.add(context, event);
		Contexts.remove(context);
		assertEquals(0, Contexts.getInactiveContexts().size());
	}

	@Test
	public void testClear() {
		List<ThreadLocalContext> list = new ArrayList<ThreadLocalContext>();

		list.add(new ThreadLocalContext(SessionScoped.class));
		list.add(new ThreadLocalContext(SessionScoped.class));
		list.add(new ThreadLocalContext(ApplicationScoped.class));

		for (ThreadLocalContext context : list) {
			Contexts.add(context, event);
		}

		Contexts.clear();
		assertEquals(0, Contexts.getActiveContexts().size());
		assertEquals(0, Contexts.getInactiveContexts().size());

		for (ThreadLocalContext context : list) {
			assertFalse(context.isActive());
		}
	}

	@Test
	public void testAdd() {
		Contexts.add(new ThreadLocalContext(SessionScoped.class), event);
		assertEquals(1, Contexts.getActiveContexts().size());
	}

	@Test
	public void testAddingRepeatedScopeType() {
		Contexts.add(new ThreadLocalContext(SessionScoped.class), event);
		assertEquals(1, Contexts.getActiveContexts().size());
		assertEquals(0, Contexts.getInactiveContexts().size());

		Contexts.add(new ThreadLocalContext(SessionScoped.class), event);
		assertEquals(1, Contexts.getActiveContexts().size());
		assertEquals(1, Contexts.getInactiveContexts().size());
	}

	@Test
	public void testAddingRepeatedScopeInstance() {
		ThreadLocalContext context1 = new ThreadLocalContext(SessionScoped.class);
		ThreadLocalContext context2 = new ThreadLocalContext(SessionScoped.class);

		Contexts.add(context1, event);
		Contexts.add(context2, event);

		assertTrue(context1.isActive());
		assertFalse(context2.isActive());

		assertEquals(1, Contexts.getActiveContexts().size());
		assertEquals(1, Contexts.getInactiveContexts().size());
	}

	@Test
	public void testIsActive() {
		ThreadLocalContext context = new ThreadLocalContext(SessionScoped.class);

		Contexts.add(context, event);
		assertTrue(context.isActive());
	}

	@Test
	public void testIsInactive() {
		ThreadLocalContext context = new ThreadLocalContext(ViewScoped.class);

		Contexts.add(new ThreadLocalContext(ViewScoped.class), event);
		Contexts.add(context, event);
		assertFalse(context.isActive());
	}

	@Test
	public void testAddWithEventNotNull() {
		event = createMock(AfterBeanDiscovery.class);
		ThreadLocalContext context = new ThreadLocalContext(SessionScoped.class);
		event.addContext(context);
		expectLastCall();
		replay(event);

		Contexts.add(context, event);
		assertEquals(1, Contexts.getActiveContexts().size());
	}
	
	// Only to get 100% on coverage report
	@Test
	public void testCreateNew() {
		Contexts context = new Contexts();
		Assert.assertNotNull(context);
	}

}
