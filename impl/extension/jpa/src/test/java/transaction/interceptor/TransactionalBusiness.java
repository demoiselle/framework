package transaction.interceptor;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.transaction.TransactionContext;
import br.gov.frameworkdemoiselle.transaction.Transactional;

public class TransactionalBusiness {

	@Inject
	@Name("pu1")
	private EntityManager em1;

	@Inject
	@Name("pu2")
	private EntityManager em2;
	
	@Inject
	private TransactionContext transactionContext;
	
	@Transactional
	public boolean isTransactionActiveWithInterceptor(){
		return transactionContext.getCurrentTransaction().isActive();
	}
	
	public boolean isTransactionActiveWithoutInterceptor(){
		return transactionContext.getCurrentTransaction().isActive();
	}

	@Transactional
	public void commitWithSuccess() {
		MyEntity1 entity1 = new MyEntity1();
		entity1.setId(createId("id-1"));
		entity1.setDescription("desc-1");

		MyEntity2 entity2 = new MyEntity2();
		entity2.setId(createId("id-2"));
		entity2.setDescription("desc-2");

		em1.persist(entity1);
		em2.persist(entity2);
	}

	@Transactional
	public void rollbackWithSuccess() throws Exception {
		MyEntity1 entity1 = new MyEntity1();
		entity1.setId(createId("id-3"));

		MyEntity2 entity2 = new MyEntity2();
		entity2.setId(createId("id-4"));

		em1.persist(entity1);
		em2.persist(entity2);
		
		throw new Exception("Exceção criada para marcar transação para rollback");
	}
	
	String createId(String id) {
		return this.getClass().getName() + "_" + id;
	}
	
}
