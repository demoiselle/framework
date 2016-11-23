/*
 * DgetEntityManager()oiselle Framework
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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.demoiselle.jee.core.api.persistence.Crud;
import org.demoiselle.jee.core.exception.DemoiselleException;

@TransactionAttribute(TransactionAttributeType.MANDATORY)
public abstract class AbstractDAO<T, I>  implements Crud<T, I>{

    @Inject
    protected Logger logger;

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
            throw new DemoiselleException("Não foi possível salvar", e.getCause());
        }
    }

    public T merge(T entity) {
        try {
            getEntityManager().merge(entity);
            return entity;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoiselleException("Não foi possível salvar", e.getCause());
        }
    }

    public void remove(I id) {
        try {
            getEntityManager().remove(getEntityManager().find(entityClass, id));
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoiselleException("Não foi possível excluir", e.getCause());
        }

    }

    public T find(I id) {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> q = cb.createQuery(entityClass);
            Root<T> c = q.from(entityClass);
            return getEntityManager().createQuery(q).getSingleResult();
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoiselleException("Não foi possível consultar", e.getCause());
        }

    }

    public ResultSet find() {
        try {
            ResultSet rs = new ResultSet();

            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<T> q = cb.createQuery(entityClass);
            Root<T> c = q.from(entityClass);

            rs.setContent(getEntityManager().createQuery(q).getResultList());
            rs.setInit(0);
            rs.setQtde(rs.getContent().size());
            rs.setTotal(rs.getContent().size());
            return rs;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoiselleException("Não foi possível consultar", e.getCause());
        }
    }

    public ResultSet find(String field, String order, int init, int qtde) {
        try {
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

            source.add(getEntityManager().createQuery(q).getResultList());

            for (int i = init; i < qtde; i++) {
                result.add(source.get(i));
            }

            rs.setContent(result);
            rs.setInit(init);
            rs.setQtde(qtde);
            rs.setTotal(source.size());
            return rs;
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw new DemoiselleException("Não foi possível consultar", e.getCause());
        }

    }

}
