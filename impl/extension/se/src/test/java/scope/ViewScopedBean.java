package scope;

import java.io.Serializable;

import org.junit.Ignore;

//@ViewScoped
@Ignore
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
