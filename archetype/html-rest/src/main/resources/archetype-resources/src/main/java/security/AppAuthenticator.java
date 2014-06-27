package ${package}.security;

import javax.enterprise.context.RequestScoped;

import br.gov.frameworkdemoiselle.security.Authenticator;
import br.gov.frameworkdemoiselle.security.Credentials;
import br.gov.frameworkdemoiselle.security.InvalidCredentialsException;
import br.gov.frameworkdemoiselle.security.User;
import br.gov.frameworkdemoiselle.util.Beans;

@RequestScoped
public class AppAuthenticator implements Authenticator {

	private static final long serialVersionUID = 1L;

	private User user;

	@Override
	public void authenticate() throws Exception {
		Credentials credentials = Beans.getReference(Credentials.class);

		if (credentials.getUsername().equals("admin") && credentials.getPassword().equals("admin")) {
			this.user = new AppUser(credentials.getUsername());
		} else {
			throw new InvalidCredentialsException();
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
