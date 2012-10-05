package br.gov.frameworkdemoiselle.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
	
	private Date birthDate;

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


	@Temporal(TemporalType.DATE)
	public Date getBirthDate() {
		return birthDate;
	}


	
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	
	
	
	

}
