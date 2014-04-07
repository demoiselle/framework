package template;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "namedEntity")
public class MyNamedEntity {

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
