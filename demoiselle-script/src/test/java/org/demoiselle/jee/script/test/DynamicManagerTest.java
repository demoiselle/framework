/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script.test;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.demoiselle.jee.script.DynamicManager;
import org.demoiselle.jee.script.DynamicManagerCache;
import org.demoiselle.jee.script.exception.DemoiselleScriptException;
import org.demoiselle.jee.script.message.DemoiselleScriptMessage;
import org.jboss.weld.junit5.auto.ActivateScopes;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnableAutoWeld
@ActivateScopes(RequestScoped.class)
@AddBeanClasses({ DynamicManager.class, DynamicManagerCache.class, DemoiselleScriptMessage.class })
@AddExtensions({ org.demoiselle.jee.core.message.MessageBundleExtension.class })
class DynamicManagerTest {

    private static final String ENGINE = "groovy";

    @Inject
    DynamicManager dm;

    @Test
    void testLoadEngine() throws ScriptException {
        assertNotNull(dm.loadEngine(ENGINE));
    }

    @Test
    void testUnloadEngine() throws ScriptException {
        dm.loadEngine(ENGINE);
        dm.unloadEngine(ENGINE);
        assertThrows(DemoiselleScriptException.class, () -> dm.clearCache(ENGINE));
    }

    @Test
    void testLoadEngineNotValid() {
        assertThrows(DemoiselleScriptException.class, () -> dm.loadEngine("randomEngine"));
    }

    @Test
    void testLoadScriptAlreadyInCache() throws ScriptException {
        String groovySource = "def a = X; X = 1";
        dm.loadScript(ENGINE, "test", groovySource);
        assertFalse(dm.loadScript(ENGINE, "test", groovySource));
    }

    @Test
    void testLoadScript() throws ScriptException {
        String groovySource = "def a = X; X = 1";
        dm.loadEngine(ENGINE);
        assertTrue(dm.loadScript(ENGINE, "testGroovy", groovySource));
    }

    @Test
    void testCacheSize() throws ScriptException {
        dm.loadEngine(ENGINE);
        dm.clearCache(ENGINE);
        assertEquals(0, dm.getCacheSize(ENGINE));
    }

    @Test
    void testCacheSizeNotLoadEngine() {
        assertThrows(DemoiselleScriptException.class, () -> dm.getCacheSize("invalidEngine"));
    }

    @Test
    void testClearCacheNotLoadEngine() {
        assertThrows(DemoiselleScriptException.class, () -> dm.clearCache("invalidEngine"));
    }

    @Test
    void testCompile() throws ScriptException {
        String groovySource = "def a = X; X = 1";
        assertNotNull(dm.compile(ENGINE, groovySource));
    }

    @Test
    void testGetScript() throws ScriptException {
        String groovySource = "def a = X; X = 1";
        dm.loadScript(ENGINE, "test1", groovySource);
        assertNotNull(dm.getScript(ENGINE, "test1"));
    }

    @Test
    void testGetScriptNotLoadEngine() {
        assertThrows(DemoiselleScriptException.class, () -> dm.getScript("invalidEngine", "invalidScript"));
    }

    @Test
    void testRemoveScript() throws ScriptException {
        String groovySource = "def a = X; X = 1";
        dm.loadScript(ENGINE, "test2", groovySource);
        dm.removeScript(ENGINE, "test2");
        assertNull(dm.getScript(ENGINE, "test2"));
    }

    @Test
    void testRemoveScriptNotLoadEngine() {
        assertThrows(DemoiselleScriptException.class, () -> dm.removeScript("invalidEngine", "invalidScript"));
    }

    @Test
    void testEvalNotLoadEngine() {
        assertThrows(DemoiselleScriptException.class, () -> dm.eval("invalidEngine", "teste3", null));
    }

    @Test
    void testEvalContext() throws ScriptException {
        String groovySource = "def a = X; X = 1";
        dm.loadScript(ENGINE, "teste3", groovySource);

        Bindings contexto = new SimpleBindings();
        contexto.put("X", 1);
        dm.eval(ENGINE, "teste3", contexto);

        assertEquals(1, contexto.get("X"));
    }

    @Test
    void testEvalContextNoCache() throws ScriptException {
        String groovySource = "'test' + X";

        SimpleBindings contexto = new SimpleBindings();
        contexto.put("X", 1);

        assertEquals("test1", dm.evalSource(ENGINE, groovySource, contexto));
    }

    @Test
    void testEvalNoContextNoCache() throws ScriptException {
        String groovySource = "def a = 1; a";
        assertEquals(1, dm.evalSource(ENGINE, groovySource, null));
    }

    @Test
    void testEvalNoContext() throws ScriptException {
        String groovySource = "def a = 1; a";
        dm.loadScript(ENGINE, "teste77", groovySource);
        assertEquals(1, dm.eval(ENGINE, "teste77", null));
    }

    @Test
    void testEvalScriptNotValid() throws ScriptException {
        dm.loadEngine(ENGINE);
        assertThrows(DemoiselleScriptException.class, () -> dm.eval(ENGINE, "teste555", null));
    }

    @Test
    void testListEngines() {
        assertTrue(dm.listEngines().size() > 0);
    }
}
