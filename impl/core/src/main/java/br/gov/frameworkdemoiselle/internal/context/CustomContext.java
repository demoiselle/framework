package br.gov.frameworkdemoiselle.internal.context;

import javax.enterprise.context.spi.Context;

public interface CustomContext extends Context {

	void setActive(boolean active);

}
