package management;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import br.gov.frameworkdemoiselle.management.internal.ManagedType;

@ApplicationScoped
public class ManagedClassStore {
	
	private List<ManagedType> managedTypes = null;

	
	public List<ManagedType> getManagedTypes() {
		return managedTypes;
	}

	public void setManagedTypes(List<ManagedType> managedTypes) {
		this.managedTypes = managedTypes;
	}
	
}
