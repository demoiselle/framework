package org.demoiselle.jee.persistence.crud.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.ws.rs.Path;

public class CrudBootstrap implements Extension {
	
	private Map<String, CrudContainer> cache;
	private Map<Class<?>, CrudContainer> businessPersistenceMap = Collections.synchronizedMap(new HashMap<>());

	public Map<String, CrudContainer> getCache() {
		if (this.cache == null) {
			this.cache = Collections.synchronizedMap(new HashMap<String, CrudContainer>());
		}

		return this.cache;
	}
	
	public void processAnnotatedType(@Observes final ProcessAnnotatedType<?> pat) {
		
		Class<?> pcsClass = pat.getAnnotatedType().getJavaClass();
		
		if (pcsClass.isAnnotation() || pcsClass.isInterface()  
				|| pcsClass.isSynthetic() || pcsClass.isArray()
				|| pcsClass.isEnum()){
	            return;
	    }
		
		if (pcsClass.isAnnotationPresent(CrudRest.class) 
				&& pcsClass.isAnnotationPresent(Path.class)){
			
			
			String path = pcsClass.getAnnotation(Path.class).value();
			Class<?> model = pcsClass.getDeclaredAnnotation(CrudRest.class).model();
										
			if(businessPersistenceMap.containsKey(model)){
				businessPersistenceMap.get(model).setRestClass(pcsClass);
				businessPersistenceMap.get(model).setPath(path);
			}
			else{
				
				CrudContainer cc = new CrudContainer();
				cc.setPath(path);
				cc.setRestClass(pcsClass);
				cc.setModel(model);
				businessPersistenceMap.put(model, cc);
			}
		}
		
		if (pcsClass.isAnnotationPresent(CrudBusiness.class)){
			Class<?> model = pcsClass.getDeclaredAnnotation(CrudBusiness.class).model();
			if(businessPersistenceMap.containsKey(model)){
				businessPersistenceMap.get(model).setBusinessClass(pcsClass);
			}
			else{
				CrudContainer cc = new CrudContainer();
				cc.setBusinessClass(pcsClass);
				cc.setModel(model);
				businessPersistenceMap.put(model, cc);
			}
		}
		
		if (pcsClass.isAnnotationPresent(CrudPersistence.class)){
			Class<?> model = pcsClass.getDeclaredAnnotation(CrudPersistence.class).model();
			if(businessPersistenceMap.containsKey(model)){
				businessPersistenceMap.get(model).setPersistenceClass(pcsClass);
			}
			else{
				CrudContainer cc = new CrudContainer();
				cc.setPersistenceClass(pcsClass);
				cc.setModel(model);
				businessPersistenceMap.put(model, cc);
			}
		}
	}
	
	public void afterDeploymentValidation(@Observes AfterDeploymentValidation event) { 
		businessPersistenceMap.forEach((k, v) -> {
			getCache().put(v.getPath(), v);
		});
		
		businessPersistenceMap = null;
	}

}
