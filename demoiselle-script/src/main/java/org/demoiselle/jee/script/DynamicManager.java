/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.demoiselle.jee.script.exception.DemoiselleScriptException;
import org.demoiselle.jee.script.message.DemoiselleScriptMessage;
import org.demoiselle.jee.core.api.script.DynamicManagerInterface;

/**
 * Dynamic Manager - Responsible for Managing Scripts, its compilation and
 * execution
 *
 * @author SERPRO
 */
@RequestScoped
public class DynamicManager implements Serializable, DynamicManagerInterface {

    private static final long serialVersionUID = -5913082351195041136L;

    @Inject
    private DemoiselleScriptMessage bundle;

    /**
     * Load a JSR-223 Script engine.
     *
     * @param engineName engine name
     * @return ScriptEngine instance of engine
     * @throws DemoiselleScriptException when interface compilable not
     * implemented by engine
     */
    public ScriptEngine loadEngine(String engineName) throws DemoiselleScriptException {
        ScriptEngine engine = (ScriptEngine) DynamicManagerCache.engineList.get(engineName);

        if (engine == null) {
            engine = new ScriptEngineManager().getEngineByName(engineName);

            if (engine == null) {
                throw new DemoiselleScriptException(bundle.cannotLoadEngine(engineName));
            }

            if (engine instanceof Compilable) {
                DynamicManagerCache.engineList.put(engineName, engine);
                DynamicManagerCache.scriptCache.put(engineName, new ConcurrentHashMap<String, Object>());

                return engine;
            } else {
                throw new DemoiselleScriptException(bundle.engineNotCompilable());
            }
        }
        return engine;
    }

    /**
     * List all valid engines names registered and found by discover mechanism.
     * To add a new engine import in the application pom.xml the engine jar.
     */
    public List<String> listEngines() {
        List<String> listaEngines = new ArrayList<String>();
        ScriptEngineManager manager = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = manager.getEngineFactories();

        factories.stream().forEach((factory) -> {
            listaEngines.addAll(factory.getNames());
        });
        return listaEngines;
    }

    /**
     * Force the unLoad a JSR-223 Script engine and clear the script cache.
     *
     * @param engineName engineName
     */
    public void unloadEngine(String engineName) {
        this.clearCache(engineName);
        DynamicManagerCache.engineList.remove(engineName);
        DynamicManagerCache.scriptCache.remove(engineName);
    }

    /**
     * Clear the script cache.
     *
     * @param engineName engineName
     */
    public void clearCache(String engineName) {
        ScriptEngine engine = (ScriptEngine) DynamicManagerCache.engineList.get(engineName);

        if (engine == null) {
            throw new DemoiselleScriptException(bundle.engineNotLoaded());
        }

        DynamicManagerCache.scriptCache.get(engineName).clear();
    }

    /**
     * Run the script with context.
     *
     * To add a variable in context to eval script use
     * context.put("variableName", value) Respective resulting values can be
     * accessed from context use context.get("variableName");
     *
     * @param engineName engineName
     * @param scriptName script name
     * @param context the variables to script logic.
     * @return Object the result of script eval.
     * @throws ScriptException when script not loaded
     */
    public Object eval(String engineName, String scriptName, Bindings context) throws ScriptException {
        CompiledScript script = null;
        Object result = null;

        if (DynamicManagerCache.scriptCache.get(engineName) == null) {
            throw new DemoiselleScriptException(bundle.engineNotLoaded());
        }

        if (DynamicManagerCache.scriptCache.get(engineName).get(scriptName) != null) {
            script = (CompiledScript) DynamicManagerCache.scriptCache.get(engineName).get(scriptName);

            if (context != null) {
                result = script.eval(context);
            } else {
                result = script.eval();
            }

            return result;
        } else {
            throw new DemoiselleScriptException(bundle.scriptNotLoaded(scriptName));
        }
    }

