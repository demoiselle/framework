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
package br.gov.frameworkdemoiselle.internal.implementation;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.gov.frameworkdemoiselle.internal.configuration.PaginationConfig;
import br.gov.frameworkdemoiselle.pagination.Pagination;
import br.gov.frameworkdemoiselle.pagination.PaginationContext;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Beans.class)
public class PaginationContextImplTest {

	private PaginationContext context;

	private PaginationConfig config = new PaginationConfig();
	
	private Pagination pagination;

	public void setUp() {
		context = new PaginationContextImpl();

		PaginationConfig config = PowerMock.createMock(PaginationConfig.class);
		EasyMock.expect(config.getPageSize()).andReturn(10).anyTimes();
		EasyMock.replay(config);

		Whitebox.setInternalState(context, "config", config);
		Whitebox.setInternalState(context, "cache", getInitialMap());
	}

	@After
	public void tearDown() {
		context = null;
	}

	private Map<Class<?>, Pagination> getInitialMap() {
		Map<Class<?>, Pagination> map = new HashMap<Class<?>, Pagination>();
		pagination = new PaginationImpl();
		map.put(getClass(), pagination);

		return map;
	}
	
	@Test
	public void testNullPaginationConfig() {
		context = new PaginationContextImpl();
		
		mockStatic(Beans.class);
		expect(Beans.getReference(PaginationConfig.class)).andReturn(config).anyTimes();
		replayAll(Beans.class);

		assertNotNull(context.getPagination(Object.class, true));
	}

	@Test
	public void testGetPaginationWithoutCreateParameter() {
		setUp();
		
		assertEquals(pagination, context.getPagination(getClass()));
		assertNull(context.getPagination(Object.class));
	}

	@Test
	public void testGetPaginationWithCreateParameterTrueValued() {
		setUp();
		
		assertEquals(pagination, context.getPagination(getClass(), true));
		assertNotNull(context.getPagination(Object.class, true));
	}

	@Test
	public void testGetPaginationWithCreateParameterFalseValued() {
		setUp();
		
		assertEquals(pagination, context.getPagination(getClass(), false));
		assertNull(context.getPagination(Object.class, false));
	}
}
