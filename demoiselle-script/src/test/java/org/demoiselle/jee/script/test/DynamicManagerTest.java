/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script.test;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import org.demoiselle.jee.script.DynamicManager;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class DynamicManagerTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	    
    private DynamicManager dm = new DynamicManager();
       
    @Test
    public void testClearCache() {        
    	dm.clearCache();   
    	Assert.assertTrue(true);
    } 
    
    @Test
    public void testunloadEngine()  {        
    	dm.unloadEngine();   
    	Assert.assertTrue(true);
    } 
    
    @Test
    public void testloadEngine() throws ScriptException  {        
        Assert.assertNotNull(dm.loadEngine("nashorn"));
    }
    
    @Test
    public void testloadEngineNotvalid() throws ScriptException  {    	
    	expectedEx.expect(NullPointerException.class);    	
        dm.loadEngine("randomEngine");
    }
       
    @Test
    public void testloadScriptAlreadyInCache() throws ScriptException  {            	         	
    	String javaScriptSource = "var a= X;  X=1 ; ";
    	dm.loadEngine("nashorn");         
    	dm.loadScript("test", javaScriptSource);    	
        Assert.assertEquals( false , dm.loadScript("test", javaScriptSource) );
    }
    
    @Test
    public void testloadScript() throws ScriptException  {        
    	String javaScriptSource = "var a= X;  X=1 ; ";
    	dm.loadEngine("nashorn");                 		                                         
        Assert.assertEquals( true , dm.loadScript("testJS", javaScriptSource));
    }
     
    @Test
    public void testCacheSize() throws ScriptException  {        
    	dm.loadEngine("nashorn");   
    	
        Assert.assertEquals(1,dm.getCacheSize());
    }
      
    @Test
    public void testGetScript() throws ScriptException {
    	String javaScriptSource = "var a= X;  X=1 ; ";
        dm.loadEngine("nashorn");     
        dm.loadScript("test1", javaScriptSource);
        dm.getScript("test1");   
        Assert.assertNotNull(dm.getScript("test1") );
    }
    
    @Test
    public void testRemoveScript() throws ScriptException {
    	String javaScriptSource = "var a= X;  X=1 ; ";
        dm.loadEngine("nashorn");     
        dm.loadScript("test2", javaScriptSource);
        dm.removeScript("test2");
        Assert.assertNull( dm.getScript("test2"));
    }
        
    @Test
    public void testEvalContext() throws ScriptException {           	
    	String javaScriptSource = "var a= X;  X=1 ; ";
        dm.loadEngine("nashorn");     
        dm.loadScript("teste3", javaScriptSource);
        
        Bindings contexto = new SimpleBindings();  
        contexto.put("X", 1);
        dm.eval("teste3", contexto);
       
        Assert.assertEquals( 1 , contexto.get("X"));
    }
       
    @Test
    public void testEvalNoContext() throws ScriptException {           	    	
    	String javaScriptSource = "var a= 1;  a; ";
        
    	dm.loadEngine("nashorn");     
        dm.loadScript("teste77", javaScriptSource);        
               
        Assert.assertEquals( 1 , dm.eval("teste77", null));
    }
     
    @Test
    public void testEvalScriptNotValid() throws ScriptException {           	
    	    	
        dm.loadEngine("nashorn");        
        expectedEx.expect(NullPointerException.class);
                      
        dm.eval("teste555", null);
    }
           
}