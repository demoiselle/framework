package ${package}.security;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import br.gov.frameworkdemoiselle.security.Authenticator;
import br.gov.frameworkdemoiselle.security.Credentials;
import br.gov.frameworkdemoiselle.security.InvalidCredentialsException;
import br.gov.frameworkdemoiselle.security.User;

@RequestScoped
public class SimpleAuthenticator implements Authenticator {

	private static final long serialVersionUID = 1L;

	@Inject
	private Credentials credentials;

	private User user;

	@Override
	public void authenticate() throws Exception {
		if (credentials.getUsername().equalsIgnoreCase("admin") && credentials.getPassword().equalsIgnoreCase("admin")) {
			this.user = createUser();
		} else {
			throw new InvalidCredentialsException("usuário ou senha inválidos");
		}
	}

	private User createUser() {
		return new User() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getId() {
				return credentials.getUsername();
			}

			@Override
			public void setAttribute(Object key, Object value) {
			}

			@Override
			public Object getAttribute(Object key) {
				return null;
			}
		};
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
