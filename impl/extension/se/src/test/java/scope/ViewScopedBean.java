package scope;

import java.io.Serializable;

import br.gov.frameworkdemoiselle.lifecycle.ViewScoped;

@ViewScoped
public class ViewScopedBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
