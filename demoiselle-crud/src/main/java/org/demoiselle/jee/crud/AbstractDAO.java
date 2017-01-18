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
import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.exception.DemoiselleCrudException;
import org.demoiselle.jee.crud.pagination.DemoisellePaginationConfig;
import org.demoiselle.jee.crud.pagination.ResultSet;

//TODO revisar
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public abstract class AbstractDAO<T, I> implements Crud<T, I> {

	@Inject
	private DemoisellePaginationConfig paginationConfig;

	@Inject
	private DemoiselleRequestContext drc;

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
			throw new DemoiselleCrudException("Não foi possível salvar", e);
		}
	}

	public T merge(T entity) {
		try {
			getEntityManager().merge(entity);
			return entity;
		} catch (Exception e) {
			// TODO: Severe? Pode cair aqui somente por ter violação de Unique
			// logger.severe(e.getMessage());
			throw new DemoiselleCrudException("Não foi possível salvar", e);
		}
	}

	public void remove(I id) {
		try {
			getEntityManager().remove(getEntityManager().find(entityClass, id));
		} catch (Exception e) {
			// logger.severe(e.getMessage());
			throw new DemoiselleCrudException("Não foi possível excluir", e);
		}

	}

	public T find(I id) {
		try {
			return getEntityManager().find(entityClass, id);
		} catch (Exception e) {
			// logger.severe(e.getMessage());
			throw new DemoiselleCrudException("Não foi possível consultar", e);
		}

	}

	@Override
	public Result find() {

		try {

		    Result result = new ResultSet();
		    
			

			CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
			CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
			
			configureCriteriaQuery(criteriaBuilder, criteriaQuery);
			
			TypedQuery<T> query = getEntityManager().createQuery(criteriaQuery);
			
			if(paginationConfig.getIsEnabled()){
			    Integer firstResult = drc.getOffset() == null ? 0 : drc.getOffset();
	            Integer maxResults = getMaxResult();
	            Long count = count();
	            
			    if(firstResult < count){
    			    query.setFirstResult(firstResult);
    			    query.setMaxResults(maxResults);
			    }
			    
			    drc.setCount(count);
			}

			result.setContent(query.getResultList());
			drc.setEntityClass(entityClass);
			
			return result;

		} catch (Exception e) {
			// logger.severe(e.getMessage());
			throw new DemoiselleCrudException("Não foi possível consultar", e);
		}
	}

    private void configureCriteriaQuery(CriteriaBuilder criteriaBuilder, CriteriaQuery<T> criteriaQuery) {
        if(drc.getFieldsFilter().isEmpty()){
            criteriaQuery.from(entityClass);
        }
        else{
            Root<T> root = criteriaQuery.from(entityClass);
            criteriaQuery.select(root).where(buildPredicates(criteriaBuilder, criteriaQuery, root));
        }
    }

	/**
     * @param root 
	 * @return
     */
    private Predicate[] buildPredicates(CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, Root<T> root) {
        List<Predicate> predicates = new ArrayList<>();
        
        drc.getFieldsFilter().forEach((key, values) -> {
            
            // Many parameters for the same key, generate OR clause
            if(values.size() > 1){
                List<Predicate> predicateSameKey = new ArrayList<>();
                values.forEach((value) -> {
                    predicateSameKey.add(criteriaBuilder.equal(root.get(key), value));
                });
                predicates.add(criteriaBuilder.or(predicateSameKey.toArray(new Predicate[]{})));
            }
            else{
                String value = values.iterator().next();
                if("null".equals(value) || value.isEmpty()){
                    predicates.add(criteriaBuilder.isNull(root.get(key)));
                }
                else{
                    predicates.add(criteriaBuilder.equal(root.get(key), values.iterator().next()));
                }
            }
        });
        
        return predicates.toArray(new Predicate[]{});
    }

    private Integer getMaxResult() {
		if (drc.getLimit() == null && drc.getOffset() == null) {
			return this.paginationConfig.getDefaultPagination();
		}

		return (drc.getLimit() - drc.getOffset()) + 1;
	}

	public Long count() {
		CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Long> countCriteria = criteriaBuilder.createQuery(Long.class);
		Root<T> entityRoot = countCriteria.from(entityClass);
		countCriteria.select(criteriaBuilder.count(entityRoot));
		countCriteria.where(buildPredicates(criteriaBuilder, countCriteria, entityRoot));
		
		return getEntityManager().createQuery(countCriteria).getSingleResult();
	}

	protected Predicate[] extractPredicates(MultivaluedMap<String, String> queryParameters,
			CriteriaBuilder criteriaBuilder, Root<T> root) {
		return new Predicate[] {};
	}
}
