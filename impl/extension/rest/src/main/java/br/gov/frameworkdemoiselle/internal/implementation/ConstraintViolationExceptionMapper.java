//package br.gov.frameworkdemoiselle.internal.implementation;
//
//import java.util.Arrays;
//
//import javax.validation.ConstraintViolation;
//import javax.validation.ConstraintViolationException;
//import javax.ws.rs.core.Context;
//import javax.ws.rs.core.Response;
//import javax.ws.rs.ext.ExceptionMapper;
//import javax.ws.rs.ext.Provider;
//
//@Provider
//public class ConstraintViolationExceptionMapper implements ExceptionMapper<Exception> {
//
//	@Context
//	private Response response;
//	
//	@Override
//	public Response toResponse(Exception exception) {
//
//		Throwable rootCause = exception;
//		while (rootCause != null) {
//			if (rootCause instanceof ConstraintViolationException) {
//				break;
//			}
//
//			rootCause = rootCause.getCause();
//		}
//
//		if (rootCause != null) {
//			for (ConstraintViolation<?> violation : ((ConstraintViolationException) rootCause)
//					.getConstraintViolations()) {
//				String parts[] = violation.getPropertyPath().toString().split("\\.|\\[|\\]\\.");
//				String property = null;
//
//				if (parts.length > 1) {
//					property = parts[1];
//
//					for (String part : Arrays.copyOfRange(parts, 2, parts.length)) {
//						property += "." + part;
//					}
//				}
//
//				System.out.println(property);
//			}
//		}
//
//		return null;
//	}
// }
