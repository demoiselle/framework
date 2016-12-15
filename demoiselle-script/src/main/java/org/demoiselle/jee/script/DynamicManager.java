/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script;

import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.demoiselle.jee.script.message.DemoiselleScriptMessages;


/** 
 * Dynamic Manager - Responsible for Managing Scripts, its compilation and execution
 *
 * @author SERPRO
 */
@ApplicationScoped
//TODO Criar interface no core
//TODO Avaliar requestscoped
public class DynamicManager {
	
	@Inject private DemoiselleScriptMessages bundle;
	
	private static ConcurrentHashMap<String, Object> scriptCache = new ConcurrentHashMap <String, Object>();
	//TODO transformar para lista
	private static ScriptEngine scriptEngine = null;

	/**
	 * Load a JSR-223 Script engine.
	 * 
	 * @param engineName engine name
	 * @return ScriptEngine instance of engine
	 * @throws ScriptException when interface compilable not implemented by engine
	 */
	//TODO avaliar uso de enum
    public ScriptEngine loadEngine(String engineName) throws ScriptException {
        DynamicManager.scriptEngine = null;
    	ScriptEngine engine =  new ScriptEngineManager().getEngineByName(engineName);
    	
    	if(engine == null){
    		//TODO usar excecao do  demoiselle
    		//TODO informar na mensagesm o engine escolhido
    		throw new ScriptException(bundle.cannotLoadEngine());	    		
    	}
    	   	    	
		if (engine instanceof Compilable) {
			DynamicManager.scriptEngine = engine;
			return engine;
		}else {
			throw new ScriptException(bundle.engineNotCompilable());
		}
    			
    }
    
    //TODO criar metodo com lista dos engine suportados
    
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
	public Object eval(String scriptName, Bindings context) throws ScriptException{
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
			//TODO informar na mensagem de erro o scriptname
			throw new ScriptException(bundle.scriptNotLoaded());
		}
	}
	
	/**
	 * Load ,compile and put script in cache.
	 * 
	 * @param scriptName script name
	 * @param source 	 source of script
	 * @return Boolean   compilation ok or not
	 * @throws ScriptException when engine not loaded 
	 */
	//TODO uso do syncron  em outro metodo interno e privado
	public synchronized Boolean loadScript(String scriptName,String source ) throws ScriptException{				
		CompiledScript compiled = null;
	
		if(DynamicManager.scriptEngine == null ){
			//TODO usar excecao demoiselle
			throw new ScriptException(bundle.engineNotLoaded());			
		}
		
		Compilable engine = (Compilable) DynamicManager.scriptEngine;
										
		if( getScript(scriptName)== null){			
			compiled = engine.compile( source );			
			DynamicManager.scriptCache.put(scriptName, compiled);
			return true;
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