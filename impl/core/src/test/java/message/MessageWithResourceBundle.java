package message;

import javax.inject.Inject;

import br.gov.frameworkdemoiselle.util.ResourceBundle;

public class MessageWithResourceBundle{

	@Inject
	private ResourceBundle bundle;

	public ResourceBundle getBundle() {
		return bundle;
	}

}
