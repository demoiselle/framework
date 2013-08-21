package transaction.manual;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TransactionRequiredException;

import junit.framework.Assert;

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
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;

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

	@Deployment
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

		MyEntity1 entity1 = new MyEntity1();
		entity1.setId(createId("id-1"));
		entity1.setDescription("desc-1");

		MyEntity2 entity2 = new MyEntity2();
		entity2.setId(createId("id-2"));
		entity2.setDescription("desc-2");

		assertFalse(transaction.isActive());
		transaction.begin();
		assertTrue(transaction.isActive());

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
	
	@Test
	public void checkEntityManagerCreatedAfterTransaction(){
		Transaction transaction = transactionContext.getCurrentTransaction();

		String id = createId("id-5");
		MyEntity1 entity1 = new MyEntity1();
		entity1.setId(id);
		entity1.setDescription("Test description");
		
		Assert.assertFalse(transaction.isActive());
		transaction.begin();
		Assert.assertTrue(transaction.isActive());
		
		EntityManager em1 = Beans.getReference(EntityManager.class, new NameQualifier("pu3"));
		
		try{
			em1.persist(entity1);
			transaction.commit();
		}
		catch(TransactionRequiredException te){
			Assert.fail("Entity Manager não ingressou em transação já em curso: "+te.getMessage());
		}
		
		entity1 = em1.find(MyEntity1.class, id);
		Assert.assertEquals("Test description", entity1.getDescription());
	}

	private String createId(String id) {
		return this.getClass().getName() + "_" + id;
	}
}
