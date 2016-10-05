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
public class CrudOperationsTest {

	public static final String[] USER_NAMES = { "Homer Jay Simpson", "Marjorie Jo-Jo Bouvier Simpson",
			"Bartholomew Jay Jo-Jo Simpson", "Elizabeth Marie Jay Jo-Jo Simpson", "Margaret Simpson" };

	public static final String[] NEW_USER_NAMES = { "Homer Simpson", "Marge Simpson", "Bart Simpson", "Lisa Simpson",
			"Maggie Simpson" };

	@PersistenceContext
	EntityManager entityManager;

	@Inject
	UserTransaction userTransaction;

	@Deployment
	public static Archive<?> createDeployment() {

		// Para rodar JUnit no Eclipse precisa add
		// -Djava.util.logging.manager=org.jboss.logmanager.LogManager
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
	public void finishPersistenceTest() throws Exception {

	}

	private int count() {
		String allUsers = "select user from User user order by user.id";
		List<User> users = entityManager.createQuery(allUsers, User.class).getResultList();

		// System.out.println("users.size(): " + users.size());

		return users.size();
	}


	@Test
	public void A_insert() throws Exception {
		
		// System.out.println("======= INSERT =======");

		userTransaction.begin();

		Assert.assertEquals(0, count());

		// Insere os dados no banco
		for (String name : USER_NAMES) {
			User user = new User(name);
			entityManager.persist(user);
		}

		// Salva as alterações no banco
		userTransaction.commit();

		// Verifica se eles existem
		Assert.assertEquals(USER_NAMES.length, count());
	}

	@Test
	public void B_update() throws Exception {
		
		// System.out.println("======= UPDATE =======");

		userTransaction.begin();

		Assert.assertEquals(USER_NAMES.length, count());

		// Altera os dados no banco
		for (int i = 0; i < USER_NAMES.length; i++) {
			TypedQuery<User> q = entityManager.createQuery("SELECT user FROM User user WHERE user.name = :name",
					User.class);
			q.setParameter("name", USER_NAMES[i]);

			User user = q.getSingleResult();
			user.setName(NEW_USER_NAMES[i]);

			entityManager.persist(user);
		}

		userTransaction.commit();

	}

	@Test
	public void C_delete() throws Exception {
		
		// System.out.println("======= DELETE =======");

		Assert.assertEquals(USER_NAMES.length, count());

		userTransaction.begin();

		// Verifica se os nomes foram alterados
		for (int i = 0; i < USER_NAMES.length; i++) {
			TypedQuery<User> q = entityManager.createQuery("SELECT user FROM User user WHERE user.name = :name",
					User.class);
			q.setParameter("name", NEW_USER_NAMES[i]);
			User user = q.getSingleResult();

			Assert.assertEquals(NEW_USER_NAMES[i], user.getName());
		}

		TypedQuery<User> q = entityManager.createQuery("SELECT user FROM User user WHERE user.name = :name",
				User.class);
		q.setParameter("name", NEW_USER_NAMES[2]);

		User user = q.getSingleResult();

		entityManager.remove(user);

		userTransaction.commit();

		Assert.assertEquals((NEW_USER_NAMES.length - 1), count());

	}

	@Test
	public void D_finish() throws Exception {
		// System.out.println("======= FINISH =======");
		
		Assert.assertEquals((NEW_USER_NAMES.length - 1), count());
	}

}
