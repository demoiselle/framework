package security.authentication.form;

import static org.apache.http.HttpStatus.SC_EXPECTATION_FAILED;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.gov.frameworkdemoiselle.security.Credentials;
import br.gov.frameworkdemoiselle.security.InvalidCredentialsException;
import br.gov.frameworkdemoiselle.security.NotLoggedInException;
import br.gov.frameworkdemoiselle.security.SecurityContext;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.Strings;

public class HelperServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getRequestURI().endsWith("/login")) {
			login(request, response);
		} else {
			logout(request, response);
		}
	}

	private void login(HttpServletRequest request, HttpServletResponse response) {
		loadCredentials(request);
		SecurityContext securityContext = Beans.getReference(SecurityContext.class);

		try {
			securityContext.login();

			if (securityContext.isLoggedIn()) {
				response.setStatus(SC_OK);
			} else {
				response.setStatus(SC_FORBIDDEN);
			}

		} catch (InvalidCredentialsException e) {
			response.setStatus(SC_FORBIDDEN);
		}
	}

	private void logout(HttpServletRequest request, HttpServletResponse response) {
		loadCredentials(request);
		SecurityContext securityContext = Beans.getReference(SecurityContext.class);

		if (isLogon(request)) {
			securityContext.login();
		}

		try {
			securityContext.logout();
			
			if (!securityContext.isLoggedIn()) {
				response.setStatus(SC_OK);
			} else {
				response.setStatus(SC_EXPECTATION_FAILED);
			}
			
		} catch (NotLoggedInException cause) {
			response.setStatus(SC_UNAUTHORIZED);
		}
	}

	private void loadCredentials(HttpServletRequest request) {
		Credentials credentials = Beans.getReference(Credentials.class);
		credentials.setUsername(request.getParameter("username"));
		credentials.setPassword(request.getParameter("password"));
	}

	private boolean isLogon(HttpServletRequest request) {
		return !Strings.isEmpty(request.getParameter("username"));
	}
}
