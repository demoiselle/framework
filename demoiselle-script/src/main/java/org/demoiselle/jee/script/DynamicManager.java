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
	 * Le e compila um script guardando em cache.
	 * 
	 * @param scriptName ...
	 * @param source ...
	 * @return Boolean ...
	 * @throws ScriptException 
	 */
	public Boolean loadScript(String scriptName,String source ) throws ScriptException{				
		CompiledScript compiled = null;
	
		Compilable engine = (Compilable) this.scriptEngine;
		
		if(engine == null ){
			return false;
		}							
		compiled = engine.compile( source );			
		scriptCache.put(scriptName, compiled);
		
		return true;
								
	}
	
	/**
	 * Remove um script do cache.
	 * 
	 * @param scriptId ...
	 */
	public void removeScript(String scriptId) {					
		this.scriptCache.remove(scriptId); 		
	}

	/**
	 * Retorna um script do cache.
	 * 
	 * @param scriptId ...
	 * @return Script requerido
	 */
	public Object getScript(String scriptId) { 
		return this.scriptCache.get(scriptId);
	}  
			
}