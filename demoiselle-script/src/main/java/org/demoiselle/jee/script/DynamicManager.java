/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/** 
 * Dynamic Manager - Responsible for Managing Scripts, its compilation and execution
 *
 * @author SERPRO
 */
@ApplicationScoped
public class DynamicManager {
	
	private static ConcurrentHashMap<String, Object> scriptCache = new ConcurrentHashMap <String, Object>();
	private static ScriptEngine scriptEngine = null;

	/**
	 * Load a JSR-223 Script engine.
	 * 
	 * @param engineName engine name
	 * @return ScriptEngine instance of engine
	 */
    public ScriptEngine loadEngine(String engineName) {
    	ScriptEngine engine =  new ScriptEngineManager().getEngineByName(engineName);
    	if(engine == null)
    		return null ;    		
    	
    	DynamicManager.scriptEngine = engine;
    	
    	return engine;
    }
	/**
	 * Run the script with context.
	 * 
	 * To add a variable in context to eval script use context.put("variableName", value) 
	 * Respective resulting values can be acessed from context use context.get("variableName"); 
	 * 
	 * @param scriptName script name
	 * @param context the variables to script logic.
	 * @return Object the result of script eval.
	 * @throws ScriptException 
	 */
	public Object eval(String scriptName, Bindings context) throws ScriptException{
		CompiledScript  script = null;
		Object result = null;
							    
		script = (CompiledScript) scriptCache.get(scriptName);
		if(context!= null)
		   	result = script.eval(context);
		else
		  	result = script.eval();
			   						
		return result;	
	}
	
	/**
	 * Load ,compile and put script in cache.
	 * 
	 * @param scriptName script name
	 * @param source 	 source of script
	 * @return Boolean   compilation ok or not
	 * @throws ScriptException 
	 */
	public synchronized Boolean loadScript(String scriptName,String source ) throws ScriptException{				
		CompiledScript compiled = null;
	
		Compilable engine = (Compilable) DynamicManager.scriptEngine;
		
		if(engine == null ){
			return false;
		}		
				
		if( this.getScript(scriptName)== null){
			compiled = engine.compile( source );			
			scriptCache.put(scriptName, compiled);		
		}	
		
		return true;
								
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
			synchronized (DynamicManager.scriptCache) {
				return DynamicManager.scriptCache.get(scriptId);	
			}		
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