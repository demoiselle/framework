package exception.handler.authorization;

import br.gov.frameworkdemoiselle.security.AuthorizationException;
import br.gov.frameworkdemoiselle.stereotype.ViewController;

@ViewController
public class AuthorizationBean {

	private String correctMessage = "Authorization Message.";

	private String exceptionMessage = "Authorization Exception!";

	public String getCorrectMessage() {
		return correctMessage;
	}

	public String getExceptionMessage() {
		throw new AuthorizationException(exceptionMessage);
	}

	public void loadExceptionMessage() {
		throw new AuthorizationException(exceptionMessage);
	}

}
