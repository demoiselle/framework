package management.testclasses;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class RequestScopedClass {

	private String info = "";

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

}
