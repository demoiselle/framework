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
	 
	 public void unloadEngine();
	 
	 public void clearCache();
	 
	 public Object eval(String scriptName, Bindings context) throws ScriptException;
	 
	 public Boolean loadScript(String scriptName,String source ) throws ScriptException;
	 
	 public void removeScript(String scriptId);
	 
	 public Object getScript(String scriptId);
	 
	 public int getCacheSize();
	 
}