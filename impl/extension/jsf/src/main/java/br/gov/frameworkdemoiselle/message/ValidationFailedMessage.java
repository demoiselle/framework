package br.gov.frameworkdemoiselle.message;

public class ValidationFailedMessage extends DefaultMessage {

	public ValidationFailedMessage(String text, Object[] params) {
		super(text, params);
	}

	public ValidationFailedMessage(String text, SeverityType severity, Object... params) {
		super(text, severity, params);
	}
}
