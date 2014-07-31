package br.gov.frameworkdemoiselle;

public class UnprocessableEntityException extends HttpViolationException {

	private static final long serialVersionUID = 1L;

	public UnprocessableEntityException() {
		super(422);
	}
}
