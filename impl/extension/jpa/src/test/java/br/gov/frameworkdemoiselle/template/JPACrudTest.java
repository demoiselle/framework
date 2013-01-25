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
package br.gov.frameworkdemoiselle.template;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.domain.Contact;
import br.gov.frameworkdemoiselle.internal.implementation.PaginationImpl;
import br.gov.frameworkdemoiselle.pagination.Pagination;
import br.gov.frameworkdemoiselle.pagination.PaginationContext;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ResourceBundle.class, Instance.class, Beans.class })
public class JPACrudTest {

	private EntityManager entityManager;

	private Contact contact;

	private ContactDAO contactDAO;

	class ContactDAO extends JPACrud<Contact, Long> {

		private static final long serialVersionUID = 1L;

	}

	@Before
	public void setUp() throws Exception {
		this.entityManager = EasyMock.createMock(EntityManager.class);
		this.contact = new Contact();
		this.contactDAO = new ContactDAO();
		setInternalState(this.contactDAO, EntityManager.class, this.entityManager);
	}

	@Test
	public void testDelete() {
		expect(this.entityManager.getReference(Contact.class, null)).andReturn(null);
		this.entityManager.remove(null);
		replay(this.entityManager);
		this.contactDAO.delete(this.contact.getId());
		verify(this.entityManager);
	}
	
	private TypedQuery<Contact> makeTypedQuery() {
		@SuppressWarnings("unchecked")
		TypedQuery<Contact> typeQuery = EasyMock.createMock(TypedQuery.class);
		expect(typeQuery.setFirstResult(EasyMock.anyInt())).andReturn(null);
		expect(typeQuery.setMaxResults(EasyMock.anyInt())).andReturn(null);
		expect(typeQuery.getResultList()).andReturn(createContacts(1));
		return typeQuery;
	}
	
	@Test
	public void testCountAll() {

		Pagination pagination = new PaginationImpl();
		pagination.setPageSize(10);
		setInternalState(this.contactDAO, "pagination", pagination);
		
		TypedQuery<Contact> typeQuery = makeTypedQuery();		

		Query query = EasyMock.createMock(Query.class);
		expect(query.getSingleResult()).andReturn(10L);
		
		expect(this.entityManager.createQuery("select this from Contact this", Contact.class)).andReturn(typeQuery);
		//expect(this.entityManager.createQuery("SELECT COUNT(THIS) FROM CONTACT THIS")).andReturn(query);
		expect(this.entityManager.createQuery("select COUNT(this) from Contact this")).andReturn(query);

		replayAll(typeQuery, query, this.entityManager);

		List<Contact> find = this.contactDAO.findAll();

		assertEquals(1, find.size());
		assertTrue(find.iterator().next().getId().equals(1L));

		verifyAll();
	}

