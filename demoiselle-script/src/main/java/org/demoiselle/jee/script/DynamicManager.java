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
 * Dynamic Manager - Responsavel por Gerenciar os Scripts, sua compilação e execução 
 *
 */
@ApplicationScoped
public class DynamicManager {
	
	private static ConcurrentHashMap<String, Object> scriptCache = new ConcurrentHashMap <String, Object>();
	private static ScriptEngine scriptEngine = null;
	
	@Inject
	private Logger logger;
	/**
	 * Carrega um engine no engine manager.
	 * @param engineName ...
	 * @return ScriptEngine ...
	 */
    public ScriptEngine loadEngine(String engineName) {
    	ScriptEngine engine =  new ScriptEngineManager().getEngineByName(engineName);
    	if(engine == null)
    		return null ;    		
    	
    	DynamicManager.scriptEngine = engine;
    	
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
	public synchronized Boolean loadScript(String scriptName,String source ) throws ScriptException{				
		CompiledScript compiled = null;
	
		Compilable engine = (Compilable) this.scriptEngine;
		
		if(engine == null ){
			return false;
		}		
				
		if( this.getScript(scriptName)== null){
			compiled = engine.compile( source );			
			scriptCache.put(scriptName, compiled);
			logger.info("Script:" + scriptName + " compilado e carregado.");
		}	
		
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
	 * @return 
	 * @return Script requerido
	 */
	public synchronized Object getScript(String scriptId){	
			synchronized (this.scriptCache) {
				return this.scriptCache.get(scriptId);	
			}		
				
	}  
		
	public int getCacheSize() { 
		return this.scriptCache.size();
	}  
}