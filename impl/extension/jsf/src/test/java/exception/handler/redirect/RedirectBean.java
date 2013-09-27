package exception.handler.redirect;

import java.io.Serializable;

import br.gov.frameworkdemoiselle.stereotype.ViewController;

@ViewController
public class RedirectBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String redirectCorrectPage = "Correct Redirect Exception!";

	private String redirectWrongPage = "Wrong Redirect Exception!";

	public String getRedirectCorrectPage() {
		throw new ExceptionWithCorrectRedirect(redirectCorrectPage);
	}

	public String getRedirectWrongPage() {
		throw new ExceptionWithWrongRedirect(redirectWrongPage);
	}

}
