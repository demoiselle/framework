package management.testclasses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import br.gov.frameworkdemoiselle.management.internal.ManagedType;
import br.gov.frameworkdemoiselle.management.internal.MonitoringManager;
import br.gov.frameworkdemoiselle.util.Beans;

/**
 * Dummy class that stores managed types detected by the management bootstrap
 * and can read/write properties and invoke operations on them, simulating a management
 * extension like JMX or SNMP. 
 * 
 * @author serpro
 *
 */
@ApplicationScoped
public class ManagedClassStore {
	
	private List<ManagedType> managedTypes = new ArrayList<ManagedType>();

	
	public List<ManagedType> getManagedTypes() {
		return managedTypes;
	}

	public void addManagedTypes(Collection<ManagedType> managedTypes){
		this.managedTypes.addAll(managedTypes);
	}
	
	public void setProperty(Class<?> managedClass , String attributeName , Object newValue){
		MonitoringManager manager = Beans.getReference(MonitoringManager.class);
		for (ManagedType type : manager.getManagedTypes()){
			if (type.getType().equals(managedClass)){
				manager.setProperty(type, attributeName, newValue);
				break;
			}
		}
	}
	
	public Object getProperty(Class<?> managedClass , String attributeName ){
		MonitoringManager manager = Beans.getReference(MonitoringManager.class);
		for (ManagedType type : manager.getManagedTypes()){
			if (type.getType().equals(managedClass)){
				return manager.getProperty(type, attributeName);
			}
		}
		
		return null;
	}
	
	public Object invoke(Class<?> managedClass , String operation , Object...  params){
		MonitoringManager manager = Beans.getReference(MonitoringManager.class);
		for (ManagedType type : manager.getManagedTypes()){
			if (type.getType().equals(managedClass)){
				return manager.invoke(type, operation, params);
			}
		}
		
		return null;
	}
}
