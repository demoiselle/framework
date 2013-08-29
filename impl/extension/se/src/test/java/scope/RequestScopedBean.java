package scope;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class RequestScopedBean {
	
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
