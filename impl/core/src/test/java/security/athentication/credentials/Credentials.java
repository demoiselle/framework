package security.athentication.credentials;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class Credentials implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String login;

	
	public String getLogin() {
		return login;
	}

	
	public void setLogin(String login) {
		this.login = login;
	}
	
		

}
