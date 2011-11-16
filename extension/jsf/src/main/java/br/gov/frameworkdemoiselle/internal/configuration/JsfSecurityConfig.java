package br.gov.frameworkdemoiselle.internal.configuration;

import java.io.Serializable;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.Configuration;

@Configuration(prefix = "frameworkdemoiselle.security")
public class JsfSecurityConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@Name("login.page")
	private String loginPage = "/login";

	//	@Name("welcome.page")
	@Name("redirect.after.login")
	private String redirectAfterLogin = "/index";

	@Name("redirect.after.logout")
	private String redirectAfterLogout = "/login";

	@Name("redirect.enabled")
	private boolean redirectEnabled = true;

	public String getLoginPage() {
		return loginPage;
	}

	public String getRedirectAfterLogin() {
		return redirectAfterLogin;
	}

	public String getRedirectAfterLogout() {
		return redirectAfterLogout;
	}

	public boolean isRedirectEnabled() {
		return redirectEnabled;
	}

}
