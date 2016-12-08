/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.crud;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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

import org.demoiselle.jee.core.api.persistence.Crud;
import org.demoiselle.jee.core.pagination.ResultSet;
import org.demoiselle.jee.persistence.crud.exception.DemoisellePersistenceCrudException;
import org.demoiselle.jee.persistence.crud.pagination.DemoisellePaginationConfig;

@TransactionAttribute(TransactionAttributeType.MANDATORY)
public abstract class AbstractDAO<T, I> implements Crud<T, I> {

    /*@Inject
    private DemoiselleCrudConfig config;*/
	
	@Inject
	private DemoisellePaginationConfig paginationConfig;

    @Inject
    protected Logger logger;
    
    @Inject
    private ResultSet resultSet;

    private final Class<T> entityClass;

    protected abstract EntityManager getEntityManager();

    public AbstractDAO() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public T persist(T entity) {
        try {
            getEntityManager().persist(entity);
            return entity;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoisellePersistenceCrudException("Não foi possível salvar", e);
        }
    }

    public T merge(T entity) {
        try {
            getEntityManager().merge(entity);
            return entity;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoisellePersistenceCrudException("Não foi possível salvar", e);
        }
    }

    public void remove(I id) {
        try {
            getEntityManager().remove(getEntityManager().find(entityClass, id));
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoisellePersistenceCrudException("Não foi possível excluir", e);
        }

    }

    public T find(I id) {
        try {
            return getEntityManager().find(entityClass, id);
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoisellePersistenceCrudException("Não foi possível consultar", e);
        }

    }

    @Override
    public ResultSet find() {
    	
    	System.out.println(this.resultSet);
    	
        try {
        	
        	Integer firstResult = resultSet.getOffset();
        	Integer maxResults = getMaxResult();
        	
        	Long count = count();
        	
        	if(firstResult < count){
	            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
	            CriteriaQuery<T> q = cb.createQuery(entityClass);
	            Root<T> c = q.from(entityClass);
	            
	            TypedQuery<T> query = getEntityManager().createQuery(q);
	            query.setFirstResult(firstResult);
	            query.setMaxResults(maxResults);
	
	            resultSet.setContent(query.getResultList());
            }
        	
        	resultSet.setEntityClass(entityClass);
            resultSet.setCount(count);
        	
            return resultSet;
            
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoisellePersistenceCrudException("Não foi possível consultar", e);
        }
    }

    private Integer getMaxResult() {
		if(this.resultSet.getLimit().equals(0) && this.resultSet.getOffset().equals(0)){
			return this.paginationConfig.getDefaultPagination();
		}
		
		return (this.resultSet.getLimit() - this.resultSet.getOffset()) + 1;
	}

	public ResultSet find(String field, String order, int init, int qtde) {
        /*try {

//            if (config.getAcceptRange() < qtde) {
//                throw new DemoisellePersistenceCrudException("A quantidade máxima aceitável é " + config.getAcceptRange());
//            }

            ResultSet rs = new ResultSet();
            List result = new ArrayList<>();
            List source = new ArrayList<>();

            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> q = cb.createQuery(entityClass);
            Root<T> c = q.from(entityClass);
            q.select(c);

            if (order.equalsIgnoreCase("asc")) {
                q.orderBy(cb.asc(c.get(field)));
            } else {
                q.orderBy(cb.desc(c.get(field)));
            }

            source.addAll(getEntityManager().createQuery(q).getResultList());

            if ((init + qtde) > source.size()) {
                qtde = source.size() - init;
            }

            for (int i = init; i < (init + qtde); i++) {
                result.add(source.get(i));
            }

            rs.setContent(result);
            rs.setOffset(init);
            rs.setLimit(qtde);
            rs.setCount(source.size());
            return rs;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoisellePersistenceCrudException("Não foi possível consultar", e);
        }*/
    	
    	return null;

    }

    public ResultSet find(MultivaluedMap<String, String> queryParams) {
        try {
            ResultSet rs = new ResultSet();
            List source = new ArrayList<>();

            CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);

            Root<T> root = criteriaQuery.from(entityClass);

            Predicate[] predicates = extractPredicates(queryParams, criteriaBuilder, root);

            if (predicates.length > 0) {
                criteriaQuery.select(criteriaQuery.getSelection()).where(predicates);
                TypedQuery<T> query = getEntityManager().createQuery(criteriaQuery);
                source.addAll(query.setMaxResults(10).getResultList());
            }

            rs.setContent(source);
            rs.setOffset(new Integer(0));
            rs.setLimit(source.size());
            rs.setCount(count());
            return rs;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoisellePersistenceCrudException("Não foi possível consultar", e);
        }
    	

    }

    public ResultSet find(MultivaluedMap<String, String> queryParams, String field, String order, int init, int qtde) {
       /* try {

//            if (config.getAcceptRange() < qtde) {
//                throw new DemoisellePersistenceCrudException("A quantidade máxima aceitável é " + config.getAcceptRange());
//            }

            ResultSet rs = new ResultSet();
            List result = new ArrayList<>();
            List source = new ArrayList<>();

            CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);

            Root<T> root = criteriaQuery.from(entityClass);
            Predicate[] predicates = extractPredicates(queryParams,
                    criteriaBuilder, root);

            if (predicates.length > 0) {

                criteriaQuery.select(criteriaQuery.getSelection()).where(predicates);
                TypedQuery<T> query = getEntityManager().createQuery(criteriaQuery);
                source.addAll(query.getResultList());

                if ((init + qtde) > source.size()) {
                    qtde = source.size() - init;
                }

                for (int i = init; i < (init + qtde); i++) {
                    result.add(source.get(i));
                }

            }

            rs.setContent(result);
            rs.setOffset(init);
            rs.setLimit(qtde);
            rs.setCount(source.size());
            return rs;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoisellePersistenceCrudException("Não foi possível consultar", e);
        }*/
    	
    	return null;
    }

    public Long count() {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> countCriteria = criteriaBuilder.createQuery(Long.class);
        Root<?> entityRoot = countCriteria.from(entityClass);
        countCriteria.select(criteriaBuilder.count(entityRoot));
        return getEntityManager().createQuery(countCriteria).getSingleResult();
    }

    protected Predicate[] extractPredicates(
            MultivaluedMap<String, String> queryParameters,
            CriteriaBuilder criteriaBuilder, Root<T> root) {
        return new Predicate[]{};
    }
}
