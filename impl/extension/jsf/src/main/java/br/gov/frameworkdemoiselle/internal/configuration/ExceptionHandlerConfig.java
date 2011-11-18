package br.gov.frameworkdemoiselle.internal.configuration;

import java.io.Serializable;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.Configuration;

@Configuration(prefix = "frameworkdemoiselle.handle")
public class ExceptionHandlerConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@Name("application.exception")
	private boolean handleApplicationException = true;

	@Name("application.exception.page")
	private String exceptionPage = "/application_error";

	public String getExceptionPage() {
		return exceptionPage;
	}
	
	public boolean isHandleApplicationException() {
		return handleApplicationException;
	}
}
