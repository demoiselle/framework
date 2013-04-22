package exception;

import br.gov.frameworkdemoiselle.exception.ExceptionHandler;
import br.gov.frameworkdemoiselle.stereotype.Controller;

@Controller
public class MultiStrategyExceptionHandler {

	private boolean exceptionHandler = false;

	private boolean exceptionTryCacth = false;

	String txt = null;

	public boolean isExceptionHandler() {
		return exceptionHandler;
	}

	public boolean isExceptionTryCacth() {
		return exceptionTryCacth;
	}

	@SuppressWarnings("unused")
	public void exceptionMultiStrategyTryAndHandler() {
		try {
			int result = 4 / 0;
		} catch (ArithmeticException e) {
			exceptionTryCacth = true;
		}
		txt.toString();
	}
	
	@SuppressWarnings("unused")
	public void exceptionMultiStrategyHandlerAndTry() {
		txt.toString();
		try {
			int result = 4 / 0;
		} catch (ArithmeticException e) {
			exceptionTryCacth = true;
		}
	}	

	public void exceptionTwoHandler() {
		try {
			txt.toString();
		} catch (NullPointerException e) {
			exceptionTryCacth = true;
		}
	}

	public void exceptionHandler() {
		txt.toString();
	}

	@ExceptionHandler
	public void handler(NullPointerException cause) {
		exceptionHandler = true;
	}

}
