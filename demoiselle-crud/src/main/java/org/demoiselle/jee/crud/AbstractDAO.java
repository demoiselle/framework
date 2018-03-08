/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.demoiselle.jee.core.api.crud.Crud;
import org.demoiselle.jee.core.api.crud.Result;
import org.demoiselle.jee.crud.exception.DemoiselleCrudException;
import org.demoiselle.jee.crud.helper.DemoiselleCrudHelper;
import org.demoiselle.jee.crud.pagination.QueryPredicatesHelper;

//TODO CLF revisar
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public abstract class AbstractDAO<T, I> implements Crud<T, I> {

    private final QueryPredicatesHelper<T> predicatesHelper;

    private final DemoiselleRequestContext drc;

    private final Class<T> entityClass;

    protected abstract EntityManager getEntityManager();

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @SuppressWarnings("unchecked")
    public AbstractDAO() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.drc = getDemoiselleRequestContext();
        this.predicatesHelper = new QueryPredicatesHelper<T>(this.entityClass, drc.getFilterContext());
    }

    /**
     * Try to retrieve the {@link RequestScoped} {@link DemoiselleRequestContext} bean. Checks if the context is active
     * by retrieving something from the proxied bean. If the context is not active, a {@link ContextNotActiveException} is
     * thrown. In this case, create a new instance of the {@link DemoiselleRequestContext}.

     * @return The {@link DemoiselleRequestContext} for the current request, if any. Otherwise, a new instance of {@link DemoiselleRequestContext}.
     */
    private DemoiselleRequestContext getDemoiselleRequestContext() {
        try {
            DemoiselleRequestContext drc =  CDI.current().select(DemoiselleRequestContext.class).get();
            drc.getFilterContext();
            return drc;
        } catch(ContextNotActiveException e) {
            return new DemoiselleRequestContextImpl();
        }
    }

    @Override
    public T persist(T entity) {
        try {
            getEntityManager().persist(entity);
            return entity;
        } catch (Exception e) {
            throw new DemoiselleCrudException("Não foi possível salvar", e);
        }
    }

    @Override
    public T mergeHalf(I id, T entity) {
        try {
            final StringBuilder sb = new StringBuilder();
            final Map<String, Object> params = new ConcurrentHashMap<>();
            //
            sb.append("UPDATE ");
            sb.append(entityClass.getCanonicalName());
            sb.append(" SET ");
            //
            for (final Field field : entityClass.getDeclaredFields()) {
                if (!field.isAnnotationPresent(ManyToOne.class)) {
                    final Column column = field.getAnnotation(Column.class);
                    //
                    if (column == null || !column.updatable()) {
                        continue;
                    }
                }
                //
                field.setAccessible(true);
                //
                final String name = field.getName();
                final Object value = field.get(entity);
                //
                if (value != null) {
                    if (!params.isEmpty()) {
                        sb.append(", ");
                    }
                    //
                    sb.append(name).append(" = :").append(name);
                    params.putIfAbsent(name, value);
                }
            }
            //
            if (!params.isEmpty()) {
                final String idName
                        = CrudUtilHelper.getMethodAnnotatedWithID(entityClass);
                //
                sb.append(" WHERE ").append(idName).append(" = :").append(idName);
                params.putIfAbsent(idName, id);
                //
                final Query query = getEntityManager().createQuery(sb.toString());
                //
                for (final Map.Entry<String, Object> entry : params.entrySet()) {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
                //
                query.executeUpdate();
            }
            //
            return entity;
        } catch (final Exception e) {
            throw new DemoiselleCrudException("Não foi possível salvar", e);
        }
    }

    @Override
    public T mergeFull(T entity) {
        try {
            return getEntityManager().merge(entity);
        } catch (Exception e) {
            // TODO: CLF Severe? Pode cair aqui somente por ter violação de Unique
            throw new DemoiselleCrudException("Não foi possível salvar", e);
        }
    }

    @Override
    public void remove(I id) {
        try {
            getEntityManager().remove(getEntityManager().find(entityClass, id));
        } catch (Exception e) {
            throw new DemoiselleCrudException("Não foi possível excluir", e);
        }
    }

    @Override
    public T find(I id) {
        try {
            return getEntityManager().find(entityClass, id);
        } catch (Exception e) {
            throw new DemoiselleCrudException("Não foi possível consultar", e);
        }

    }

    @Override
    public Result find() {
        try {
            CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root<T> root = criteriaQuery.from(entityClass);
            return new DemoiselleCrudHelper(getEntityManager(), entityClass)
                    .executeQuery(criteriaQuery, root);
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoiselleCrudException("Não foi possível consultar", e);
        }
    }

    public Long count() {
        return new DemoiselleCrudHelper<>(getEntityManager(), entityClass).getCount();
    }


}