package jtatransaction.interceptor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;

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
import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.transaction.JTATransaction;
import br.gov.frameworkdemoiselle.transaction.TransactionContext;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(Arquillian.class)
public class InterceptorJTATransactionTest {
	
	private static final String PATH = "src/test/resources/interceptor";

	private TransactionContext transactionContext;
	
	@PersistenceContext(unitName="pu1")
	private EntityManager em1;
	
	@PersistenceContext(unitName="pu2")
	private EntityManager em2;
	
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive deployment = Tests.createDeployment(InterceptorJTATransactionTest.class);
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
		
		TransactionalBusiness business = Beans.getReference(TransactionalBusiness.class);
		
		business.commitWithSuccess();

		MyEntity1 persisted1 = em1.find(MyEntity1.class, TransactionalBusiness.createId("id-1"));
		MyEntity2 persisted2 = em2.find(MyEntity2.class, TransactionalBusiness.createId("id-2"));

		assertEquals("desc-1", persisted1.getDescription());
		assertEquals("desc-2", persisted2.getDescription());
		
		assertFalse(transactionContext.getCurrentTransaction().isActive());
		
	}

	@Test(expected = TransactionRequiredException.class)
	public void checkNoTransactionAutomaticallyLoaded() {
		TransactionalBusiness business = Beans.getReference(TransactionalBusiness.class);
		business.checkNoTransactionAutomaticallyLoaded();
	}

	@Test
	public void rollbackWithSuccess() {
		TransactionalBusiness business = Beans.getReference(TransactionalBusiness.class); 
		try{
			business.rollbackWithSuccess();
		}
		catch(DemoiselleException de){
			//Exceção esperada
		}

		MyEntity1 persisted1 = em1.find(MyEntity1.class, TransactionalBusiness.createId("id-3"));
		MyEntity2 persisted2 = em2.find(MyEntity2.class, TransactionalBusiness.createId("id-4"));
		assertNull(persisted1);
		assertNull(persisted2);
	}
	
}
