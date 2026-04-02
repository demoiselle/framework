/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.integration;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.demoiselle.jee.script.DynamicManager;
import org.demoiselle.jee.script.DynamicManagerCache;
import org.demoiselle.jee.script.exception.DemoiselleScriptException;
import org.demoiselle.jee.script.message.DemoiselleScriptMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: Configuration → Script execution.
 *
 * <p>Validates the flow of loading a script engine, loading/compiling scripts
 * with configured parameters, and executing them to produce expected results.</p>
 *
 * <p>Validates: Requirements 9.2, 9.3</p>
 */
@EnabledIf("isScriptAvailable")
class ConfigScriptIT {

    static boolean isScriptAvailable() {
        try {
            Class.forName("org.demoiselle.jee.script.DynamicManager");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static final String ENGINE = "groovy";

    private DynamicManager dm;

    @BeforeEach
    void setUp() throws Exception {
        DynamicManagerCache cache = new DynamicManagerCache();
        DemoiselleScriptMessage messages = createScriptMessages();

        dm = new DynamicManager();
        setField(dm, "cache", cache);
        setField(dm, "bundle", messages);
    }

    // ---------------------------------------------------------------
    // Test 1: Load engine and execute script with configured parameters
    // Validates: Requirements 9.2, 9.3
    // ---------------------------------------------------------------

    @Test
    void loadEngineAndExecuteScriptWithParameters() throws ScriptException {
        // 1. Load the Groovy script engine (simulates configuration loading)
        assertNotNull(dm.loadEngine(ENGINE), "Groovy engine should load successfully");

        // 2. Define a script that uses configured parameters
        String script = "greeting + ' ' + name";

        // 3. Load and cache the script
        assertTrue(dm.loadScript(ENGINE, "greetScript", script));

        // 4. Execute with configured parameter bindings
        Bindings context = new SimpleBindings();
        context.put("greeting", "Hello");
        context.put("name", "Demoiselle");

        Object result = dm.eval(ENGINE, "greetScript", context);
        assertEquals("Hello Demoiselle", result);
    }

    // ---------------------------------------------------------------
    // Test 2: Execute script source directly with parameters (no cache)
    // Validates: Requirements 9.2, 9.3
    // ---------------------------------------------------------------

    @Test
    void evalSourceWithConfiguredParameters() throws ScriptException {
        String source = "base * multiplier + offset";

        SimpleBindings context = new SimpleBindings();
        context.put("base", 10);
        context.put("multiplier", 3);
        context.put("offset", 5);

        Object result = dm.evalSource(ENGINE, source, context);
        assertEquals(35, result);
    }

    // ---------------------------------------------------------------
    // Test 3: Full flow — load, cache, update, and re-execute script
    // Validates: Requirements 9.2, 9.3
    // ---------------------------------------------------------------

    @Test
    void fullFlow_loadCacheUpdateAndReExecute() throws ScriptException {
        dm.loadEngine(ENGINE);

        // Load initial script version
        String v1 = "value * 2";
        assertTrue(dm.loadScript(ENGINE, "calc", v1));

        Bindings ctx = new SimpleBindings();
        ctx.put("value", 7);
        assertEquals(14, dm.eval(ENGINE, "calc", ctx));

        // Update script to new version
        String v2 = "value * 3";
        assertTrue(dm.updateScript(ENGINE, "calc", v2));

        // Re-execute with same parameters — result should reflect updated script
        ctx.put("value", 7);
        assertEquals(21, dm.eval(ENGINE, "calc", ctx));
    }

    // ---------------------------------------------------------------
    // Test 4: Script cache management across multiple scripts
    // Validates: Requirements 9.2, 9.3
    // ---------------------------------------------------------------

    @Test
    void scriptCacheManagement() throws ScriptException {
        dm.loadEngine(ENGINE);

        dm.loadScript(ENGINE, "s1", "1 + 1");
        dm.loadScript(ENGINE, "s2", "2 + 2");
        dm.loadScript(ENGINE, "s3", "3 + 3");

        assertEquals(3, dm.getCacheSize(ENGINE));
        assertTrue(dm.listScriptCache(ENGINE).contains("s1"));
        assertTrue(dm.listScriptCache(ENGINE).contains("s2"));
        assertTrue(dm.listScriptCache(ENGINE).contains("s3"));

        // Remove one script and verify cache shrinks
        dm.removeScript(ENGINE, "s2");
        assertEquals(2, dm.getCacheSize(ENGINE));
        assertNull(dm.getScript(ENGINE, "s2"));
    }

    // ---------------------------------------------------------------
    // Test 5: Script execution without engine loaded throws exception
    // Validates: Requirements 9.3
    // ---------------------------------------------------------------

    @Test
    void evalWithoutEngineLoaded_throwsException() {
        assertThrows(DemoiselleScriptException.class,
                () -> dm.eval("nonexistent", "script", null));
    }

    // ---------------------------------------------------------------
    // Test 6: Script with return value and complex bindings
    // Validates: Requirements 9.2, 9.3
    // ---------------------------------------------------------------

    @Test
    void scriptWithComplexBindings() throws ScriptException {
        dm.loadEngine(ENGINE);

        String script = "items.collect { it.toUpperCase() }.join(', ')";
        dm.loadScript(ENGINE, "transform", script);

        Bindings ctx = new SimpleBindings();
        ctx.put("items", java.util.List.of("alpha", "beta", "gamma"));

        Object result = dm.eval(ENGINE, "transform", ctx);
        assertEquals("ALPHA, BETA, GAMMA", result);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    /**
     * Creates a simple proxy implementation of DemoiselleScriptMessage
     * for use outside a CDI container.
     */
    private static DemoiselleScriptMessage createScriptMessages() {
        return (DemoiselleScriptMessage) java.lang.reflect.Proxy.newProxyInstance(
                DemoiselleScriptMessage.class.getClassLoader(),
                new Class<?>[]{ DemoiselleScriptMessage.class },
                (proxy, method, args) -> {
                    String name = method.getName();
                    return switch (name) {
                        case "cannotLoadEngine" -> "Cannot load engine: " + args[0];
                        case "engineNotLoaded" -> "Engine not loaded";
                        case "engineNotCompilable" -> "Engine not compilable";
                        case "scriptNotLoaded" -> "Script not loaded: " + args[0];
                        default -> "unknown message";
                    };
                }
        );
    }
}
