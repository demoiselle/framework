package producer.response;

import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.gov.frameworkdemoiselle.util.Beans;

public class HelperServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpServletResponse httpResponse = Beans.getReference(HttpServletResponse.class);

		if (httpResponse != null) {
			response.setStatus(SC_OK);
		} else {
			response.setStatus(SC_INTERNAL_SERVER_ERROR);
		}
	}
}
