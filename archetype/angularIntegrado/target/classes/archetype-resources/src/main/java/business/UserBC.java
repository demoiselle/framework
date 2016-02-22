package ${package}.business;

import br.gov.frameworkdemoiselle.lifecycle.Startup;
import br.gov.frameworkdemoiselle.stereotype.BusinessController;
import br.gov.frameworkdemoiselle.template.DelegateCrud;
import br.gov.frameworkdemoiselle.transaction.Transactional;
import ${package}.entity.User;
import ${package}.persistence.UserDAO;
import java.util.List;
import ${package}.util.Util;

/**
 *
 * @author 70744416353
 */


@BusinessController
public class UserBC extends DelegateCrud<User, Long, UserDAO> {

    private static final long serialVersionUID = -7801407214303725321L;

    @Startup
    @Transactional
    public void load() {
        if (findAll().isEmpty()) {

            User user = new User();
            user.setName("ADMIN");
            user.setEmail("admin@demoiselle.gov.br");
            user.setSenha(Util.MD5("123456"));
            user.setPerfil("ADMINISTRADOR");

            getDelegate().insert(user);

            user = new User();
            user.setName("USUARIO");
            user.setEmail("usuario@demoiselle.gov.br");
            user.setSenha(Util.MD5("456789"));
            user.setPerfil("USUARIO");

            getDelegate().insert(user);
        }
    }

    /**
     *
     * @return
     */
    public Long count() {
        return getDelegate().count();
    }

    /**
     *
     * @param field
     * @param order
     * @param init
     * @param qtde
     * @return
     */
    public List list(String field, String order, int init, int qtde) {
        return getDelegate().list(field, order, init, qtde);
    }

    /**
     *
     * @param email
     * @param senha
     * @return
     */
    public User loadEmailPass(String email, String senha) {
        return getDelegate().loadEmailPass(email, senha);
    }

    public List list(String campo, String valor) {
        return getDelegate().list(campo, valor);
    }

}
