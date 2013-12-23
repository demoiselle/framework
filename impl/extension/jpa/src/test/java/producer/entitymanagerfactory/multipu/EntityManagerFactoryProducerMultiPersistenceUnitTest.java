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
package producer.entitymanagerfactory.multipu;

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

import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;
import test.Tests;

@RunWith(Arquillian.class)
public class EntityManagerFactoryProducerMultiPersistenceUnitTest {

	private static final String PATH = "src/test/resources/producer/entitymanagerfactory/multi-pu";

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
		WebArchive deployment = Tests.createDeployment(EntityManagerFactoryProducerMultiPersistenceUnitTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/persistence.xml"), "META-INF/persistence.xml");
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"), "demoiselle.properties");

		return deployment;
	}

	@Test
	public void produceDefaultEntityManagerFactory() {
		EntityManagerFactory emf = Beans.getReference(EntityManagerFactory.class);
		assertNotNull(emf);
		assertTrue(emf.getMetamodel().getEntities().toString().contains(DummyEntityDefault.class.getSimpleName()));

		EntityManager em = emf.createEntityManager();

		DummyEntityDefault entityOne = new DummyEntityDefault(descriptionOne, 1L);
		DummyEntityDefault entityTwo = new DummyEntityDefault(descriptionTwo, 2L);

		String jpqlOne = "select ded from DummyEntityDefault ded where ded.description like :descriptionOne";
		String jpqlTwo = "select ded from DummyEntityDefault ded where ded.description like :descriptionTwo";
		String jpqlAll = "select ded from DummyEntityDefault as ded";

		loadListBegin(em, entityOne, entityTwo, jpqlOne, jpqlTwo, jpqlAll);
		assertTrue(listOne.size() == 1);
		assertTrue(((DummyEntityDefault) listOne.get(0)).getId() == 1L);
		assertTrue(((DummyEntityDefault) listOne.get(0)).getDescription().equals(descriptionOne));

		assertTrue(listTwo.size() == 1);
		assertTrue(((DummyEntityDefault) listTwo.get(0)).getId() == 2L);
		assertTrue(((DummyEntityDefault) listTwo.get(0)).getDescription().equals(descriptionTwo));

		assertTrue(listAll.size() == 2);
		
		loadListUpdate(em, entityOne, entityTwo);
		assertTrue(listOne.size() == 0);
		assertTrue(listTwo.size() == 0);
		assertTrue(listAll.size() == 2);

		DummyEntityDefault ded = em.find(DummyEntityDefault.class, 1L);
		assertTrue(ded.getDescription().equals("Entity for test one with description modified."));

		removeAndLoadList(em, entityOne, false);
		assertTrue(listAll.size() == 1);

		removeAndLoadList(em, entityTwo, true);
		assertTrue(listAll.size() == 0);

	}

	@Test
	public void produceNamedOneEntityManagerFactory() {
		EntityManagerFactory emf = Beans.getReference(EntityManagerFactory.class, new NameQualifier("pu"));
		assertNotNull(emf);
		assertTrue(emf.getMetamodel().getEntities().toString().contains(DummyEntityDefault.class.getSimpleName()));
		
		EntityManager em = emf.createEntityManager();

		DummyEntityDefault entityOne = new DummyEntityDefault(descriptionOne, 1L);
		DummyEntityDefault entityTwo = new DummyEntityDefault(descriptionTwo, 2L);

		String jpqlOne = "select ded from DummyEntityDefault ded where ded.description like :descriptionOne";
		String jpqlTwo = "select ded from DummyEntityDefault ded where ded.description like :descriptionTwo";
		String jpqlAll = "select ded from DummyEntityDefault as ded";

		loadListBegin(em, entityOne, entityTwo, jpqlOne, jpqlTwo, jpqlAll);
		assertTrue(listOne.size() == 1);
		assertTrue(((DummyEntityDefault) listOne.get(0)).getId() == 1L);
		assertTrue(((DummyEntityDefault) listOne.get(0)).getDescription().equals(descriptionOne));

		assertTrue(listTwo.size() == 1);
		assertTrue(((DummyEntityDefault) listTwo.get(0)).getId() == 2L);
		assertTrue(((DummyEntityDefault) listTwo.get(0)).getDescription().equals(descriptionTwo));

		assertTrue(listAll.size() == 2);
		
		loadListUpdate(em, entityOne, entityTwo);
		assertTrue(listOne.size() == 0);
		assertTrue(listTwo.size() == 0);
		assertTrue(listAll.size() == 2);

		DummyEntityDefault ded = em.find(DummyEntityDefault.class, 1L);
		assertTrue(ded.getDescription().equals("Entity for test one with description modified."));

		removeAndLoadList(em, entityOne, false);
		assertTrue(listAll.size() == 1);

		removeAndLoadList(em, entityTwo, true);
		assertTrue(listAll.size() == 0);
	}

