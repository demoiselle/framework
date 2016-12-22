/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.core.api.script;

import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 *
 * @author SERPRO
 */
public interface DynamicManagerInterface {

	 public ScriptEngine loadEngine(String engineName);
	 
	 public List<String> listEngines(); 
	 
	 public void unloadEngine(String engineName);
	 
	 public void clearCache(String engineName);
	 
	 public Object eval(String engineName, String scriptName, Bindings context) throws ScriptException;
	 
	 public Boolean loadScript(String engineName, String scriptName,String source ) throws ScriptException;
	 
	 public void removeScript(String engineName, String scriptId);
	 
	 public Object getScript(String engineName, String scriptId);
	 
	 public int getCacheSize(String engineName);
	 
}