/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.MultivaluedMap;

import org.demoiselle.jee.core.api.crud.Crud;
import org.demoiselle.jee.crud.exception.DemoisellePersistenceCrudException;
import org.demoiselle.jee.crud.pagination.DemoisellePaginationConfig;
import org.demoiselle.jee.crud.pagination.ResultSet;

//TODO revisar
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public abstract class AbstractDAO<T, I> implements Crud<T, I> {

	/*
	 * @Inject private DemoiselleCrudConfig config;
	 */
	@Inject
	private DemoisellePaginationConfig paginationConfig;

	// private static final Logger logger =
	// Logger.getLogger(AbstractDAO.class.getName());

	@Inject
	private ResultSet resultSet;

	private final Class<T> entityClass;

	protected abstract EntityManager getEntityManager();

	@SuppressWarnings("unchecked")
	public AbstractDAO() {
		this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	public T persist(T entity) {
		try {
			getEntityManager().persist(entity);
			return entity;
		} catch (Exception e) {
			// TODO: Severe? Pode cair aqui somente por ter violação de Unique
			// logger.severe(e.getMessage());
			throw new DemoisellePersistenceCrudException("Não foi possível salvar", e);
		}
	}

	public T merge(T entity) {
		try {
			getEntityManager().merge(entity);
			return entity;
		} catch (Exception e) {
			// TODO: Severe? Pode cair aqui somente por ter violação de Unique
			// logger.severe(e.getMessage());
			throw new DemoisellePersistenceCrudException("Não foi possível salvar", e);
		}
	}

	public void remove(I id) {
		try {
			getEntityManager().remove(getEntityManager().find(entityClass, id));
		} catch (Exception e) {
			// logger.severe(e.getMessage());
			throw new DemoisellePersistenceCrudException("Não foi possível excluir", e);
		}

	}

	public T find(I id) {
		try {
			return getEntityManager().find(entityClass, id);
		} catch (Exception e) {
			// logger.severe(e.getMessage());
			throw new DemoisellePersistenceCrudException("Não foi possível consultar", e);
		}

	}

	@Override
	public ResultSet find() {

		try {

			Integer firstResult = resultSet.getOffset();
			Integer maxResults = getMaxResult();

			Long count = count();

			if (firstResult < count) {
				CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
				CriteriaQuery<T> q = cb.createQuery(entityClass);
				q.from(entityClass);

				TypedQuery<T> query = getEntityManager().createQuery(q);
				query.setFirstResult(firstResult);
				query.setMaxResults(maxResults);

				resultSet.setContent(query.getResultList());
			}

			resultSet.setEntityClass(entityClass);
			resultSet.setCount(count);

			return resultSet;

		} catch (Exception e) {
			// logger.severe(e.getMessage());
			throw new DemoisellePersistenceCrudException("Não foi possível consultar", e);
		}
	}

	private Integer getMaxResult() {
		if (this.resultSet.getLimit().equals(0) && this.resultSet.getOffset().equals(0)) {
			return this.paginationConfig.getDefaultPagination();
		}

		return (this.resultSet.getLimit() - this.resultSet.getOffset()) + 1;
	}

	public Long count() {
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Long> countCriteria = criteriaBuilder.createQuery(Long.class);
		Root<?> entityRoot = countCriteria.from(entityClass);
		countCriteria.select(criteriaBuilder.count(entityRoot));
		return getEntityManager().createQuery(countCriteria).getSingleResult();
	}

	protected Predicate[] extractPredicates(MultivaluedMap<String, String> queryParameters,
			CriteriaBuilder criteriaBuilder, Root<T> root) {
		return new Predicate[] {};
	}
}
