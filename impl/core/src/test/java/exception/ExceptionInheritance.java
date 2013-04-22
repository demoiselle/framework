package exception;

import br.gov.frameworkdemoiselle.exception.ExceptionHandler;
import br.gov.frameworkdemoiselle.stereotype.Controller;

@Controller
public class ExceptionInheritance {

	private boolean handlerSuperClass = false;
	
	private boolean handlerClass = false;
	
	public boolean isHandlerSuperClass() {
		return handlerSuperClass;
	}
	
	public boolean isHandlerClass() {
		return handlerClass;
	}

	public void throwNullPointerException() {
		throw new NullPointerException();
	}
	
	public void throwArithmeticException() {
		throw new ArithmeticException();
	}

	@ExceptionHandler
	public void handle(ArithmeticException e) {
		handlerClass = true;
	}
	
	@ExceptionHandler
	public void handle(RuntimeException e) {
		handlerSuperClass = true;
	}
	
}
