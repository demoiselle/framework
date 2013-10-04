package br.gov.frameworkdemoiselle.internal.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.spi.PassivationCapable;


public class ContextualStore<T> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final String PREFIX = ContextualStore.class.getCanonicalName() + "#";
	
	private AtomicInteger idGenerator = new AtomicInteger();
	
	private HashMap<String, Contextual<T>> idToContextual = new HashMap<String, Contextual<T>>();
	
	private HashMap<Contextual<T>, String> contextualToId = new HashMap<Contextual<T>, String>();
	
	public String tryRegisterAndGetId(Contextual<T> contextual){
		String returnedId;
		
		if (contextualToId.containsKey(contextual)){
			returnedId = contextualToId.get(contextual);
		}
		else if (contextual instanceof PassivationCapable){
			returnedId = ((PassivationCapable)contextual).getId();
			idToContextual.put(returnedId, contextual);
			contextualToId.put(contextual, returnedId);
		}
		else{
			returnedId = PREFIX + idGenerator.addAndGet(1);
			idToContextual.put(returnedId, contextual);
			contextualToId.put(contextual, returnedId);
		}
		
		return returnedId;
	}
	
	public Contextual<T> getContextual(String id){
		return idToContextual.get(id);
	}
	
	public void clear(){
		idToContextual.clear();
		contextualToId.clear();
	}

}
