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
package br.gov.frameworkdemoiselle.internal.context;
import org.junit.Ignore;
import java.util.Map;
import java.util.TreeMap;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;
import org.powermock.reflect.Whitebox;
@Ignore
public class ContextStoreTest {

	private ContextStore store;

	private Map<String, Object> map;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		store = new ContextStore();
		map = PowerMock.createMock(Map.class);
		Whitebox.setInternalState(store, "map", map);
	}

	@Test
	public void testContains() {
		EasyMock.expect(map.containsKey(EasyMock.anyObject(String.class))).andReturn(true);
		EasyMock.replay(map);

		Assert.assertTrue(store.contains(""));
		EasyMock.verify(map);
	}

	@Test
	public void testGet() {
		EasyMock.expect(map.get(EasyMock.anyObject(String.class))).andReturn("testing");
		EasyMock.replay(map);

		Assert.assertEquals("testing", store.get(""));
		EasyMock.verify(map);
	}

	@Test
	public void testPut() {
		Map<String, Object> map = new TreeMap<String, Object>();
		Whitebox.setInternalState(store, "map", map);
		store.put("testing", map);
		Assert.assertTrue(map.containsKey("testing"));
	}

}
