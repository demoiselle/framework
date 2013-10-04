package br.gov.frameworkdemoiselle.internal.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.inject.spi.PassivationCapable;


@SuppressWarnings("rawtypes")
public class ContextualStore implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final String PREFIX = ContextualStore.class.getCanonicalName() + "#";
	
	private AtomicInteger idGenerator = new AtomicInteger();
	
	private HashMap<String, Contextual> idToContextual = new HashMap<String, Contextual>();
	
	private HashMap<Contextual, String> contextualToId = new HashMap<Contextual, String>();
	
	/**
	 * The an unique ID for the given contextual. If it's the first time
	 * this contextual is accessed, registers the contextual for latter retrieval. 
	 * 
	 * @param contextual The contextual to generate an ID
	 * @return The unique ID for the contextual
	 */
	public String tryRegisterAndGetId(Contextual contextual){
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
	
	public Contextual getContextual(String id){
		return idToContextual.get(id);
	}
	
	public void clear(){
		idToContextual.clear();
		contextualToId.clear();
	}

}
