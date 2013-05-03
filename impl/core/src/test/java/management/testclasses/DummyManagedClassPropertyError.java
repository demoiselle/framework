package management.testclasses;

import br.gov.frameworkdemoiselle.management.annotation.Managed;
import br.gov.frameworkdemoiselle.management.annotation.Property;


/**
 * 
 * Used in tests to detect if the bootstrap detects wrong annotations
 * 
 * @author serpro
 *
 */
@Managed
public class DummyManagedClassPropertyError {

	/**
	 * Property with no setter or getter
	 */
	@Property
	private Long property;
	
}
