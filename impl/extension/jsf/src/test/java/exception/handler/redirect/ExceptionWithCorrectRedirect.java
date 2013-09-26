package exception.handler.redirect;

import br.gov.frameworkdemoiselle.annotation.Redirect;
import br.gov.frameworkdemoiselle.exception.ApplicationException;

@Redirect(viewId="/redirect.jsf")
@ApplicationException
public class ExceptionWithCorrectRedirect extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public ExceptionWithCorrectRedirect(String msg) {
		super(msg);
	}
}
