package ${package}.security;

import javax.inject.Inject;

import br.gov.frameworkdemoiselle.security.SecurityContext;
import ${package}.entity.User;

/**
 *
 * @author 70744416353
 */
public class Authorizer implements br.gov.frameworkdemoiselle.security.Authorizer {

    private static final long serialVersionUID = 1L;

    @Inject
    private SecurityContext securityContext;

    @Override
    public boolean hasRole(String role) throws Exception {
        if (securityContext.isLoggedIn()) {
            User user = (User) securityContext.getUser();
            if (user.getPerfil() != null) {
                if (role.equalsIgnoreCase(user.getPerfil())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasPermission(String resource, String operation) throws Exception {
        return false;
    }

}
