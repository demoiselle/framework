package exception;

import java.util.NoSuchElementException;

import br.gov.frameworkdemoiselle.exception.ExceptionHandler;
import br.gov.frameworkdemoiselle.stereotype.Controller;

@Controller
public class OneException {
	
	private boolean exceptionHandler = false;
	
	private boolean exceptionHandlerIllegalArgument1 = false;

	private boolean exceptionHandlerIllegalArgument2 = false;

	private boolean exceptionHandlerIllegalArgument3 = false;	

	public boolean isExceptionHandler() {
		return exceptionHandler;
	}
	
	public boolean isExceptionHandlerIllegalArgument1() {
		return exceptionHandlerIllegalArgument1;
	}

	public boolean isExceptionHandlerIllegalArgument2() {
		return exceptionHandlerIllegalArgument2;
	}

	public boolean isExceptionHandlerIllegalArgument3() {
		return exceptionHandlerIllegalArgument3;
	}	
	
	@SuppressWarnings("null")
	public void throwExceptionWithHandler() {
		String txt = null;
		txt.toString();
	}

	@SuppressWarnings("unused")
	public void throwExceptionWithoutHandler() {
		int result = 4/0;
	}
	
	public void throwIllegalArgumentException() {
		throw new IllegalArgumentException();
	}
	
	public void throwNoSuchElementException() {
		throw new NoSuchElementException();
	}	

	@ExceptionHandler
	public void handler(NullPointerException cause) {
		exceptionHandler = true;
	}

	@ExceptionHandler
	public void handler1(IllegalArgumentException cause) {
		exceptionHandlerIllegalArgument1 = true;
	}
	
	@ExceptionHandler
	public void handler3(IllegalArgumentException cause) {
		exceptionHandlerIllegalArgument3 = true;
	}
	
	@ExceptionHandler
	public void handler2(IllegalArgumentException cause) {
		exceptionHandlerIllegalArgument2 = true;
	}
	
	@ExceptionHandler
	public void handlerWithError(NoSuchElementException cause) {
		int a = 2/0;
	}
}