	@Test
	public void produceNamedTwoEntityManagerFactory() {
		EntityManagerFactory emf = Beans.getReference(EntityManagerFactory.class, new NameQualifier("pu2"));
		assertNotNull(emf);
		assertTrue(emf.getMetamodel().getEntities().toString().contains(DummyEntityNamed.class.getSimpleName()));
		
		EntityManager em = emf.createEntityManager();

		DummyEntityNamed entityOne = new DummyEntityNamed(descriptionOne, 1L);
		DummyEntityNamed entityTwo = new DummyEntityNamed(descriptionTwo, 2L);

		String jpqlOne = "select den from DummyEntityNamed den where den.description like :descriptionOne";
		String jpqlTwo = "select den from DummyEntityNamed den where den.description like :descriptionTwo";
		String jpqlAll = "select den from DummyEntityNamed as den";

		loadListBegin(em, entityOne, entityTwo, jpqlOne, jpqlTwo, jpqlAll);
		assertTrue(listOne.size() == 1);
		assertTrue(((DummyEntityNamed) listOne.get(0)).getId() == 1L);
		assertTrue(((DummyEntityNamed) listOne.get(0)).getDescription().equals(descriptionOne));

		assertTrue(listTwo.size() == 1);
		assertTrue(((DummyEntityNamed) listTwo.get(0)).getId() == 2L);
		assertTrue(((DummyEntityNamed) listTwo.get(0)).getDescription().equals(descriptionTwo));

		assertTrue(listAll.size() == 2);
		
		loadListUpdate(em, entityOne, entityTwo);
		assertTrue(listOne.size() == 0);
		assertTrue(listTwo.size() == 0);
		assertTrue(listAll.size() == 2);

		DummyEntityNamed den = em.find(DummyEntityNamed.class, 1L);
		assertTrue(den.getDescription().equals("Entity for test one with description modified."));

		removeAndLoadList(em, entityOne, false);
		assertTrue(listAll.size() == 1);

		removeAndLoadList(em, entityTwo, true);
		assertTrue(listAll.size() == 0);
	}

	@SuppressWarnings("unused")
	@Test(expected = PersistenceException.class)
	public void produceNamedInexistentEntityManagerFactory() {
		EntityManagerFactory emf = Beans.getReference(EntityManagerFactory.class, new NameQualifier("pu3"));
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
	
	private void loadListUpdate(EntityManager em, DummyEntityDefault entityOne, DummyEntityDefault entityTwo) {
		entityOne.setDescription("Entity for test one with description modified.");
		em.merge(entityOne);
		entityTwo.setDescription("Entity for test two with description modified.");
		em.merge(entityTwo);
		
		listOne = queryOne.getResultList();
		listTwo = queryTwo.getResultList();
		listAll = queryAll.getResultList();		
	}
	
	private void loadListUpdate(EntityManager em, DummyEntityNamed entityOne, DummyEntityNamed entityTwo) {
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

}
