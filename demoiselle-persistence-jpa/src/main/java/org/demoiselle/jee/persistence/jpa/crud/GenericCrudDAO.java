/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.jpa.crud;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public abstract class GenericCrudDAO<T> {

	private Class<T> entityClass;

	public GenericCrudDAO(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	protected abstract EntityManager getEntityManager();

	public void create(T entity) {
		getEntityManager().persist(entity);
	}

	public void edit(T entity) {
		getEntityManager().merge(entity);
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

	@SuppressWarnings("rawtypes")
	public GenericDataPage pageResult(String sort, String order, Integer from, Integer size, String search,
			String fields, HashMap<String, String> filter) {

		// TODO: Rever esta validação
		// if (GPUtils.isEmpty(sort, order) || !((order.equalsIgnoreCase("asc")
		// || order.equalsIgnoreCase("desc"))
		// && (GPUtils.fieldInClass(sort, this.entityClass)))) {
		// throw new GPException(GPMessage.LIST_PARAM_ERROR);
		// }

		if (from == null) {
			from = 0;
		}
		if (size == null) {
			size = Integer.MAX_VALUE;
		}
		boolean hasSearch = search != null && !search.isEmpty();

		String query = new String("select u from " + this.entityClass.getSimpleName() + " u ");
		if (hasSearch) {
			query += " where lower(concat(";
			String[] f = fields.split(",");
			for (int i = 0; i < f.length; i++) {
				query += "u." + f[i];
				if (i < f.length - 1) {
					query += ", ' ',";
				}
			}
			query += ")) like concat('%', :part, '%')";
		}

		if (filter != null && !filter.isEmpty()) {
			Iterator<String> keys = filter.keySet().iterator();
			if (hasSearch) {
				while (keys.hasNext()) {
					String key = keys.next();
					query += " AND u." + key + "=" + filter.get(key);
				}
			} else {
				query += " where ";
				while (keys.hasNext()) {
					String key = keys.next();
					query += " u." + key + "=" + filter.get(key);
					if (keys.hasNext()) {
						query += " and ";
					}
				}
			}
		}
		// Total de Registros
		String query_total = query.replaceFirst("select u", "select COUNT(u)");
		Query qr = getEntityManager().createQuery(query_total);
		if (hasSearch) {
			qr.setParameter("part", search.toLowerCase());
		}
		Long total = (Long) qr.getSingleResult();

		// Conteudo
		qr = getEntityManager().createQuery(query.toString() + " ORDER BY " + sort + " " + order);
		List content = null;
		if (hasSearch) {
			qr.setParameter("part", search.toLowerCase());
		}
		content = qr.setFirstResult(from).setMaxResults(size).getResultList();
		return new GenericDataPage(content, from, size, total, fields, search);
	}

	public GenericDataPage list(String field, String order) {
		List<T> list = getEntityManager()
				.createQuery("select u from " + this.entityClass.getSimpleName() + " u ORDER BY " + field + " " + order,
						this.entityClass)
				.getResultList();
		return new GenericDataPage(list, 0, list.size(), list.size());
	}

	public GenericDataPage list() {
		List<T> list = getEntityManager()
				.createQuery("select u from " + this.entityClass.getSimpleName() + " u ", this.entityClass)
				.getResultList();
		return new GenericDataPage(list, 0, list.size(), list.size());
	}

	public List<T> list(String field, String order, int init, int qtde) {
		return getEntityManager()
				.createQuery("select u from " + this.entityClass.getSimpleName() + " u ORDER BY " + field + " " + order,
						this.entityClass)
				.setFirstResult(init).setMaxResults(qtde).getResultList();
	}

	public List<T> find(String whereField, String whereValue, String fieldOrder, String order, int init, int qtde) {
		return getEntityManager()
				.createQuery("select u from " + this.entityClass.getSimpleName() + " u where u." + whereField + " = "
						+ whereValue + " ORDER BY " + fieldOrder + " " + order, this.entityClass)
				.setFirstResult(init).setMaxResults(qtde).getResultList();
	}

	public Long count() {
		return (Long) getEntityManager().createQuery("select COUNT(u) from " + this.entityClass.getSimpleName() + " u")
				.getSingleResult();
	}

	public Long count(String whereField, String whereValue) {
		return (Long) getEntityManager().createQuery("select COUNT(u) from " + this.entityClass.getSimpleName()
				+ " u where u." + whereField + " = " + whereValue).getSingleResult();
	}

}
