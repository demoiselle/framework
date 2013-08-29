package scope;

import br.gov.frameworkdemoiselle.annotation.ViewScoped;

@ViewScoped
public class ViewScopedBean {
	
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
