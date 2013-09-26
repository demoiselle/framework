package exception.handler.redirect;

import br.gov.frameworkdemoiselle.stereotype.ViewController;

@ViewController
public class RedirectBean {

	private String redirectCorrectPage = "Correct Redirect Exception!";
	
	private String redirectWrongPage = "Wrong Redirect Exception!";

	public String getRedirectCorrectPage() {
		throw new ExceptionWithCorrectRedirect(redirectCorrectPage);
	}

	public String getRedirectWrongPage() {
		throw new ExceptionWithWrongRedirect(redirectWrongPage);
	}
	
}
