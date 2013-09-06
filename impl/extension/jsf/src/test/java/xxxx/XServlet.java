package xxxx;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.gov.frameworkdemoiselle.security.Credentials;
import br.gov.frameworkdemoiselle.security.SecurityContext;

@WebServlet("/login")
public class XServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Inject
	private SecurityContext securityContext;

	@Inject
	private Credentials credentials;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doGet(request, response);

		credentials.setUsername("admin");
		credentials.setPassword("changeit");

		securityContext.login();
	}
}