	@Test
	public void testFailCountAll() {

		Pagination pagination = new PaginationImpl();
		setInternalState(this.contactDAO, "pagination", pagination);

		TypedQuery<Contact> typeQuery = makeTypedQuery();		
		
		Query query = EasyMock.createMock(Query.class);

		expect(query.getSingleResult()).andThrow(new DemoiselleException(""));
		expect(this.entityManager.createQuery("select this from Contact this", Contact.class)).andReturn(typeQuery);
		expect(this.entityManager.createQuery("select COUNT(this) from Contact this")).andReturn(query);	

		replayAll(query, this.entityManager);

		try {
			this.contactDAO.findAll();
			fail();
		} catch (DemoiselleException exception) {
		}

		verifyAll();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHandleException() throws Throwable {
		try {
			TransactionRequiredException throwed = new TransactionRequiredException();
			ResourceBundle bundle = PowerMock.createMock(ResourceBundle.class);
			Instance<ResourceBundle> instance = PowerMock.createMock(Instance.class);
			expect(
					bundle.getString("no-transaction-active", "frameworkdemoiselle.transaction.class",
							Configuration.DEFAULT_RESOURCE)).andReturn("message");
			expect(instance.get()).andReturn(bundle);
			setInternalState(contactDAO, "bundle", instance);
			replayAll();
			contactDAO.handleException(throwed);
			fail();
		} catch (DemoiselleException exception) {
			assertEquals(exception.getMessage(), "message");
		}
		verifyAll();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFailFindByExample() {

		CriteriaBuilder criteriaBuilder = PowerMock.createMock(CriteriaBuilder.class);
		CriteriaQuery<Contact> criteriaQuery = PowerMock.createMock(CriteriaQuery.class);
		Root<Contact> root = PowerMock.createMock(Root.class);

		expect(this.entityManager.getCriteriaBuilder()).andReturn(criteriaBuilder);
		expect(criteriaBuilder.createQuery(Contact.class)).andReturn(criteriaQuery);
		expect(criteriaQuery.from(Contact.class)).andReturn(root);

		expect(this.entityManager.createQuery(criteriaQuery)).andThrow(new DemoiselleException(""));

		Predicate predicate = PowerMock.createMock(Predicate.class);

		expect(root.get("id")).andReturn(null);
		expect(criteriaBuilder.equal(EasyMock.anyObject(Predicate.class), EasyMock.anyObject())).andReturn(predicate);
		expect(criteriaQuery.where(new Predicate[] { EasyMock.anyObject(Predicate.class) })).andReturn(criteriaQuery);
		expect(criteriaQuery.select(EasyMock.anyObject(Root.class))).andReturn(criteriaQuery);

		replayAll(criteriaBuilder, criteriaQuery, root, this.entityManager);

		Contact example = new Contact();
		example.setId(1L);
		try {
			this.contactDAO.findByExample(example);
			fail();
		} catch (DemoiselleException ce) {
		}

		verifyAll();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindByExample() {

		TypedQuery<Contact> typedQuery = EasyMock.createMock(TypedQuery.class);
		CriteriaBuilder criteriaBuilder = PowerMock.createMock(CriteriaBuilder.class);
		CriteriaQuery<Contact> criteriaQuery = PowerMock.createMock(CriteriaQuery.class);
		Root<Contact> root = PowerMock.createMock(Root.class);

		expect(this.entityManager.getCriteriaBuilder()).andReturn(criteriaBuilder);
		expect(criteriaBuilder.createQuery(Contact.class)).andReturn(criteriaQuery);
		expect(criteriaQuery.from(Contact.class)).andReturn(root);

		expect(this.entityManager.createQuery(criteriaQuery)).andReturn(typedQuery);
		expect(typedQuery.getResultList()).andReturn(createContacts(1));

		Predicate predicate = PowerMock.createMock(Predicate.class);

		expect(root.get("id")).andReturn(null);
		expect(criteriaBuilder.equal(EasyMock.anyObject(Predicate.class), EasyMock.anyObject())).andReturn(predicate);
		expect(criteriaQuery.where(new Predicate[] { EasyMock.anyObject(Predicate.class) })).andReturn(criteriaQuery);
		expect(criteriaQuery.select(EasyMock.anyObject(Root.class))).andReturn(criteriaQuery);

		replayAll(typedQuery, criteriaBuilder, criteriaQuery, root, this.entityManager);

		Contact example = new Contact();
		example.setId(1L);
		List<Contact> find = this.contactDAO.findByExample(example);

		assertEquals(1, find.size());
		assertTrue(find.iterator().next().getId().equals(1L));

		verifyAll();
	}

	@Test
	public void testFindAll() {
		
		Pagination pagination = new PaginationImpl();
		pagination.setPageSize(10);
		setInternalState(this.contactDAO, "pagination", pagination);
		
		TypedQuery<Contact> typeQuery = makeTypedQuery();		

		Query query = EasyMock.createMock(Query.class);
		expect(query.getSingleResult()).andReturn(10L);
		
		expect(this.entityManager.createQuery("select this from Contact this", Contact.class)).andReturn(typeQuery);
		expect(this.entityManager.createQuery("select COUNT(this) from Contact this")).andReturn(query);		

		replayAll(typeQuery, query, this.entityManager);

		List<Contact> find = this.contactDAO.findAll();

		assertEquals(1, find.size());
		assertTrue(find.iterator().next().getId().equals(1L));
		
		verifyAll();
	}

	@Test
	public void testFailFindAll() {

		PaginationContext actualContext = PowerMock.createMock(PaginationContext.class);
		expect(actualContext.getPagination(Contact.class)).andReturn(null);
		/*@SuppressWarnings("unchecked")
		Instance<PaginationContext> paginationContext = PowerMock.createMock(Instance.class);
		expect(paginationContext.get()).andReturn(actualContext);*/
		
		PowerMock.mockStatic(Beans.class);
		expect(Beans.getReference(PaginationContext.class)).andReturn(actualContext);
		
		@SuppressWarnings("unchecked")
		TypedQuery<Contact> typeQuery = EasyMock.createMock(TypedQuery.class);
		expect(typeQuery.getResultList()).andThrow(new DemoiselleException(""));

		Query query = EasyMock.createMock(Query.class);
		expect(this.entityManager.createQuery("select this from Contact this", Contact.class)).andReturn(typeQuery);		

		replayAll(typeQuery, query, this.entityManager);

		try {
			this.contactDAO.findAll();
			fail();
		} catch (DemoiselleException exception) {
		}

		verifyAll();
	}

	private List<Contact> createContacts(int quantity) {
		List<Contact> contacts = new ArrayList<Contact>();
		for (int i = 0; i < quantity; i++) {
			Contact contact = new Contact();
			contact.setId((long) quantity);
			contacts.add(contact);
		}
		return contacts;
	}

	@Test
	public void testFailInsert() {
		this.entityManager.persist(null);
		EasyMock.expectLastCall().andThrow(new DemoiselleException(""));
		replay(this.entityManager);
		try {
			this.contactDAO.insert(null);
			fail();
		} catch (DemoiselleException exc) {
		}
		verify(this.entityManager);
	}

	@Test
	public void testInsert() {
		this.entityManager.persist(this.contact);
		replay(this.entityManager);
		setInternalState(this.contactDAO, EntityManager.class, this.entityManager);
		this.contactDAO.insert(this.contact);
		verify(this.entityManager);
	}

	@Test
	public void testLoad() {
		Contact contact = new Contact();
		expect(this.entityManager.find(Contact.class, 1L)).andReturn(contact);
		replay(this.entityManager);
		Contact returnedContact = this.contactDAO.load(1L);
		assertEquals(contact.hashCode(), returnedContact.hashCode());
		verify(this.entityManager);
	}

	@Test
	public void testFailLoad() {
		expect(this.entityManager.find(Contact.class, 1L)).andThrow(new DemoiselleException(""));
		replay(this.entityManager);
		try {
			this.contactDAO.load(1L);
			fail();
		} catch (DemoiselleException exc) {
		}
		verify(this.entityManager);
	}

	@Test
	public void testUpdate() {
		expect(this.entityManager.merge(this.contact)).andReturn(null);
		replay(this.entityManager);
		setInternalState(this.contactDAO, EntityManager.class, this.entityManager);
		this.contactDAO.update(this.contact);
		verify(this.entityManager);
	}

	@Test
	public void testFailUpdate() {
		this.entityManager.merge(null);
		EasyMock.expectLastCall().andThrow(new DemoiselleException(""));
		replay(this.entityManager);
		try {
			this.contactDAO.update(null);
			fail();
		} catch (DemoiselleException exc) {
		}
		verify(this.entityManager);
	}
	
	/**
	 * Test if the JPACrud will correctly obtain a new entity manager the first
	 * time it is called and the entity manager is still null.
	 */
	@Test
	public void testCreateEntityManagerIfNotExist(){
		PowerMock.mockStatic(Beans.class);
		expect(Beans.getReference(EntityManager.class)).andReturn(entityManager);
		PowerMock.replay(Beans.class);
		
		setInternalState(this.contactDAO, EntityManager.class, (EntityManager)null);
		Assert.assertNotNull(this.contactDAO.getEntityManager());
	}
	
	@Test
	public void testCriteriaQuery(){
		Map<String, Object> configOverrides = new HashMap<String, Object>();
		configOverrides.put("javax.persistence.provider", "org.hibernate.ejb.HibernatePersistence");
		configOverrides.put("javax.persistence.jdbc.url", "jdbc:hsqldb:hsql:.");
		configOverrides.put("hibernate.show_sql", "true");
		configOverrides.put("hibernate.hbm2ddl.auto", "create-drop");
		
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("pu1", configOverrides);
		this.entityManager = factory.createEntityManager();
		Whitebox.setInternalState(this.contactDAO, EntityManager.class, (EntityManager)null);
		
		PowerMock.mockStatic(Beans.class);
		expect(Beans.getReference(EntityManager.class)).andReturn(entityManager);
		PowerMock.replay(Beans.class);
		
		CriteriaQuery<Contact> query = this.contactDAO.createCriteriaQuery();
		query.select( query.from(Contact.class) );

	}

}
