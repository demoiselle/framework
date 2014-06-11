package ${package}.security;

import org.codehaus.jackson.annotate.JsonProperty;

import br.gov.frameworkdemoiselle.security.User;

public class AppUser implements User {

	private static final long serialVersionUID = 1L;

	@JsonProperty("username")
	private String id;

	public AppUser(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public Object getAttribute(Object key) {
		return null;
	}

	@Override
	public void setAttribute(Object key, Object value) {
	}
}
