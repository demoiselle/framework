package jtatransaction.interceptor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MyEntity1 {

	@Id
	private String id;

	private String description;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
