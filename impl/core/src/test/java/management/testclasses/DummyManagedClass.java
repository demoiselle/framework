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
	private String name;
	
	@Property
	@AllowedValues(allows={"f","m","F","M"},valueType=ValueType.INTEGER)
	private Integer id;
	
	@Property
	private Integer firstFactor , secondFactor;
	
	@Property
	private String uuid;
	
	@Property
	private String writeOnlyProperty;
	
	@Property
	private String readOnlyProperty = "Default Value";
	
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

	
	public String getName() {
		return name;
	}

	
	public void setName(String name) {
		this.name = name;
	}

	
	public String getReadOnlyProperty() {
		return readOnlyProperty;
	}

	
	public Integer getFirstFactor() {
		return firstFactor;
	}

	
	public void setFirstFactor(Integer firstFactor) {
		this.firstFactor = firstFactor;
	}

	
	public Integer getSecondFactor() {
		return secondFactor;
	}

	
	public void setSecondFactor(Integer secondFactor) {
		this.secondFactor = secondFactor;
	}
	
	@Operation
	public Integer calculateFactorsNonSynchronized(Integer firstFactor , Integer secondFactor){
		setFirstFactor(firstFactor);
		setSecondFactor(secondFactor);
		
		try {
			int temp = firstFactor + secondFactor;
			Thread.sleep( (long)(Math.random() * 100));
			
			temp = temp + firstFactor;
			Thread.sleep( (long)(Math.random() * 100));
			
			temp = temp + secondFactor;
			Thread.sleep( (long)(Math.random() * 100));
			
			return temp;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Operation
	public synchronized Integer calculateFactorsSynchronized(Integer firstFactor , Integer secondFactor){
		setFirstFactor(firstFactor);
		setSecondFactor(secondFactor);
		
		try {
			int temp = firstFactor + secondFactor;
			Thread.sleep( (long)(Math.random() * 100));
			
			temp = temp + firstFactor;
			Thread.sleep( (long)(Math.random() * 100));
			
			temp = temp + secondFactor;
			Thread.sleep( (long)(Math.random() * 100));
			
			return temp;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void nonOperationAnnotatedMethod(){
		System.out.println("Test");
	}
	
	
}
