package exception.handler.redirect;

import br.gov.frameworkdemoiselle.annotation.Redirect;
import br.gov.frameworkdemoiselle.exception.ApplicationException;

@Redirect(viewId="/inexist.jsf")
@ApplicationException
public class ExceptionWithWrongRedirect extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public ExceptionWithWrongRedirect(String msg) {
		super(msg);
	}
}
