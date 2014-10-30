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
package producer.entitymanagerfactory.uniquepu;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;

@RunWith(Arquillian.class)
public class EntityManagerFactoryProducerUniquePersistenceUnitTest {
	
	private static final String PATH = "src/test/resources/producer/entitymanagerfactory/unique-pu";
	
	private final String descriptionOne = "Entity for Test One.";
	private final String descriptionTwo = "Entity for Test Two.";

	private List<?> listOne;
	private List<?> listTwo;
	private List<?> listAll;
	
	private Query queryOne;
	private Query queryTwo;
	private Query queryAll;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(EntityManagerFactoryProducerUniquePersistenceUnitTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/persistence.xml"), "META-INF/persistence.xml");
		return deployment;
	}

	@Test
	public void produceDefaultEntityManagerFactory() {
		EntityManagerFactory emf = Beans.getReference(EntityManagerFactory.class);
		assertNotNull(emf);
		assertTrue(emf.getMetamodel().getEntities().toString().contains(DummyEntity.class.getSimpleName()));
		
		EntityManager em = emf.createEntityManager();

		DummyEntity entityOne = new DummyEntity(descriptionOne, 1L);
		DummyEntity entityTwo = new DummyEntity(descriptionTwo, 2L);

		String jpqlOne = "select ded from DummyEntity ded where ded.description like :descriptionOne";
		String jpqlTwo = "select ded from DummyEntity ded where ded.description like :descriptionTwo";
		String jpqlAll = "select ded from DummyEntity as ded";

		loadListBegin(em, entityOne, entityTwo, jpqlOne, jpqlTwo, jpqlAll);
		assertTrue(listOne.size() == 1);
		assertTrue(((DummyEntity) listOne.get(0)).getId() == 1L);
		assertTrue(((DummyEntity) listOne.get(0)).getDescription().equals(descriptionOne));

		assertTrue(listTwo.size() == 1);
		assertTrue(((DummyEntity) listTwo.get(0)).getId() == 2L);
		assertTrue(((DummyEntity) listTwo.get(0)).getDescription().equals(descriptionTwo));

		assertTrue(listAll.size() == 2);
		
		loadListUpdate(em, entityOne, entityTwo);
		assertTrue(listOne.size() == 0);
		assertTrue(listTwo.size() == 0);
		assertTrue(listAll.size() == 2);

		DummyEntity ded = em.find(DummyEntity.class, 1L);
		assertTrue(ded.getDescription().equals("Entity for test one with description modified."));

		removeAndLoadList(em, entityOne, false);
		assertTrue(listAll.size() == 1);

		removeAndLoadList(em, entityTwo, true);
		assertTrue(listAll.size() == 0);

	}
	
	private void loadListBegin(EntityManager em, Object entityOne, Object entityTwo,
			String jpqlOne, String jpqlTwo, String jpqlAll) {
		em.getTransaction().begin();
		
		em.persist(entityOne);
		em.persist(entityTwo);
		
		queryOne = em.createQuery(jpqlOne).setParameter("descriptionOne", descriptionOne);
		queryTwo = em.createQuery(jpqlTwo).setParameter("descriptionTwo", descriptionTwo);
		queryAll = em.createQuery(jpqlAll);
		
		listOne = queryOne.getResultList();
		listTwo = queryTwo.getResultList();
		listAll = queryAll.getResultList();
	}
	
	private void loadListUpdate(EntityManager em, DummyEntity entityOne, DummyEntity entityTwo) {
		entityOne.setDescription("Entity for test one with description modified.");
		em.merge(entityOne);
		entityTwo.setDescription("Entity for test two with description modified.");
		em.merge(entityTwo);
		
		listOne = queryOne.getResultList();
		listTwo = queryTwo.getResultList();
		listAll = queryAll.getResultList();		
	}
	
	private void removeAndLoadList(EntityManager em, Object entityOne, boolean commit) {
		em.remove(entityOne);
		listAll = queryAll.getResultList();
		if(commit) {
			em.getTransaction().commit();
		}
	}
	
	@SuppressWarnings("unused")
	@Test(expected=PersistenceException.class)
	public void produceNamedInexistentEntityManagerFactory() {
		EntityManagerFactory emf = Beans.getReference(EntityManagerFactory.class, new NameQualifier("pu2"));
	}
}
