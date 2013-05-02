package management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import br.gov.frameworkdemoiselle.management.internal.ManagedType;

@ApplicationScoped
public class ManagedClassStore {
	
	private List<ManagedType> managedTypes = new ArrayList<ManagedType>();

	
	public List<ManagedType> getManagedTypes() {
		return managedTypes;
	}

	public void addManagedTypes(Collection<ManagedType> managedTypes){
		this.managedTypes.addAll(managedTypes);
	}
	
}
