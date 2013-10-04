package br.gov.frameworkdemoiselle.internal.context;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.context.spi.CreationalContext;

@SuppressWarnings("rawtypes")
public class BeanStore implements Iterable<String>,Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Map<String, Object> instanceCache = Collections.synchronizedMap( new HashMap<String, Object>() );
	private Map<String, CreationalContext> creationalCache = Collections.synchronizedMap( new HashMap<String, CreationalContext>() );;
	
	public <T> void put(String id, T instance,CreationalContext<T> creationalContext){
		if (!instanceCache.containsKey(id)){
			instanceCache.put(id, instance);
			creationalCache.put(id, creationalContext);
		}
	}
	
	public Object getInstance(String id){
		return instanceCache.get(id);
	}
	
	public CreationalContext getCreationalContext(String id){
		return creationalCache.get(id);
	}
	
	public void clear(){
		instanceCache.clear();
		creationalCache.clear();
	}
	
	public boolean contains(String id){
		return instanceCache.containsKey(id);
	}

	@Override
	public Iterator<String> iterator() {
		return instanceCache.keySet().iterator();
	}

}
