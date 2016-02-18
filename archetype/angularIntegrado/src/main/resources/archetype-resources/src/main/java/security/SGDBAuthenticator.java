package ${package}.security;

import br.gov.frameworkdemoiselle.security.AuthenticationException;
import br.gov.frameworkdemoiselle.security.Authenticator;
import br.gov.frameworkdemoiselle.security.Credentials;
import br.gov.frameworkdemoiselle.security.Token;
import br.gov.frameworkdemoiselle.util.Beans;
import ${package}.business.UserBC;
import ${package}.cover.UserCover;
import ${package}.entity.User;
import java.security.Principal;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author 70744416353
 */
@RequestScoped
public class SGDBAuthenticator implements Authenticator {

    private static final long serialVersionUID = 1L;

    @Inject
    private Credentials credentials;

    private User user;

    @Inject
    private UserBC usuarioBC;

    @Inject
    private JWTManager jwt;

    @Inject
    private transient HttpServletRequest httpRequest;

    @Inject
    private Logger logger;

    @Override
    public void authenticate() {

        Token token = Beans.getReference(Token.class);

        UserCover cover = jwt.hasToken(token.getValue());

        if (cover == null) {

            if (credentials.getUsername() != null && credentials.getPassword() != null) {

                try {

                    this.user = usuarioBC.loadEmailPass(credentials.getUsername(), credentials.getPassword());

                    if (this.user != null) {
                        this.user.setIp(httpRequest.getRemoteAddr());
                        generateToken(token);
                    } else {
                        throw new AuthenticationException("Usuário ou senha inválidos");
                    }

                } catch (Exception ex) {
                    throw new AuthenticationException("Usuário ou senha inválidos");
                }

            }

        } else {
            this.user = new User();
            this.user.setId(cover.getId());
            this.user.setName(cover.getNome());
            this.user.setPerfil(cover.getPerfil());
        }

    }

    @Override
    public void unauthenticate() throws Exception {
        this.user = null;
    }

    @Override
    public Principal getUser() {
        return this.user;
    }

    private void generateToken(Token token) {
        String chave = jwt.addToken(this.user);
        token.setValue(chave);
        this.user.setToken(chave);
    }

}
