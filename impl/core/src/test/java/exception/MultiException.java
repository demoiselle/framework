package exception;

import br.gov.frameworkdemoiselle.exception.ExceptionHandler;
import br.gov.frameworkdemoiselle.stereotype.Controller;

@Controller
public class MultiException {

	private boolean nullPointerExceptionHandler = false;

	private boolean arithmeticExceptionHandler = false;
	
	public boolean isNullPointerExceptionHandler() {
		return nullPointerExceptionHandler;
	}

	public boolean isArithmeticExceptionHandler() {
		return arithmeticExceptionHandler;
	}
	
	public void throwNullPointerException() {
		throw new NullPointerException();
	}

	public void throwArithmeticException() {
		throw new ArithmeticException();
	}
	
	@SuppressWarnings({ "null", "unused" })
	public void throwTwoException() {
		String txt = null;
		txt.toString();
		int result = 4 / 0;
	}

	@ExceptionHandler
	public void handler(NullPointerException cause) {
		nullPointerExceptionHandler = true;
	}

	@ExceptionHandler
	public void handler(ArithmeticException cause) {
		arithmeticExceptionHandler = true;
	}	
}
