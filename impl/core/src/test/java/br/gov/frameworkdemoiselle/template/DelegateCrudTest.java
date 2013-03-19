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
//import static org.powermock.api.easymock.PowerMock.mockStatic;
//import static org.powermock.api.easymock.PowerMock.replayAll;
//import static org.powermock.api.easymock.PowerMock.verifyAll;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.easymock.EasyMock;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.powermock.api.easymock.PowerMock;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;
//
//import br.gov.frameworkdemoiselle.util.Beans;
//import br.gov.frameworkdemoiselle.util.Reflections;
//
//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ Crud.class, Beans.class, Reflections.class })
//public class DelegateCrudTest {
//
//	private DelegateCrud<Contact, Long, Delegated> delegateCrud;
//
//	private Crud<Contact, Long> mockCrud;
//
//	@SuppressWarnings("unchecked")
//	@Before
//	public void before() {
//		delegateCrud = new DelegateCrud<Contact, Long, Delegated>();
//		mockCrud = PowerMock.createMock(Crud.class);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testDelete() {
//		mockStatic(Beans.class);
//		mockStatic(Reflections.class);
//
//		expect(Reflections.getGenericTypeArgument(EasyMock.anyObject(Class.class), EasyMock.anyInt())).andReturn(null);
//		expect(Beans.getReference(EasyMock.anyObject(Class.class))).andReturn(mockCrud).times(2);
//
//		mockCrud.delete(1L);
//		PowerMock.expectLastCall();
//		
//		PowerMock.replay(Reflections.class, Beans.class, mockCrud);
//
//		delegateCrud.delete(1L);
//
//		PowerMock.verify();
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testUpdate() {
//		Whitebox.setInternalState(delegateCrud, "delegate", mockCrud);
//		
//		mockStatic(Beans.class);
//		
//		expect(Beans.getReference(EasyMock.anyObject(Class.class))).andReturn(mockCrud);
//		
//		Contact update = new Contact();
//		mockCrud.update(update);
//		replayAll(Beans.class, mockCrud);
//
//		delegateCrud.update(update);
//
//		verifyAll();
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testInsert() {
//		Whitebox.setInternalState(delegateCrud, "delegate", mockCrud);
//		
//		mockStatic(Beans.class);
//	
//		expect(Beans.getReference(EasyMock.anyObject(Class.class))).andReturn(mockCrud);
//		
//		Contact insert = new Contact();
//		mockCrud.insert(insert);
//		replayAll(mockCrud);
//
//		delegateCrud.insert(insert);
//
//		verifyAll();
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testFindAll() {
//		mockStatic(Beans.class);
//		mockStatic(Reflections.class);
//
//		expect(Reflections.getGenericTypeArgument(EasyMock.anyObject(Class.class), EasyMock.anyInt())).andReturn(null);
//		expect(Beans.getReference(EasyMock.anyObject(Class.class))).andReturn(mockCrud);
//
//		List<Contact> returned = new ArrayList<Contact>();
//		expect(mockCrud.findAll()).andReturn(returned);
//		replayAll(Reflections.class, Beans.class, mockCrud);
//
//		assertEquals(returned, delegateCrud.findAll());
//
//		verifyAll();
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testLoad() {
//		mockStatic(Beans.class);
//
//		expect(Beans.getReference(EasyMock.anyObject(Class.class))).andReturn(mockCrud);
//
//		Contact contact = new Contact();
//		expect(mockCrud.load(1L)).andReturn(contact);
//		replayAll(Beans.class, mockCrud);
//
//		Whitebox.setInternalState(delegateCrud, "delegateClass", delegateCrud.getClass(), delegateCrud.getClass());
//
//		assertEquals(contact, delegateCrud.load(1L));
//		verifyAll();
//	}
//
//	class Contact {
//
//		private Long id;
//
//		public Long getId() {
//			return id;
//		}
//
//		public void setId(Long id) {
//			this.id = id;
//		}
//	}
//
//	@SuppressWarnings("serial")
//	class Delegated implements Crud<Contact, Long> {
//
//		@Override
//		public void delete(Long id) {
//		}
//
//		@Override
//		public List<Contact> findAll() {
//			return null;
//		}
//
//		@Override
//		public void insert(Contact bean) {
//		}
//
//		@Override
//		public Contact load(Long id) {
//			return null;
//		}
//
//		@Override
//		public void update(Contact bean) {
//		}
//
//	}
//
//}
