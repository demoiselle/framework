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
import java.util.concurrent.locks.ReadWriteLock;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
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
 * execution.
 *
 * <p>
 * Thread-safety is provided by a {@link ReadWriteLock} <em>per engine</em>, held
 * in the application-scoped {@link DynamicManagerCache}. Mutating operations
 * ({@code loadEngine}, {@code unloadEngine}, {@code clearCache},
 * {@code loadScript}, {@code updateScript}, {@code removeScript}) acquire the
 * write lock, while read operations ({@code eval}, {@code getScript},
 * {@code listScriptCache}, {@code getCacheSize}) acquire the read lock. This
 * makes each operation atomic with respect to the shared cache and prevents the
 * {@code NullPointerException}/corruption that previously occurred when a
 * concurrent {@code unloadEngine}/{@code clearCache} removed the per-engine map
 * between a null-check and a subsequent access.
 * </p>
 *
 * <p>
 * Because different engines use different locks, operations on unrelated engines
 * still run concurrently.
 * </p>
 *
 * @author SERPRO
 */
@RequestScoped
public class DynamicManager implements Serializable, DynamicManagerInterface {

    private static final long serialVersionUID = -5913082351195041136L;

    @Inject
    private DemoiselleScriptMessage bundle;

    @Inject
    private DynamicManagerCache cache;

