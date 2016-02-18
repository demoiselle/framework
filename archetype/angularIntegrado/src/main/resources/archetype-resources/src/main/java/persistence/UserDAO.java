package ${package}.persistence;

import br.gov.frameworkdemoiselle.stereotype.PersistenceController;
import br.gov.frameworkdemoiselle.template.JPACrud;
import ${package}.entity.User;
import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

/**
 *
 * @author 70744416353
 */
@PersistenceController
public class UserDAO extends JPACrud<User, Long> {

    private static final long serialVersionUID = 1L;


    public User loadByFone(String telephoneNumber) {
        String jpql = "SELECT u from " + this.getBeanClass().getSimpleName() + " u where u.telephoneNumber = :telephoneNumber";

        TypedQuery<User> query = getEntityManager().createQuery(jpql, User.class);
        query.setParameter("telephoneNumber", telephoneNumber);

        User result;
        try {
            result = query.getSingleResult();
        } catch (NoResultException cause) {
            result = null;
        }

        return result;
    }


    public User loadEmailPass(String email, String senha) {
        String jpql = "SELECT u from " + this.getBeanClass().getSimpleName() + " u where u.email = :email and u.senha = :senha";

        TypedQuery<User> query = getEntityManager().createQuery(jpql, User.class);
        query.setParameter("email", email);
        query.setParameter("senha", senha);

        User result;
        try {
            result = query.getSingleResult();
        } catch (NoResultException cause) {
            result = null;
        }

        return result;
    }

    /**
     *
     * @return
     */
    public Long count() {
        return (Long) getEntityManager().createQuery("select COUNT(u) from " + this.getBeanClass().getSimpleName() + " u").getSingleResult();
    }

    /**
     *
     * @param field
     * @param order
     * @param init
     * @param qtde
     * @return
     */
    @SuppressWarnings("unchecked")
    public List list(String field, String order, int init, int qtde) {
        return getEntityManager().createQuery("select u from " + this.getBeanClass().getSimpleName() + " u ORDER BY " + field + " " + order).setFirstResult(init).setMaxResults(qtde).getResultList();
    }

    /**
     *
     * @param field
     * @return
     */
    @SuppressWarnings("unchecked")
    public List list(String campo, String valor) {
        return getEntityManager().createQuery("select u from " + this.getBeanClass().getSimpleName() + " u " + " where " + campo + " = " + valor + " ORDER BY " + campo).getResultList();
    }

}
