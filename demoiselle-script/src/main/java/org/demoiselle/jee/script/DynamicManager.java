/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script;

import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Dynamic Manager - Responsavel por Gerenciar os Scripts, sua compilação e execução 
 *
 */
public class DynamicManager {
	
	private Map<String, Object> scriptCache; 
	private ScriptEngine scriptEngine;

	public DynamicManager(){
		scriptCache   = new HashMap<String, Object>();
		scriptEngine  = null;
	}
	
	public DynamicManager(String engineName){
		scriptCache   = new HashMap<String, Object>();		
		loadEngine(engineName);
	}
	/**
	 * Carrega um engine no engine manager.
	 * @param engineName ...
	 * @return ScriptEngine ...
	 */
    public ScriptEngine loadEngine(String engineName) {
    	ScriptEngine engine =  new ScriptEngineManager().getEngineByName(engineName);
    	if(engine == null)
    		return null ;    		
    	
    	this.scriptEngine = engine;
    	
    	return engine;
    }
	/**
	 * Executa o script de acordo com o contexto passado.
	 * 
	 * @param scriptName ...
	 * @param context ...
	 * @return ...
	 */
	public Object eval(String scriptName, Bindings context){
		CompiledScript  script = null;
		Object result = null;
		
		try {						    
				script = (CompiledScript) scriptCache.get(scriptName);
			    if(context!= null)
			    	result = script.eval(context);
			    else
			    	result = script.eval();
			   		
		} catch (ScriptException e) {
			e.printStackTrace();
		}
				
		return result;	
	}
	
	/**
	 * Executa o script sem um contexto especifico.
	 * 
	 * @param scriptName ...
	 * @return ...
	 */
	public Object eval(String scriptName){
		CompiledScript  script = null;
		Object result = null;
		
		try {						    
				script = (CompiledScript) scriptCache.get(scriptName);			    
			    result = script.eval();
			   		
		} catch (ScriptException e) {
			e.printStackTrace();
		}
				
		return result;	
	}
	
	
	/**
	 * Le e compila um script guardando em cache.
	 * 
	 * @param scriptName ...
	 * @param source ...
	 * @return Boolean ...
	 */
	public Boolean loadScript(String scriptName,String source ){				
		CompiledScript compiled = null;
			
		try {			
			Compilable engine = (Compilable) this.scriptEngine;
		
			if(engine == null ){
				return false;
			}							
			 compiled = engine.compile( source );			
			 scriptCache.put(scriptName, compiled);
			
			return true;
		
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		
		return false;		
	}
	
	/**
	 * Remove um script do cache.
	 * 
	 * @param scriptId ...
	 */
	public void removeScriptCache(String scriptId) {					
		this.scriptCache.remove(scriptId); 		
	}  
			
}