package security;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import br.gov.frameworkdemoiselle.security.Credentials;
import br.gov.frameworkdemoiselle.security.SecurityContext;

@WebServlet("/login")
public class SecurityServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private Credentials credentials;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doGet(request, response);
		
		credentials.setUsername("users");
		credentials.setPassword("users");
		securityContext.login();
		response.setStatus(HttpStatus.SC_OK);
	}
}
