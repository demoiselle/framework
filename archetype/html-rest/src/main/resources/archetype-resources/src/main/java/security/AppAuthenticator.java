package ${package}.security;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import br.gov.frameworkdemoiselle.security.Authenticator;
import br.gov.frameworkdemoiselle.security.Credentials;
import br.gov.frameworkdemoiselle.security.InvalidCredentialsException;
import br.gov.frameworkdemoiselle.security.User;

@RequestScoped
public class AppAuthenticator implements Authenticator {

	private static final long serialVersionUID = 1L;

	@Inject
	private Credentials credentials;

	private User user;

	@Override
	public void authenticate() throws Exception {
		if ("admin".equals(credentials.getUsername()) && "admin".equals(credentials.getPassword())) {
			this.user = new AppUser(credentials.getUsername());
		} else {
			throw new InvalidCredentialsException("usuário ou senha inválidos");
		}
	}

	@Override
	public void unauthenticate() throws Exception {
		this.user = null;
	}

	@Override
	public User getUser() {
		return this.user;
	}
}
