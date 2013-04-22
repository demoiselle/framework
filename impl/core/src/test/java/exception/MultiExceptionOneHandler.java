package exception;

import java.awt.geom.IllegalPathStateException;

import br.gov.frameworkdemoiselle.exception.ExceptionHandler;
import br.gov.frameworkdemoiselle.stereotype.Controller;

@Controller
public class MultiExceptionOneHandler {

	public void throwIllegalPathException() {
		throw new IllegalPathStateException();
	}
	
	@ExceptionHandler
	public void handler(IllegalPathStateException cause, IllegalStateException cause2) {
	}	
}
