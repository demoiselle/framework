package br.gov.frameworkdemoiselle.internal.implementation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.NameQualifier;

@Provider
public class ParseExceptionMapper implements ExceptionMapper<Throwable> {

	private transient ResourceBundle bundle;

	private transient Logger logger;

	private Set<Class<?>> getTypes(Class<?> target) {
		Set<Class<?>> classesInterfaces = new HashSet<Class<?>>();
		classesInterfaces.add(target);
		classesInterfaces.addAll(Arrays.asList(target.getInterfaces()));

		Class<?> superClass = target.getSuperclass();

		if (superClass != null) {
			classesInterfaces.add(superClass);
			classesInterfaces.addAll(getTypes(superClass));
		}

		return classesInterfaces;
	}

	@Override
	public Response toResponse(Throwable exception) {

		// Throwable original = exception;
		//
		// while (exception != null) {
		// System.out.println("xxxxxxxxxxxxxxxxxxxxxx : " + exception.getClass().getCanonicalName());
		//
		// exception = exception.getCause();
		// }
		//
		// exception = original;
		//
		// Class<>
		//
		// while (exception != null) {
		// System.out.println("xxxxxxxxxxxxxxxxxxxxxx : " + exception.getClass().getIgetCanonicalName());
		//
		// exception.getClass().getSuperclass();
		//
		// exception = exception.getP;
		// }

		for (Class<?> type : getTypes(exception.getClass())) {

			System.out.println("___________________ " + type.getCanonicalName());

			if (type.getCanonicalName().toLowerCase().indexOf("unrecognized") > -1) {
				getLogger().error("XXXXXXXXXXXXXX", exception);
				return Response.status(400).build();
			}
		}

		// System.out.println("xxxxxxxxxxxxxx : " + getTypes(exception.getClass()));

		// if (exception.getMessage().toLowerCase().indexOf("unrecognized") > -1) {
		// getLogger().error("XXXXXXXXXXXXXX", exception);
		// return Response.status(400).build();
		// }
		//
		throw new RuntimeException(exception);

		// return null;
	}

	private ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = Beans.getReference(ResourceBundle.class, new NameQualifier("demoiselle-rest-bundle"));
		}

		return bundle;
	}

	private Logger getLogger() {
		if (logger == null) {
			logger = Beans.getReference(Logger.class, new NameQualifier(ParseExceptionMapper.class.getName()));
		}

		return logger;
	}
}
