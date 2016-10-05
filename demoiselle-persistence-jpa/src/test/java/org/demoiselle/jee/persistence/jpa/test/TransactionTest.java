/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.jpa.test;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;

import org.demoiselle.jee.persistence.jpa.entity.User;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@RunWith(Arquillian.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TransactionTest {

	public static final String[] USER_NAMES = { "Homer Jay Simpson", "Marjorie Jo-Jo Bouvier Simpson",
			"Bartholomew Jay Jo-Jo Simpson", "Elizabeth Marie Jay Jo-Jo Simpson", "Margaret Simpson" };

	@PersistenceContext
	EntityManager entityManager;

	@Inject
	UserTransaction userTransaction;

	@Deployment
	public static Archive<?> createDeployment() {
		WebArchive war = ShrinkWrap.create(WebArchive.class, "persistence-test.war");
		war.addPackage(User.class.getPackage());
		war.addAsResource("test-persistence.xml", "META-INF/persistence.xml");
		war.addAsWebInfResource("jbossas-ds.xml");
		war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		return war;
	}

	@Before
	public void preparePersistenceTest() throws Exception {

	}

	@After
	public void commitTransaction() throws Exception {

	}

	@Test
	public void A_noTransaction() throws Exception {
		try {
			User user = new User(USER_NAMES[0]);
			entityManager.persist(user);

			// Se chegar AQUI tem que FALHAR
			Assert.fail();
		} catch (TransactionRequiredException e) {
			Assert.assertTrue(true);
		} catch (Exception e) {
			// Se chegar AQUI tem que FALHAR
			Assert.fail();
		}
	}

	@Test
	public void B_transaction_PersistCommit() throws Exception {
		userTransaction.begin();
		User user = new User(USER_NAMES[0]);
		entityManager.persist(user);
		userTransaction.commit();
		Assert.assertTrue(true);
	}

	@Test
	public void C_transaction_PersistCommit_Verify() throws Exception {
		TypedQuery<User> q = entityManager.createQuery("SELECT user FROM User user WHERE name = :name", User.class);
		q.setParameter("name", USER_NAMES[0]);
		User user = q.getSingleResult();
		Assert.assertEquals(USER_NAMES[0], user.getName());
	}

	@Test
	public void D_transaction_Persist() throws Exception {
		userTransaction.begin();
		User user = new User(USER_NAMES[1]);
		entityManager.persist(user);
		Assert.assertTrue(true);
	}

	@Test
	public void E_transaction_Persist_Verify() throws Exception {
		TypedQuery<User> q = entityManager.createQuery("SELECT user FROM User user WHERE name = :name", User.class);
		q.setParameter("name", USER_NAMES[1]);
		List<User> list = q.getResultList();
		Assert.assertEquals(0, list.size());
	}

}
