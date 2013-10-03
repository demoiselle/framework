package context.staticcontext;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;

@SessionScoped
public class SessionBean implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String data = "test";

	
	public String getData() {
		return data;
	}

	
	public void setData(String data) {
		this.data = data;
	}
	
	

}
