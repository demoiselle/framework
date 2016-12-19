/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.demoiselle.jee.script.exception.DemoiselleScriptException;
import org.demoiselle.jee.script.message.DemoiselleScriptMessages;
import org.demoiselle.jee.core.api.script.DynamicManagerInterface;

/** 
 * Dynamic Manager - Responsible for Managing Scripts, its compilation and execution
 *
 * @author SERPRO
 */
@ApplicationScoped
//TODO Avaliar requestscoped
public class DynamicManager implements DynamicManagerInterface {
	
	@Inject private DemoiselleScriptMessages bundle;
	
	private static ConcurrentHashMap<String, Object> scriptCache = new ConcurrentHashMap <String, Object>();
	//TODO transformar para lista
	private static ScriptEngine scriptEngine = null;

	/**
	 * Load a JSR-223 Script engine.
	 * 
	 * @param engineName engine name
	 * @return ScriptEngine instance of engine
	 * @throws DemoiselleScriptException when interface compilable not implemented by engine
	 */
    public ScriptEngine loadEngine(String engineName) throws DemoiselleScriptException {
        DynamicManager.scriptEngine = null;
    	ScriptEngine engine =  new ScriptEngineManager().getEngineByName(engineName);
    	
    	if(engine == null){
    		throw new DemoiselleScriptException(bundle.cannotLoadEngine(engineName));	    		
    	}
    	   	    	
		if (engine instanceof Compilable) {
			DynamicManager.scriptEngine = engine;
			return engine;
		}else {
			throw new DemoiselleScriptException(bundle.engineNotCompilable());
		}    			
    }
    
    /**
	 * List all valid engines names registered and found by discover mechanism. 
     * To add a new engine import in the application pom.xml the engine jar.
	 */    
    public List<String> listEngines() {
    	List<String> listaEngines = new ArrayList<String>(); 
	    ScriptEngineManager manager = new ScriptEngineManager();
	    List<ScriptEngineFactory> factories = manager.getEngineFactories();
	    
	    for (ScriptEngineFactory factory : factories) {    	      	   
	        listaEngines.addAll(factory.getNames());
	    }
	    return listaEngines;
	  }
    
    /**
	 * Force the unLoad a JSR-223 Script engine and clear the script cache.
	 */
    public void unloadEngine(){
    	this.clearCache();
    	DynamicManager.scriptEngine = null;         
    }
    
    /**
	 * Clear the script cache.
	 */
    public void clearCache(){
    	DynamicManager.scriptCache.clear();    	
    }
        
	/**
	 * Run the script with context.
	 * 
	 * To add a variable in context to eval script use context.put("variableName", value) 
	 * Respective resulting values can be accessed from context use context.get("variableName"); 
	 * 
	 * @param scriptName script name
	 * @param context the variables to script logic.
	 * @return Object the result of script eval.
	 * @throws ScriptException when script not loaded
	 */
	public Object eval(String scriptName, Bindings context) throws ScriptException {
		CompiledScript  script = null;
		Object result = null;
							    
		if(scriptCache.get(scriptName) !=null) {
			script = (CompiledScript) scriptCache.get(scriptName);
		
			if(context!= null)
			   	result = script.eval(context);
			else
			  	result = script.eval();
				   						
			return result;	
		}else {
			throw new DemoiselleScriptException(bundle.scriptNotLoaded(scriptName));
		}
	}
	
	/**
	 * Load ,compile and put script in cache.
	 * 
	 * @param scriptName script name
	 * @param source 	 source of script
	 * @return Boolean   compilation ok or not
	 * @throws ScriptException  compile error
	 */
	private synchronized boolean load(String scriptName, String source) throws ScriptException{		
		CompiledScript compiled = null;
		Compilable engine = (Compilable) DynamicManager.scriptEngine;
		compiled = engine.compile( source );			
		DynamicManager.scriptCache.put(scriptName, compiled);
		
		return true;
	}
		
	/**
	 * Load ,compile and put script in cache.
	 * 
	 * @param scriptName script name
	 * @param source 	 source of script
	 * @return Boolean   compilation ok or not
	 * @throws ScriptException when engine not loaded 
	 */
	public Boolean loadScript(String scriptName,String source ) throws ScriptException{				
		
		if(DynamicManager.scriptEngine == null ){
			throw new DemoiselleScriptException(bundle.engineNotLoaded());			
		}
														
		if( getScript(scriptName)== null){
			return load(scriptName,source);					
		}	
		
		return false;							
	}
	
	/**
	 * Delete the script from cache.
	 * 
	 * @param scriptId script name
	 */
	public void removeScript(String scriptId) {					
		DynamicManager.scriptCache.remove(scriptId); 		
	}

	/**
	 * Return the script from cache.
	 * 
	 * @param scriptId script
	 * @return Script 
	 */
	public synchronized Object getScript(String scriptId){			
		return DynamicManager.scriptCache.get(scriptId);						
	}  
	
	/**
	 * Returns size of scriptCache
	 * 
	 * @return number of scripts cached.
	 */
	public int getCacheSize() { 
		return DynamicManager.scriptCache.size();
	}  
}