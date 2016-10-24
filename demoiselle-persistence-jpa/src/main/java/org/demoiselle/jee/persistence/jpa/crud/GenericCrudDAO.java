/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.jpa.crud;

import java.util.List;

import javax.persistence.EntityManager;

public abstract class GenericCrudDAO<T> {

	private Class<T> entityClass;

	public GenericCrudDAO(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	protected abstract EntityManager getEntityManager();
	
	public T create(T entity) {
		getEntityManager().persist(entity);
		return entity;
	}

	public T edit(T entity) {
		return getEntityManager().merge(entity);		
	}

	public void remove(T entity) {
		getEntityManager().remove(getEntityManager().merge(entity));
	}

	public T find(Object id) {
		return getEntityManager().find(entityClass, id);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<T> findAll() {
		javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
		cq.select(cq.from(entityClass));
		return getEntityManager().createQuery(cq).getResultList();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<T> findRange(int[] range) {
		javax.persistence.criteria.CriteriaQuery cq = getEntityManager().getCriteriaBuilder().createQuery();
		cq.select(cq.from(entityClass));
		javax.persistence.Query q = getEntityManager().createQuery(cq);
		q.setMaxResults(range[1] - range[0] + 1);
		q.setFirstResult(range[0]);
		return q.getResultList();
	}

	public GenericDataPage list() {
		List<T> list = getEntityManager()
				.createQuery("select u from " + this.entityClass.getSimpleName() + " u ", this.entityClass)
				.getResultList();
		return new GenericDataPage(list, 0, list.size(), list.size());
	}

	public List<T> find(String whereField, String whereValue, String fieldOrder, String order, int init, int qtde) {
		return getEntityManager()
				.createQuery("select u from " + this.entityClass.getSimpleName() + " u where u." + whereField
						+ " = :value ORDER BY " + fieldOrder + " " + order.toUpperCase(), this.entityClass)
				.setParameter("value", whereValue).setFirstResult(init).setMaxResults(qtde).getResultList();
	}

	public Long count() {
		return (Long) getEntityManager().createQuery("select COUNT(u) from " + this.entityClass.getSimpleName() + " u")
				.getSingleResult();
	}

}
