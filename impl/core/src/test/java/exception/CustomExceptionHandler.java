package exception;

import br.gov.frameworkdemoiselle.exception.ExceptionHandler;
import br.gov.frameworkdemoiselle.stereotype.Controller;

@Controller
public class CustomExceptionHandler {

	private boolean exceptionHandler = false;

	public boolean isExceptionHandler() {
		return exceptionHandler;
	}

	public void setExceptionHandler(boolean exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}	
	
	public void throwExceptionWithMessage() {
		throw new CustomException();
	}
	
	@ExceptionHandler
	public void handler(CustomException exception) {
		setExceptionHandler(true);
	}
}
