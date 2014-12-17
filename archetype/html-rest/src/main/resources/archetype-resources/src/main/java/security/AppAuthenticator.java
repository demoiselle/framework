package ${package}.security;

import java.security.Principal;

import br.gov.frameworkdemoiselle.security.Credentials;
import br.gov.frameworkdemoiselle.security.InvalidCredentialsException;
import br.gov.frameworkdemoiselle.security.TokenAuthenticator;
import br.gov.frameworkdemoiselle.util.Beans;

public class AppAuthenticator extends TokenAuthenticator {

	private static final long serialVersionUID = 1L;

	@Override
	protected Principal customAuthentication() throws Exception {
		Principal user = null;
		final Credentials credentials = Beans.getReference(Credentials.class);
		final String username = credentials.getUsername();

		if (credentials.getPassword().equals("secret")) {
			user = new Principal() {

				@Override
				public String getName() {
					return username;
				}
			};

		} else {
			throw new InvalidCredentialsException();
		}

		return user;
	}
}
