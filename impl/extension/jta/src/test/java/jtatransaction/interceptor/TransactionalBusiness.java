package jtatransaction.interceptor;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.stereotype.BusinessController;
import br.gov.frameworkdemoiselle.transaction.Transactional;

@BusinessController
public class TransactionalBusiness {
	
	@PersistenceContext(unitName="pu1")
	private EntityManager em1;
	
	@PersistenceContext(unitName="pu2")
	private EntityManager em2;
	
	@Transactional
	public void commitWithSuccess() {
		MyEntity1 entity1 = new MyEntity1();
		entity1.setId(createId("id-1"));
		entity1.setDescription("desc-1");

		MyEntity2 entity2 = new MyEntity2();
		entity2.setId(createId("id-2"));
		entity2.setDescription("desc-2");

		em1.joinTransaction();
		em2.joinTransaction();

		em1.persist(entity1);
		em2.persist(entity2);
		
		em1.flush();
		em2.flush();
	}

	public void checkNoTransactionAutomaticallyLoaded() {
		MyEntity1 entity = new MyEntity1();
		entity.setId(createId("id-2"));

		em1.persist(entity);
		em1.flush();
	}

	@Transactional
	public void rollbackWithSuccess() {
		MyEntity1 entity1 = new MyEntity1();
		entity1.setId(createId("id-3"));

		MyEntity2 entity2 = new MyEntity2();
		entity2.setId(createId("id-4"));

		em1.joinTransaction();
		em2.joinTransaction();

		em1.persist(entity1);
		em2.persist(entity2);
		em1.flush();
		em2.flush();
		
		em1.clear();
		em2.clear();

		throw new DemoiselleException("Forçando rollback");
	}
	
	public static String createId(String id) {
		return TransactionalBusiness.class.getName() + "_" + id;
	}

}
