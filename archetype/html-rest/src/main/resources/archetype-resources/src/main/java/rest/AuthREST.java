package ${package}.rest;

import java.security.Principal;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import br.gov.frameworkdemoiselle.security.Credentials;
import br.gov.frameworkdemoiselle.security.LoggedIn;
import br.gov.frameworkdemoiselle.security.SecurityContext;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ValidatePayload;

@Path("auth")
public class AuthREST {

	@Inject
	private SecurityContext securityContext;

	@POST
	@Path("login")
	@ValidatePayload
	@Consumes("application/json")
	@Produces("application/json")
	public Principal login(CredentialsBody body) {
		Credentials credentials = Beans.getReference(Credentials.class);
		credentials.setUsername(body.username);
		credentials.setPassword(body.password);

		securityContext.login();
		return securityContext.getUser();
	}

	@POST
	@LoggedIn
	@Path("logout")
	@ValidatePayload
	public void logout() {
		securityContext.logout();
	}

	public static class CredentialsBody {

		@NotNull(message = "{required.field}")
		@Size(min = 1, message = "{required.field}")
		public String username;

		@NotNull(message = "{required.field}")
		@Size(min = 1, message = "{required.field}")
		public String password;
	}
}
