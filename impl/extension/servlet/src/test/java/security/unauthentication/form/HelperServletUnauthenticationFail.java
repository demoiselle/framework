package security.unauthentication.form;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.gov.frameworkdemoiselle.security.AuthenticationException;
import br.gov.frameworkdemoiselle.security.Credentials;
import br.gov.frameworkdemoiselle.security.SecurityContext;
import br.gov.frameworkdemoiselle.util.Beans;

public class HelperServletUnauthenticationFail extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String result = request.getHeader("Authorization");
		result = (result == null ? request.getHeader("authorization") : result);

		Credentials credentials = Beans.getReference(Credentials.class);
		credentials.setUsername(request.getParameter("username"));
		credentials.setPassword(request.getParameter("password"));
		try {
			Beans.getReference(SecurityContext.class).logout();
			response.setStatus(SC_OK);
		} catch (AuthenticationException e) {
			response.setStatus(SC_FORBIDDEN);
		}
	}
}
