package exception.basic;

import java.util.NoSuchElementException;

import br.gov.frameworkdemoiselle.exception.ExceptionHandler;
import br.gov.frameworkdemoiselle.stereotype.Controller;

@Controller
public class ExceptionNested {

	private boolean exceptionHandler = false;

	public boolean isExceptionHandler() {
		return exceptionHandler;
	}

	public void throwNoSuchElementException() {
		throw new NoSuchElementException();
	}

	@ExceptionHandler
	@SuppressWarnings("unused")
	public void handlerWithError(NoSuchElementException cause) {
		int a = 2 / 0;
	}

	@ExceptionHandler
	public void handler(ArithmeticException cause) {
		exceptionHandler = true;
	}
}
