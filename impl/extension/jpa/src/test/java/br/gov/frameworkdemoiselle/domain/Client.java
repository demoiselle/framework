package br.gov.frameworkdemoiselle.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Simle entity for test classes
 * @author serpro
 *
 */
@Entity
public class Client implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	
	private String name;

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	
	public void setId(Long id) {
		this.id = id;
	}

	
	public String getName() {
		return name;
	}

	
	public void setName(String name) {
		this.name = name;
	}
	
	

}