    /**
     * Load ,compile and put script in cache.
     *
     * @param engineName engineName
     * @param scriptName script name
     * @param source source of script
     * @return Boolean compilation ok or not
     * @throws ScriptException compile error
     */
    private synchronized boolean load(String engineName, ScriptEngine engineObj, String scriptName, String source) throws ScriptException {
        Compilable engine = (Compilable) engineObj;
        CompiledScript compiled = engine.compile(source);

        DynamicManagerCache.scriptCache.get(engineName).put(scriptName, compiled);

        return true;
    }

    /**
     * Only load the engine and compile the source, not cache.
     *
     * @param engineName engine name
     * @param source source of script
     * @return CompiledScript compiled script
     * @throws ScriptException when engine not loaded
     */
    public CompiledScript compile(String engineName, String source) throws ScriptException {
        Compilable engine = (Compilable) loadEngine(engineName);

        return engine.compile(source);
    }

    /**
     * Load ,compile and put script in cache.
     *
     * @param engineName engineName
     * @param scriptName script name
     * @param source source of script
     * @return Boolean compilation ok or not
     * @throws ScriptException when engine not loaded
     */
    public Boolean loadScript(String engineName, String scriptName, String source) throws ScriptException {
        ScriptEngine engineObj = (ScriptEngine) DynamicManagerCache.engineList.get(engineName);

        if (engineObj == null) {
            engineObj = this.loadEngine(engineName);
        }

        if (this.getScript(engineName, scriptName) == null) {
            return load(engineName, engineObj, scriptName, source);
        }

        return false;
    }

    /**
     * List scripts ids in engine cache.
     *
     * @param engineName engineName
     * @return Set all scripts ids
     */
    public Set<String> listScriptCache(String engineName) {
        if (DynamicManagerCache.scriptCache.get(engineName) == null) {
            throw new DemoiselleScriptException(bundle.engineNotLoaded());
        }
        return DynamicManagerCache.scriptCache.get(engineName).keySet();
    }

    /**
     * Update the script in cache.
     *
     * @param engineName engineName
     * @param scriptName script name
     * @param source source of script
     * @return Boolean compilation ok or not
     * @throws ScriptException when engine not loaded
     */
    public Boolean updateScript(String engineName, String scriptName, String source) throws ScriptException {
        ScriptEngine engineObj = (ScriptEngine) DynamicManagerCache.engineList.get(engineName);

        if (engineObj == null) {
            engineObj = this.loadEngine(engineName);
        }

        if (this.getScript(engineName, scriptName) == null) {
            throw new DemoiselleScriptException(bundle.scriptNotLoaded(scriptName));
        } else {
            return load(engineName, engineObj, scriptName, source);
        }

    }

    /**
     * Delete the script from cache.
     *
     * @param engineName engineName
     * @param scriptId script name
     */
    public synchronized void removeScript(String engineName, String scriptId) {

        if (DynamicManagerCache.scriptCache.get(engineName) == null) {
            throw new DemoiselleScriptException(bundle.engineNotLoaded());
        }

        DynamicManagerCache.scriptCache.get(engineName).remove(scriptId);
    }

    /**
     * Return the script from cache.
     *
     * @param engineName engineName
     * @param scriptId script
     * @return Script
     */
    public synchronized Object getScript(String engineName, String scriptId) {

        if (DynamicManagerCache.scriptCache.get(engineName) == null) {
            throw new DemoiselleScriptException(bundle.engineNotLoaded());
        }

        return DynamicManagerCache.scriptCache.get(engineName).get(scriptId);
    }

    /**
     * Returns size of scriptCache
     *
     * @return number of scripts cached.
     */
    public int getCacheSize(String engineName) {

        if (DynamicManagerCache.scriptCache.get(engineName) == null) {
            throw new DemoiselleScriptException(bundle.engineNotLoaded());
        }
        return DynamicManagerCache.scriptCache.get(engineName).size();
    }

    /**
     * Run the script source with no cache.
     *
     * @param engineName
     * @param source
     * @param context
     * @return Object the result of evaluate script.
     * @throws ScriptException
     */
    public Object evalSource(String engineName, String source, SimpleBindings context) throws ScriptException {
        CompiledScript script = null;
        Object result = null;

        script = compile(engineName, source);

        if (context != null) {
            result = script.eval(context);
        } else {
            result = script.eval();
        }

        return result;
    }

}
