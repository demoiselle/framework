package jtatransaction.manual;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TransactionRequiredException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.transaction.JTATransaction;
import br.gov.frameworkdemoiselle.transaction.Transaction;
import br.gov.frameworkdemoiselle.transaction.TransactionContext;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(Arquillian.class)
public class ManualJTATransactionTest {
	
	private static final String PATH = "src/test/resources/manual";

	private TransactionContext transactionContext;
	
	@PersistenceContext(unitName="pu1")
	private EntityManager em1;
	
	@PersistenceContext(unitName="pu2")
	private EntityManager em2;
	
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(ManualJTATransactionTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/persistence.xml"), "META-INF/persistence.xml");
		
		return deployment;
	}
	
	@Before
	public void clearDatabase(){
		transactionContext = Beans.getReference(TransactionContext.class);
		
		transactionContext.getCurrentTransaction().begin();
		em1.joinTransaction();
		em2.joinTransaction();
		em1.createQuery("DELETE FROM MyEntity1").executeUpdate();
		em2.createQuery("DELETE FROM MyEntity2").executeUpdate();
		em1.flush();
		em2.flush();
		em1.clear();
		em2.clear();
		transactionContext.getCurrentTransaction().commit();
	}

	@Test
	public void checkJTATransactionType() {
		assertEquals(JTATransaction.class, transactionContext.getCurrentTransaction().getClass());
	}

	@Test
	public void commitWithSuccess() {
		Transaction transaction = transactionContext.getCurrentTransaction();

		MyEntity1 entity1 = new MyEntity1();
		entity1.setId(createId("id-1"));
		entity1.setDescription("desc-1");

		MyEntity2 entity2 = new MyEntity2();
		entity2.setId(createId("id-2"));
		entity2.setDescription("desc-2");

		assertFalse(transaction.isActive());
		
		transaction.begin();
		assertTrue(transaction.isActive());
		
		em1.joinTransaction();
		em2.joinTransaction();

		em1.persist(entity1);
		em2.persist(entity2);
		
		transaction.commit();
		em1.clear();
		em2.clear();

		MyEntity1 persisted1 = em1.find(MyEntity1.class, createId("id-1"));
		MyEntity2 persisted2 = em2.find(MyEntity2.class, createId("id-2"));

		assertEquals("desc-1", persisted1.getDescription());
		assertEquals("desc-2", persisted2.getDescription());
	}

	@Test(expected = TransactionRequiredException.class)
	public void checkNoTransactionAutomaticallyLoaded() {
		MyEntity1 entity = new MyEntity1();
		entity.setId(createId("id-2"));

		em1.persist(entity);
		em1.flush();
	}

	@Test
	public void rollbackWithSuccess() {
		Transaction transaction = transactionContext.getCurrentTransaction();

		MyEntity1 entity1 = new MyEntity1();
		entity1.setId(createId("id-3"));

		MyEntity2 entity2 = new MyEntity2();
		entity2.setId(createId("id-4"));

		assertFalse(transaction.isMarkedRollback());
		transaction.begin();
		assertTrue(transaction.isActive());
		
		em1.joinTransaction();
		em2.joinTransaction();

		em1.persist(entity1);
		em2.persist(entity2);
		em1.flush();
		em2.flush();
		transaction.setRollbackOnly();

		if (transaction.isMarkedRollback()) {
			transaction.rollback();
		}

		em1.clear();
		em2.clear();

		MyEntity1 persisted1 = em1.find(MyEntity1.class, createId("id-3"));
		MyEntity2 persisted2 = em2.find(MyEntity2.class, createId("id-4"));
		assertNull(persisted1);
		assertNull(persisted2);
	}
	
	private String createId(String id) {
		return this.getClass().getName() + "_" + id;
	}
}
