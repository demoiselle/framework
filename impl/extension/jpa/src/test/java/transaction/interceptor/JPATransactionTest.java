package transaction.interceptor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.transaction.TransactionContext;

@RunWith(Arquillian.class)
public class JPATransactionTest {

	private static final String PATH = "src/test/resources/transaction/interceptor";

	@Inject
	private TransactionalBusiness tb;

	@Inject
	@Name("pu1")
	private EntityManager em1;

	@Inject
	@Name("pu2")
	private EntityManager em2;

	@Inject
	private TransactionContext transactionContext;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(JPATransactionTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/persistence.xml"), "META-INF/persistence.xml");

		return deployment;
	}

	@Before
	public void eraseDatabases() {
		transactionContext.getCurrentTransaction().begin();
		em1.createQuery("DELETE FROM MyEntity1").executeUpdate();
		em2.createQuery("DELETE FROM MyEntity2").executeUpdate();
		transactionContext.getCurrentTransaction().commit();
	}

	@Test
	public void isTransactionActiveWithInterceptor() {
		assertTrue(tb.isTransactionActiveWithInterceptor());
	}

	@Test
	public void isTransactionActiveWithoutInterceptor() {
		assertFalse(tb.isTransactionActiveWithoutInterceptor());
	}

	@Test
	public void commitWithSuccess() {
		tb.commitWithSuccess();

		MyEntity1 entity1 = em1.find(MyEntity1.class, tb.createId("id-1"));
		MyEntity2 entity2 = em2.find(MyEntity2.class, tb.createId("id-2"));

		assertEquals("desc-1", entity1.getDescription());
		assertEquals("desc-2", entity2.getDescription());
	}

	@Test
	public void rollbackWithSuccess() {
		try {
			tb.rollbackWithSuccess();
		} catch (Exception e) {
			assertEquals("Exceção criada para marcar transação para rollback", e.getMessage());
		} finally {
			MyEntity1 entity1 = em1.find(MyEntity1.class, tb.createId("id-3"));
			MyEntity2 entity2 = em2.find(MyEntity2.class, tb.createId("id-4"));

			assertNull(entity1);
			assertNull(entity2);
		}
	}

}
