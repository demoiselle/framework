package transaction.manual;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TransactionRequiredException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.transaction.JPATransaction;
import br.gov.frameworkdemoiselle.transaction.Transaction;
import br.gov.frameworkdemoiselle.transaction.TransactionContext;

@RunWith(Arquillian.class)
public class JPATransactionTest {

	private static final String PATH = "src/test/resources/transaction/manual";

	@Inject
	private TransactionContext transactionContext;

	@Inject
	@Name("pu1")
	private EntityManager em1;

	@Inject
	@Name("pu2")
	private EntityManager em2;

	@Deployment(testable = true)
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(JPATransactionTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/persistence.xml"), "META-INF/persistence.xml");

		return deployment;
	}

	@Test
	public void checkJPATransactionType() {
		assertEquals(JPATransaction.class, transactionContext.getCurrentTransaction().getClass());
	}

	@Test
	public void commitWithSuccess() {
		Transaction transaction = transactionContext.getCurrentTransaction();

		MyEntity entity = new MyEntity();
		entity.setId(createId("id-1"));
		entity.setDescription("desc-1");

		assertFalse(transaction.isActive());
		transaction.begin();
		assertTrue(transaction.isActive());

		em1.persist(entity);
		em2.persist(entity);
		transaction.commit();
		em1.clear();
		em2.clear();

		MyEntity persisted1 = em1.find(MyEntity.class, createId("id-1"));
		MyEntity persisted2 = em2.find(MyEntity.class, createId("id-1"));

		assertEquals("desc-1", persisted1.getDescription());
		assertEquals("desc-1", persisted2.getDescription());
	}

	@Test(expected = TransactionRequiredException.class)
	public void checkNoTransactionAutomaticallyLoaded() {
		MyEntity entity = new MyEntity();
		entity.setId(createId("id-2"));

		em1.persist(entity);
		em1.flush();
	}

	//
	// @Test
	// public void rollbackWithSuccess() {
	// Transaction transaction = transactionContext.getCurrentTransaction();
	//
	// MyEntity entity = new MyEntity();
	// entity.setId(createId("id-3"));
	//
	// assertFalse(transaction.isMarkedRollback());
	// transaction.begin();
	// assertTrue(transaction.isActive());
	//
	// em1.persist(entity);
	// em2.persist(entity);
	// em1.flush();
	// em2.flush();
	// transaction.setRollbackOnly();
	//
	// if (transaction.isMarkedRollback()) {
	// transaction.rollback();
	// }
	//
	// em1.clear();
	// em2.clear();
	//
	// MyEntity persisted1 = em1.find(MyEntity.class, createId("id-3"));
	// MyEntity persisted2 = em2.find(MyEntity.class, createId("id-3"));
	// assertNull(persisted1);
	// assertNull(persisted2);
	// }

	private String createId(String id) {
		return this.getClass().getName() + "_" + id;
	}
}
