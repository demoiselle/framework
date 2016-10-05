/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.jpa.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.demoiselle.jee.core.message.DemoiselleMessage;
import org.demoiselle.jee.persistence.jpa.entity.User;
import org.demoiselle.jee.persistence.jpa.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class PersistenceTest {

	private static final String[] USER_NAMES = { "Super Mario Brothers", "Mario Kart", "F-Zero" };

	@PersistenceContext
	EntityManager entityManager;

	@Inject
	UserTransaction userTransaction;

	@Inject
	DemoiselleMessage messsages;

	@Deployment
	public static Archive<?> createDeployment() {

		final JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "persistence-test.jar");
		testJar.addPackage(DemoiselleMessage.class.getPackage());
		testJar.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

		// Para rodar JUnit no Eclipse precisa add
		// -Djava.util.logging.manager=org.jboss.logmanager.LogManager
		WebArchive war = ShrinkWrap.create(WebArchive.class, "persistence-test.war");
		war.addPackage(User.class.getPackage());
		war.addAsResource("test-persistence.xml", "META-INF/persistence.xml");
		war.addAsWebInfResource("jbossas-ds.xml");
		war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		// Para funcionar o Delta Spike (Messages) é necessário informar todos
		// os arquivos
		war.addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJpaArchive());
		war.addAsLibraries(testJar);

		return war;
	}

	@Before
	public void preparePersistenceTest() throws Exception {

		System.out.println(messsages.engineOn());

		clearData();
		insertData();
		startTransaction();
	}

	private void clearData() throws Exception {
		userTransaction.begin();
		entityManager.joinTransaction();
		System.out.println("Dumping old records...");
		entityManager.createQuery("delete from User").executeUpdate();
		userTransaction.commit();
	}

	private void insertData() throws Exception {
		userTransaction.begin();
		entityManager.joinTransaction();
		System.out.println("Inserting records...");
		for (String title : USER_NAMES) {
			User user = new User(title);
			entityManager.persist(user);
		}
		userTransaction.commit();
		// clear the persistence context (first-level cache)
		entityManager.clear();
	}

	private void startTransaction() throws Exception {
		userTransaction.begin();
		entityManager.joinTransaction();
	}

	@After
	public void commitTransaction() throws Exception {
		userTransaction.commit();
	}

	@Test
	public void shouldFindAllUsersUsingJpqlQuery() throws Exception {
		String fetchingAllUsersInJpql = "select g from User g order by g.id";

		System.out.println("Selecting (using JPQL)...");
		List<User> users = entityManager.createQuery(fetchingAllUsersInJpql, User.class).getResultList();

		System.out.println("Found " + users.size() + " users (using JPQL):");
		assertContainsAllUsers(users);
	}

	private static void assertContainsAllUsers(Collection<User> retrievedUsers) {
		Assert.assertEquals(USER_NAMES.length, retrievedUsers.size());
		final Set<String> retrievedUserTitles = new HashSet<String>();
		for (User user : retrievedUsers) {
			System.out.println("* " + user);
			retrievedUserTitles.add(user.getName());
		}
		Assert.assertTrue(retrievedUserTitles.containsAll(Arrays.asList(USER_NAMES)));
	}

}
