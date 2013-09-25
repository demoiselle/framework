package security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import br.gov.frameworkdemoiselle.security.Credentials;
import br.gov.frameworkdemoiselle.security.SecurityContext;
import br.gov.frameworkdemoiselle.util.Beans;

@WebServlet("/login")
public class SecurityServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String result = request.getHeader("Authorization");
		result = (result == null ? request.getHeader("authorization") : result);

		Credentials credentials = Beans.getReference(Credentials.class);
		credentials.setUsername(request.getParameter("username"));
		credentials.setPassword(request.getParameter("password"));

		Beans.getReference(SecurityContext.class).login();

		response.setStatus(HttpStatus.SC_OK);
	}
}