    /**
     * Load a JSR-223 Script engine.
     *
     * @param engineName engine name
     * @return ScriptEngine instance of engine
     * @throws DemoiselleScriptException when interface compilable not
     * implemented by engine
     */
    public ScriptEngine loadEngine(String engineName) throws DemoiselleScriptException {
        ReadWriteLock lock = cache.lockFor(engineName);
        lock.writeLock().lock();
        try {
            ScriptEngine engine = (ScriptEngine) cache.getEngineList().get(engineName);

            if (engine == null) {
                engine = new ScriptEngineManager().getEngineByName(engineName);

                if (engine == null) {
                    throw new DemoiselleScriptException(bundle.cannotLoadEngine(engineName));
                }

                if (engine instanceof Compilable) {
                    cache.getEngineList().put(engineName, engine);
                    cache.getScriptCache().put(engineName, new ConcurrentHashMap<String, Object>());

                    return engine;
                } else {
                    throw new DemoiselleScriptException(bundle.engineNotCompilable());
                }
            }
            return engine;
        } finally {
            lock.writeLock().unlock();
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

        factories.stream().forEach((factory) -> {
            listaEngines.addAll(factory.getNames());
        });
        return listaEngines;
    }

    /**
     * Force the unLoad a JSR-223 Script engine and clear the script cache.
     * Atomic with respect to the engine.
     *
     * @param engineName engineName
     */
    public void unloadEngine(String engineName) {
        cache.withWriteLock(engineName, () -> {
            ConcurrentHashMap<String, Object> scripts = cache.getScriptCache().get(engineName);
            if (scripts != null) {
                scripts.clear();
            }
            cache.getEngineList().remove(engineName);
            cache.getScriptCache().remove(engineName);
        });
    }

    /**
     * Clear the script cache. Atomic with respect to the engine.
     *
     * @param engineName engineName
     */
    public void clearCache(String engineName) {
        cache.withWriteLock(engineName, () -> {
            ConcurrentHashMap<String, Object> scripts = cache.getScriptCache().get(engineName);
            if (scripts == null) {
                throw new DemoiselleScriptException(bundle.engineNotLoaded());
            }
            scripts.clear();
        });
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
        ReadWriteLock lock = cache.lockFor(engineName);
        lock.readLock().lock();
        try {
            ConcurrentHashMap<String, Object> scripts = cache.getScriptCache().get(engineName);
            if (scripts == null) {
                throw new DemoiselleScriptException(bundle.engineNotLoaded());
            }

            CompiledScript script = (CompiledScript) scripts.get(scriptName);
            if (script == null) {
                throw new DemoiselleScriptException(bundle.scriptNotLoaded(scriptName));
            }

            if (context != null) {
                return script.eval(context);
            }
            return script.eval();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Load, compile and put script in cache. Caller must already hold the write
     * lock for {@code engineName}.
     *
     * @param engineName engineName
     * @param scriptName script name
     * @param source source of script
     * @return Boolean compilation ok or not
     * @throws ScriptException compile error
     */
    private boolean load(String engineName, ScriptEngine engineObj, String scriptName, String source) throws ScriptException {
        Compilable engine = (Compilable) engineObj;
        CompiledScript compiled = engine.compile(source);

        ConcurrentHashMap<String, Object> scripts = cache.getScriptCache().get(engineName);
        if (scripts == null) {
            throw new DemoiselleScriptException(bundle.engineNotLoaded());
        }
        scripts.put(scriptName, compiled);

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
     * Load, compile and put script in cache. Atomic with respect to the engine.
     *
     * @param engineName engineName
     * @param scriptName script name
     * @param source source of script
     * @return Boolean compilation ok or not
     * @throws ScriptException when engine not loaded
     */
    public Boolean loadScript(String engineName, String scriptName, String source) throws ScriptException {
        ReadWriteLock lock = cache.lockFor(engineName);
        lock.writeLock().lock();
        try {
            if (cache.getEngineList().get(engineName) == null) {
                // Reentrant write lock: engine creation and cache initialization
                // remain in the same critical section as script compilation.
                this.loadEngine(engineName);
            }
            ScriptEngine engineObj = (ScriptEngine) cache.getEngineList().get(engineName);
            if (engineObj == null) {
                throw new DemoiselleScriptException(bundle.engineNotLoaded());
            }

            ConcurrentHashMap<String, Object> scripts = cache.getScriptCache().get(engineName);
            if (scripts != null && scripts.get(scriptName) != null) {
                return false;
            }
            return load(engineName, engineObj, scriptName, source);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * List scripts ids in engine cache.
     *
     * @param engineName engineName
     * @return Set all scripts ids
     */
    public Set<String> listScriptCache(String engineName) {
        ReadWriteLock lock = cache.lockFor(engineName);
        lock.readLock().lock();
        try {
            ConcurrentHashMap<String, Object> scripts = cache.getScriptCache().get(engineName);
            if (scripts == null) {
                throw new DemoiselleScriptException(bundle.engineNotLoaded());
            }
            return scripts.keySet();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Update the script in cache. Atomic with respect to the engine.
     *
     * @param engineName engineName
     * @param scriptName script name
     * @param source source of script
     * @return Boolean compilation ok or not
     * @throws ScriptException when engine not loaded
     */
    public Boolean updateScript(String engineName, String scriptName, String source) throws ScriptException {
        ReadWriteLock lock = cache.lockFor(engineName);
        lock.writeLock().lock();
        try {
            if (cache.getEngineList().get(engineName) == null) {
                this.loadEngine(engineName);
            }
            ScriptEngine engineObj = (ScriptEngine) cache.getEngineList().get(engineName);
            if (engineObj == null) {
                throw new DemoiselleScriptException(bundle.engineNotLoaded());
            }

            ConcurrentHashMap<String, Object> scripts = cache.getScriptCache().get(engineName);
            if (scripts == null || scripts.get(scriptName) == null) {
                throw new DemoiselleScriptException(bundle.scriptNotLoaded(scriptName));
            }
            return load(engineName, engineObj, scriptName, source);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Delete the script from cache. Atomic with respect to the engine.
     *
     * @param engineName engineName
     * @param scriptId script name
     */
    public void removeScript(String engineName, String scriptId) {
        cache.withWriteLock(engineName, () -> {
            ConcurrentHashMap<String, Object> scripts = cache.getScriptCache().get(engineName);
            if (scripts == null) {
                throw new DemoiselleScriptException(bundle.engineNotLoaded());
            }
            scripts.remove(scriptId);
        });
    }

    /**
     * Return the script from cache.
     *
     * @param engineName engineName
     * @param scriptId script
     * @return Script
     */
    public Object getScript(String engineName, String scriptId) {
        return cache.withReadLock(engineName, () -> {
            ConcurrentHashMap<String, Object> scripts = cache.getScriptCache().get(engineName);
            if (scripts == null) {
                throw new DemoiselleScriptException(bundle.engineNotLoaded());
            }
            return scripts.get(scriptId);
        });
    }

    /**
     * Returns size of scriptCache
     *
     * @return number of scripts cached.
     */
    public int getCacheSize(String engineName) {
        return cache.withReadLock(engineName, () -> {
            ConcurrentHashMap<String, Object> scripts = cache.getScriptCache().get(engineName);
            if (scripts == null) {
                throw new DemoiselleScriptException(bundle.engineNotLoaded());
            }
            return scripts.size();
        });
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
        CompiledScript script = compile(engineName, source);

        if (context != null) {
            return script.eval(context);
        }
        return script.eval();
    }

}
