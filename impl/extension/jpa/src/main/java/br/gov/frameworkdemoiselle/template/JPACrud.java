/*
 * Demoiselle Framework
 * Copyright (C) 2010 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.template;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.Enumerated;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.pagination.Pagination;
import br.gov.frameworkdemoiselle.pagination.PaginationContext;
import br.gov.frameworkdemoiselle.transaction.Transactional;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.Reflections;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * JPA specific implementation for Crud interface.
 * 
 * @param <T>
 *            bean object type
 * @param <I>
 *            bean id type
 * @author SERPRO
 * @see Crud
 */
public class JPACrud<T, I> implements Crud<T, I> {

	private static final long serialVersionUID = 1L;

	private EntityManager entityManager;

	@Inject
	private Instance<PaginationContext> paginationContext;

	private Pagination pagination;

	@Inject
	@Name("demoiselle-jpa-bundle")
	private Instance<ResourceBundle> bundle;

	private Class<T> beanClass;

	protected Class<T> getBeanClass() {
		if (this.beanClass == null) {
			this.beanClass = Reflections.getGenericTypeArgument(this.getClass(), 0);
		}

		return this.beanClass;
	}

	protected CriteriaBuilder getCriteriaBuilder() {
		return getEntityManager().getCriteriaBuilder();
	}

	protected EntityManager getEntityManager() {
		if (this.entityManager == null) {
			this.entityManager = Beans.getReference(EntityManager.class);
		}

		return this.entityManager;
	}

	protected Pagination getPagination() {
		if (pagination == null) {
			PaginationContext context = paginationContext.get();
			pagination = context.getPagination(getBeanClass());
		}

		return pagination;
	}

	protected CriteriaQuery<T> createCriteriaQuery() {
		return getCriteriaBuilder().createQuery(getBeanClass());
	}

	protected Query createQuery(final String ql) {
		return getEntityManager().createQuery(ql);
	}

	protected void handleException(Throwable cause) throws Throwable {
		if (cause instanceof TransactionRequiredException) {
			String message = bundle.get().getString("no-transaction-active", "frameworkdemoiselle.transaction.class",
					Configuration.DEFAULT_RESOURCE);

			throw new DemoiselleException(message, cause);

		} else {
			throw cause;
		}
	}

	@Override
	@Transactional
	public void insert(final T entity) {
		getEntityManager().persist(entity);
	}

	@Override
	@Transactional
	public void delete(final I id) {
		T entity = getEntityManager().getReference(getBeanClass(), id);
		getEntityManager().remove(entity);
	}

	@Override
	@Transactional
	public void update(final T entity) {
		getEntityManager().merge(entity);
	}

	@Override
	public T load(final I id) {
		return getEntityManager().find(getBeanClass(), id);
	}

	@Override
	public List<T> findAll() {
		return findByJPQL("select this from " + getBeanClass().getSimpleName() + " this");
	}

	/**
	 * Search JPQL integrated into the context of paging
	 * 
	 * @param jpql
	 *            - query in syntax JPQL
	 * @return a list of entities
	 */
	protected List<T> findByJPQL(String jpql) {
		TypedQuery<T> listQuery = getEntityManager().createQuery(jpql, getBeanClass());

		if (getPagination() != null) {
			String countQuery = createCountQuery(jpql);
			Query query = getEntityManager().createQuery(countQuery);
			Number cResults = (Number) query.getSingleResult();
			getPagination().setTotalResults(cResults.intValue());
			listQuery.setFirstResult(getPagination().getFirstResult());
			listQuery.setMaxResults(getPagination().getPageSize());
		}

		return listQuery.getResultList();
	}

	/**
	 * Search CriteriaQuery integrated into the context of paging
	 * 
	 * @param criteriaQuery
	 *            - structure CriteriaQuery
	 * @return a list of entities
	 */
	public List<T> findByCriteriaQuery(CriteriaQuery<T> criteriaQuery) {
		TypedQuery<T> listQuery = getEntityManager().createQuery(criteriaQuery);

		if (getPagination() != null) {
			CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
			CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
			countQuery.select(builder.count(countQuery.from(getBeanClass())));
			countQuery.where(criteriaQuery.getRestriction());
			getEntityManager().createQuery(countQuery);
			getPagination().setTotalResults((int) (getEntityManager().createQuery(countQuery).getSingleResult() + 0));
			listQuery.setFirstResult(getPagination().getFirstResult());
			listQuery.setMaxResults(getPagination().getPageSize());
		}

		return listQuery.getResultList();
	}

	/**
	 * Converts a query into a count query
	 * 
	 * @param query
	 * @return
	 */
	private String createCountQuery(String query) {
		Matcher matcher = Pattern.compile("[Ss][Ee][Ll][Ee][Cc][Tt](.+)[Ff][Rr][Oo][Mm]").matcher(query);

		if (matcher.find()) {
			String group = matcher.group(1).trim();
			query = query.replaceFirst(group, "COUNT(" + group + ")");
			matcher = Pattern.compile("[Oo][Rr][Dd][Ee][Rr](.+)").matcher(query);

			if (matcher.find()) {
				group = matcher.group(0);
				query = query.replaceFirst(group, "");
			}

			return query;

		} else {
			throw new DemoiselleException(bundle.get().getString("malformed-jpql"));
		}
	}

	/**
	 * Retrieves a list of entities based on a single example instance of it.
	 * <p>
	 * See below a sample of its usage:
	 * 
	 * <pre>
	 * Employee example = new Employee();
	 * example.setId(12345);
	 * return (List&lt;Employee&gt;) findByExample(example);
	 * </pre>
	 * 
	 * @param example
	 *            an entity example
	 * @return a list of entities
	 */
	protected List<T> findByExample(final T example) {
		final CriteriaQuery<T> criteria = createCriteriaByExample(example);
		return getEntityManager().createQuery(criteria).getResultList();
	}

	/**
	 * Support method which will be used for construction of criteria-based queries.
	 * 
	 * @param example
	 *            an example of the given entity
	 * @return an instance of {@code CriteriaQuery}
	 */
	private CriteriaQuery<T> createCriteriaByExample(final T example) {
		final CriteriaBuilder builder = getCriteriaBuilder();
		final CriteriaQuery<T> query = builder.createQuery(getBeanClass());
		final Root<T> entity = query.from(getBeanClass());

		final List<Predicate> predicates = new ArrayList<Predicate>();
		final Field[] fields = example.getClass().getDeclaredFields();

		for (Field field : fields) {
			if (!field.isAnnotationPresent(Column.class) && !field.isAnnotationPresent(Basic.class)
					&& !field.isAnnotationPresent(Enumerated.class)) {
				continue;
			}

			Object value = null;

			try {
				field.setAccessible(true);
				value = field.get(example);

			} catch (IllegalArgumentException e) {
				continue;

			} catch (IllegalAccessException e) {
				continue;

			}

			if (value == null) {
				continue;
			}

			final Predicate pred = builder.equal(entity.get(field.getName()), value);
			predicates.add(pred);
		}

		return query.where(predicates.toArray(new Predicate[0])).select(entity);
	}

}
