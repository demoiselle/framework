package management.testclasses;

import java.util.UUID;

import br.gov.frameworkdemoiselle.management.annotation.Managed;
import br.gov.frameworkdemoiselle.management.annotation.Operation;
import br.gov.frameworkdemoiselle.management.annotation.Property;
import br.gov.frameworkdemoiselle.management.annotation.validation.AllowedValues;
import br.gov.frameworkdemoiselle.management.annotation.validation.AllowedValues.ValueType;

@Managed
public class DummyManagedClass {
	
	@Property
	@AllowedValues(allows={"f","m","F","M"},valueType=ValueType.INTEGER)
	private Integer id;
	
	@Property
	private String uuid;
	
	@Property
	private String writeOnlyProperty;
	
	/**
	 * Propriedade para testar detecção de métodos GET e SET quando propriedade tem apenas uma letra.
	 */
	@Property
	private Integer a;
	
	/**
	 * Propriedade para testar detecção de métodos GET e SET quando propriedade tem apenas letras maiúsculas.
	 */
	@Property
	private Integer MAIUSCULO;

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public void setWriteOnlyProperty(String newValue){
		this.writeOnlyProperty = newValue;
	}
	
	public Integer getA() {
		return a;
	}

	public void setA(Integer a) {
		this.a = a;
	}
	
	public Integer getMAIUSCULO() {
		return MAIUSCULO;
	}

	
	public void setMAIUSCULO(Integer mAIUSCULO) {
		MAIUSCULO = mAIUSCULO;
	}

	@Operation(description="Generates a random UUID")
	public String generateUUID(){
		this.uuid = UUID.randomUUID().toString();
		return this.uuid;
	}
}
