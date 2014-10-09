package ${package}.security;

import java.security.Principal;

import javax.enterprise.context.RequestScoped;

import br.gov.frameworkdemoiselle.security.Authenticator;
import br.gov.frameworkdemoiselle.security.Credentials;
import br.gov.frameworkdemoiselle.security.InvalidCredentialsException;
import br.gov.frameworkdemoiselle.util.Beans;

@RequestScoped
public class AppAuthenticator implements Authenticator {

	private static final long serialVersionUID = 1L;

	private Principal user;

	@Override
	public void authenticate() throws Exception {
		final Credentials credentials = Beans.getReference(Credentials.class);

		if (credentials.getPassword().equals("secret")) {
			this.user = new Principal() {

				@Override
				public String getName() {
					return credentials.getUsername();
				}
			};

		} else {
			throw new InvalidCredentialsException();
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
}
