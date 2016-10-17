/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

// Implementacao simples que difere do GroovyScriptEngine aceitando ler um script direto de um objeto string previamente carregado, 
// permitindo guardar os objetos compilados em cache
// sem monitorar as mudanças no fonte.
public class SimpleGroovyEngine {
	
	private Map<String,Script> scriptCache; //cache simples
	private GroovyClassLoader groovyClassLoader;	
	private boolean cache;
	
	public SimpleGroovyEngine(){	
	    ClassLoader parent = getClass().getClassLoader();
	    groovyClassLoader  = new GroovyClassLoader(parent);			     
	    scriptCache = new HashMap<String,Script>();
	}
	
	public Map<String, Script> getScriptCache() {
		return scriptCache;
	}

	public void setScriptCache(Map<String, Script> scriptCache) {
		this.scriptCache = scriptCache;
	}

	public Script getScript(String scriptId ) {		 
		return scriptCache.get(scriptId);
		
	}	
	public boolean isCache() {
		return cache;
	}

	public void setCache(boolean cache) {
		this.cache = cache;
	}

	@SuppressWarnings("unchecked")
	public Script loadScript(String scriptId , String regraScript ) {		 
		Script script = null;		
		try {			
			script= scriptCache.get(scriptId);
			if (script == null) {															
				Class<Script> clazz= groovyClassLoader.parseClass(regraScript);									  
				script = clazz.newInstance();
					       		    
			    scriptCache.put(scriptId, script);		 
			}	   	   	       	    	   	
			 
		 } catch (InstantiationException | IllegalAccessException e) {				
				e.printStackTrace();
		 }
		return script;
		    
	}

	public void run(String scriptId, ArrayList<Object> listaFatos ) {		
		Script script = scriptCache.get(scriptId);
		
		if( script != null ){			   	   	       	    	 
			Binding binding = new Binding();
			
			for(Object item : listaFatos ) {
				binding.setVariable(item.getClass().getSimpleName(), item);							
			} 
			
			script.setBinding(binding);
			script.run();			
		}
		    
	}
	
	public void run(String scriptId, Binding listaFatos ) {		
		Script script = scriptCache.get(scriptId);
		
		if( script != null ){			   	   	       	    	 			
			script.setBinding(listaFatos);
			script.run();			
		}
		    
	}
	
	public void removeScriptCache(String scriptId) {					
		this.scriptCache.remove(scriptId); 		
	}  
	
}