package br.gov.frameworkdemoiselle.internal.context;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.spi.CreationalContext;

@SuppressWarnings("rawtypes")
public class BeanStore implements Iterable<String>,Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private ConcurrentMap<String, Object> instanceCache = new ConcurrentHashMap<String, Object>();
	private ConcurrentMap<String, CreationalContext> creationalCache = new ConcurrentHashMap<String, CreationalContext>();
	
	public <T> void put(String id, T instance,CreationalContext<T> creationalContext){
		instanceCache.putIfAbsent(id, instance);
		creationalCache.putIfAbsent(id, creationalContext);
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
